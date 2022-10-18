package com.draw.free

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.draw.free.databinding.ActivityWalletSettingBinding
import com.draw.free.setting.CheckPasswordActivity

class WalletSettingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWalletSettingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 바인딩
        binding = DataBindingUtil.setContentView(this, R.layout.activity_wallet_setting)


        // onClickListener
        binding.btnBack.setOnClickListener {
            onBackPressed()
            finish()
        }
        binding.btnChangePassword.setOnClickListener {
            // 비밀번호 입력 화면
            val intent = Intent(this, CheckPasswordActivity::class.java)
            intent.putExtra("next", "changePassword")
            intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
            startActivity(intent)
        }
        binding.btnSeeSecretKey.setOnClickListener {
            // 비밀번호 입력 화면
            val intent = Intent(this, CheckPasswordActivity::class.java)
            intent.putExtra("next", "seeSecretKey")
            intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
            startActivity(intent)
        }
        binding.btnSeeSeedPhrase.setOnClickListener {
            // 비밀번호 입력 화면
            val intent = Intent(this, CheckPasswordActivity::class.java)
            intent.putExtra("next", "seeSeedPhrase")
            intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
            startActivity(intent)
        }
    }
}