package com.draw.free.model

import android.os.Parcelable
import androidx.room.*
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import java.util.*


@TypeConverters(Converters::class)
data class Comment(
    @PrimaryKey
    @SerializedName("id")
    val id: String,

    @SerializedName("comment")
    val comment: String,

    @SerializedName("created_at")
    val createdAt: Date,


    @SerializedName("producer")
    @Embedded
    val producer: Producer,

    @SerializedName("likes")
    val likes: Int,

    @SerializedName("my_like")
    val myLike: Boolean = false,
)


@Parcelize
data class Producer(
    @SerializedName("id")
    @ColumnInfo(name = "producerId")
    val producerId: String,

    @SerializedName("account_id")
    @ColumnInfo(name = "producerAccountId")
    val producerAccountID: String,

    @SerializedName("pf_picture")
    @ColumnInfo(name = "producerPfPicture")
    var producerPfPicture: String?

) : Parcelable
