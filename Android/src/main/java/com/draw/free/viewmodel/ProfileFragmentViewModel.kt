package com.draw.free.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.draw.free.Global
import com.draw.free.interfaceaction.ToNextWork
import com.draw.free.model.Post
import com.draw.free.model.UserProfile
import com.draw.free.network.BaseResponse
import com.draw.free.network.RetrofitClient
import com.draw.free.network.dao.UserService
import com.draw.free.util.CustomList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class ProfileFragmentViewModel : ViewModel() {
    enum class Type {
        Write, Like
    }

    enum class FollowerType(value: String) {
        None("None"),
        Follower("Follower"), // Follower: 상대방이 나를 팔로우
        Following("Following"), // Following: 내가 상대방을 팔로우
        Requested("Requested"),
        FollowingEachOther("Following Each Other"),
        RequestedByMe("Requested By Me"),
        RequestedByCounterpart("Requested By Counterpart"),
        RequestedByBoth("Requested By Both")
    }

    private val userService = RetrofitClient.getUserService()

    var logOut: ToNextWork? = null

    var lastTarget: String = ""
        set(value) {
            field = value.ifEmpty { Global.userProfile!!.accountId }
        }

    private val _isScretAccount = MutableLiveData<Boolean>()
    val isSecretAccount
        get() = _isScretAccount

    fun setCanShowPost(value : Boolean) {
        _isScretAccount.postValue(value)
    }

    private val _targetUserProfile: MutableLiveData<UserProfile> = MutableLiveData()
    val targetUserProfile: LiveData<UserProfile>
        get() = _targetUserProfile

    private var _writePosts: CustomList<Post>? = null
    private var _likePosts: CustomList<Post>? = null

    val writePosts: CustomList<Post>
        get() = _writePosts!!

    val likePosts: CustomList<Post>
        get() = _likePosts!!


    private val userDao: UserService = RetrofitClient.getUserService()

    fun destroyViewModel() {
        Timber.e("삭제함")
        _writePosts = null
        _likePosts = null
    }

    fun setCustomPostList() {
        val update = lastTarget

        val getWritePostService: suspend (pageSize: Int, offset: String) -> List<Post> =
            { pageSize, offset ->
                RetrofitClient.getPostService()
                    .getUsersPost(offset = offset, size = pageSize, accountId = update).data
            }


        val getLikePostService: suspend (pageSize: Int, offset: String) -> List<Post> =
            { pageSize, offset ->
                RetrofitClient.getPostService()
                    .getUsersLikePosts(offset = offset, size = pageSize, accountId = update).data
            }

        _writePosts = CustomList(10, api = getWritePostService) { data -> data.id }
        _likePosts = CustomList(10, api = getLikePostService) { data -> data.id }
    }

    fun followUser() {
        var count = 0;

        userService.followUser(lastTarget).enqueue(BaseResponse<String>() { response ->
            if (response.isSuccessful && response.code() == 200) {
                when (response.body()) {
                    "None" -> {
                        --count
                        Global.makeToast("언팔로우 하였습니다.")
                    }
                    "Follower" -> {
                        ++count
                        Global.makeToast("팔로우 하였습니다.")
                    }
                    "Requested" -> {
                        Global.makeToast("팔로우 신청을 하였습니다.")
                    }
                }

                val user = _targetUserProfile.value
                user!!.numberOfFollowers += count;
                _targetUserProfile.postValue(user)

                checkRelationShip()
                return@BaseResponse true
            }

            return@BaseResponse false
        })

    }

    fun checkRelationShip() {
        if (Global.userProfile == null || Global.userProfile?.uniqueId == lastTarget) {
            Timber.d("로그인 안하거나 자기 자신 이므로 체크할 필요가 없음")
            return
        }


        userService.checkRelationShip(lastTarget).enqueue(BaseResponse<String>() { response ->
            if (response.isSuccessful && response.code() == 200) {
                val user = _targetUserProfile.value
                if (user != null) {
                    user.relation = response.body()!!
                }

                _targetUserProfile.postValue(user)
                return@BaseResponse true
            }

            return@BaseResponse false
        })
    }

    fun getUserProfile() {
        Timber.d("대상 : $lastTarget")

        if (lastTarget.isEmpty() || (Global.userProfile != null && lastTarget == Global.userProfile?.accountId)) {
            _targetUserProfile.postValue(Global.userProfile)
            Timber.d("THIS__ : $lastTarget")


            userDao.getMyProfile().enqueue(BaseResponse<UserProfile>() { response ->
                if (response.code() == 200) {
                    _targetUserProfile.postValue(response.body())
                    return@BaseResponse true
                }

                return@BaseResponse false
            })


        } else {
            userDao.getUserProfile(lastTarget).enqueue(BaseResponse<UserProfile>() { response ->
                if (response.code() == 200) {
                    _targetUserProfile.postValue(response.body())
                    return@BaseResponse true
                }

                return@BaseResponse false
            })
        }


    }


}