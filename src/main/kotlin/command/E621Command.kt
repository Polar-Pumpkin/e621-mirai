package me.parrot.mirai.command

import me.parrot.mirai.E621
import me.parrot.mirai.E621.reload
import me.parrot.mirai.config.Responses
import me.parrot.mirai.config.Settings
import me.parrot.mirai.util.Histories
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.UserCommandSender
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.data.MessageSource.Key.quote
import kotlin.math.ceil
import kotlin.math.roundToInt

/**
 * e621
 * me.parrot.mirai.command.E621Command
 *
 * @author legoshi
 * @version 1
 * @since 2023/10/31 20:33
 */
object E621Command : CompositeCommand(E621, "e621") {

    @SubCommand
    suspend fun CommandSender.reload() {
        Settings.reload()
        Responses.reload()
        reply { +"重载完毕" }
    }

    @SubCommand
    suspend fun UserCommandSender.last(user: User = this.user) {
        val post = Histories.getUser(user.id) ?: return reply { +"未找到搜图记录" }
        reply {
            +"\n"
            +"【e621 ID】${post.id}\n"
            +"【评级】${post.rating.uppercase()}\n"
            +"【喜欢】${post.favorite}\n"
            +"【分数】${post.score.total} (+${post.score.up}, ${post.score.down})\n"
            post.tags.artist.takeIf { it.isNotEmpty() }?.asTags("【画师】\n")
            +"【来源】\n"
            post.sources
                .ifEmpty { listOf("(无)") }
                .forEach { +"$it\n" }
            if (post.description.isNotBlank()) {
                +"【描述】\n"
                +post.description
            }
        }
    }

    @SubCommand
    suspend fun UserCommandSender.lastTags(user: User = this.user) {
        val post = Histories.getUser(user.id) ?: return reply { +"未找到搜图记录" }
        reply {
            with(post.tags) {
                +general.asTags("【通用】\n")
                +artist.asTags("【艺术家】\n")
                +copyright.asTags("【版权】\n")
                +character.asTags("【角色】\n")
                +species.asTags("【种族】\n")
                +invalid.asTags("【无效标签】\n")
                +lore.asTags("【补充信息】\n")
            }
        }
    }

    @SubCommand
    suspend fun CommandSender.alias(action: String, argument: String) {
        when (action) {
            "create", "add", "+" -> {
                val parts = argument.split("->", limit = 2)
                val name = parts[0]
                Settings.alias[name]?.let {
                    return reply { +"昵称 $name 已存在: $it" }
                }

                if (parts.size == 1) {
                    if (this !is UserCommandSender) {
                        return reply { +"格式错误, 请使用: 昵称->实际关键词" }
                    }
                    reply { +"请发送完整的关键词内容以设置昵称: $name" }
                    E621.globalEventChannel()
                        .filterIsInstance<MessageEvent>()
                        .filter { it.sender.id == user.id }
                        .subscribeOnce<MessageEvent> { _ ->
                            val contents = message.contentsList().filter { it !is UnsupportedMessage }
                            val texts = contents.filterIsInstance<PlainText>()
                            if (contents.size != 1 || contents.size != texts.size) {
                                return@subscribeOnce reply { +"请发送一条仅含有文本内容的消息以设置实际关键词, 本次编辑已取消" }
                            }
                            val keyword = texts.first().content
                            Settings.alias[name] = keyword
                            reply { +"已保存昵称: $name -> $keyword" }
                        }
                    return
                }

                val keyword = parts[1]
                val search = Settings.alias[keyword] ?: keyword
                Settings.alias[name] = search
                reply { +"已保存昵称: $name -> $search" }
            }

            "delete", "del", "remove", "rem", "-" -> {
                reply {
                    if (Settings.alias.remove(argument) != null) {
                        +"昵称 $argument 已删除"
                    } else {
                        +"昵称 $argument 不存在"
                    }
                }
            }

            "list" -> {
                val page = argument.toIntOrNull()
                    ?.takeIf { it > 0 }
                    ?: return reply { +"页码错误, 请使用正整数" }
                Settings.alias.entries.onPage(this, page) { (name, keyword) -> +"$name -> $keyword\n" }
            }

            else -> reply { +"未知的操作: $action" }
        }
    }

    @SubCommand
    suspend fun CommandSender.responses(category: String, action: String, argument: String) {
        val responses = when (category) {
            "sensitive" -> Responses.sensitive
            "empty" -> Responses.empty
            "failure" -> Responses.failure
            "cooldown" -> Responses.cooldown
            else -> {
                reply { +"未知的响应: $category" }
                return
            }
        }

        when (action) {
            "add", "append", "+" -> {
                responses.add(argument)
                reply { +"已添加响应: $argument" }
            }

            "delete", "del", "remove", "rem", "-" -> {
                reply {
                    if (responses.remove(argument)) {
                        +"已删除响应: $argument"
                    } else {
                        +"该响应不存在: $argument"
                    }
                }
            }

            "list" -> {
                val page = argument.toIntOrNull()
                    ?.takeIf { it > 0 }
                    ?: return reply { +"页码错误, 请使用正整数" }
                responses.onPage(this, page) { +"$it\n" }
            }

            else -> reply { +"未知的操作: $action" }
        }
    }

    private fun List<String>.asTags(prefix: String = "\n", suffix: String = "\n"): String {
        return prefix + joinToString(" ") { it.replace(' ', '_') } + suffix
    }

    private suspend fun CommandSender.reply(block: MessageChainBuilder.() -> Unit) {
        sendMessage(buildMessageChain {
            user?.let {
                +At(it)
                +" "
            }
            block()
        })
    }

    private suspend fun MessageEvent.reply(block: MessageChainBuilder.() -> Unit) {
        subject.sendMessage(buildMessageChain {
            +message.quote()
            +" "
            block()
        })
    }

    private suspend fun <E> Collection<E>.onPage(
        sender: CommandSender,
        page: Int,
        size: Long = 10L,
        block: MessageChainBuilder.(E) -> Unit
    ) {
        val pages = ceil(this.size / size.toDouble()).roundToInt()
        if (page < 1 || page > pages) {
            return sender.reply { +"未找到第 $page 页, 共 $pages 页" }
        }
        sender.reply {
            +"第 $page 页 / 共 $pages 页\n"
            this@onPage.stream()
                .skip((page - 1) * size)
                .limit(size)
                .forEach { block(it) }
        }
    }

}