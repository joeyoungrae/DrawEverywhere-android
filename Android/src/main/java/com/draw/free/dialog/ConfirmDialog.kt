package com.draw.free.dialog

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import com.draw.free.databinding.DialogConfirmBinding
import com.draw.free.viewmodel.ConfirmDialogModel


class ConfirmDialog(context: Context, val viewModel: ConfirmDialogModel) : AlertDialog(context) {

    @Override
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = DialogConfirmBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)
        binding.viewModel = viewModel;

        binding.dialogDetailText.text = viewModel.mDescription

        binding.btnPositive.setOnClickListener {
            viewModel.clickYes?.next()
            dismiss()
        }
    }

    override fun show() {
        setCancelable(false)
        super.show()
    }

}