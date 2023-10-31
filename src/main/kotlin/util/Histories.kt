package me.parrot.mirai.util

import me.parrot.mirai.data.Post
import java.util.concurrent.TimeUnit

/**
 * e621
 * me.parrot.mirai.util.Histories
 *
 * @author legoshi
 * @version 1
 * @since 2023/10/31 22:37
 */
object Histories {

    private val viewed: MutableMap<Long, Post> = mutableMapOf()
    private val users: MutableMap<Long, Post> = mutableMapOf()

    private val interval = TimeUnit.MINUTES.toMillis(1)

    fun getViewed(): Set<Int> {
        val now = System.currentTimeMillis()
        viewed.keys.filter { now - it >= interval }.forEach(viewed::remove)
        return viewed.values.map(Post::id).toSet()
    }

    fun getUser(userId: Long): Post? = users[userId]

    fun record(userId: Long, post: Post) {
        users[userId] = post
        viewed[System.currentTimeMillis()] = post
    }

}