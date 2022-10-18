package com.draw.free.setting.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.draw.free.Global
import com.draw.free.network.BaseResponse
import com.draw.free.network.RetrofitClient
import timber.log.Timber

class RestoreWalletByPhraseViewModel : ViewModel() {
    val secretPhrase = MutableLiveData<String>()
    val mIsLoading = MutableLiveData(false)

    fun commit(next: (secretKey : String) -> Unit) {
        if (secretPhrase.value.isNullOrEmpty()) {
            Global.makeToast("시드 문구를 입력해주세요.")
            return
        }

        Timber.e("secret Pharse : ${secretPhrase.value}")
        val words = secretPhrase.value?.split(" ")
        if (words?.size != 12) {
            Global.makeToast("열 두 자리의 시드 문구를 입력해주세요.")
            return
        }

        mIsLoading.postValue(true)
        RetrofitClient.getSolanaService().connectWallet(secretPhrase.value!!, "").enqueue(BaseResponse { response ->
            if (response.code() == 200) {
                val result = response.body()!!
                Timber.e("퍼블릭 키 : ${result.publicKey}")
                Timber.e("시크릿 키 : ${result.secretKey}")
                Timber.e("구문 키 : ${result.seedPhrase}")
                Global.prefs.seedPhrase = result.seedPhrase
                next(result.secretKey)
                mIsLoading.postValue(false)
                return@BaseResponse true
            } else {
                Timber.e("실패했습니다.")
            }
            mIsLoading.postValue(false)
            return@BaseResponse false
        })
    }
}