package com.draw.free.nft

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.draw.free.Global
import com.draw.free.R
import com.draw.free.databinding.ActivityNftPreviewBinding
import com.draw.free.model.ParcelableNftMetadata
import com.draw.free.model.ParcelableNftSelling
import com.draw.free.nft.viewmodel.NftPreviewActivityViewModel
import com.draw.free.nft.viewmodel.NftPreviewActivityViewModel.NftApiType
import com.draw.free.setting.RestoreWalletByPhraseActivity
import timber.log.Timber

class NftPreviewActivity : BaseLauncherActivity() {

    private lateinit var binding: ActivityNftPreviewBinding
    private lateinit var type: NftApiType
    private lateinit var metadata: ParcelableNftMetadata

    var sellData: ParcelableNftSelling? = null
    var auctionCache: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        if (Global.prefs.walletPassword.isNullOrEmpty()) {
            Global.makeToast("지갑을 활성화 해주세요.")
            startActivity(Intent(baseContext, RestoreWalletByPhraseActivity::class.java))
            finish()
        }

        if (intent.hasExtra("type") && intent.hasExtra("metadata")) {
            type = (intent.getSerializableExtra("type") as NftApiType?)!!
            metadata = intent.getParcelableExtra("metadata")!!
            when (type) {
                NftApiType.Sell -> if (intent.hasExtra("sellData")) sellData = intent.getParcelableExtra("sellData") else finish()
                NftApiType.Buy -> if (intent.hasExtra("auctionCache")) auctionCache = intent.getStringExtra("auctionCache") else finish()
                NftApiType.EndSale -> if (intent.hasExtra("auctionCache")) auctionCache = intent.getStringExtra("auctionCache") else finish()
                NftApiType.SettleBill -> if (intent.hasExtra("auctionCache")) auctionCache = intent.getStringExtra("auctionCache") else finish()
                else -> {}
            }
        } else {
            Timber.e("인텐트 제대로 안넘어옴")
            finish()
        }

        // 바인딩
        binding = DataBindingUtil.setContentView(this, R.layout.activity_nft_preview)
        val previewViewModel = ViewModelProvider(this).get(NftPreviewActivityViewModel::class.java)
        previewViewModel.type = type
        previewViewModel.walletAddress = Global.userProfile!!.walletAddress!!
        previewViewModel.metadata = metadata
        previewViewModel.auctionCache = auctionCache

        // 타이틀 설정
        when (type) {
            NftApiType.Mint -> binding.previewTitle.text = getString(R.string.mint__nft)
            NftApiType.Sell -> binding.previewTitle.text = getString(R.string.sell_nft)
            NftApiType.Buy -> binding.previewTitle.text = getString(R.string.buy_nft)
            NftApiType.EndSale -> binding.previewTitle.text = getString(R.string.end_sale)
            NftApiType.SettleBill -> binding.previewTitle.text = getString(R.string.settle_bill)
        }

        // 값 채우기
        Glide.with(this).load(metadata.thumbnail).into(binding.ivThumbnail)
        binding.txtTitle.text = metadata.name
        binding.txtSymbol.text = metadata.symbol
        binding.tvAddress.text = Global.userProfile!!.walletAddress!!

        // 예상비용 가져오기
        binding.txtCost.visibility = View.INVISIBLE
        binding.costLoading.visibility = View.VISIBLE
        previewViewModel.setExpectedCost()

        // 잔액 가져오기
        binding.txtBalance.visibility = View.INVISIBLE
        binding.balanceLoading.visibility = View.VISIBLE
        previewViewModel.setWalletBalance()

        // Observer
        previewViewModel.mExpectedCost.observe(this) {
            if (!it.isNullOrEmpty()) {
                binding.costLoading.visibility = View.INVISIBLE
                binding.txtCost.text = it
                binding.txtCost.visibility = View.VISIBLE
            }
        }
        previewViewModel.mBalance.observe(this) {
            if (!it.isNullOrEmpty()) {
                binding.balanceLoading.visibility = View.INVISIBLE
                binding.txtBalance.text = it
                binding.txtBalance.visibility = View.VISIBLE
            }
        }

        // 뒤로가기 버튼
        binding.btnBack.setOnClickListener {
            onBackPressed()
            finish()
        }

        // 완료 버튼
        binding.btnFinish.setOnClickListener {
            // 예상비용과 잔액 데이터 있는지 확인
            if (binding.txtCost.text.isNullOrEmpty() || binding.txtBalance.text.isNullOrEmpty()) {
                Global.makeToast(getString(R.string.wait_until_getting_cost_and_balance_notice))
                return@setOnClickListener
            }
            // 클릭 막기
            binding.btnBack.isClickable = false
            binding.btnFinish.isClickable = false


            if (previewViewModel.checkEnoughBalance()) {
                // 비밀번호 입력 화면으로 이동
                val intent = Intent(this, EnterPasswordActivity::class.java)
                intent.putExtra("type", type)
                when (type) {
                    NftApiType.Mint -> intent.putExtra("metadata", metadata)
                    NftApiType.Sell -> intent.putExtra("sellData", sellData)
                    NftApiType.Buy -> intent.putExtra("auctionCache", auctionCache)
                    NftApiType.EndSale -> intent.putExtra("auctionCache", auctionCache)
                    NftApiType.SettleBill -> intent.putExtra("auctionCache", auctionCache)
                }


                launcher.launch(intent)

            } else {
                Global.makeToast(getString(R.string.not_enough_balance_notice))
            }
            // 클릭 가능
            binding.btnBack.isClickable = true
            binding.btnFinish.isClickable = true
        }
    }
}