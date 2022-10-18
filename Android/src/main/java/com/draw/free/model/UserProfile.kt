package com.draw.free.model

import com.google.gson.annotations.SerializedName

data class UserProfile  (

    @SerializedName("id")
    val uniqueId: String,

    @SerializedName("account_id")
    var accountId: String,

    @SerializedName("pf_picture")
    val pfPicture: String? = "",

    @SerializedName("pf_name")
    val pfName: String? = "",

    @SerializedName("pf_description")
    val pfDescription: String? = "",

    @SerializedName("account_type")
    var accountType: String,

    @SerializedName("wallet_address")
    val walletAddress : String? = "",

    @SerializedName("relation")
    var relation : String = "",

    @SerializedName("num_of_posts")
    val numberOfPost : Int = 0,

    @SerializedName("num_of_followers")
    var numberOfFollowers : Int = 0,

    @SerializedName("num_of_followings")
    val numberOfFollowings : Int = 0,

)
