package com.draw.free.setting.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.draw.free.Global
import com.draw.free.nft.util.Argon2kt
import com.draw.free.nft.util.LocalEncryption

class ChangePasswordViewModel : ViewModel() {

    lateinit var prePassword: String
    val mWalletPw = MutableLiveData<String>()
    val mWalletPwChk = MutableLiveData<String>()
    val mEqual = MutableLiveData(false)

    // 비밀번호 일치 확인
    fun checkPassword() {
        if (!mWalletPw.value.isNullOrEmpty() && !mWalletPwChk.value.isNullOrEmpty()) {
            mEqual.value = mWalletPw.value == mWalletPwChk.value
        } else {
            mEqual.value = false
        }
    }

    // 지갑 비밀번호 저장 && 시크릿키 암호화키 변경
    fun changeWalletPassword() {
        if (!mWalletPw.value.isNullOrEmpty() && mEqual.value == true) {
            // 시크릿키 암호화키 변경
            val cipher = Global.prefs.walletSecretKeyCipher
            val secretKey = LocalEncryption.decrypt(cipher!!, prePassword)
            Global.prefs.walletSecretKeyCipher = LocalEncryption.encrypt(secretKey, mWalletPw.value!!)

            // 클라에 암호화해서 저장
            val encodedString: String = Argon2kt.getEncodedString(mWalletPw.value!!)
            Global.prefs.walletPassword = encodedString

            Global.makeToast("지갑 비밀번호가 변경되었습니다")
        } else {
            Global.makeToast("지갑 비밀번호를 변경할 수 없습니다")
        }
    }


    // 시크릿키 암호화키를 사용하여 지갑 비밀번호 설정
    fun setWalletPasswordWithSecretKey(secretKeyString : String) {
        if (!mWalletPw.value.isNullOrEmpty() && mEqual.value == true) {

            // 클라에 암호화해서 저장
            val walletPassword = Argon2kt.getEncodedString(mWalletPw.value!!)
            val walletSecretKeyCipher = LocalEncryption.encrypt(secretKeyString, mWalletPw.value!!)
            Global.prefs.walletPassword = walletPassword
            Global.prefs.walletSecretKeyCipher = walletSecretKeyCipher

            Global.makeToast("지갑 비밀번호가 변경되었습니다")
        } else {
            Global.makeToast("지갑 비밀번호를 변경할 수 없습니다")
        }
    }
}