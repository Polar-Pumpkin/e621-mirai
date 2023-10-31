package me.parrot.mirai.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * e621
 * me.parrot.mirai.data.PostFile
 *
 * @author legoshi
 * @version 1
 * @since 2023/10/31 19:42
 */
@Serializable
data class PostFile(
    val width: Int,
    val height: Int,
    @SerialName("ext")
    val extension: String,
    val size: Int,
    val md5: String,
    val url: String
)
