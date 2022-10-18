package com.draw.free.model

import com.google.gson.annotations.SerializedName

data class NftListingResponse(
    @SerializedName("nft_list")
    val data : List<Nft>
)

data class AuctionListingResponse(
    @SerializedName("auction_list")
    val data : List<Auction>
)

data class MessageResponse(
    @SerializedName("message")
    val message : String
)

data class StatusResponse(
    @SerializedName("status")
    val status: String,

    @SerializedName("progress")
    val progress: String,

    @SerializedName("message")
    val message: String
)

data class WalletChangeResponse(
    @SerializedName("public_key")
    val publicKey: String,

    @SerializedName("secret_key")
    val secretKey: String,

    @SerializedName("seed_phrase")
    val seedPhrase: String
)