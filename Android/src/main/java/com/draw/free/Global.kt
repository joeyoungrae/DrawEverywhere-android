package com.draw.free

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.res.AssetManager
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.draw.free.ar.util.JniInterface2
import com.draw.free.model.UserProfile
import com.draw.free.network.RetrofitClient
import com.draw.free.util.CustomList
import com.draw.free.util.NotifyMessage
import com.draw.free.util.RequestFollow
import com.google.android.filament.utils.Utils
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.kakao.sdk.common.KakaoSdk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader


class Global : Application() {
    companion object {
        const val maxWidth = 0.99f
        const val minWidth = 0.11f

        val handler = Handler(Looper.getMainLooper())

        var userProfile: UserProfile? = null
            set(value) {
                Timber.d("유저 대입__")
                field = value
            }

        lateinit var INSTANCE: Global
            private set
        lateinit var prefs: Prefs
        lateinit var googleSignUp: GoogleSignUp
        private const val nearClip = 0.001f
        private const val farClip = 100.0f
        lateinit var notifyLists: CustomList<NotifyMessage>


        fun getContext(): Context {
            return INSTANCE.applicationContext
        }


        @JvmStatic
        fun makeToast(message: String) {
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(INSTANCE.applicationContext, message, Toast.LENGTH_SHORT).show()
            }
        }


        fun getRawTextFile(resId: Int): String {
            val inputStream = INSTANCE.applicationContext.resources.openRawResource(resId)
            try {
                val reader = BufferedReader(InputStreamReader(inputStream))
                val sb = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    sb.append(line).append("\n")
                }
                reader.close()
                return sb.toString()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            throw RuntimeException("Failed read {$resId} File")
        }

        @JvmStatic
        fun getAssetManager(): AssetManager {
            return getContext().assets
        }

        @JvmStatic
        fun getCacheDir(): String {
            return getContext().cacheDir.path.toString()
        }


        fun clear() {
            prefs.clear()
        }

    }

    init {
        INSTANCE = this
    }


    override fun onCreate() {
        super.onCreate()
        //prefs = Prefs(applicationContext)

        val masterKey = MasterKey.Builder(applicationContext, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()


        val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
            applicationContext,
            "secret_shared_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        prefs = Prefs(sharedPreferences)



        Timber.d("prefs 초기화함");

        Timber.plant(Timber.DebugTree())


        // 카카오 SDK 초기화
        KakaoSdk.init(this, JniInterface2.kakaoApi())
        // 구글 로그인 클라이언트 초기화
        googleSignUp = GoogleSignUp(applicationContext)

        notifyLists = CustomList(10, { size: Int, offset: String ->
            val response =
                RetrofitClient.getUserService().getNotAcceptedFollowsList(offset, size).execute()
            if (response.isSuccessful) {
                when (response.code()) {
                    200 -> {
                        val data = response.body()!!
                        val list = ArrayList<NotifyMessage>()
                        for (i in 0 until data.data.size) {
                            list.add(RequestFollow(data.data[i]))
                        }

                        return@CustomList list
                    }
                }
            }

            return@CustomList emptyList()
        }, { it.getKey() })

        // 필라멘트 초기화
        Utils.init()
    }
}

class Prefs(pref: SharedPreferences) {
    private val prefs = pref

    fun getPrefs(): SharedPreferences {
        return prefs
    }

    fun clear() {
        Timber.e("clear 작동")
        prefs.edit().clear().commit()
    }

    var seedPhrase: String?
        get() = prefs.getString("seedPhrase", null)
        set(value) {
            prefs.edit().putString("seedPhrase", value).apply()
        }

    var accessToken: String?
        get() = prefs.getString("accessToken", null)
        set(value) {
            prefs.edit().putString("accessToken", value).apply()
        }

    var refreshToken: String?
        get() = prefs.getString("refreshToken", null)
        set(value) {
            prefs.edit().putString("refreshToken", value).apply()
        }

    var walletPassword: String?
        get() = prefs.getString("walletPassword", null)
        set(value) {
            prefs.edit().putString("walletPassword", value).apply()
        }

    var walletPublicKey: String?
        get() = prefs.getString("walletPublicKey", null)
        set(value) {
            prefs.edit().putString("walletPublicKey", value).apply()
        }

    var walletSalt: String?
        get() = prefs.getString("walletSalt", null)
        set(value) {
            prefs.edit().putString("walletSalt", value).apply()
        }

    var walletSecretKeyCipher: String?
        get() = prefs.getString("walletSecretKeyCipher", null)
        set(value) {
            prefs.edit().putString("walletSecretKeyCipher", value).apply()
        }

    var getKeyword: String?
        get() = prefs.getString("recentlyKeyword", null)
        set(value) {
            prefs.edit().putString("recentlyKeyword", value).apply()
        }
}


class GoogleSignUp(context: Context) {
    private var gso: GoogleSignInOptions =
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(JniInterface2.googleClientID()).requestEmail()
            .build()

    var mGoogleSignInClient: GoogleSignInClient = GoogleSignIn.getClient(context, gso);
}