package com.draw.free.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.draw.free.model.Post
import com.draw.free.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class EditPostActivityViewModel() {
    private val _getPost = MutableLiveData<Boolean>()

    var _post: Post? = null
        set(value) {
            field = value
            _getPost.postValue(true)
        }

    val post : Post
    get() = _post!!

    val getPost: LiveData<Boolean>
        get() = _getPost

    fun setPost(post : Post) {
        _post = post
    }


    /* fun getPostById(postId : String) {
        CoroutineScope(Dispatchers.IO).launch {
            val response = RetrofitClient.getPostService().getPost(postId).execute()
            if (response.isSuccessful && response.code() == 200) {
                Timber.d("정상적으로 포스트를 가져옴")
                post = response.body()!!
                _getPost.postValue(true)
            } else {
                Timber.d("실패함")
                _getPost.postValue(false)
            }
        }
    } */

}