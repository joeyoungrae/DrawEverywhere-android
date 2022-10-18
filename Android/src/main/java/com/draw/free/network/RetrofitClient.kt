package com.draw.free.network

import com.draw.free.ar.util.JniInterface2
import com.draw.free.network.dao.*
import com.google.gson.GsonBuilder
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


interface RetrofitClient {

    companion object {
        var BASE_URL = JniInterface2.baseUrl()
        private var INSTANCE: Retrofit? = null

        private val okHttpClient: OkHttpClient
        private val test: OkHttpClient

        init {
            val log = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            okHttpClient = OkHttpClient.Builder()
                .connectTimeout(1, TimeUnit.MINUTES)
                .readTimeout(3, TimeUnit.MINUTES)
                .addNetworkInterceptor(log)
                .addInterceptor(AuthInterceptor())
                .build()

            test = OkHttpClient.Builder()
                .build()
        }

        fun makeInstance() : Retrofit {
            val gson = GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
                .create()

            INSTANCE = Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson)).build();

           return INSTANCE!!
        }

        fun getInstance(): Retrofit {
            if (INSTANCE == null) {
                val gson = GsonBuilder()
                    .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
                    .create()

                INSTANCE = Retrofit.Builder()
                    .client(okHttpClient)
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson)).build();
            }

            return INSTANCE!!
        }


        // 토큰 요청의 경우 Intercepter가 없어야 함.
        fun getToken() : UtilService {
            val retrofit = Retrofit.Builder()
                .client(test)
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create()).build();

            return retrofit.create(UtilService::class.java)
        }

        fun getPostService(): PostService {
            INSTANCE = getInstance()
            return INSTANCE!!.create(PostService::class.java)
        }

        fun getUtilService(): UtilService {
            INSTANCE = getInstance()
            return INSTANCE!!.create(UtilService::class.java)
        }

        fun getUserService(): UserService {
            INSTANCE = getInstance()
            return INSTANCE!!.create(UserService::class.java)
        }

        fun getCommentService(): CommentService {
            INSTANCE = getInstance()
            return INSTANCE!!.create(CommentService::class.java)
        }

        fun getSolanaService(): SolanaService {
            INSTANCE = getInstance()
            return INSTANCE!!.create(SolanaService::class.java)
        }
    }



}

