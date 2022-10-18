package com.draw.free.setting

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.draw.free.Global
import com.draw.free.R
import com.draw.free.databinding.ActivitySeeSecretKeyBinding
import com.draw.free.nft.util.Base58.Companion.decodeBase58
import com.draw.free.nft.util.LocalEncryption
import com.draw.free.nft.util.U8Array.Companion.toUIntArray
import com.draw.free.nft.viewmodel.PasswordAndProcessingViewModel
import timber.log.Timber

class SeeSecretKeyActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySeeSecretKeyBinding
    private lateinit var password: String

    @OptIn(ExperimentalUnsignedTypes::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent.hasExtra("password")) {
            password = intent.getStringExtra("password")!!
        } else {
            Timber.e("인텐트 파라미터 없음")
            finish()
        }

        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE);


        // 바인딩
        binding = DataBindingUtil.setContentView(this, R.layout.activity_see_secret_key)

        // 비공개키 값 넣기
        val cipher = Global.prefs.walletSecretKeyCipher
        val secretKey = LocalEncryption.decrypt(cipher!!, password)
        val uIntArray = secretKey.decodeBase58().toUIntArray()
        val stringArray = uIntArray.map { it.toString() }.toTypedArray()
        binding.tvSecretKey.text = stringArray.contentToString()

        // 종료 버튼
        binding.btnCancel.setOnClickListener {
            onBackPressed()
            finish()
        }
    }
}