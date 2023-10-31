package me.parrot.mirai.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * e621
 * me.parrot.mirai.data.PostFlag
 *
 * @author legoshi
 * @version 1
 * @since 2023/10/31 19:47
 */
@Serializable
data class PostFlag(
    val pending: Boolean,
    val flagged: Boolean,
    @SerialName("note_locked")
    val noteLocked: Boolean,
    @SerialName("status_locked")
    val statusLocked: Boolean,
    @SerialName("rating_locked")
    val ratingLocked: Boolean,
    val deleted: Boolean
)
