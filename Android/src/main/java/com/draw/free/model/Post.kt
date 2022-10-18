package com.draw.free.model

import android.os.Parcelable
import androidx.room.*
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import java.util.*


class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long): Date {
        return Date(value)
    }

    @TypeConverter
    fun dateToTimestamp(date: Date): Long {
        return date.time
    }
}

@Parcelize
data class SimpleProfile(
    @SerializedName("id")
    val id: String,

    @SerializedName("account_id")
    val accountID: String,

    @SerializedName("pf_picture")
    var pfPicture: String?

) : Parcelable


@Parcelize
@TypeConverters(Converters::class)
data class Post(
    @SerializedName("id")
    @PrimaryKey
    val id: String,

    @SerializedName("title")
    val title: String,

    @SerializedName("content")
    val content: String,

    @SerializedName("place")
    val place: String?,

    @SerializedName("is_original")
    val isOriginal: Boolean,

    @SerializedName("drawing")
    val drawing: String,

    @SerializedName("created_at")
    val createdAt: Date,

    @SerializedName("thumbnail")
    val thumbnail: String,

    @SerializedName("producer")
    val producer: Producer,

    @SerializedName("viewed_by")
    val viewBy: String,

    @SerializedName("animated_thumbnail")
    val animatedThumbnail: String,

    @SerializedName("video")
    val video: String,

    @SerializedName("likes")
    var likes: Int = 0,

    @SerializedName("comments")
    var comments: Int = 0,

    @SerializedName("status")
    var status: String = "",

    @SerializedName("my_like")
    var myLike: Boolean = false,

    @SerializedName("is_minted")
    var isMinted: Boolean = false,

    @SerializedName("is_mine")
    var isMine: Boolean = false
) : Parcelable {
    override fun equals(other: Any?): Boolean {
        if (other == null || other !is Post) {
            return false
        }

        return id == other.id
    }

    override fun hashCode(): Int {
        return Objects.hashCode(id)
    }
}
