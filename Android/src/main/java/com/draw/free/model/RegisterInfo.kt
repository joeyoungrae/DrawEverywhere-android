package com.draw.free.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


enum class JoinType(val joinType: String) {
    KAKAO("kakao"),
    GOOGLE("google"),
    NAVER("naver");
}

@Parcelize
data class RegisterInfo(

    val joinType: String,
    val oauthAccessToken: String,
    val oauthId: String,
    var accountId: String? = null

) : Parcelable