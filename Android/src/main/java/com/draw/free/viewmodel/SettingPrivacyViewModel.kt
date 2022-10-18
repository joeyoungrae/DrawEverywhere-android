package com.draw.free.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.draw.free.Global
import com.draw.free.network.BaseResponse
import com.draw.free.network.RetrofitClient

class SettingPrivacyViewModel : ViewModel() {
    val isPrivateAccount = MutableLiveData(Global.userProfile!!.accountType == "private")


    fun toggleAccountType() {
        val type = if (isPrivateAccount.value!!) "public" else "private"

        RetrofitClient.getUserService().changeAccountType(type).enqueue(
            BaseResponse { response ->
                if (response.code() == 200) {
                    isPrivateAccount.postValue(type == "private")
                    Global.userProfile!!.accountType = type
                    return@BaseResponse true
                }

                return@BaseResponse false
            })


        //isPrivateAccount.postValue(!isPrivateAccount.value!!)
        //Global.makeToast("타입이 변경됨 ${isPrivateAccount.value}")
    }

}