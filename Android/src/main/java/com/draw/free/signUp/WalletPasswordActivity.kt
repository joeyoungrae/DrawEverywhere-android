package com.draw.free.signUp

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.content.res.ResourcesCompat.getDrawable
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.draw.free.Global
import com.draw.free.MainActivity
import com.draw.free.R
import com.draw.free.databinding.ActivityWalletPasswordBinding
import com.draw.free.interfaceaction.ToNextWork
import com.draw.free.model.RegisterInfo
import com.draw.free.viewmodel.RegisterViewModel
import timber.log.Timber

class WalletPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWalletPasswordBinding
    private lateinit var mRegisterInfo: RegisterInfo

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent.hasExtra("registerInfo")) {
            mRegisterInfo = intent.getParcelableExtra("registerInfo")!!
        } else {
            Timber.e("전달받은 registerInfo가 없습니다.")
            finish()
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_wallet_password)

        val registerViewModel = ViewModelProvider(this).get(RegisterViewModel::class.java)

        binding.registerViewModel = registerViewModel

        // 비밀번호 확인 Observer
        registerViewModel.mWalletPw.observe(this) {
            registerViewModel.checkPassword()
        }
        registerViewModel.mWalletPwChk.observe(this) {
            registerViewModel.checkPassword()
        }
        
        // 다음 버튼 활성화 Observer
        registerViewModel.mEqual.observe(this) {
            if (it) {
                // 버튼 활성화
                binding.btnNext.isClickable = true
                binding.btnNext.background = resources.getDrawable(R.drawable.shape_round_gradation_2, null)
            } else {
                // 버튼 비활성화
                binding.btnNext.isClickable = false
                binding.btnNext.background = resources.getDrawable(R.drawable.shape_round_dark_gray, null)
            }
        }

        binding.btnBack.setOnClickListener {
            finish()
        }

        // 다음 버튼 onClickListener 연결
        binding.btnNext.setOnClickListener {
            // 클릭 불가
            binding.btnNext.isClickable = false

            // 클라에 비밀번호 저장
            registerViewModel.saveWalletPassword()

            // 계정 id 받는 화면으로 넘어감
            val intent = Intent(Global.getContext(), AccountIdActivity::class.java)
            intent.putExtra("registerInfo", mRegisterInfo)
            intent.putExtra("password", registerViewModel.mWalletPw.value!!)
            startActivity(intent)

            // 클릭 가능
            binding.btnNext.isClickable = true
        }
    }
}