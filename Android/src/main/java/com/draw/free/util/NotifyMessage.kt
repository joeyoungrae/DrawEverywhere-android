package com.draw.free.util

import com.draw.free.model.UserProfile

// NotifyMessage
abstract class NotifyMessage {
    abstract fun getKey() : String
}

// 팔로우 요청
class RequestFollow(val targetUser : UserProfile) : NotifyMessage() {
    override fun getKey() : String {
        return "RequestFollow:${targetUser.accountId}"
    }
}