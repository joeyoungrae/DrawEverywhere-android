package com.draw.free.dialog

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import com.draw.free.databinding.DialogAgreementBinding
import com.draw.free.viewmodel.YesOrNoDialogModel


class AgreementDialog(context: Context, val viewModel: YesOrNoDialogModel) : AlertDialog(context) {

    @Override
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = DialogAgreementBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)
        binding.viewModel = viewModel;

        binding.tvPrivacy.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            val uri: Uri = Uri.parse("https://sites.google.com/view/draweverywhere/%ED%99%88/privacypolicy")
            intent.data = uri
            context.startActivity(intent)

        }

        binding.tvTerm.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            val uri: Uri = Uri.parse("https://sites.google.com/view/draweverywhere/%ED%99%88/termsofservice")
            intent.data = uri
            context.startActivity(intent)
        }

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