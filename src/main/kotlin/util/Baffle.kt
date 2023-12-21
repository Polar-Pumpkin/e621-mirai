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

    private var timestamp = 0L
    private val individuals = mutableMapOf<Long, Long>()

    private val second = TimeUnit.SECONDS.toMillis(1)
    private val interval
        get() = TimeUnit.SECONDS.toMillis(Settings.interval)

    fun next(id: Long, update: Boolean = true): Boolean {
        val now = System.currentTimeMillis()
        // Shared cooldown: 1s
        if (now - timestamp < second) {
            return false
        }

        val last = individuals[id]
        // Individuals cooldown: configuration
        if (last != null && now - last < interval) {
            return false
        }
        // Update shared & individuals timestamp
        timestamp = now
        if (update) {
            individuals[id] = now
        }
        return true
    }

}