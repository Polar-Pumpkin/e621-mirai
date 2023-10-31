package me.parrot.mirai.command

import me.parrot.mirai.E621
import me.parrot.mirai.E621.reload
import me.parrot.mirai.config.Responses
import me.parrot.mirai.config.Settings
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.buildMessageChain

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
        sendMessage(buildMessageChain {
            user?.let {
                +At(it)
                +" "
            }
            +"重载完毕"
        })
    }

}