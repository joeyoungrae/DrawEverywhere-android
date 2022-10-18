package com.draw.free.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.draw.free.Global
import com.draw.free.R
import com.draw.free.customView.GlbView
import com.draw.free.databinding.FragmentAuctionDetailBinding
import com.draw.free.model.Auction
import com.draw.free.model.ParcelableNftMetadata
import com.draw.free.nft.NftPreviewActivity
import com.draw.free.nft.viewmodel.NftPreviewActivityViewModel
import com.draw.free.viewmodel.AuctionDetailFragmentViewModel

import java.text.Format
import java.text.SimpleDateFormat

class AuctionDetailFragment : BaseInnerFragment<FragmentAuctionDetailBinding>() {

    interface AuctionDetailAction {
        fun openWallet(address: String)
        fun pop()
    }

    lateinit var mint: String
    var auction: Auction? = null
    lateinit var auctionDetailAction: AuctionDetailAction
    private lateinit var viewModel: AuctionDetailFragmentViewModel
    private lateinit var requestManager: RequestManager
    lateinit var glbView: GlbView

    @SuppressLint("SimpleDateFormat")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requestManager = Glide.with(this)

        // GLB 뷰어 설정
        glbView = GlbView()
        glbView.loadEntity()
        glbView.setSurfaceView(binding.surface)
        glbView.loadIndirectLight(requireContext())

        // 뷰모델
        viewModel = ViewModelProvider(this).get(AuctionDetailFragmentViewModel::class.java)
        viewModel.mint = mint

        // 상태 숨기기
        binding.onSale.visibility = View.GONE
        binding.endedSale.visibility = View.GONE
        binding.sold.visibility = View.GONE
        binding.notSettled.visibility = View.GONE
        binding.settled.visibility = View.GONE

        // 버튼 숨기기
        binding.btnBuyNft.visibility = View.GONE
        binding.btnEndSale.visibility = View.GONE
        binding.btnSettle.visibility = View.GONE

        // buyer 숨기기
        binding.txtBuyer.visibility = View.GONE
        binding.buyerLayout.visibility = View.GONE

        // 전체 레이아웃 숨기기
        binding.scrollLayout.visibility = View.INVISIBLE

