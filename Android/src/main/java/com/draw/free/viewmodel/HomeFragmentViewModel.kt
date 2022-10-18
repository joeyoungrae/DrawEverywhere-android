package com.draw.free.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.draw.free.Global
import com.draw.free.model.*
import com.draw.free.network.BaseResponse
import com.draw.free.network.RetrofitClient
import com.draw.free.network.dao.PostService
import com.draw.free.network.dao.SolanaService
import com.draw.free.util.*
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Response
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import javax.security.auth.callback.Callback

class HomeFragmentViewModel() : ViewModel() {

    private var curMainContents: MainContentsList = MainContentsList()
    private var _mCurMainContents = MutableLiveData<MainContentsList>()
    val mCurMainContents
        get() = _mCurMainContents

    private val postDao: PostService = RetrofitClient.getPostService()
    private val isCacheData = AtomicBoolean(false)

    fun getInitialMainPosts(isRefresh : Boolean = false) {
        if (isRefresh || !isCacheData.getAndSet(true)) {

            // 최초의 initial -> 통신 전 빈 값 바인딩
            if (curMainContents.getSize() == 0) {
                curMainContents.addNft(
                    FixedNftContents(
                        FixedNftContentsTheme.RECENTLY_MINTED,
                        emptyList()
                    )
                )
                curMainContents.addDrawing(
                    FixedContents(
                        FixedPostContentsTheme.TRENDING_DRAWINGS,
                        emptyList()
                    )
                )
                curMainContents.addPost(
                    FixedContents(
                        FixedPostContentsTheme.LIKED_POSTS,
                        emptyList()
                    )
                )
                _mCurMainContents.postValue(curMainContents)
            }

            postDao.getPostByRankingForMain().enqueue(BaseResponse<MainPostList>() { response ->
                if (response.code() == 200) {

                    // 최근에 발행된 nfts
                   val mintedNftList = FixedNftContents(
                        FixedNftContentsTheme.RECENTLY_MINTED,
                        response.body()!!.recentlyMintedList
                    )

                    curMainContents.addNft(mintedNftList)
                    _mCurMainContents.postValue(curMainContents)


                    // 인기 그림
                    val popularDrawList = FixedContents(
                        FixedPostContentsTheme.TRENDING_DRAWINGS,
                        response.body()!!.topDrawingPostList
                    )
                    curMainContents.addDrawing(popularDrawList)
                    _mCurMainContents.postValue(curMainContents)


                    // 인기 좋아요 포스트
                    val popularLikePostList = FixedContents(
                        FixedPostContentsTheme.LIKED_POSTS,
                        response.body()!!.topLikePostList
                    )
                    curMainContents.addPost(popularLikePostList)
                    _mCurMainContents.postValue(curMainContents)


                    // 인기 유저
                    val popularUserResult = response.body()!!.topUserPostList
                    for (element in popularUserResult) {
                        val user = element.user
                        val post = element.posts
                        val starUser = UserContents(user, post)
                        curMainContents.addUserPost(starUser)
                    }

                    return@BaseResponse true
                }

                return@BaseResponse false
            })

        }

        _mCurMainContents.postValue(curMainContents)
    }

}