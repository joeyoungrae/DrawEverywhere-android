package com.draw.free.interfaceaction

import com.draw.free.model.Post
import com.draw.free.util.CustomList


interface IOpenProfile {
    fun open(postId : String)
}

interface IOpenPost {
    fun open(postId : String, customPostList: CustomList<Post>)
}
