package me.parrot.mirai.listener

import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import me.parrot.mirai.E621
import me.parrot.mirai.config.Responses
import me.parrot.mirai.config.Settings
import me.parrot.mirai.data.Post
import net.mamoe.mirai.console.command.CommandSender.Companion.toCommandSender
import net.mamoe.mirai.console.permission.PermissionService
import net.mamoe.mirai.console.permission.PermissionService.Companion.hasPermission
import net.mamoe.mirai.event.EventHandler
import net.mamoe.mirai.event.SimpleListenerHost
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.MessageSource.Key.quote
import net.mamoe.mirai.message.data.buildMessageChain
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.message.data.flash
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import okhttp3.Credentials
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Request
import java.util.concurrent.TimeUnit
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

    private val pattern = Regex("来张(?<search>.+)图")
    private var timestamp = System.currentTimeMillis()

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
        val content = message.content
        val result = pattern.matchEntire(content) ?: return
        val search = result.groups["search"]?.value ?: return
        val commander = toCommandSender()
        if (!commander.hasPermission(searchPermission)) {
            return
        }

        if (System.currentTimeMillis() - timestamp < TimeUnit.SECONDS.toMillis(Settings.interval)) {
            subject.sendMessage(Responses.cooldown.randomOrNull() ?: return)
            return
        }
        timestamp = System.currentTimeMillis()

        val url = "https://e621.net/posts.json".toHttpUrl()
            .newBuilder()
            .addQueryParameter("tags", Settings.alias[search] ?: search)
            .addQueryParameter("page", "1")
            .build()
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "e621-mirai/1.0 (by EntityParrot on e621)")
            .header("Authorization", Credentials.basic(Settings.username, Settings.token))
            .build()

        E621.client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                subject.sendMessage(Responses.failure.randomOrNull() ?: return)
                return
            }

            val payload = Json.decodeFromString<Map<String, List<Post>>>(response.body!!.string())
            val posts = payload["posts"]!!
            if (posts.isEmpty()) {
                subject.sendMessage(Responses.empty.randomOrNull() ?: return)
                return
            }

            val post = posts.sortedBy { it.score.total }.take(10).random()
            if (post.rating != "s" && !commander.hasPermission(sensitivePermission)) {
                subject.sendMessage(Responses.sensitive.randomOrNull() ?: return)
                return
            }

            E621.client.newCall(Request.Builder().url(post.file.url).build()).execute().use { download ->
                if (!download.isSuccessful) {
                    subject.sendMessage(Responses.failure.randomOrNull() ?: return)
                    return
                }
                val resource = download.body!!.byteStream().toExternalResource(post.file.extension)
                subject.sendMessage(
                    resource.uploadAsImage(subject).let {
                        if (post.rating != "s") {
                            it.flash()
                        } else {
                            it
                        }
                    }
                )
            }
        }
    }

}