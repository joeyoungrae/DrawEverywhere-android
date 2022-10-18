package com.draw.free.fragment

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.draw.free.Global
import com.draw.free.R
import com.draw.free.customView.GlbView
import com.draw.free.databinding.FragmentNftDetailBinding
import com.draw.free.model.Nft
import com.draw.free.model.ParcelableNftMetadata
import com.draw.free.model.ParcelableNftSelling
import com.draw.free.nft.NftPriceActivity
import com.draw.free.viewmodel.NftDetailFragmentViewModel
import timber.log.Timber
import java.text.Format
import java.text.SimpleDateFormat

class NftDetailFragment : BaseInnerFragment<FragmentNftDetailBinding>() {
    interface NftDetailAction {
        fun openWalletFragment(walletAddress: String)
        fun openAuctionDetail(mint: String)
        fun pop()
    }

    lateinit var nft: Nft
    lateinit var postId: String
    lateinit var nftDetailAction: NftDetailAction
    var glbView: GlbView? = null
    lateinit var viewModel: NftDetailFragmentViewModel
    private lateinit var requestManager: RequestManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(NftDetailFragmentViewModel::class.java)
        viewModel.setNft(nft)

        viewModel.liveNft.observe(viewLifecycleOwner) { nft ->
            if (nft == null) {
                Global.makeToast("유효하지 않은 NFT 입니다.")
                return@observe
            }

            requestManager = Glide.with(this)

            // GLB 뷰어 설정 -> null일 때 최초 한번만 실행함.
            if (glbView == null) {
                glbView = GlbView()
                glbView!!.loadEntity()
                glbView!!.setSurfaceView(binding.surface)
                glbView!!.loadModelData(nft.glb)
                glbView!!.loadIndirectLight(requireContext())
                glbView!!.onResume()
            }

            // 현재 판매중인지 확인
            if (nft.isOnSale) {
                binding.btnGoAuction.visibility = View.VISIBLE
            } else {
                binding.btnGoAuction.visibility = View.INVISIBLE
            }

            binding.turnBackground.setOnClickListener {
                if (glbView != null) {
                    glbView?.changeBackground()
                    if (glbView!!.isBackgroundLight) {
                        binding.turnBackground.background =
                            ContextCompat.getDrawable(requireContext(), R.drawable.ic_lamp_light)
                    } else {
                        binding.turnBackground.background =
                            ContextCompat.getDrawable(requireContext(), R.drawable.ic_lamp_dark)
                    }
                }
            }

            // creator 프로필
            binding.tvCreatorAddress.text = nft.data.creators[0].address
            // owner 프로필
            binding.tvOwnerAddress.text = nft.owner

            // nft name
            binding.tvName.text = nft.data.name
            // nft symbol
            binding.tvSymbol.text = nft.data.symbol
            // nft desc
            binding.tvDesc.text = nft.data.description
            // desc 텍스트뷰 스크롤
            binding.tvDesc.movementMethod = ScrollingMovementMethod()
            // mint
            binding.tvMint.text = nft.mint
            // holder address
            binding.tvHolder.text = nft.holder
            // update_authority
            binding.tvUpdateAuthority.text = nft.updateAuthority
            // seller_fee_basis_point
            binding.tvSellerFeeBasisPoints.text = (nft.data.sellerFeeBasisPoints / 100.0).toString()
            // uri
            binding.tvUri.text = nft.data.uri
            binding.tvUri.paintFlags = Paint.UNDERLINE_TEXT_FLAG
            // created_at
            val format: Format = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
            binding.tvCreatedAt.text = format.format(nft.createdAt)


            // 버튼 설정
            binding.btnSellNft.visibility = View.GONE
            if (Global.userProfile != null) { // 로그인 되어있는 경우만
                if (Global.userProfile!!.walletAddress == nft.owner) { // 본인의 nft인 경우
                    if (!nft.isOnSale) {
                        binding.btnSellNft.visibility = View.VISIBLE
                    }
                }
            }

            // ClickListener
            // 유저 프로필로 이동
            binding.creatorLayout.setOnClickListener {
                nftDetailAction.pop()
                nftDetailAction.openWalletFragment(nft.data.creators[0].address)
            }
            binding.ownerLayout.setOnClickListener {
                nftDetailAction.pop()
                nftDetailAction.openWalletFragment(nft.owner)

            }
            // uri 열기
            binding.tvUri.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(binding.tvUri.text.toString()))
                startActivity(intent)
            }
            // 주소 복사
            binding.tvMint.setOnClickListener {
                // 지갑 주소 복사
                val clipboardManager = Global.getContext()
                    .getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clipData = ClipData.newPlainText("address", binding.tvMint.text)
                clipboardManager.setPrimaryClip(clipData)
                Global.makeToast("클립보드에 복사되었습니다!")
            }
            binding.tvHolder.setOnClickListener {
                // 지갑 주소 복사
                val clipboardManager = Global.getContext()
                    .getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clipData = ClipData.newPlainText("address", binding.tvHolder.text)
                clipboardManager.setPrimaryClip(clipData)
                Global.makeToast("클립보드에 복사되었습니다!")
            }
            binding.tvUpdateAuthority.setOnClickListener {
                // 지갑 주소 복사
                val clipboardManager = Global.getContext()
                    .getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clipData = ClipData.newPlainText("address", binding.tvUpdateAuthority.text)
                clipboardManager.setPrimaryClip(clipData)
                Global.makeToast("클립보드에 복사되었습니다!")
            }
            // 버튼 클릭
            binding.btnSellNft.setOnClickListener {
                // nft 판매 등록
                val intent = Intent(requireActivity(), NftPriceActivity::class.java)
                intent.putExtra("sellData", ParcelableNftSelling(nft.mint, nft.holder!!, null))
                intent.putExtra(
                    "metadata", ParcelableNftMetadata(
                        nft.post,
                        nft.thumbnail,
                        nft.data.name,
                        nft.data.symbol,
                        nft.data.description,
                        nft.data.sellerFeeBasisPoints.toString(),
                        nft.data.creators[0].address
                    )
                )
                // 무조건 새로운 태스크로 실행
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
                startActivity(intent)
                // 현재 페이지에서 나오기
                nftDetailAction.pop()
            }
            binding.btnGoAuction.setOnClickListener {
                // 옥션 화면으로 이동
                nftDetailAction.openAuctionDetail(nft.mint)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (!isHidden) {
            showBottomNav.show(false)
            setPreviousButton.set(isSet = true)
        }

        val prefs = Global.prefs.getPrefs()
        if (prefs != null && prefs.getBoolean("nftDetail_update", false)) {
            prefs.edit().remove("nftDetail_update").apply()
            viewModel.refreshNft(nft.mint)

            Timber.e("nft 변경 사항 있어서 새로고침")
        }

        glbView?.onStart()
    }

    override fun onResume() {
        super.onResume()
        glbView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        glbView?.onPause()
    }

    override fun onStop() {
        super.onStop()
        glbView?.onStop()
    }


    override fun onDestroy() {
        super.onDestroy()
        glbView?.onDestroy()
    }

}

