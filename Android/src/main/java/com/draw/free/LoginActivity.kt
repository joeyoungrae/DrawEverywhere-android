package com.draw.free

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.draw.free.databinding.ActivityLoginBinding
import com.draw.free.dialog.LoadingDialog
import com.draw.free.fragment.LoginFragment
import com.draw.free.interfaceaction.ToNextWork
import com.draw.free.signUp.AccountIdActivity
import com.draw.free.viewmodel.LoadingDialogModel
import com.draw.free.viewmodel.LoginViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.SignInButton
import com.google.android.gms.tasks.Task
import timber.log.Timber

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding;
    private lateinit var afterGoogleLogin: ActivityResultLauncher<Intent>;
    private lateinit var bcontext: Activity;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);

        bcontext = this;

        val loginViewModel = ViewModelProvider(this).get(LoginViewModel::class.java)

        binding.loginViewModel = loginViewModel

        loginViewModel.afterLogin = object : LoginFragment.LoginFragmentAction {
            override fun next() {
                // 홈화면으로 이동
                val intent = Intent(applicationContext, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
            }

        }


        // 구글 로그인
        afterGoogleLogin = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(it.data)
                loginViewModel.googleOauth(task)
            } else {
                Timber.e("Failed ${it.resultCode}")
                binding.loginViewModel?.mIsLoading?.value = false
            }
        }

        loadingForLogin();
        needRegister();

        // 카카오 로그인 버튼 onClickListener 연결
        val btnKakao = findViewById<ImageView>(R.id.btn_kakao)
        btnKakao.setOnClickListener { loginViewModel.kakaoLogin(bcontext) }

        // 구글 로그인 버튼 onClickListener 연결
        val btnGoogle = findViewById<SignInButton>(R.id.btn_google)
        btnGoogle.setOnClickListener { loginViewModel.googleLogin(afterGoogleLogin) }

        // TODO: 네이버 로그인 버튼 onClickListener 연결


    }

    // Loading Dialog 처리
    private fun loadingForLogin() {
        val customProgressDialog = LoadingDialog(this, LoadingDialogModel(getString(R.string.waitLogin)));
        customProgressDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        customProgressDialog.setCancelable(false);

        binding.loginViewModel?.mIsLoading?.observe(this) {
            if (it) {
                customProgressDialog.show();
            } else {
                customProgressDialog.dismiss();
            }
        }
    }

    // 회원가입 처리
    private fun needRegister() {
        binding.loginViewModel?.registerInfo?.observe(this) {
            val intent = Intent(applicationContext, AccountIdActivity::class.java).apply {
                putExtra("registerInfo", it)
            }
            startActivity(intent)
        }
    }
}