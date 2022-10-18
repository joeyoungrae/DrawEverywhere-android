package com.draw.free.viewmodel

import androidx.lifecycle.ViewModel
import com.draw.free.Global
import com.draw.free.network.BaseResponse
import com.draw.free.network.RetrofitClient
import com.draw.free.util.CustomList
import com.draw.free.util.NotifyMessage
import retrofit2.Retrofit
import timber.log.Timber

class NotifyFragmentViewModel : ViewModel() {
    fun acceptFollow(itemKey : String, list : CustomList<NotifyMessage>) {
        val getUserAccount = itemKey.split(":")[1];
        RetrofitClient.getUserService().acceptanceAcceptFollow(getUserAccount).enqueue(BaseResponse { response ->
            if (response.isSuccessful && response.code() == 200) {
                Global.makeToast("수락하셨습니다.")
                list.deleteByKey(itemKey)
                return@BaseResponse true
            }

            return@BaseResponse false
        })
    }

    fun refuseFollow(itemKey : String, list : CustomList<NotifyMessage>) {
        val getUserAccount = itemKey.split(":")[1];
        RetrofitClient.getUserService().rejectionRejectFollows(getUserAccount).enqueue(BaseResponse { response ->
            if (response.code() == 200) {
                Global.makeToast("거절하셨습니다.")
                list.deleteByKey(itemKey)
                return@BaseResponse true
            }

            return@BaseResponse false
        })

    }
}