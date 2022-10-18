package com.draw.free.network.dao

import com.draw.free.dto.NormalSuccessResponse
import com.draw.free.model.NormalResponse
import com.draw.free.model.UserProfile
import com.draw.free.model.UserProfileListingResponse
import com.draw.free.network.dto.CommonResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*


interface UserService {

    @Headers("Content-Type: application/json")
    @POST("api/users/{user_id}/report")
    fun reportUser(@Path("user_id") userId: String): Call<NormalResponse>


    @Headers("Content-Type: application/json")
    @DELETE("api/users")
    fun deleteUser() : Call<String>

    @Headers("Content-Type: application/json")
    @GET("api/users/relation/{account_id}")
    fun checkRelationShip(@Path("account_id") accountID: String): Call<String>

    @Headers("Content-Type: application/json")
    @POST("api/users/followings/{following}")
    fun followUser(@Path("following") accountId: String): Call<String>


    @Headers("Content-Type: application/json")
    @GET("api/users/{account_id}/followers/list/{offset}/{size}")
    suspend fun getFollowsList(@Path("account_id") accountID: String, @Path("offset") offset: String, @Path("size") size: Int) : UserProfileListingResponse

    @Headers("Content-Type: application/json")
    @GET("api/users/{account_id}/followings/list/{offset}/{size}")
    suspend fun getFollowings(@Path("account_id") accountID: String, @Path("offset") offset: String, @Path("size") size: Int) : UserProfileListingResponse

    @Headers("Content-Type: application/json")
    @GET("api/users/profile")
    fun getUserProfile(@Query("account_id") accountID: String?): Call<UserProfile>


    @Headers("Content-Type: application/json")
    @PUT("api/users/account_type")
    fun changeAccountType(@Query("account_type") accountType : String) : Call<NormalSuccessResponse>


    @Headers("Content-Type: application/json")
    @GET("api/users/profile")
    fun getMyProfile(): Call<UserProfile>


    @Multipart
    @PUT("api/users/profile")
    fun setProfile(@Part accountID_changed: MultipartBody.Part,
                   @Part name_changed: MultipartBody.Part,
                   @Part desc_changed: MultipartBody.Part,
                   @Part pf_pic_changed: MultipartBody.Part,
                   @Part accountID: MultipartBody.Part,
                   @Part pf_name: MultipartBody.Part,
                   @Part pf_description: MultipartBody.Part,
                   @Part pf_picture: MultipartBody.Part): Call<UserProfile>

    @POST("api/auth/logout")
    fun logout(): Call<ResponseBody>

    @GET("api/auth/account_id/{account_id}/existence-check/")
    fun checkAccountID(@Path("account_id") accountID: String): Call<ResponseBody>

    @Headers("Content-Type: application/x-www-form-urlencoded")
    @POST("api/auth/login/{join_type}")
    fun login(@Path("join_type") joinType: String, @Body body: RequestBody): Call<ResponseBody>

    @Headers("Content-Type: application/x-www-form-urlencoded")
    @POST("api/auth/register/{join_type}")
    fun register(@Path("join_type") joinType: String, @Body body: RequestBody): Call<ResponseBody>

    @Headers("Content-Type: application/json")
    @POST("api/users/followers/list/{offset}/{size}/not-accepted")
    fun getNotAcceptedFollowsList(@Path("offset") offset : String,@Path("size") size : Int) : Call<UserProfileListingResponse>// UserProfileListingResponse

    @Headers("Content-Type: application/json")
    @PUT("api/users/followers/{follower}/acceptance")
    fun acceptanceAcceptFollow(@Path("follower") accountId: String): Call<CommonResponse>

    @Headers("Content-Type: application/json")
    @DELETE("/api/users/followers/{follower}/rejection")
    fun rejectionRejectFollows(@Path("follower") accountId: String): Call<CommonResponse>

}