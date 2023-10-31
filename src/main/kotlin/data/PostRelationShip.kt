package me.parrot.mirai.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * e621
 * me.parrot.mirai.data.PostRelationShip
 *
 * @author legoshi
 * @version 1
 * @since 2023/10/31 19:49
 */
@Serializable
data class PostRelationShip(
    @SerialName("parent_id")
    val parentId: Int?,
    @SerialName("has_children")
    val hasChildren: Boolean,
    @SerialName("has_active_children")
    val hasActiveChildren: Boolean,
    val children: List<Int>
)
