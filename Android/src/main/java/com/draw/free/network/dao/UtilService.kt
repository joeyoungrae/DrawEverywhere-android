package com.draw.free.network.dao

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface UtilService {
    @POST("api/auth/token")
    fun getNewAccessToken(@Header("Refresh") refreshToken: String, @Header("Authorization") accessToken: String): Call<ResponseBody>

    @GET("shutdown/check")
    fun shutdownCheck() : Call<ResponseBody>
}