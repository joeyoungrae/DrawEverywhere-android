package com.draw.free.network

import com.draw.free.Global
import com.draw.free.interfaceaction.ToNextWork
import com.draw.free.viewmodel.ConfirmDialogModel
import kotlinx.coroutines.delay
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import timber.log.Timber
import kotlin.system.exitProcess

class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var newRequest: Request = chain.request()

        if (Global.prefs.accessToken != null) {
            newRequest = newRequest.newBuilder()
                .addHeader("accept", "Content-Type: application/json")
                .addHeader("Authorization", Global.prefs.accessToken ?: "")
                .build()
        } else {
            Timber.d("비로그인 회원")
        }

        val response = chain.proceed(newRequest)

        // 리디렉션시 재 요청 처리
        if (response.code == 307) {
            newRequest = newRequest.newBuilder()
                .addHeader("accept", "Content-Type: application/json")
                .addHeader("Authorization", Global.prefs.accessToken ?: "")
                .build()

            chain.proceed(newRequest)
        }

        if (response.code == 301) {
            try {
                val c = JSONObject(response.peekBody(2048).string())
                val code: String = c.getString("code")

                Timber.e("__a")

                when (code) {
                    "3010001" -> {
                        val retryResponse = RetrofitClient.getToken().getNewAccessToken(
                            Global.prefs.refreshToken ?: "",
                            Global.prefs.accessToken ?: ""
                        ).execute()

                        val code2 = retryResponse.code()

                        when (code2) {
                            200 -> {
                                var message = retryResponse.body()?.string()?.trim()!!
                                Global.prefs.accessToken =
                                    JSONObject(message).getString("accessToken")
                                if (JSONObject(message).has("refreshToken")) {
                                    Global.prefs.refreshToken =
                                        JSONObject(message).getString("refreshToken")
                                }

                                newRequest = newRequest.newBuilder().removeHeader("Authorization")
                                    .addHeader("Authorization", Global.prefs.accessToken ?: "")
                                    .build()

                                return chain.proceed(newRequest)
                            }
                            400 -> {
                                Global.prefs.accessToken = ""
                                Global.prefs.refreshToken = ""

                                //Global.createDialog()
                            }
                            else -> {
                                Timber.e("What")
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                return response
            }
        }

        Timber.d("전달")
        return response
    }
}