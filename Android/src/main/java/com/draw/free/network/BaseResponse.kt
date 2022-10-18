package com.draw.free.network

import com.draw.free.Global

import retrofit2.Call
import retrofit2.Response
import timber.log.Timber



class BaseResponse<T>(
    private val failureResponse: ((Call<T>, t: Throwable) -> Boolean)? = null,
    private val successResponse: (Response<T>) -> Boolean
) : retrofit2.Callback<T> {

    override fun onResponse(call: Call<T>, response: Response<T>) {
        val code = response.code()
        Timber.d("도착한 코드 $code")

        if (successResponse(response)) {
            return
        }


    }

    override fun onFailure(call: Call<T>, t: Throwable) {

        if (failureResponse != null) {
            if (failureResponse!!(call, t)) {
                return
            }
        }

        Global.makeToast("인터넷 연결 상태를 확인하여 주세요.")
        t.printStackTrace()
    }
}