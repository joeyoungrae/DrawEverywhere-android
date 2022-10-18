package com.draw.free.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import com.draw.free.databinding.DialogLoadingBinding
import com.draw.free.viewmodel.LoadingDialogModel

// 다이얼로그 창으로 띄울 것.
class LoadingDialog(context: Context, val loadingViewModel: LoadingDialogModel) : Dialog(context) {

    @Override
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        val binding = DialogLoadingBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)
        binding.viewModel = loadingViewModel;


    }

    @Override
    override fun show() {
        super.show()
    }


}