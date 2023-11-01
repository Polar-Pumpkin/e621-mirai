package me.parrot.mirai.command

import me.parrot.mirai.E621
import me.parrot.mirai.E621.reload
import me.parrot.mirai.config.Responses
import me.parrot.mirai.config.Settings
import me.parrot.mirai.util.Histories
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.UserCommandSender
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.MessageChainBuilder
import net.mamoe.mirai.message.data.buildMessageChain
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
    suspend fun UserCommandSender.last() {
        val post = Histories.getUser(user.id)
        if (post == null) {
            reply { +"未找到最近一次搜图记录" }
            return
        }
        reply {
            +"Id: ${post.id}\n"
            +"Rating: ${post.rating}\n"
            +"Sources:\n"
            post.sources.ifEmpty { listOf("(Empty)") }
                .forEach { +"$it\n" }
            +"Description:\n"
            +post.description
        }
    }

    @SubCommand
    suspend fun CommandSender.alias(action: String, argument: String) {
        when (action) {
            "create" -> {
                val parts = argument.split("->", limit = 2)
                if (parts.size != 2) {
                    reply { +"格式错误, 请使用: 昵称->实际关键词" }
                    return
                }

                val (name, keyword) = parts
                Settings.alias[name]?.let {
                    reply { +"昵称 $name 已存在: $it" }
                    return
                }
                Settings.alias[name] = keyword
                reply { +"已保存昵称: $name -> $keyword" }
            }

            "delete" -> {
                reply {
                    if (Settings.alias.remove(argument) != null) {
                        +"昵称 $argument 已删除"
                    } else {
                        +"昵称 $argument 不存在"
                    }
                }
            }

            "list" -> {
                val page = argument.toIntOrNull()?.takeIf { it > 0 }
                if (page == null) {
                    reply { +"页码错误, 请使用正整数" }
                    return
                }

                val alias = Settings.alias.entries
                val size = 10L
                val total = alias.size
                val pages = ceil(total / size.toDouble()).roundToInt()
                if (page > pages) {
                    reply { +"没有第 $page 页, 共 $pages 页" }
                    return
                }
                reply {
                    +"第 $page 页 / 共 $pages 页\n"
                    alias.stream()
                        .skip((page - 1) * size)
                        .limit(size)
                        .forEach { (name, keyword) ->
                            +"$name -> $keyword\n"
                        }
                }
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
            "add" -> {
                responses.add(argument)
                reply { +"已添加响应: $argument" }
            }

            "remove" -> {
                reply {
                    if (responses.remove(argument)) {
                        +"已删除响应: $argument"
                    } else {
                        +"该响应不存在: $argument"
                    }
                }
            }

            "list" -> {
                val page = argument.toIntOrNull()?.takeIf { it > 0 }
                if (page == null) {
                    reply { +"页码错误, 请使用正整数" }
                    return
                }

                val size = 10L
                val total = responses.size
                val pages = ceil(total / size.toDouble()).roundToInt()
                if (page > pages) {
                    reply { +"没有第 $page 页, 共 $pages 页" }
                    return
                }
                reply {
                    +"第 $page 页 / 共 $pages 页\n"
                    responses.stream()
                        .skip((page - 1) * size)
                        .limit(size)
                        .forEach { +"$it\n" }
                }
            }

            else -> reply { +"未知的操作: $action" }
        }
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

}