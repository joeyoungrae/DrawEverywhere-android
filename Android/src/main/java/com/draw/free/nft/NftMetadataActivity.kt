package com.draw.free.nft

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.draw.free.Global
import com.draw.free.R
import com.draw.free.databinding.ActivityNftMetadataBinding
import com.draw.free.nft.viewmodel.NftMetadataActivityViewModel
import com.draw.free.nft.viewmodel.NftMetadataActivityViewModel.CreatorFeeType
import com.draw.free.nft.viewmodel.NftPreviewActivityViewModel.NftApiType
import com.draw.free.setting.RestoreWalletByPhraseActivity
import timber.log.Timber

class NftMetadataActivity : BaseLauncherActivity() {

    private lateinit var binding: ActivityNftMetadataBinding
    private lateinit var postId: String
    private lateinit var thumbnail: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Global.prefs.walletPassword.isNullOrEmpty()) {
            Global.makeToast("지갑을 활성화 해주세요.")
            startActivity(Intent(baseContext, RestoreWalletByPhraseActivity::class.java))
            finish()
        }

        if (intent.hasExtra("postId") && intent.hasExtra("thumbnail")) {
            postId = intent.getStringExtra("postId")!!
            thumbnail = intent.getStringExtra("thumbnail")!!
        } else {
            Timber.e("인텐트 제대로 안넘어옴")
            finish()
        }

        // 바인딩
        binding = DataBindingUtil.setContentView(this, R.layout.activity_nft_metadata)
        val metadataViewModel =
            ViewModelProvider(this).get(NftMetadataActivityViewModel::class.java)
        metadataViewModel.postId = postId
        metadataViewModel.creatorId = Global.userProfile!!.uniqueId
        metadataViewModel.thumbnail = thumbnail
        binding.nftMetadataActivityViewModel = metadataViewModel

        // Observer
        metadataViewModel.mTitle.observe(this) {
            if (metadataViewModel.checkTitleLength()) {
                binding.tvTitleNotice.visibility = View.INVISIBLE
            } else {
                if (binding.etTitle.text.isNullOrEmpty()) {
                    binding.tvTitleNotice.text = getString(R.string.notice_empty)
                } else {
                    binding.tvTitleNotice.text = getString(R.string.notice_long)
                }
                binding.tvTitleNotice.visibility = View.VISIBLE
            }
        }
        metadataViewModel.mSymbol.observe(this) {
            if (metadataViewModel.checkSymbolLength()) {
                binding.tvSymbolNotice.visibility = View.INVISIBLE
            } else {
                if (binding.etSymbol.text.isNullOrEmpty()) {
                    binding.tvSymbolNotice.text = getString(R.string.notice_empty)
                } else {
                    binding.tvSymbolNotice.text = getString(R.string.notice_long)
                }
                binding.tvSymbolNotice.visibility = View.VISIBLE
            }
        }
        metadataViewModel.mDesc.observe(this) {
            if (metadataViewModel.checkDescLength()) {
                binding.tvDescNotice.visibility = View.INVISIBLE
            } else {
                if (binding.etDesc.text.isNullOrEmpty()) {
                    binding.tvDescNotice.text = getString(R.string.notice_empty)
                } else {
                    binding.tvDescNotice.text = getString(R.string.notice_long)
                }
                binding.tvDescNotice.visibility = View.VISIBLE
            }
        }
        metadataViewModel.mCreatorFee.observe(this) {
            when (metadataViewModel.checkCreatorFee()) {
                CreatorFeeType.NotNumberFormat -> {
                    binding.tvFeeNotice.text = getString(R.string.only_number)
                    binding.tvFeeNotice.visibility = View.VISIBLE
                }
                CreatorFeeType.NotInRange -> {
                    binding.tvFeeNotice.text = getString(R.string.fee_range)
                    binding.tvFeeNotice.visibility = View.VISIBLE
                }
                CreatorFeeType.Empty -> {
                    binding.tvFeeNotice.text = getString(R.string.notice_empty)
                    binding.tvFeeNotice.visibility = View.VISIBLE
                }
                CreatorFeeType.WrongDecimal -> {
                    binding.tvFeeNotice.text = getString(R.string.fee_decimal)
                    binding.tvFeeNotice.visibility = View.VISIBLE
                }
                CreatorFeeType.Ok -> {
                    binding.tvFeeNotice.visibility = View.INVISIBLE
                }
            }
        }

        // 뒤로가기 버튼
        binding.btnBack.setOnClickListener {
            onBackPressed()
            finish()
        }

        // 다음 버튼
        binding.btnNext.setOnClickListener {
            // 클릭 막기
            binding.etTitle.isClickable = false
            binding.etSymbol.isClickable = false
            binding.etDesc.isClickable = false
            binding.etCreatorFee.isClickable = false
            binding.btnBack.isClickable = false
            binding.btnNext.isClickable = false


            val metadata = metadataViewModel.checkAll()
            if (metadata != null) {

                // 미리보기 화면으로 이동
                val intent = Intent(this, NftPreviewActivity::class.java)
                intent.putExtra("type", NftApiType.Mint)
                intent.putExtra("metadata", metadata)
                launcher.launch(intent)
            } else {
                Global.makeToast(getString(R.string.notice_check_again))
            }
            // 클릭 가능
            binding.etTitle.isClickable = true
            binding.etSymbol.isClickable = true
            binding.etDesc.isClickable = true
            binding.etCreatorFee.isClickable = true
            binding.btnBack.isClickable = true
            binding.btnNext.isClickable = true
        }

    }

}