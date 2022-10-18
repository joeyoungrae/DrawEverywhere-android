package com.draw.free.fragment

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.draw.free.Global
import com.draw.free.R
import com.draw.free.databinding.ActivityLoginBinding
import com.draw.free.dialog.LoadingDialog
import com.draw.free.signUp.WalletPasswordActivity
import com.draw.free.viewmodel.LoadingDialogModel
import com.draw.free.viewmodel.LoginViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount

import com.google.android.gms.tasks.Task
import timber.log.Timber

class LoginFragment() : BaseInnerFragment<ActivityLoginBinding>() {
    companion object {
        private var INSTANCE: LoginFragment? = null;

        fun getInstance(): LoginFragment {
            if (INSTANCE == null) {
                INSTANCE = LoginFragment();
            }

            return INSTANCE!!;
        }
    }

    private lateinit var afterGoogleLogin: ActivityResultLauncher<Intent>;


    interface LoginFragmentAction {
        fun next();
    }

    lateinit var afterLogin : LoginFragmentAction


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val loginViewModel = ViewModelProvider(this).get(LoginViewModel::class.java)

        binding.loginViewModel = loginViewModel
        loginViewModel.afterLogin = afterLogin

        val customProgressDialog =
            LoadingDialog(view.context, LoadingDialogModel(getString(R.string.waitLogin)));
        customProgressDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        customProgressDialog.setCancelable(false);


        binding.loginViewModel?.mIsLoading?.observe(viewLifecycleOwner) {
            if (it) {
                customProgressDialog.show();
            } else {
                customProgressDialog.dismiss();
            }
        }


        // 구글 로그인
        afterGoogleLogin = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == AppCompatActivity.RESULT_OK) {
                val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(it.data)
                loginViewModel.googleOauth(task)
            } else {
                loginViewModel.mIsLoading.value = false
                Timber.e("실패 ${it.resultCode}" )
            }
        }

        loadingForLogin();
        needRegister();

        // 카카오 로그인 버튼 onClickListener 연결
        val btnKakao = binding.btnKakao
        btnKakao.setOnClickListener { loginViewModel.kakaoLogin(Global.getContext()) }

        // 구글 로그인 버튼 onClickListener 연결
        val btnGoogle = binding.btnGoogle
        btnGoogle.setOnClickListener { loginViewModel.googleLogin(afterGoogleLogin) }


    }

    // Loading Dialog 처리
    private fun loadingForLogin() {
        val customProgressDialog =
            LoadingDialog(requireContext(), LoadingDialogModel(getString(R.string.waitLogin)));
        customProgressDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        customProgressDialog.setCancelable(false);

        binding.loginViewModel?.mIsLoading?.observe(viewLifecycleOwner) {
            if (it) {
                customProgressDialog.show();
            } else {
                customProgressDialog.dismiss();
            }
        }
    }


    override fun onStart() {
        super.onStart()
        if(!isHidden) {
            setPreviousButton.set(isSet = false, isLight = false)
            showBottomNav.show(isShowed = true, lightColor = false)
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            showBottomNav.show(isShowed = true, lightColor = false)
        } else {
            showBottomNav.show(isShowed = true, lightColor = true)
        }
    }

    // 회원가입 처리
    private fun needRegister() {
        binding.loginViewModel?.registerInfo?.observe(viewLifecycleOwner) {
            val intent = Intent(Global.getContext(), WalletPasswordActivity::class.java).apply {
                putExtra("registerInfo", it)
            }
            startActivity(intent)
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        INSTANCE = null
    }


}