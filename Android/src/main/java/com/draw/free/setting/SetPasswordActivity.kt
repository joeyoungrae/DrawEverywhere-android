package com.draw.free.setting

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.draw.free.R
import com.draw.free.databinding.ActivityChangePasswordBinding
import com.draw.free.databinding.ActivitySetPasswordBinding
import com.draw.free.setting.viewmodel.ChangePasswordViewModel
import timber.log.Timber

class SetPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySetPasswordBinding
    private lateinit var secretKey: String

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent.hasExtra("password")) {
            secretKey = intent.getStringExtra("password")!!
        } else {
            Timber.e("인텐트 파라미터 없음")
            finish()
        }

        // 바인딩
        binding = DataBindingUtil.setContentView(this, R.layout.activity_set_password)
        val viewModel = ViewModelProvider(this)[ChangePasswordViewModel::class.java]
        binding.changePasswordViewModel = viewModel

        // 비밀번호 확인 Observer
        viewModel.mWalletPw.observe(this) {
            viewModel.checkPassword()
        }
        viewModel.mWalletPwChk.observe(this) {
            viewModel.checkPassword()
        }

        // 다음 버튼 활성화 Observer
        viewModel.mEqual.observe(this) {
            if (it) {
                // 버튼 활성화
                binding.btnFinish.isClickable = true
                binding.btnFinish.background = resources.getDrawable(R.drawable.shape_round_gradation_2, null)
            } else {
                // 버튼 비활성화
                binding.btnFinish.isClickable = false
                binding.btnFinish.background = resources.getDrawable(R.drawable.shape_round_dark_gray, null)
            }
        }

        // 다음 버튼 onClickListener 연결
        binding.btnFinish.setOnClickListener {
            // 클릭 불가
            binding.btnFinish.isClickable = false

            // 지갑 비밀번호 저장 && 시크릿키 암호화키 변경
            viewModel.setWalletPasswordWithSecretKey(secretKey)

            // 화면 종료
            finish()
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }
    }

    override fun finish() {
        val data = Intent()
        data.putExtra("finish", true)
        setResult(RESULT_OK, data)
        super.finish()
    }
}