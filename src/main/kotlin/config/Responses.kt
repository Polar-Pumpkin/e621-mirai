package me.parrot.mirai.config

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.value

/**
 * e621
 * me.parrot.mirai.config.Responses
 *
 * @author legoshi
 * @version 1
 * @since 2023/10/31 20:28
 */
object Responses : AutoSavePluginConfig("responses") {

    val sensitive: MutableList<String> by value(
        mutableListOf(
            "🫣这个......不能给你看！",
            "😡"
        )
    )

    val empty: MutableList<String> by value(mutableListOf("什么也没找到呢"))

    val failure: MutableList<String> by value(mutableListOf("下载失败了呢"))

}