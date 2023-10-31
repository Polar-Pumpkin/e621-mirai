package me.parrot.mirai

import me.parrot.mirai.command.E621Command
import me.parrot.mirai.config.Responses
import me.parrot.mirai.config.Settings
import me.parrot.mirai.listener.SearchListener
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.event.registerTo
import okhttp3.OkHttpClient

object E621 : KotlinPlugin(
    JvmPluginDescription(id = "me.parrot.mirai.e621", name = "e621", version = "1.0.0") {
        author("legoshi")
        info("""Search e621 images""")
    }
) {

    internal val client = OkHttpClient()

    override fun onEnable() {
        Settings.reload()
        Responses.reload()
        E621Command.register()
        SearchListener.registerTo(globalEventChannel())
    }

}