package com.draw.free.setting

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.draw.free.R
import com.draw.free.databinding.ActivityWalletRestoreBinding
import com.draw.free.dialog.LoadingDialog
import com.draw.free.nft.BaseLauncherActivity
import com.draw.free.setting.viewmodel.RestoreWalletByPhraseViewModel
import com.draw.free.viewmodel.LoadingDialogModel

class RestoreWalletByPhraseActivity : BaseLauncherActivity() {

    private lateinit var prePassword: String
    private lateinit var binding: ActivityWalletRestoreBinding
    private lateinit var viewModel : RestoreWalletByPhraseViewModel

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_wallet_restore)
        viewModel = ViewModelProvider(this)[RestoreWalletByPhraseViewModel::class.java]
        binding.viewModel = viewModel

        binding.btnCancel.setOnClickListener {
            finish()
        }

        binding.btnConfirm.setOnClickListener {
            viewModel.commit { secretKey ->

                val intent = Intent(baseContext, SetPasswordActivity::class.java)
                intent.putExtra("password", secretKey)
                launcher.launch(intent)
            }
        }

        loadingForLogin()



    }

    private fun loadingForLogin() {
        val customProgressDialog = LoadingDialog(this, LoadingDialogModel(getString(R.string.waitLogin)));
        customProgressDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        customProgressDialog.setCancelable(false);

        viewModel.mIsLoading.observe(this) {
            if (it) {
                customProgressDialog.show();
            } else {
                customProgressDialog.dismiss();
            }
        }
    }
}