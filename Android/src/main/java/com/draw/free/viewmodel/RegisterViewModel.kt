package com.draw.free.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.draw.free.Global
import com.draw.free.interfaceaction.ToNextWork
import com.draw.free.model.RegisterInfo
import com.draw.free.model.UserProfile
import com.draw.free.network.BaseResponse
import com.draw.free.network.RetrofitClient
import com.draw.free.util.Argon2kt.Companion.getEncodedString
import com.draw.free.util.Encryption.Companion.encrypt
import okhttp3.FormBody
import org.json.JSONObject
import timber.log.Timber

class RegisterViewModel : ViewModel() {

    val mAccountId = MutableLiveData<String>()
    val mWalletPw = MutableLiveData<String>()
    val mWalletPwChk = MutableLiveData<String>()
    val mEqual = MutableLiveData(false)
    var registerInfo: RegisterInfo? = null
    var password: String? = null
    lateinit var afterLogin: (String) -> Unit

    // 비밀번호 일치 확인
    fun checkPassword() {
        if (!mWalletPw.value.isNullOrEmpty() && !mWalletPwChk.value.isNullOrEmpty()) {
            mEqual.value = mWalletPw.value == mWalletPwChk.value
        } else {
            mEqual.value = false
        }
    }

    // 지갑 비밀번호 저장
    fun saveWalletPassword() {
        if (!mWalletPw.value.isNullOrEmpty() && mEqual.value == true) {
            // 클라에 암호화해서 저장
            val encodedString: String = getEncodedString(mWalletPw.value!!)

            Global.prefs.walletPassword = encodedString

        } else {
            Global.makeToast("비밀번호를 입력해주세요")
        }
    }

    // accountId 중복 확인
    fun checkAccountId() {
        if (mAccountId.value != null) {

            RetrofitClient.getUserService().checkAccountID(mAccountId.value.toString())
                .enqueue(BaseResponse { response ->
                    when (response.code()) {
                        200 -> {
                            Timber.d("해당 계정 아이디 사용 가능")
                            registerInfo?.accountId = mAccountId.value.toString()
                            register()
                            return@BaseResponse true
                        }
                        400 -> {
                            Timber.d("계정 아이디 중복으로 사용 불가 or 계정 아이디 길이 30자 초과")
                            Global.makeToast("사용할 수 없는 계정 아이디입니다")
                            return@BaseResponse true
                        }
                    }

                    return@BaseResponse false
                })
        }
    }

    // 회원가입 (및 로그인)
    private fun register() {
        val body = FormBody.Builder()
            .add("oauth_access_token", registerInfo?.oauthAccessToken!!)
            .add("oauth_id", registerInfo?.oauthId!!)
            .add("account_id", registerInfo?.accountId!!).build()

        RetrofitClient.getUserService().register(registerInfo?.joinType!!, body)
            .enqueue(BaseResponse { response ->
                val message = response.body()!!.string()

                when (response.code()) {
                    200 -> {
                        Timber.d("회원가입 성공")
                        Global.prefs.accessToken =
                            JSONObject(message!!).getString("accessToken")
                        Global.prefs.refreshToken =
                            JSONObject(message).getString("refreshToken")
                        Timber.d("accessToken: " + Global.prefs.accessToken)
                        Timber.d("refreshToken: " + Global.prefs.refreshToken)

                        val u = JSONObject(message).getJSONObject("profile")

                        Global.userProfile = UserProfile(
                            uniqueId = u.getString("id"),
                            accountId = u.getString("account_id"),
                            pfPicture = u.getString("pf_picture"),
                            pfName = u.getString("pf_name"),
                            pfDescription = u.getString("pf_description"),
                            accountType = u.getString("account_type"),
                            walletAddress = u.getString("wallet_address")
                        )

                        // 지갑 주소 저장, 지갑 secretKey 를 password 로 암호화해서 저장
                        val wallet = JSONObject(message).getJSONObject("keypair")
                        Global.prefs.walletPublicKey = wallet.getString("public_key")
                        val secretKey = wallet.getString("secret_key")
                        Global.prefs.walletSecretKeyCipher = encrypt(secretKey, password!!)


                        val seedPhrase = wallet.getString("seed_phrase")


                        Timber.d("지갑 정보 저장 성공")
                        Timber.d("walletPassword: " + Global.prefs.walletPassword)
                        Timber.d("walletPublicKey: " + Global.prefs.walletPublicKey)
                        Timber.d("walletSalt: " + Global.prefs.walletSalt)
                        Timber.d("walletSecretKeyCipher: " + Global.prefs.walletSecretKeyCipher)

                        afterLogin(seedPhrase)
                        return@BaseResponse true
                    }
                    400 -> {
                        Timber.e("회원가입 실패")
                        Global.makeToast("회원가입에 실패하였습니다")
                    }

                }

                return@BaseResponse false
            })
    }
}

