package com.draw.free.model

import androidx.room.*
import com.google.gson.annotations.SerializedName
import java.util.*

data class Auction(
    @SerializedName("id")
    val id: String,

    @SerializedName("auction_cache")
    val auctionCache: String,

    @SerializedName("state")
    val state: String,

    @SerializedName("price")
    val price: Double,

    @SerializedName("auctioneer")
    val auctioneer: String,

    @SerializedName("created_at")
    val createdAt: Date,

    @SerializedName("is_settled")
    val isSettled: Boolean,

    @SerializedName("buyer")
    val buyer: String?,

    @SerializedName("nft")
    val nft: Nft

) {
    override fun equals(other: Any?): Boolean {
        if (other == null || other !is Auction) {
            return false
        }

        return id == other.id
    }

    override fun hashCode(): Int {
        return Objects.hashCode(id)
    }
}


data class SimpleAuctionData(
    @SerializedName("auction_cache")
    val auctionCache: String,

    @SerializedName("price")
    val price: Double,

    @SerializedName("is_settled")
    val isSettled: Boolean?
)
