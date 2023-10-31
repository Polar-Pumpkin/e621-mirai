package me.parrot.mirai.data

import kotlinx.serialization.Serializable

/**
 * e621
 * me.parrot.mirai.data.PostTagList
 *
 * @author legoshi
 * @version 1
 * @since 2023/10/31 19:45
 */
@Serializable
data class PostTag(
    val general: List<String>,
    val artist: List<String>,
    val copyright: List<String>,
    val character: List<String>,
    val species: List<String>,
    val invalid: List<String>,
    val meta: List<String>,
    val lore: List<String>
)
