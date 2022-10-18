package com.draw.free.viewmodel


import androidx.lifecycle.ViewModel

import com.draw.free.Global
import com.draw.free.model.UserProfile
import com.draw.free.network.BaseResponse
import com.draw.free.network.RetrofitClient
import com.draw.free.util.CustomList
import timber.log.Timber


class UserRelationListViewModel(val accountId: String) : ViewModel() {
    enum class ListType {
        Follow, Following
    }

    lateinit var followerList : CustomList<UserProfile>
    lateinit var followingList : CustomList<UserProfile>

    private val userService = RetrofitClient.getUserService()


    lateinit var mOpenUserProfile : (accountId : String) -> Unit

    fun openProfile(accountId : String) {
        mOpenUserProfile(accountId)
    }


    fun setInitial() {
        followerList = CustomList<UserProfile>(api = { pageSize: Int, offset: String ->
            return@CustomList userService.getFollowsList(accountId, offset, pageSize).data }, getKey = { it.accountId }, pageSize = 10,)


        followingList = CustomList(10, api = { pageSize: Int, offset: String ->
            return@CustomList userService.getFollowings(accountId, offset, pageSize).data }, getKey = { it.accountId })

        followerList.getNextData()
        followingList.getNextData()
    }


    val followerUser: (String, (String) -> Unit) -> Unit = { target: String, updateRelation: (String) -> Unit ->
            userService.followUser(target).enqueue(BaseResponse() { response ->
                if (response.code() == 200) {
                    val status : String = response.body()!!
                    val followerUser : UserProfile? by lazy {
                        val pos = followerList.findPositionByKey(target)
                        if (pos != -1) {
                            followerList.getDataByPosition(pos)
                        } else {
                            null
                        }
                    }

                    val followingUser : UserProfile? by lazy {
                        val pos = followingList.findPositionByKey(target)
                        if (pos != -1) {
                            followingList.getDataByPosition(pos)
                        } else {
                            null
                        }
                    }


                    when (status) {
                        "Follower" -> {
                            Global.makeToast("팔로우 하였습니다.")
                        }
                        "Requested" -> {
                            Global.makeToast("팔로우 신청을 하였습니다..")
                        }
                        "None" -> {
                            Global.makeToast("언팔로우 하였습니다.")
                        }
                    }

                    followerUser?.relation = status
                    followingUser?.relation = status

                    followerList.updateLiveData()
                    followingList.updateLiveData()


                    return@BaseResponse true
                }

                return@BaseResponse false
            })

    }
}