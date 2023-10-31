package me.parrot.mirai.util

import me.parrot.mirai.config.Settings
import java.util.concurrent.TimeUnit

/**
 * e621
 * me.parrot.mirai.util.Baffle
 *
 * @author legoshi
 * @version 1
 * @since 2023/10/31 22:31
 */
object Baffle {

    private val timestamp = System.currentTimeMillis()
    private val individuals = mutableMapOf<Long, Long>()

    private val second = TimeUnit.SECONDS.toMillis(1)
    private val interval
        get() = TimeUnit.SECONDS.toMillis(Settings.interval)

    fun next(id: Long, update: Boolean = true): Boolean {
        val now = System.currentTimeMillis()
        val last = individuals[id]
        if (update) {
            individuals[id] = now
        }

        if (now - timestamp < second || last == null) {
            return false
        }
        return now - last >= interval
    }

}