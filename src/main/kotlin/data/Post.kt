@file:UseSerializers(DateSerializer::class)

package me.parrot.mirai.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import me.parrot.mirai.serializer.DateSerializer
import java.util.*

/**
 * e621
 * me.parrot.mirai.data.Post
 *
 * @author legoshi
 * @version 1
 * @since 2023/10/31 19:39
 */
@Serializable
data class Post(
    val id: Int,
    @SerialName("created_at")
    val createdAt: Date,
    @SerialName("updated_at")
    val updatedAt: Date,
    val file: PostFile,
    val preview: PostPreview,
    val sample: PostSample,
    val score: PostScore,
    val tags: PostTag,
    @SerialName("locked_tags")
    val lockedTags: List<String>,
    @SerialName("change_seq")
    val changeSeq: Int,
    val flags: PostFlag,
    val rating: String,
    @SerialName("fav_count")
    val favorite: Int,
    val sources: List<String>,
    val pools: List<Int>,
    val relationships: PostRelationShip,
    @SerialName("approver_id")
    val approverId: Int?,
    @SerialName("uploader_id")
    val uploaderId: Int,
    val description: String,
    @SerialName("comment_count")
    val comment: Int,
    @SerialName("is_favorited")
    val isFavorited: Boolean,
    @SerialName("has_notes")
    val hasNotes: Boolean,
    val duration: Double?
)
