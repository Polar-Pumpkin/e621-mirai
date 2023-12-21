package me.parrot.mirai.listener

import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import me.parrot.mirai.E621
import me.parrot.mirai.config.Responses
import me.parrot.mirai.config.Settings
import me.parrot.mirai.data.Post
import me.parrot.mirai.util.Baffle
import me.parrot.mirai.util.Histories
import net.mamoe.mirai.console.command.CommandSender.Companion.toCommandSender
import net.mamoe.mirai.console.permission.PermissionService
import net.mamoe.mirai.console.permission.PermissionService.Companion.hasPermission
import net.mamoe.mirai.event.EventHandler
import net.mamoe.mirai.event.SimpleListenerHost
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.ImageType
import net.mamoe.mirai.message.data.MessageSource.Key.quote
import net.mamoe.mirai.message.data.buildMessageChain
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import okhttp3.Credentials
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Request
import kotlin.coroutines.CoroutineContext

/**
 * e621
 * me.parrot.mirai.listener.SearchListener
 *
 * @author legoshi
 * @version 1
 * @since 2023/10/31 19:56
 */
object SearchListener : SimpleListenerHost() {

    private val pattern = Regex("来张(?<search>.+?)(?<coerce>清水|色)?图")

    private val searchPermission by lazy {
        PermissionService.INSTANCE.register(
            E621.permissionId("search"),
            "Allow to search images on chat"
        )
    }
    private val sensitivePermission by lazy {
        PermissionService.INSTANCE.register(
            E621.permissionId("sensitive-content"),
            "Allow to search sensitive images on chat"
        )
    }

    override fun handleException(context: CoroutineContext, exception: Throwable) {
        val cause = exception.rootCause
        cause.printStackTrace()

        val event = exception.event ?: return
        if (event is MessageEvent) {
            E621.launch {
                event.subject.sendMessage(buildMessageChain {
                    +event.message.quote()
                    +" "
                    +"处理操作时遇到错误:\n"
                    +"${cause::class.java.canonicalName}\n"
                    +"- - - - -\n"
                    +(cause.message ?: "null")
                })
            }
        }
    }

    @EventHandler
    suspend fun MessageEvent.onMessage() {
        val match = pattern.matchEntire(message.content) ?: return
        val keyword = match.groups["search"]?.value?.trim() ?: return
        val commander = toCommandSender()
        if (!commander.hasPermission(searchPermission)) {
            return
        }

        if (!Baffle.next(subject.id)) {
            subject.sendMessage(Responses.cooldown.randomOrNull() ?: return)
            return
        }

        val search = Settings.alias[keyword] ?: keyword
        E621.logger.info(search)

        val url = "https://e621.net/posts.json".toHttpUrl()
            .newBuilder()
            .addQueryParameter("tags", search)
            .addQueryParameter("limit", "60")
            .build()
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "e621-mirai/1.0 (by EntityParrot on e621)")
            .header("Authorization", Credentials.basic(Settings.username, Settings.token))
            .build()

        val hasSensitive = commander.hasPermission(sensitivePermission)
        E621.client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                subject.sendMessage(Responses.failure.randomOrNull() ?: return)
                return
            }

            val payload = Json.decodeFromString<Map<String, List<Post>>>(response.body!!.string())
            val viewed = Histories.getViewed()
            val posts = payload["posts"]!!
                .asSequence()
                .filter { it.id !in viewed }
                .filter {
                    when (match.groups["coerce"]?.value) {
                        "清水" -> it.rating == "s"
                        "色" -> it.rating != "s"
                        else -> hasSensitive || it.rating == "s"
                    }
                }
                .filter { ImageType.match(it.file.extension) != ImageType.UNKNOWN }
                .sortedByDescending { it.score.total }
                .take(40)
                .toList()
            if (posts.isEmpty()) {
                subject.sendMessage(Responses.empty.randomOrNull() ?: return)
                return
            }

            val post = posts.shuffled().random()
            if (post.rating != "s" && !hasSensitive) {
                subject.sendMessage(Responses.sensitive.randomOrNull() ?: return)
                return
            }

            E621.client.newCall(Request.Builder().url(post.sample.url).build()).execute().use { download ->
                if (!download.isSuccessful) {
                    subject.sendMessage(Responses.failure.randomOrNull() ?: return)
                    return
                }

                Histories.record(sender.id, post)
                download.body!!.byteStream().use { stream ->
                    val resource = stream.toExternalResource("jpg").toAutoCloseable()
                    val image = subject.uploadImage(resource)
                    subject.sendMessage(image)
                }
            }
        }
    }

}