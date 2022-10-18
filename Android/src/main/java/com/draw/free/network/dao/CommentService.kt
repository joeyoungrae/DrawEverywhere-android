package com.draw.free.network.dao

import com.google.gson.annotations.SerializedName
import com.draw.free.model.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface CommentService {
    @Headers("Content-Type: application/json")
    @GET("api/comments/list/{offset}/{size}")
    suspend fun getComments(@Path("offset") offset: String, @Path("size") size: Int, @Query("post_id") postId : String): CommentListingResponse

    @Headers("Content-Type: application/json")
    @DELETE("api/comments/{comment_id}")
    fun deleteComment(@Path("comment_id") commentId : String): Call<ResponseBody>

    @Headers("Content-Type: application/json")
    @POST("api/comments/{comment_id}/like")
    fun recommendComment(@Path("comment_id") commentId : String): Call<String>

    @FormUrlEncoded
    @POST("api/comments")
    fun addComment(@Field("post_id") postId : String, @Field("comment") comment : String): Call<Comment>

}