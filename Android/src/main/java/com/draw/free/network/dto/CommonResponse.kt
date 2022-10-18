package com.draw.free.network.dto

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class CommonResponse(
    @SerializedName("message")
    private var status: String,

)
