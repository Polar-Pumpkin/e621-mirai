package me.parrot.mirai.config

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.value

/**
 * e621
 * me.parrot.mirai.config.Settings
 *
 * @author legoshi
 * @version 1
 * @since 2023/10/31 19:54
 */
object Settings : AutoSavePluginConfig("settings") {

    val username: String by value()
    val token: String by value()
    val alias: MutableMap<String, String> by value()

}