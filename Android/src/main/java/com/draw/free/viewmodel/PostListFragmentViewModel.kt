package com.draw.free.viewmodel

import androidx.lifecycle.ViewModel
import com.draw.free.model.Post
import com.draw.free.network.RetrofitClient
import com.draw.free.util.CustomList


class PostListFragmentViewModel : ViewModel() {

    lateinit var hashtag: String

    fun initialGetCustomPost(): CustomList<Post> {

        val postListByTag = CustomList(10, { pageSize : Int, offset: String ->
            return@CustomList RetrofitClient.getPostService().getPostsByTag(offset = offset, size = pageSize, words = hashtag).data
        }, getKey = { it.id })

        postListByTag.getNextData()
        return postListByTag

    }

}