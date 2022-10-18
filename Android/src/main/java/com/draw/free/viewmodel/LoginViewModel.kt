package com.draw.free.viewmodel

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.draw.free.Global
import com.draw.free.fragment.LoginFragment
import com.draw.free.interfaceaction.ICallback
import com.draw.free.model.JoinType
import com.draw.free.model.RegisterInfo
import com.draw.free.model.UserProfile
import com.draw.free.network.BaseResponse
import com.draw.free.network.RetrofitClient
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.kakao.sdk.auth.AuthApiClient
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.common.model.KakaoSdkError
import com.kakao.sdk.user.UserApiClient
import okhttp3.FormBody
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber

class LoginViewModel : ViewModel() {

    val mIsLoading = MutableLiveData<Boolean>();
    val registerInfo = MutableLiveData<RegisterInfo>();
    var afterLogin: LoginFragment.LoginFragmentAction? = null

    // 카카오계정으로 로그인 공통 callback 구성
    val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
        if (error != null) {
            error.printStackTrace()
            Timber.e("카카오계정으로 로그인 실패")
            mIsLoading.value = false;

        } else if (token != null) {
            Timber.e("카카오계정으로 로그인 성공 ${token.accessToken}")
            // 서버에 카카오 액세스 토큰 전송
            val body = FormBody.Builder().add("kakao_token", token.accessToken).build()
            RetrofitClient.getUserService().login(JoinType.KAKAO.joinType, body)
                .enqueue(BaseResponse { response ->
                    when (response.code()) {
                        200 -> {
                            Timber.e("요청 성공")
                            val message = response.body()!!.string()

                            Timber.d("로그인 성공")
                            Global.prefs.accessToken = JSONObject(message).getString("accessToken")
                            Global.prefs.refreshToken =
                                JSONObject(message).getString("refreshToken")

                            val profileData = JSONObject(message).getString("profile");
                            val data = Gson().fromJson(profileData, UserProfile::class.java)

                            Global.userProfile = data

                            afterLogin?.next() // 로그인 완료 후 처리할 일이 있을 경우

                            mIsLoading.value = false;
                            return@BaseResponse true
                        }
                        301 -> {
                            try {
                                val message = response.errorBody()!!.string()

                                val trySign = JSONObject(message)
                                registerInfo.value = RegisterInfo(
                                    JoinType.KAKAO.joinType,
                                    token.accessToken,
                                    trySign.getString("oauth_id"),
                                    null
                                )

                            } catch (e: JSONException) {
                                Timber.e("응답 json 변환 에러")
                            }

                            mIsLoading.value = false;
                            return@BaseResponse true
                        }
                    }


                    return@BaseResponse false
                })
        }
    }

    fun kakaoLogin(context: Context) {
        mIsLoading.value = true;

        if (AuthApiClient.instance.hasToken()) {
            UserApiClient.instance.accessTokenInfo { _, error ->
                if (error != null) {
                    if (error is KakaoSdkError && error.isInvalidTokenError()) {
                        //로그인 필요
                        kakaoOauth(context)
                    } else {
                        //기타 에러
                        mIsLoading.value = false;
                    }
                } else {
                    //토큰 유효성 체크 성공(필요 시 토큰 갱신됨)
                    kakaoOauth(context)
                }
            }
        } else {
            //로그인 필요
            Timber.e("4. 로그인 필요")
            kakaoOauth(context)
        }
    }

    private fun kakaoOauth(context: Context) {
        // 카카오톡이 설치되어 있으면 카카오톡으로 로그인, 아니면 카카오계정으로 로그인
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
            UserApiClient.instance.loginWithKakaoTalk(context) { token, error ->
                if (error != null) {
                    Timber.e("카카오톡으로 로그인 실패")

                    Timber.e(error.toString())

                    // 사용자가 카카오톡 설치 후 디바이스 권한 요청 화면에서 로그인을 취소한 경우,
                    // 의도적인 로그인 취소로 보고 카카오계정으로 로그인 시도 없이 로그인 취소로 처리 (예: 뒤로 가기)
                    if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                        mIsLoading.value = false;
                        return@loginWithKakaoTalk
                    }

                    // 카카오톡에 연결된 카카오계정이 없는 경우, 카카오계정으로 로그인 시도
                    UserApiClient.instance.loginWithKakaoAccount(context, callback = callback)
                } else if (token != null) {
                    Timber.e("카카오톡으로 로그인 성공 ${token.accessToken}")

                    // 서버에 카카오 액세스 토큰 전송
                    val body = FormBody.Builder().add("kakao_token", token.accessToken).build()

                    RetrofitClient.getUserService().login(JoinType.KAKAO.joinType, body).enqueue(BaseResponse { response ->

                            val message = response.body()!!.string()
                            when (response.code()) {
                                200 -> {
                                    Timber.d("로그인 성공")
                                    Global.prefs.accessToken =
                                        JSONObject(message!!).getString("accessToken")
                                    Global.prefs.refreshToken =
                                        JSONObject(message).getString("refreshToken")

                                    val profileData = JSONObject(message).getString("profile");

                                    val data = Gson().fromJson(profileData, UserProfile::class.java)

                                    Global.userProfile = data


                                    afterLogin?.next() // 로그인 완료 후 처리할 일이 있을 경우
                                    return@BaseResponse true
                                }

                                301 -> {
                                    Timber.d("회원가입 필요")
                                    try {
                                        val response = JSONObject(message!!)
                                        registerInfo.value = RegisterInfo(
                                            JoinType.KAKAO.joinType,
                                            token.accessToken,
                                            response.getString("oauth_id"),
                                            null
                                        )

                                    } catch (e: JSONException) {
                                        Timber.e("응답 json 변환 에러")
                                        return@BaseResponse false
                                    }

                                    return@BaseResponse true
                                }
                            }



                            return@BaseResponse false
                        })
                }
            }
        } else {
            UserApiClient.instance.loginWithKakaoAccount(context, callback = callback)
        }
    }

    fun googleLogin(afterGoogleLogin: ActivityResultLauncher<Intent>) {
        mIsLoading.value = true;

        val signInIntent: Intent = Global.googleSignUp.mGoogleSignInClient.signInIntent
        afterGoogleLogin.launch(signInIntent)
    }

    fun googleOauth(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            val idToken = account.idToken
            Timber.e("구글 계정으로 로그인 성공 ${idToken!!}")

            // 서버에 구글 idToken 전송
            val body = FormBody.Builder().add("google_token", idToken).build()

            RetrofitClient.getUserService().login(JoinType.GOOGLE.joinType, body)
                .enqueue(BaseResponse { response ->
                    mIsLoading.value = false;


                    when (response.code()) {
                        200 -> {
                            val message = response.body()!!.string()
                            Timber.d("로그인 성공")
                            Global.prefs.accessToken =
                                JSONObject(message!!).getString("accessToken")
                            Global.prefs.refreshToken =
                                JSONObject(message).getString("refreshToken")

                            val profileData = JSONObject(message).getString("profile");
                            Timber.d("profile Data : $profileData")

                            val data = Gson().fromJson(profileData, UserProfile::class.java)

                            Global.userProfile = data

                            afterLogin?.next()
                            return@BaseResponse true
                        }

                        301 -> {
                            Timber.d("회원가입 필요")
                            try {
                                val message = response.errorBody()!!.string()

                                val response = JSONObject(message!!)
                                registerInfo.value = RegisterInfo(
                                    JoinType.GOOGLE.joinType,
                                    idToken,
                                    response.getString("oauth_id"),
                                    null
                                )

                            } catch (e: JSONException) {
                                Timber.e("응답 json 변환 에러")
                            }
                            return@BaseResponse true
                        }
                    }
                    return@BaseResponse false
                })

        } catch (e: ApiException) {
            Timber.e("GoogleSignInResult:failed code=" + e.statusCode)
            mIsLoading.value = false;
        }
    }
}