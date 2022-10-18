package com.draw.free.model

import android.os.Parcelable
import androidx.room.*
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.util.*

data class Nft(
    @SerializedName("id")
    @PrimaryKey
    val id: String,

    @SerializedName("post")
    val post: String,

    @SerializedName("update_authority")
    val updateAuthority: String,

    @SerializedName("mint")
    val mint: String,

    @SerializedName("data")
    val data: Data,

    @SerializedName("primary_sale_happened")
    val primarySaleHappened: Boolean,

    @SerializedName("is_mutable")
    val isMutable: Boolean,

    @SerializedName("edition_nonce")
    val editionNonce: Int,

    @SerializedName("collection")
    val collection: String?,

    @SerializedName("uses")
    val uses: Int?,

    @SerializedName("thumbnail")
    val thumbnail: String,

    @SerializedName("glb")
    val glb: String,

    @SerializedName("created_at")
    val createdAt: Date,

    @SerializedName("owner")
    val owner: String,

    @SerializedName("holder")
    val holder: String?,

    @SerializedName("is_on_sale")
    val isOnSale: Boolean,

    @SerializedName("simple_auction_data")
    var simpleAuctionData: SimpleAuctionData? = null

) {
    override fun equals(other: Any?): Boolean {
        if (other == null || other !is Nft) {
            return false
        }

        return id == other.id
    }

    override fun hashCode(): Int {
        return Objects.hashCode(id)
    }
}

data class Data (
    @SerializedName("name")
    val name: String,

    @SerializedName("symbol")
    val symbol: String,

    @SerializedName("description")
    val description: String,

    @SerializedName("uri")
    var uri: String,

    @SerializedName("seller_fee_basis_points")
    var sellerFeeBasisPoints: Int,

    @SerializedName("creators")
    var creators: List<Creator>

)

data class Creator (
    @SerializedName("address")
    var address: String,

    @SerializedName("share")
    var share: Int,
)

data class WalletWithProfile(
    @SerializedName("address")
    val address: String,

    @SerializedName("profile")
    val profile: SimpleProfile
)


@Parcelize
data class ParcelableNftMetadata(
    var post: String?,

    var thumbnail: String?,

    var name: String?,

    var symbol: String?,

    var description: String?,

    var sellerFeeBasisPoints: String?,

    var creator: String?

) : Parcelable


@Parcelize
data class ParcelableNftSelling(
    var mint: String,

    var holder: String,

    var price: String?

) : Parcelable