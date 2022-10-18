package com.draw.free.model

import com.google.gson.annotations.SerializedName
import org.bouncycastle.oer.its.Latitude
import org.bouncycastle.oer.its.Longitude

data class CommentListingResponse(
    @SerializedName("comment_list")
    val data : List<Comment>
)


data class NormalResponse(
    @SerializedName("message")
    val message: String
)

data class PostListingResponse(
    @SerializedName("post_list")
    val data: List<Post>
)

data class TagListingResponse(
    @SerializedName("hashtag_list")
    val data: List<PostHashTag>
)

data class UserListingResponse(
    @SerializedName("profile_list")
    val data: List<UserProfile>
)

data class SearchResponse(
    @SerializedName("post_list")
    val postList: List<Post>,

    @SerializedName("profile_list")
    val profileList: List<UserProfile>,

    @SerializedName("hashtag_list")
    val hashtagList : List<PostHashTag>
)

data class PostLocation (
    @SerializedName("longitude")
    val longitude : Double,

    @SerializedName("latitude")
    val latitude: Double,
)

data class MainPostList (
    @SerializedName("recently_minted_top10")
    val recentlyMintedList : List<Nft>,

    @SerializedName("drawing_top10")
    val topDrawingPostList: List<Post>,

    @SerializedName("like_top10")
    val topLikePostList: List<Post>,

    @SerializedName("user_top10")
    val topUserPostList: List<UserAndPostListForAdapter>

)

class PostList(
    @SerializedName("post_list")
    val posts: List<Post>
)

class PopularUsersPostList(
    @SerializedName("user_list")
    val usersAndPostList: List<UserAndPostList>
)

class PopularHashtagPostList(
    @SerializedName("hashtag_list")
    val hashTagAndPostList: List<HashTagAndPostList>
)

class UserAndPostList(
    @SerializedName("user")
    val user: UserProfile,

    @SerializedName("post_list")
    val posts: List<Post>
)


class UserAndPostListForAdapter (
    @SerializedName("user")
    val user: UserProfile,

    @SerializedName("post_list")
    val posts: List<Post>
)

class HashTagAndPostList(
    @SerializedName("hashtag")
    val hashTag: String,

    @SerializedName("post_list")
    val posts: List<Post>
)
