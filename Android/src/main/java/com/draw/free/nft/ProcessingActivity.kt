package com.draw.free.nft

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.draw.free.Global
import com.draw.free.R
import com.draw.free.ar.util.JniInterface2
import com.draw.free.databinding.ActivityNftProcessingBinding
import com.draw.free.dialog.ConfirmDialog
import com.draw.free.interfaceaction.ToNextWork
import com.draw.free.model.ParcelableNftMetadata
import com.draw.free.model.ParcelableNftSelling
import com.draw.free.nft.viewmodel.NftPreviewActivityViewModel.NftApiType
import com.draw.free.nft.viewmodel.PasswordAndProcessingViewModel
import com.draw.free.viewmodel.ConfirmDialogModel
import timber.log.Timber

class ProcessingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNftProcessingBinding
    lateinit var type: NftApiType
    lateinit var password: String
    var metadata: ParcelableNftMetadata? = null
    var sellData: ParcelableNftSelling? = null
    var auctionCache: String? = null



    override fun onBackPressed() {
        Timber.e("뒤로가기 X")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 스크린 꺼지지 않도록
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        if (intent.hasExtra("type") && intent.hasExtra("password")) {
            type = (intent.getSerializableExtra("type") as NftApiType?)!!
            password = intent.getStringExtra("password")!!
            when (type) {
                NftApiType.Mint -> if (intent.hasExtra("metadata")) metadata = intent.getParcelableExtra("metadata") else finish()
                NftApiType.Sell -> if (intent.hasExtra("sellData")) sellData = intent.getParcelableExtra("sellData") else finish()
                NftApiType.Buy -> if (intent.hasExtra("auctionCache")) auctionCache = intent.getStringExtra("auctionCache") else finish()
                NftApiType.EndSale -> if (intent.hasExtra("auctionCache")) auctionCache = intent.getStringExtra("auctionCache") else finish()
                NftApiType.SettleBill -> if (intent.hasExtra("auctionCache")) auctionCache = intent.getStringExtra("auctionCache") else finish()
            }
        } else {
            Timber.e("인텐트 제대로 안넘어옴")
            finish()
        }


        // 바인딩
        binding = DataBindingUtil.setContentView(this, R.layout.activity_nft_processing)
        val processingViewModel = ViewModelProvider(this).get(PasswordAndProcessingViewModel::class.java)
        val activity = this
        processingViewModel.password = password
        processingViewModel.serverPublicKey = JniInterface2.serverEccPublicKey()
        processingViewModel.failedProcessing = object : ToNextWork {
            override fun next() {
                val dialogModelForCloseActivity = ConfirmDialogModel(baseContext.getString(R.string.notice_nft_failed))
                dialogModelForCloseActivity.clickYes = object : ToNextWork {
                    override fun next() {
                        finish()
                    }
                }

                val dialogForClose = ConfirmDialog(activity, dialogModelForCloseActivity);
                dialogForClose.show()
                dialogForClose.setCancelable(false)
            }
        }

        when (type) {
            NftApiType.Mint -> {
                processingViewModel.metadata = metadata
                // 안내문
                binding.txtNoticeNft.text = getString(R.string.notice_minting)
                binding.txtNoticeTime.text = getString(R.string.notice_time_2)
                // 콜백
                processingViewModel.afterProcessing  = object : ToNextWork {
                    override fun next() {
                        Global.makeToast(getString(R.string.notice_minting_success))
                        updateAuctionDetailFragment()
                        finish()
                    }
                }
                // 서버에 요청
                processingViewModel.mintNft()
            }
            NftApiType.Sell -> {
                processingViewModel.sellData = sellData
                // 안내문
                binding.txtNoticeNft.text = getString(R.string.notice_selling)
                binding.txtNoticeTime.text = getString(R.string.notice_time_3)
                // 콜백
                processingViewModel.afterProcessing  = object : ToNextWork {
                    override fun next() {
                        Global.makeToast(getString(R.string.notice_selling_success))
                        updateAuctionDetailFragment()
                        finish()
                    }
                }
                // 서버에 요청
                processingViewModel.sellNft()
            }
            NftApiType.Buy -> {
                processingViewModel.auctionCache = auctionCache
                // 안내문
                binding.txtNoticeNft.text = getString(R.string.notice_buying)
                binding.txtNoticeTime.text = getString(R.string.notice_time_2)
                // 콜백
                processingViewModel.afterProcessing  = object : ToNextWork {
                    override fun next() {
                        Global.makeToast(getString(R.string.notice_buying_success))
                        updateAuctionDetailFragment()
                        finish()
                    }
                }
                // 서버에 요청
                processingViewModel.buyNft()
            }
            NftApiType.EndSale -> {
                processingViewModel.auctionCache = auctionCache
                // 안내문
                binding.txtNoticeNft.text = getString(R.string.notice_end_sale)
                binding.txtNoticeTime.text = getString(R.string.notice_time_2)
                // 콜백
                processingViewModel.afterProcessing  = object : ToNextWork {
                    override fun next() {
                        Global.makeToast(getString(R.string.notice_end_sale_success))
                        updateAuctionDetailFragment()
                        finish()
                    }
                }
                // 서버에 요청
                processingViewModel.endSale()
            }
            NftApiType.SettleBill -> {
                processingViewModel.auctionCache = auctionCache
                // 안내문
                binding.txtNoticeNft.text = getString(R.string.notice_settling)
                binding.txtNoticeTime.text = getString(R.string.notice_time_2)
                // 콜백
                processingViewModel.afterProcessing  = object : ToNextWork {
                    override fun next() {
                        Global.makeToast(getString(R.string.notice_settling_success))
                        updateAuctionDetailFragment()
                        finish()
                    }
                }
                // 서버에 요청
                processingViewModel.settleBill()
            }
        }
    }

    override fun finish() {
        val data = Intent()
        data.putExtra("finish", true)
        setResult(RESULT_OK, data)
        super.finish()
    }

    private fun updateAuctionDetailFragment() {
        val prefs = Global.prefs.getPrefs()
        prefs.edit().putBoolean("nftMain_update", true).apply()
        prefs.edit().putBoolean("nftDetail_update", true).apply()
        prefs.edit().putBoolean("myProfileFragment_update", true).apply()
        prefs.edit().putBoolean("nftListFragment_update", true).apply()
        prefs.edit().putBoolean("auctionListFragment_update", true).apply()
    }
}