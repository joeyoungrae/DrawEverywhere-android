package com.draw.free.dialog

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import com.draw.free.databinding.DialogYesOrNoBinding
import com.draw.free.viewmodel.YesOrNoDialogModel


class YesOrNoDialog(context: Context, val viewModel: YesOrNoDialogModel) : AlertDialog(context) {

    @Override
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = DialogYesOrNoBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)
        binding.viewModel = viewModel;

        binding.dialogDetailText.text = viewModel.mDescription

        binding.btnPositive.setOnClickListener {
            viewModel.clickYes?.next()
            dismiss()
        }

        binding.btnCancel.setOnClickListener {
            viewModel.clickNo?.next()
            dismiss()
        }


    }

}