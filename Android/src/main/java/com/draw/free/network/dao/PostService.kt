package com.draw.free.network.dao

import com.draw.free.model.*
import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface PostService {
    // TODO 포스트 ID기반 위치 가져오기
    @Headers("Content-Type: application/json")
    @POST("api/posts/{post_id}/report")
    fun reportPost(@Path("post_id") postId: String): Call<NormalResponse>



    @Headers("Content-Type: application/json")
    @GET("api/posts/{post_id}/location")
    fun getLocationFromPostID(@Path("post_id") postId: String): Call<PostLocation>
    
    // 검색시 전부 가져오기
    @Headers("Content-Type: application/json")
    @GET("api/search/words/{words}")
    fun getSearchResultAtOnce(@Path("words") words: String): Call<SearchResponse>

    @Headers("Content-Type: application/json")
    @GET("api/posts/list/{offset}/{size}/user/{account_id}")
    suspend fun getUsersPost(
        @Path("offset") offset: String,
        @Path("size") size: Int,
        @Path("account_id") accountId: String
    ): PostListingResponse

    @Headers("Content-Type: application/json")
    @GET("api/posts/list/{offset}/{size}/user/{account_id}/likes")
    suspend fun getUsersLikePosts(
        @Path("offset") offset: String,
        @Path("size") size: Int,
        @Path("account_id") accountId: String
    ): PostListingResponse

    @Headers("Content-Type: application/json")
    @GET("api/posts/list/{offset}/{size}/title/{words}")
    suspend fun getPostsByTitle(
        @Path("offset") offset: String,
        @Path("size") size: Int,
        @Path("words") words: String
    ): PostListingResponse

    @Headers("Content-Type: application/json")
    @GET("api/posts/list/{offset}/{size}/hashtag/{words}")
    suspend fun getPostsByTag(
        @Path("offset") offset: String,
        @Path("size") size: Int,
        @Path("words") words: String
    ): PostListingResponse

    @Headers("Content-Type: application/json")
    @GET("api/posts/hashtag_list/{offset}/{size}/hashtag/{words}")
    suspend fun getTagsAndCount(
        @Path("offset") offset: String,
        @Path("size") size: Int,
        @Path("words") words: String
    ): TagListingResponse

    @Headers("Content-Type: application/json")
    @GET("api/users/list/{offset}/{size}/search/{words}")
    suspend fun getUsersByKeyword(
        @Path("offset") offset: String,
        @Path("size") size: Int,
        @Path("words") words: String
    ): UserListingResponse

    @GET("api/posts/lists/home/top10")
    fun getPostByRankingForMain(): Call<MainPostList>

    @POST("api/posts/{post_id}/like")
    fun likePost(@Path("post_id") postId: String): Call<String>

    /* @Multipart
    @POST("api/posts")
    fun uploadPost(
        @Part is_Original: MultipartBody.Part,
        @Part longitude: MultipartBody.Part,
        @Part latitude: MultipartBody.Part,
        @Part place: MultipartBody.Part,
        @Part title: MultipartBody.Part,
        @Part content: MultipartBody.Part,
        @Part video: MultipartBody.Part,
        @Part drawing: MultipartBody.Part,
        @Part viewedBy: MultipartBody.Part
    ): Call<ResponseBody> */

    @FormUrlEncoded
    @POST("api/posts")
    fun uploadPost(
        @Field("temp_s3_name") id : String,
        @Field("longitude") longitude : String,
        @Field("latitude") latitude : String,
        @Field("title") title : String,
        @Field("content") content : String,
        @Field("place") place : String,
        @Field("is_original") is_original : String,
        @Field("viewed_by") viewed_by : String,
    ) : Call<ResponseBody>

    @FormUrlEncoded
    @POST("api/posts")
    fun uploadOtherARPost(
        @Field("temp_s3_name") id : String,
        @Field("title") title : String,
        @Field("content") content : String,
        @Field("drawing_post_id") postId : String,
        @Field("is_original") is_original : String,
        @Field("viewed_by") viewed_by : String,
    ) : Call<ResponseBody>

    /*@Multipart
    @POST("api/posts")
    fun uploadOtherARPost(
        @Part is_Original: MultipartBody.Part,
        @Part title: MultipartBody.Part,
        @Part content: MultipartBody.Part,
        @Part video: MultipartBody.Part,
        @Part drawingPostId: MultipartBody.Part,
        @Part viewedBy: MultipartBody.Part
    ): Call<ResponseBody>*/

    @DELETE("api/posts/{post_id}")
    fun deletePost(@Path("post_id") postId: String): Call<ResponseBody>

    @FormUrlEncoded
    @PUT("api/posts/{post_id}")
    fun editPost(
        @Path("post_id") postId: String,
        @Field("title") title: String,
        @Field("content") content: String,
        @Field("viewed_by") viewBy: String,
        @Field("place") place: String,
        @Field("place_changed") placeChanged: String
    ): Call<ResponseBody>


}