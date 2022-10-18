package com.draw.free.model

import com.google.gson.annotations.SerializedName

data class UserProfileListingResponse(
    @SerializedName("profile_list")
    val data : List<UserProfile>
)