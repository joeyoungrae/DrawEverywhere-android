package com.draw.free.util

import com.draw.free.model.Nft
import com.draw.free.model.Post
import com.draw.free.model.UserProfile
import timber.log.Timber
import java.lang.RuntimeException
import java.util.*
import kotlin.collections.ArrayList

abstract class MainContents

enum class FixedPostContentsTheme {
    TRENDING_DRAWINGS, LIKED_POSTS
}

enum class FixedNftContentsTheme {
    RECENTLY_MINTED
}

class FixedContents(val typeName : FixedPostContentsTheme, val posts : List<Post>) : MainContents() {

    private val mCustomPostList : CustomList<Post> = CustomList(0, null) { data -> data.id }

    init {
        mCustomPostList.setList(posts)
    }

    fun convertCustomPostList() : CustomList<Post> {
        return mCustomPostList
    }

}

class FixedNftContents(val typeName : FixedNftContentsTheme, val nfts : List<Nft>) : MainContents() {

    private val mCustomNftList : CustomList<Nft> = CustomList<Nft>(0, null) {
        it.id
    }


    init {
        mCustomNftList.setList(nfts)
    }

    fun convertCustomNftList() : CustomList<Nft> {
        return mCustomNftList
    }

}

class HashTagContents(val hashtag : String, val posts : List<Post>) : MainContents() {
    private val mCustomPostList : CustomList<Post> = CustomList(0, null) { data -> data.id }

    init {
        mCustomPostList.setList(posts)
    }

    fun convertCustomPostList() : CustomList<Post> {
        return mCustomPostList
    }

    override fun equals(other: Any?): Boolean {
        return (other != null) && (hashtag == (other as HashTagContents).hashtag)
    }

    override fun hashCode(): Int {
        return Objects.hashCode(hashtag)
    }
}

class UserContents(val user : UserProfile, val posts : List<Post>) : MainContents() {
    private val mCustomPostList : CustomList<Post> = CustomList(0, null) { data -> data.id }

    init {
        mCustomPostList.setList(posts)
    }

    fun convertCustomPostList() : CustomList<Post> {
        return mCustomPostList
    }

    override fun equals(other: Any?): Boolean {
        return (other != null) && (user.uniqueId == (other as UserContents).user.uniqueId)
    }

    override fun hashCode(): Int {
        return Objects.hashCode(user)
    }
}

class MainContentsList {
    // 홈화면 컨텐츠 테마들
    var recentlyMintedNft = ArrayList<FixedNftContents>()
    var popularDrawingList = ArrayList<FixedContents>()
    var popularPostList = ArrayList<FixedContents>()
    val popularUserPostList = ArrayList<UserContents>()
    val popularHashTagPostList = ArrayList<HashTagContents>()

    private var popularDrawingOffSet: Int = 0
    private var popularPostOffSet: Int = 0
    private var popularUserOffSet: Int = 0
    private var popularHashtagOffSet: Int = 0

    private fun calculateOffset() {
        popularDrawingOffSet = recentlyMintedNft.size // popularDrawing 시작 인덱스
        popularPostOffSet = popularDrawingOffSet + popularDrawingList.size // popularPost 시작 인덱스
        popularUserOffSet = popularPostOffSet + popularPostList.size // popularUserPost 시작 인덱스
        popularHashtagOffSet = popularUserOffSet + popularUserPostList.size // popularHashtagPost 시작 인덱스
    }

    fun addNft(contents: FixedNftContents) {
        recentlyMintedNft = ArrayList()
        recentlyMintedNft.add(contents)
        calculateOffset()
    }
    fun addDrawing(contents : FixedContents) {
        popularDrawingList = ArrayList()
        popularDrawingList.add(contents)
        calculateOffset()
    }
    fun addPost(contents : FixedContents) {
        popularPostList = ArrayList()
        popularPostList.add(contents)
        calculateOffset()
    }
    fun addUserPost(contents : UserContents) {
        if (popularUserPostList.contains(contents)) {
            Timber.d("이미 있는 아이템이므로, 업데이트 함")
            val index = popularUserPostList.indexOf(contents)
            popularUserPostList[index] = contents
        } else {
            popularUserPostList.add(contents)
        }
        calculateOffset()
    }
    fun addHashTag(contents : HashTagContents) {
        Timber.d("추가함")
        if (popularHashTagPostList.contains(contents)) {
            popularHashTagPostList[popularHashTagPostList.indexOf(contents)] = contents
        } else {
            popularHashTagPostList.add(contents)
        }
        calculateOffset()
    }



    fun get(position: Int): MainContents {
        if (position >= getSize()) {
            throw RuntimeException("Out of Index")
        }

        if (position >= popularHashtagOffSet) {
            return popularHashTagPostList[position - popularHashtagOffSet]
        }

        if (position >= popularUserOffSet) {
            return popularUserPostList[position - popularUserOffSet]
        }

        if (position >= popularPostOffSet) {
            return popularPostList[position - popularPostOffSet]
        }

        if (position >= popularDrawingOffSet) {
            return popularDrawingList[position - popularDrawingOffSet]
        }

        return recentlyMintedNft[position]
    }

    fun getSize(): Int {
        return recentlyMintedNft.size + popularDrawingList.size + popularPostList.size + popularUserPostList.size + popularHashTagPostList.size
    }
}
