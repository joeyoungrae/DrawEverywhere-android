package com.draw.free.fragment

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.draw.free.Global
import com.draw.free.R
import com.draw.free.WalletSettingActivity
import com.draw.free.databinding.FragmentWalletBinding
import com.draw.free.fragment.viewPagerFragment.AuctionListFragment
import com.draw.free.fragment.viewPagerFragment.NftListFragment
import com.draw.free.model.Auction
import com.draw.free.model.Nft
import com.draw.free.viewmodel.viewPagerFragmentViewModel.WalletListFragmentViewModel
import com.google.android.material.tabs.TabLayoutMediator
import com.draw.free.viewmodel.viewPagerFragmentViewModel.WalletListFragmentViewModel.Type

class WalletFragment : BaseInnerFragment<FragmentWalletBinding>() {
    interface WalletAction {
        fun openProfile(accountId: String)
        fun openNft(nft: Nft)
        fun openAuction(auction: Auction)
    }

    lateinit var walletAction: WalletAction
    var targetAddress: String = ""
    private lateinit var viewModel: WalletListFragmentViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[WalletListFragmentViewModel::class.java]
        viewModel.targetAddress = targetAddress

        // 지갑 잔액 가져오기
        viewModel.setBalance()

        // 데이터 가져오기
        viewModel.setOwnedNftList()
        viewModel.customOwnedNftList.getNextData()

        viewModel.setMintedNftList()
        viewModel.customMintedNftList.getNextData()

    }


    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // 뷰 바인딩
        super.onCreateView(inflater, container, savedInstanceState);

        // 뷰페이저 어댑터 연결
        val adapter = WalletViewPagerAdapter(this)
        binding.viewPager.adapter = adapter
        binding.viewPager.currentItem = 0

        // 탭 레이아웃과 뷰페이저 연결
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when (position) {
                0 -> {
                    tab.text = getString(R.string.owned_nft)
                    tab.icon = resources.getDrawable(R.drawable.icon_owned_nft, null)
                }
                1 -> {
                    tab.text = getString(R.string.minted_nft)
                    tab.icon = resources.getDrawable(R.drawable.icon_minted_nft, null)
                }
                2 -> {
                    tab.text = getString(R.string.my_auction)
                    tab.icon = resources.getDrawable(R.drawable.icon_auction, null)
                }
            }
        }.attach()

        // 지갑 주소
        binding.tvAddress.text = targetAddress

        // ClickListener
        binding.tvAddress.setOnClickListener {
            // 지갑 주소 복사
            val clipboardManager = Global.getContext().getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("address", binding.tvAddress.text)
            clipboardManager.setPrimaryClip(clipData)

            Global.makeToast("클립보드에 복사되었습니다!")
        }

        // 지갑 잔액 Observer
        viewModel.balance.observe(viewLifecycleOwner) {
            binding.tvBalance.text = it.toString()
        }

        return binding.root

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 세팅버튼
        binding.btnSetting.setOnClickListener {
            val intent = Intent(requireActivity(), WalletSettingActivity::class.java)
            startActivity(intent)
        }
    }


    // 뷰페이저 어댑터
    inner class WalletViewPagerAdapter(fragment: Fragment?) :
        FragmentStateAdapter(fragment!!) {
        override fun createFragment(position: Int): Fragment {
            val fragment =  when(position)
            {
                0-> {
                    val nftListfragment = NftListFragment()
                    nftListfragment.viewModel = viewModel
                    nftListfragment.targetAddress = targetAddress
                    nftListfragment.type = Type.OWNED_NFT
                    nftListfragment.action = walletAction
                    nftListfragment
                }
                1-> {
                    val nftListfragment = NftListFragment()
                    nftListfragment.viewModel = viewModel
                    nftListfragment.targetAddress = targetAddress
                    nftListfragment.type = Type.MINTED_NFT
                    nftListfragment.action = walletAction
                    nftListfragment
                }
                2 -> {
                    val auctionListFragment = AuctionListFragment()
                    auctionListFragment.viewModel = viewModel
                    auctionListFragment.targetAddress = targetAddress
                    auctionListFragment.type = Type.AUCTION
                    auctionListFragment.action = walletAction
                    auctionListFragment
                }
                else-> {
                    val nftListfragment = NftListFragment()
                    nftListfragment
                }
            }
            return fragment
        }

        override fun getItemCount(): Int {
            return 3
        }
    }

    override fun onStart() {
        super.onStart()
        if(!isHidden) {
            setPreviousButton.set(isSet = true)
            showBottomNav.show(true)
        }
    }

}