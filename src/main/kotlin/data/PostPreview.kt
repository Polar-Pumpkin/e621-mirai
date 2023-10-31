package me.parrot.mirai.data

import kotlinx.serialization.Serializable

/**
 * e621
 * me.parrot.mirai.data.PostPreview
 *
 * @author legoshi
 * @version 1
 * @since 2023/10/31 19:42
 */
@Serializable
data class PostPreview(
    val width: Int,
    val height: Int,
    val url: String
)
