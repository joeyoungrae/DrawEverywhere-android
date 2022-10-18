package com.draw.free.nft

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.draw.free.Global
import com.draw.free.R
import com.draw.free.databinding.ActivityNftPriceBinding
import com.draw.free.model.ParcelableNftMetadata
import com.draw.free.model.ParcelableNftSelling
import com.draw.free.nft.viewmodel.NftPreviewActivityViewModel.NftApiType
import com.draw.free.nft.viewmodel.NftPriceActivityViewModel.CheckPriceType
import com.draw.free.nft.viewmodel.NftPriceActivityViewModel
import com.draw.free.setting.RestoreWalletByPhraseActivity
import timber.log.Timber

class NftPriceActivity : BaseLauncherActivity() {

    private lateinit var binding: ActivityNftPriceBinding
    private lateinit var sellData: ParcelableNftSelling
    private lateinit var metadata: ParcelableNftMetadata

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Global.prefs.walletPassword.isNullOrEmpty()) {
            Global.makeToast("지갑을 활성화 해주세요.")
            startActivity(Intent(baseContext, RestoreWalletByPhraseActivity::class.java))
            finish()
        }


        if (intent.hasExtra("sellData") && intent.hasExtra("metadata")) {
            sellData = intent.getParcelableExtra("sellData")!!
            metadata = intent.getParcelableExtra("metadata")!!
        } else {
            Timber.e("인텐트 제대로 안넘어옴")
            finish()
        }

        // 바인딩
        binding = DataBindingUtil.setContentView(this, R.layout.activity_nft_price)
        val priceViewModel = ViewModelProvider(this).get(NftPriceActivityViewModel::class.java)
        binding.nftPriceActivityViewModel = priceViewModel

        // 값 넣기
        Glide.with(this).load(metadata.thumbnail).into(binding.ivThumbnail)
        binding.txtTitle.text = metadata.name
        binding.txtSymbol.text = metadata.symbol

        // 뒤로가기 버튼
        binding.btnBack.setOnClickListener {
            onBackPressed()
            finish()
        }

        // 다음 버튼
        binding.btnNext.setOnClickListener {
            // 클릭 막기
            binding.btnBack.isClickable = false
            binding.etPrice.isClickable = false
            binding.btnNext.isClickable = false

            when (priceViewModel.checkPrice()) {
                CheckPriceType.Empty -> {
                    Global.makeToast(getString(R.string.price_empty_notice))
                }
                CheckPriceType.NotNumberFormat -> {
                    Global.makeToast(getString(R.string.not_number_format_notice))
                }
                CheckPriceType.NotInRange -> {
                    Global.makeToast(getString(R.string.price_not_in_range_notice))
                }
                CheckPriceType.WrongDecimal -> {
                    Global.makeToast(getString(R.string.price_wrong_decimal_notice))
                }
                CheckPriceType.Ok -> {
                    // 가격 저장
                    sellData.price = binding.etPrice.text.toString()
                    // 미리보기 화면으로 이동
                    val intent = Intent(this, NftPreviewActivity::class.java)
                    intent.putExtra("type", NftApiType.Sell)
                    intent.putExtra("sellData", sellData)
                    intent.putExtra("metadata", metadata)
                    launcher.launch(intent)
                }
            }
            // 클릭 가능
            binding.btnBack.isClickable = true
            binding.etPrice.isClickable = true
            binding.btnNext.isClickable = true
        }

    }
}