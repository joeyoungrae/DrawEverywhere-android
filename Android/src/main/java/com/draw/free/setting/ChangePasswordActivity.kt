package com.draw.free.setting

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.draw.free.R
import com.draw.free.databinding.ActivityChangePasswordBinding
import com.draw.free.setting.viewmodel.ChangePasswordViewModel
import timber.log.Timber

class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChangePasswordBinding
    private lateinit var prePassword: String

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent.hasExtra("password")) {
            prePassword = intent.getStringExtra("password")!!
        } else {
            Timber.e("인텐트 파라미터 없음")
            finish()
        }

        // 바인딩
        binding = DataBindingUtil.setContentView(this, R.layout.activity_change_password)
        val viewModel = ViewModelProvider(this).get(ChangePasswordViewModel::class.java)
        viewModel.prePassword = prePassword
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
            viewModel.changeWalletPassword()

            // 화면 종료
            finish()
        }

        binding.btnCancel.setOnClickListener {
            onBackPressed()
            finish()
        }
    }
}