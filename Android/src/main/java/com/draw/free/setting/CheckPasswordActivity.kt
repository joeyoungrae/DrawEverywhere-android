package com.draw.free.setting

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.draw.free.Global
import com.draw.free.R
import com.draw.free.databinding.ActivityCheckPasswordBinding
import com.draw.free.nft.viewmodel.PasswordAndProcessingViewModel
import timber.log.Timber

class CheckPasswordActivity : AppCompatActivity() {

    private lateinit var next: String
    private lateinit var binding: ActivityCheckPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent.hasExtra("next")) {
            next = intent.getStringExtra("next")!!
        } else {
            Timber.e("인텐트 파라미터 없음")
            finish()
        }

        // 바인딩
        binding = DataBindingUtil.setContentView(this, R.layout.activity_check_password)
        val passwordViewModel =
            ViewModelProvider(this).get(PasswordAndProcessingViewModel::class.java)
        binding.passwordAndProcessingViewModel = passwordViewModel


        // 완료 버튼
        binding.btnConfirm.setOnClickListener {
            binding.btnCancel.isClickable = false
            binding.etPassword.isClickable = false
            binding.btnConfirm.isClickable = false

            // 로딩화면으로 이동, 이전 액티비티 모두 삭제
            if (passwordViewModel.checkPassword()) {
                lateinit var intent: Intent
                when (next) {
                    "changePassword" -> {
                        intent = Intent(this, ChangePasswordActivity::class.java)
                    }
                    "seeSecretKey" -> {
                        intent = Intent(this, SeeSecretKeyActivity::class.java)
                    }
                    "seeSeedPhrase" -> {
                        val instance = Intent(this, SeeSeedPhraseActivity::class.java)
                        instance.putExtra("seedPhrase", Global.prefs.seedPhrase)
                        intent = instance
                    }
                }
                intent.putExtra("password", passwordViewModel.mPassword.value)
                intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
                startActivity(intent)
                finish()
            } else {
                Global.makeToast("비밀번호가 올바르지 않습니다")
            }

            binding.btnCancel.isClickable = true
            binding.etPassword.isClickable = true
            binding.btnConfirm.isClickable = true
        }

        // 종료 버튼
        binding.btnCancel.setOnClickListener {
            onBackPressed()
            finish()
        }
    }
}