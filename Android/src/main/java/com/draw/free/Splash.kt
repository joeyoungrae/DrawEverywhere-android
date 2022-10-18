package com.draw.free

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.draw.free.dialog.*
import com.draw.free.interfaceaction.ToNextWork
import com.draw.free.model.UserProfile
import com.draw.free.network.BaseResponse
import com.draw.free.network.RetrofitClient
import com.draw.free.viewmodel.ConfirmDialogModel
import com.google.common.net.HttpHeaders.USER_AGENT
import retrofit2.Call
import timber.log.Timber
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import kotlin.system.exitProcess


class Splash : AppCompatActivity() {

    val handler = Handler(Looper.getMainLooper())

    fun blocking() {
        runOnUiThread {
            val dialogModelForCloseActivity = ConfirmDialogModel("현재 점검 중이라 어플을 이용할 수 없습니다.")
            dialogModelForCloseActivity.clickYes = object : ToNextWork {
                override fun next() {
                    finish()
                }
            }

            val dialogForClose = ConfirmDialog(this, dialogModelForCloseActivity);
            dialogForClose.show()
        }
    }

    fun showNotice(next : ToNextWork?) {
        runOnUiThread {
            val dialogModelForCloseActivity = ConfirmDialogModel("현재 그림그리기 저장을 할 수 없습니다.")
            dialogModelForCloseActivity.clickYes = next;

            val dialogForClose = ConfirmDialog(this, dialogModelForCloseActivity);
            dialogForClose.show()
        }
    }

    fun userCheck() {
        RetrofitClient.getUserService().getMyProfile()
            .enqueue(BaseResponse({ c: Call<UserProfile>, t: Throwable ->
                Timber.e("실패했습니다.")

                val dialogModelForCloseActivity = ConfirmDialogModel("인터넷 연결 상태를 확인해주세요.")
                dialogModelForCloseActivity.clickYes = object : ToNextWork {
                    override fun next() {
                        handler.postDelayed({ exitProcess(-1) }, 500)
                    }
                }

                val dialogForClose = ConfirmDialog(this, dialogModelForCloseActivity);
                dialogForClose.show()
                dialogForClose.setCancelable(false)

                return@BaseResponse true
            }) { response ->

                if (response.isSuccessful && response.code() == 200) {
                    Global.userProfile = response.body()
                    Global.makeToast("로그인 되었습니다.")
                }


                val next = object : ToNextWork {
                    override fun next() {
                        handler.post {
                            val intent = Intent(baseContext, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    }

                }

                showNotice(next);

                return@BaseResponse true
            })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        RetrofitClient.getUtilService().shutdownCheck().enqueue(BaseResponse() {
            if (it.isSuccessful && it.code() == 200) {
                userCheck()

                return@BaseResponse true
            } else {
                blocking()
                return@BaseResponse true
            }
        })


        /*Thread() {
            val obj = URL(getString(R.string.url_check))
            val con = obj.openConnection() as HttpURLConnection
            con.requestMethod = getString(R.string.method)
            con.setRequestProperty("User-Agent", USER_AGENT)
            val responseCode = con.responseCode
            if (responseCode == 200) {
                userCheck()
            } else {
                blocking()
            }
        }.start();*/
    }


}