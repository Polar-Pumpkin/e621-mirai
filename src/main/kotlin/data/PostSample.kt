package me.parrot.mirai.data

import kotlinx.serialization.Serializable

/**
 * e621
 * me.parrot.mirai.data.PostSample
 *
 * @author legoshi
 * @version 1
 * @since 2023/10/31 19:43
 */
@Serializable
data class PostSample(
    val has: Boolean,
    val height: Int,
    val width: Int,
    val url: String,
    val alternates: String
)
