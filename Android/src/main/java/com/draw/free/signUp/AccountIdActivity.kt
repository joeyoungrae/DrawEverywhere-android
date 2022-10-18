package com.draw.free.signUp

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.draw.free.MainActivity
import com.draw.free.R
import com.draw.free.databinding.ActivityAccountIdBinding
import com.draw.free.dialog.AgreementDialog
import com.draw.free.dialog.YesOrNoDialog
import com.draw.free.interfaceaction.ToNextWork
import com.draw.free.model.RegisterInfo
import com.draw.free.setting.SeeSeedPhraseActivity
import com.draw.free.viewmodel.RegisterViewModel
import com.draw.free.viewmodel.YesOrNoDialogModel
import timber.log.Timber

class AccountIdActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAccountIdBinding
    private lateinit var registerInfo: RegisterInfo
    private lateinit var password: String
    private lateinit var launcher: ActivityResultLauncher<Intent>
    private lateinit var afterLogin: ToNextWork

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent.hasExtra("registerInfo")) {
            registerInfo = intent.getParcelableExtra("registerInfo")!!
            password = intent.getStringExtra("password")!!
        } else {
            Timber.e("인텐트 제대로 안넘어옴")
            finish()
        }

        // 바인딩
        binding = DataBindingUtil.setContentView(this, R.layout.activity_account_id)
        val accountIdViewModel = ViewModelProvider(this).get(RegisterViewModel::class.java)
        binding.registerViewModel = accountIdViewModel

        // 회원가입에 필요한 파라미터 넘기기
        accountIdViewModel.registerInfo = registerInfo
        accountIdViewModel.password = password

        afterLogin = object : ToNextWork {
            override fun next() {
                // 홈화면으로 이동
                val intent = Intent(applicationContext, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
            }
        }

        val context: Context = this;

        accountIdViewModel.afterLogin = { it ->
            val intent = Intent(context, SeeSeedPhraseActivity::class.java)
            intent.putExtra("seedPhrase", it)
            launcher.launch(intent)
        }
        // 완료 버튼 활성화 Observer
        accountIdViewModel.mAccountId.observe(this) {
            if (!it.isNullOrEmpty()) { // TODO: 최소 글자수와 최대 글자수 만족했을 때만 클릭가능하도록 수정
                // 버튼 활성화
                binding.btnFinish.isClickable = true
                binding.btnFinish.background =
                resources.getDrawable(R.drawable.shape_round_gradation_2, null)
            } else {
                // 버튼 비활성화
                binding.btnFinish.isClickable = false
                binding.btnFinish.background =
                resources.getDrawable(R.drawable.shape_round_dark_gray, null)
            }
        }

        // 완료 버튼 onClickListener 연결
        binding.btnFinish.setOnClickListener {
            // ConfirmDialog 처리
            val dialogModelForCloseActivity = YesOrNoDialogModel("")
            dialogModelForCloseActivity.clickYes = object : ToNextWork {
                override fun next() {
                    // 클릭 불가
                    binding.btnFinish.isClickable = false

                    accountIdViewModel.checkAccountId()

                    // 클릭 가능
                    binding.btnFinish.isClickable = true
                }
            }

            val dialogForClose = AgreementDialog(this, dialogModelForCloseActivity);
            dialogForClose.show()
        }

        binding.btnBack.setOnClickListener {
            finish()
        }

    }

    override fun onStart() {
        super.onStart()
        launcher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                afterLogin.next()
            }
    }
}