package me.parrot.mirai.data

import kotlinx.serialization.Serializable

/**
 * e621
 * me.parrot.mirai.data.PostScore
 *
 * @author legoshi
 * @version 1
 * @since 2023/10/31 19:44
 */
@Serializable
data class PostScore(
    val up: Int,
    val down: Int,
    val total: Int
)
