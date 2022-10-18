package com.draw.free.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.draw.free.Global
import com.draw.free.fragment.SearchFragment
import com.draw.free.model.Post
import com.draw.free.model.PostHashTag
import com.draw.free.model.UserProfile
import com.draw.free.network.BaseResponse
import com.draw.free.network.RetrofitClient
import com.draw.free.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.RuntimeException

class SearchFragmentViewModel : ViewModel() {
    enum class SearchType {
        Title, Tags, User
    }

    lateinit var mOpenPost: (postId: String) -> Unit
    lateinit var mOpenUserProfile: (accountId: String) -> Unit
    lateinit var mOpenTag: (hashTag: String) -> Unit
    private val _type = MutableLiveData(SearchType.Title)
    val typeLiveData: LiveData<SearchType>
        get() = _type

    val postListByTitle = MutableLiveData<CustomList<Post>>()
    val hashTagsByKeyword = MutableLiveData<CustomList<PostHashTag>>()
    val userListsByKeyword = MutableLiveData<CustomList<UserProfile>>()
    private val userService = RetrofitClient.getUserService()


    fun openPost(postId: String) {
        mOpenPost(postId)
    }

    fun openProfile(accountId: String) {
        mOpenUserProfile(accountId)
    }

    fun openTag(hashTag: String) {
        mOpenTag(hashTag)
    }

    fun switchType(type: SearchType) {
        Timber.e("type : ${type.name}")
        _type.value = type
    }

    fun search(keyword: String) {

        Timber.d("Search, type : ${typeLiveData.value} keyWorld : $keyword")

        RetrofitClient.getPostService().getSearchResultAtOnce(keyword).enqueue(BaseResponse {
            if (it.isSuccessful && it.code() == 200) {
                val data = it.body()!!

                val postListByTitle = CustomList(10, api = { pageSize: Int, offset: String ->
                    return@CustomList RetrofitClient.getPostService()
                        .getPostsByTitle(offset = offset, size = pageSize, words = keyword).data
                }, getKey = { it.id })

                postListByTitle.addList(data.postList)
                this.postListByTitle.postValue(postListByTitle)


                val userListsByKeyword = CustomList(10, api = { pageSize: Int, offset: String ->
                    return@CustomList RetrofitClient.getPostService()
                        .getUsersByKeyword(offset = offset, size = pageSize, words = keyword).data
                }, getKey = {
                    it.accountId
                })

                userListsByKeyword.addList(data.profileList)
                this.userListsByKeyword.postValue(userListsByKeyword)


                val hashTagsByKeyword = CustomList(10, api = { pageSize: Int, offset: String ->
                    return@CustomList RetrofitClient.getPostService()
                        .getTagsAndCount(offset = offset, size = pageSize, words = keyword).data
                }, { it.hashtag })

                hashTagsByKeyword.addList(data.hashtagList)
                this.hashTagsByKeyword.postValue(hashTagsByKeyword)




                return@BaseResponse true
            }

            return@BaseResponse false
        })
    }

    val followerUser: (String, (String) -> Unit) -> Unit =
        { target: String, updateRelation: (String) -> Unit ->
            //CoroutineScope(Dispatchers.IO).launch {
            //  val response = userService.followUser(target).execute()

            userService.followUser(target).enqueue(BaseResponse() {
                if (it.isSuccessful && it.code() == 200) {
                    val status: String = it.body()!!

                    when (status) {
                        "None" -> {
                            Global.makeToast("언팔로우 하였습니다.")
                        }
                        "Follower" -> {
                            Global.makeToast("팔로우 하였습니다.")
                        }
                        "Requested" -> {
                            Global.makeToast("팔로우 신청을 하였습니다.")

                        }
                    }
                    updateRelation(status)
                    return@BaseResponse true
                }

                return@BaseResponse false
            })
        }
}


