package com.draw.free.nft

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.draw.free.Global
import com.draw.free.R
import com.draw.free.databinding.ActivityEnterPasswordBinding
import com.draw.free.dialog.LoadingDialog
import com.draw.free.model.ParcelableNftMetadata
import com.draw.free.model.ParcelableNftSelling
import com.draw.free.nft.viewmodel.NftPreviewActivityViewModel.NftApiType
import com.draw.free.nft.viewmodel.PasswordAndProcessingViewModel
import com.draw.free.viewmodel.LoadingDialogModel
import timber.log.Timber

class EnterPasswordActivity : BaseLauncherActivity() {

    private lateinit var binding: ActivityEnterPasswordBinding
    lateinit var type: NftApiType
    var metadata: ParcelableNftMetadata? = null
    var sellData: ParcelableNftSelling? = null
    var auctionCache: String? = null
    private var loadingDialog: LoadingDialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent.hasExtra("type")) {
            type = (intent.getSerializableExtra("type") as NftApiType?)!!
            when (type) {
                NftApiType.Mint -> if (intent.hasExtra("metadata")) metadata =
                    intent.getParcelableExtra("metadata") else finish()
                NftApiType.Sell -> if (intent.hasExtra("sellData")) sellData =
                    intent.getParcelableExtra("sellData") else finish()
                NftApiType.Buy -> if (intent.hasExtra("auctionCache")) auctionCache =
                    intent.getStringExtra("auctionCache") else finish()
                NftApiType.EndSale -> if (intent.hasExtra("auctionCache")) auctionCache =
                    intent.getStringExtra("auctionCache") else finish()
                NftApiType.SettleBill -> if (intent.hasExtra("auctionCache")) auctionCache =
                    intent.getStringExtra("auctionCache") else finish()
            }
        } else {
            Timber.e("인텐트 제대로 안넘어옴")
            finish()
        }

        if (loadingDialog == null) {
            loadingDialog = LoadingDialog(this, LoadingDialogModel("비밀번호 확인 중입니다"))
            loadingDialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            loadingDialog!!.setCancelable(false)
        }


        // 바인딩
        // 왜 안 되지
        binding = DataBindingUtil.setContentView(this, R.layout.activity_enter_password)

        val passwordViewModel =
            ViewModelProvider(this).get(PasswordAndProcessingViewModel::class.java)
        when (type) {
            NftApiType.Mint -> passwordViewModel.metadata = metadata
            NftApiType.Sell -> passwordViewModel.sellData = sellData
            NftApiType.Buy -> passwordViewModel.auctionCache = auctionCache
            NftApiType.EndSale -> passwordViewModel.auctionCache = auctionCache
            NftApiType.SettleBill -> passwordViewModel.auctionCache = auctionCache
        }
        binding.passwordAndProcessingViewModel = passwordViewModel

        // 종료 버튼
        binding.btnCancel.setOnClickListener {
            onBackPressed()
            finish()
        }

        // 완료 버튼
        binding.btnConfirm.setOnClickListener {
            binding.btnCancel.isClickable = false
            binding.etPassword.isClickable = false
            binding.btnConfirm.isClickable = false

            // 로딩화면으로 이동, 이전 액티비티 모두 삭제
            loadingDialog?.show()
            if (passwordViewModel.checkPassword()) {
                val intent = Intent(this, ProcessingActivity::class.java)
                intent.putExtra("type", type)
                intent.putExtra("password", binding.etPassword.text.toString())
                when (type) {
                    NftApiType.Mint -> intent.putExtra("metadata", metadata)
                    NftApiType.Sell -> intent.putExtra("sellData", sellData)
                    NftApiType.Buy -> intent.putExtra("auctionCache", auctionCache)
                    NftApiType.EndSale -> intent.putExtra("auctionCache", auctionCache)
                    NftApiType.SettleBill -> intent.putExtra("auctionCache", auctionCache)
                }

                launcher.launch(intent)
            } else {
                Global.makeToast("비밀번호가 올바르지 않습니다")
            }

            binding.btnCancel.isClickable = true
            binding.etPassword.isClickable = true
            binding.btnConfirm.isClickable = true
            loadingDialog?.dismiss()
        }
    }

    override fun finish() {
        super.finish()
    }
}