        if (auction == null) {
            // 옥션 정보 가져오기
            viewModel.setAuction()
            // Observer
            viewModel.auction.observe(viewLifecycleOwner) {
                // GLB 로드
                glbView.loadModelData(it.nft.glb)
                // auctioneer 프로필
                binding.tvAuctioneerAddress.text = it.auctioneer
                // buyer 프로필
                if (it.buyer != null) {
                    binding.tvBuyerAddress.text = it.buyer
                    binding.txtBuyer.visibility = View.VISIBLE
                    binding.buyerLayout.visibility = View.VISIBLE
                }
                // nft name
                binding.tvName.text = it.nft.data.name
                // nft symbol
                binding.tvSymbol.text = it.nft.data.symbol
                // nft desc
                binding.tvDesc.text = it.nft.data.description
                // desc 텍스트뷰 스크롤
                binding.tvDesc.movementMethod = ScrollingMovementMethod()
                // price
                binding.txtPrice.text = String.format("%.9f", it.price)
                // created_at
                val format: Format = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
                binding.tvCreatedAt.text = format.format(it.createdAt)

                // 상태 마크 보이기
                if (it.state == "started") {
                    binding.onSale.visibility = View.VISIBLE // 판매 중
                }
                if (it.state == "ended" && it.buyer == null) {
                    binding.endedSale.visibility = View.VISIBLE // 본인이 판매 종료한 경우
                }
                if (it.state == "ended" && it.buyer != null) {
                    binding.sold.visibility = View.VISIBLE // 다른 유저가 구매한 경우
                    if (it.isSettled) {
                        binding.settled.visibility = View.VISIBLE // 정산 완료
                    } else {
                        binding.notSettled.visibility = View.VISIBLE // 정산 미완료
                    }
                }

                // 버튼 설정
                if (Global.userProfile != null) { // 로그인 되어있는 경우만
                    if (Global.userProfile!!.walletAddress == it.auctioneer) { // 본인이 판매자인 경우
                        if (it.state == "started") {
                            binding.btnEndSale.visibility = View.VISIBLE
                        }
                        if (it.state == "ended" && it.buyer != null && !it.isSettled) {
                            binding.btnSettle.visibility = View.VISIBLE
                        }
                    } else { // 본인의 nft가 아닌 경우
                        if (it.state == "started") {
                            binding.btnBuyNft.visibility = View.VISIBLE
                        }
                    }
                }

                // 전체 레이아웃 보이기
                binding.scrollLayout.visibility = View.VISIBLE
            }

            // ClickListener
            // 유저 프로필로 이동
            binding.auctioneerLayout.setOnClickListener {
                // TODO 지갑으로 이동
                auctionDetailAction.openWallet(viewModel.auction.value!!.auctioneer)
            }
            binding.buyerLayout.setOnClickListener {
                // TODO 지갑으로 이동
                auctionDetailAction.openWallet(viewModel.auction.value!!.buyer!!)
            }
            binding.btnBuyNft.setOnClickListener {
                // nft 구매
                val intent = Intent(requireActivity(), NftPreviewActivity::class.java)
                intent.putExtra("type", NftPreviewActivityViewModel.NftApiType.Buy)
                intent.putExtra("auctionCache", viewModel.auction.value!!.auctionCache)
                intent.putExtra(
                    "metadata", ParcelableNftMetadata(
                        viewModel.auction.value!!.nft.post,
                        viewModel.auction.value!!.nft.thumbnail,
                        viewModel.auction.value!!.nft.data.name,
                        viewModel.auction.value!!.nft.data.symbol,
                        viewModel.auction.value!!.nft.data.description,
                        viewModel.auction.value!!.nft.data.sellerFeeBasisPoints.toString(),
                        viewModel.auction.value!!.nft.data.creators[0].address
                    )
                )
                // 무조건 새로운 태스크로 실행
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
                startActivity(intent)
                // 현재 페이지에서 나오기
                auctionDetailAction.pop()
            }
            binding.btnEndSale.setOnClickListener {
                // nft 판매 취소
                val intent = Intent(requireActivity(), NftPreviewActivity::class.java)
                intent.putExtra("type", NftPreviewActivityViewModel.NftApiType.EndSale)
                intent.putExtra("auctionCache", viewModel.auction.value!!.auctionCache)
                intent.putExtra(
                    "metadata", ParcelableNftMetadata(
                        viewModel.auction.value!!.nft.post,
                        viewModel.auction.value!!.nft.thumbnail,
                        viewModel.auction.value!!.nft.data.name,
                        viewModel.auction.value!!.nft.data.symbol,
                        viewModel.auction.value!!.nft.data.description,
                        viewModel.auction.value!!.nft.data.sellerFeeBasisPoints.toString(),
                        viewModel.auction.value!!.nft.data.creators[0].address
                    )
                )
                // 무조건 새로운 태스크로 실행
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
                startActivity(intent)
                // 현재 페이지에서 나오기
                auctionDetailAction.pop()
            }
            binding.btnSettle.setOnClickListener {
                // nft 정산
                val intent = Intent(requireActivity(), NftPreviewActivity::class.java)
                intent.putExtra("type", NftPreviewActivityViewModel.NftApiType.SettleBill)
                intent.putExtra("auctionCache", viewModel.auction.value!!.auctionCache)
                intent.putExtra(
                    "metadata", ParcelableNftMetadata(
                        viewModel.auction.value!!.nft.post,
                        viewModel.auction.value!!.nft.thumbnail,
                        viewModel.auction.value!!.nft.data.name,
                        viewModel.auction.value!!.nft.data.symbol,
                        viewModel.auction.value!!.nft.data.description,
                        viewModel.auction.value!!.nft.data.sellerFeeBasisPoints.toString(),
                        viewModel.auction.value!!.nft.data.creators[0].address
                    )
                )
                // 무조건 새로운 태스크로 실행
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
                startActivity(intent)
                // 현재 페이지에서 나오기
                auctionDetailAction.pop()
            }
        } else {
            // GLB 로드
            glbView.loadModelData(auction!!.nft.glb)

            // auctioneer 프로필
            binding.tvAuctioneerAddress.text = auction!!.auctioneer
            // buyer 프로필
            if (auction!!.buyer != null) {
                binding.tvBuyerAddress.text = auction!!.buyer!!
                binding.txtBuyer.visibility = View.VISIBLE
                binding.buyerLayout.visibility = View.VISIBLE
            }
            // nft name
            binding.tvName.text = auction!!.nft.data.name
            // nft symbol
            binding.tvSymbol.text = auction!!.nft.data.symbol
            // nft desc
            binding.tvDesc.text = auction!!.nft.data.description
            // desc 텍스트뷰 스크롤
            binding.tvDesc.movementMethod = ScrollingMovementMethod()
            // price
            binding.txtPrice.text = String.format("%.9f", auction!!.price)
            // created_at
            val format: Format = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
            binding.tvCreatedAt.text = format.format(auction!!.createdAt)

            // 상태 마크 보이기
            if (auction!!.state == "started") {
                binding.onSale.visibility = View.VISIBLE // 판매 중
            }
            if (auction!!.state == "ended" && auction!!.buyer == null) {
                binding.endedSale.visibility = View.VISIBLE // 본인이 판매 종료한 경우
            }
            if (auction!!.state == "ended" && auction!!.buyer != null) {
                binding.sold.visibility = View.VISIBLE // 다른 유저가 구매한 경우
                if (auction!!.isSettled) {
                    binding.settled.visibility = View.VISIBLE // 정산 완료
                } else {
                    binding.notSettled.visibility = View.VISIBLE // 정산 미완료
                }
            }

            // 버튼 설정
            if (Global.userProfile != null) { // 로그인 되어있는 경우만
                if (Global.userProfile!!.walletAddress == auction!!.auctioneer) { // 본인이 판매자인 경우
                    if (auction!!.state == "started") {
                        binding.btnEndSale.visibility = View.VISIBLE
                    }
                    if (auction!!.state == "ended" && auction!!.buyer != null && !auction!!.isSettled) {
                        binding.btnSettle.visibility = View.VISIBLE
                    }
                } else { // 본인의 nft가 아닌 경우
                    if (auction!!.state == "started") {
                        binding.btnBuyNft.visibility = View.VISIBLE
                    }
                }
            }

            // 전체 레이아웃 보이기
            binding.scrollLayout.visibility = View.VISIBLE


            // ClickListener
            // 유저 프로필로 이동
            binding.auctioneerLayout.setOnClickListener {
                auctionDetailAction.openWallet(viewModel.auction.value!!.auctioneer!!)

            }
            binding.buyerLayout.setOnClickListener {
                if (auction!!.buyer != null) {
                    auctionDetailAction.openWallet(viewModel.auction.value!!.buyer!!)

                }
            }
            // 버튼 클릭
            binding.btnBuyNft.setOnClickListener {
                // nft 구매
                val intent = Intent(requireActivity(), NftPreviewActivity::class.java)
                intent.putExtra("type", NftPreviewActivityViewModel.NftApiType.Buy)
                intent.putExtra("auctionCache", auction!!.auctionCache)
                intent.putExtra(
                    "metadata", ParcelableNftMetadata(
                        auction!!.nft.post, auction!!.nft.thumbnail,
                        auction!!.nft.data.name, auction!!.nft.data.symbol,
                        auction!!.nft.data.description,
                        auction!!.nft.data.sellerFeeBasisPoints.toString(),
                        auction!!.nft.data.creators[0].address
                    )
                )
                // 무조건 새로운 태스크로 실행
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
                startActivity(intent)
                // 현재 페이지에서 나오기
                auctionDetailAction.pop()
            }
            binding.btnEndSale.setOnClickListener {
                // nft 판매 취소
                val intent = Intent(requireActivity(), NftPreviewActivity::class.java)
                intent.putExtra("type", NftPreviewActivityViewModel.NftApiType.EndSale)
                intent.putExtra("auctionCache", auction!!.auctionCache)
                intent.putExtra(
                    "metadata", ParcelableNftMetadata(
                        auction!!.nft.post, auction!!.nft.thumbnail,
                        auction!!.nft.data.name, auction!!.nft.data.symbol,
                        auction!!.nft.data.description,
                        auction!!.nft.data.sellerFeeBasisPoints.toString(),
                        auction!!.nft.data.creators[0].address
                    )
                )
                // 무조건 새로운 태스크로 실행
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
                startActivity(intent)
                // 현재 페이지에서 나오기
                auctionDetailAction.pop()
            }
            binding.btnSettle.setOnClickListener {
                // nft 정산
                val intent = Intent(requireActivity(), NftPreviewActivity::class.java)
                intent.putExtra("type", NftPreviewActivityViewModel.NftApiType.SettleBill)
                intent.putExtra("auctionCache", auction!!.auctionCache)
                intent.putExtra(
                    "metadata", ParcelableNftMetadata(
                        auction!!.nft.post, auction!!.nft.thumbnail,
                        auction!!.nft.data.name, auction!!.nft.data.symbol,
                        auction!!.nft.data.description,
                        auction!!.nft.data.sellerFeeBasisPoints.toString(),
                        auction!!.nft.data.creators[0].address
                    )
                )
                // 무조건 새로운 태스크로 실행
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
                startActivity(intent)
                // 현재 페이지에서 나오기
                auctionDetailAction.pop()
            }
        }

    }

    override fun onStart() {
        super.onStart()
        showBottomNav.show(false)
        setPreviousButton.set(isSet = true)
        glbView.onStart()
    }

    override fun onResume() {
        super.onResume()
        glbView.onResume()
    }

    override fun onPause() {
        super.onPause()
        glbView.onPause()
    }


    override fun onStop() {
        super.onStop()
        glbView.onStop()

    }

    override fun onDestroy() {
        super.onDestroy()
        glbView.onDestroy()
    }
}