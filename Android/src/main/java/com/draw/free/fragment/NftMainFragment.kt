package com.draw.free.fragment

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.draw.free.AuctionAdapter
import com.draw.free.Global
import com.draw.free.R
import com.draw.free.databinding.FragmentNftBinding
import com.draw.free.model.Auction

import com.draw.free.viewmodel.NftFragmentViewModel

import timber.log.Timber

class NftMainFragment : BaseInnerFragment<FragmentNftBinding>() {
    lateinit var requestManager: RequestManager
    lateinit var adapter: AuctionAdapter
    private lateinit var viewModel: NftFragmentViewModel
    var mainAction: MainAction? = null

    interface MainAction {
        fun openAuction(auction: Auction)
    }

    companion object {
        private var INSTANCE: NftMainFragment? = null;

        fun getInstance(): NftMainFragment {
            if (INSTANCE == null) {
                INSTANCE = NftMainFragment()
            }

            return INSTANCE!!;
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rv = binding.rvNftOutline

        requestManager = Glide.with(this)
        viewModel = ViewModelProvider(this)[NftFragmentViewModel::class.java]

        viewModel.liveType.observe(viewLifecycleOwner) {
            when (it) {
                NftFragmentViewModel.Type.All -> {
                    binding.txtAuction.text =
                        getString(R.string.all_auction) //@string/started_auction
                }
                NftFragmentViewModel.Type.OnSale -> {
                    binding.txtAuction.text = getString(R.string.started_auction)
                }
                NftFragmentViewModel.Type.Cancel -> {
                    binding.txtAuction.text = getString(R.string.canceled_auction)
                }
                NftFragmentViewModel.Type.Sold -> {
                    binding.txtAuction.text = getString(R.string.ended_auction)
                }
                else -> {

                }
            }
        }

        binding.progressBar.visibility = View.VISIBLE

        viewModel.initialList()
        viewModel.customListLive.observe(viewLifecycleOwner) {
            // 어댑터 연결
            adapter = AuctionAdapter(it, AuctionAdapter.ShowType.NFT_TAB, requestManager, mainAction)
            rv.layoutManager = GridLayoutManager(requireContext(), 2)
            rv.adapter = adapter
            binding.progressBar.visibility = View.VISIBLE

            // 색깔 바꾸기
            // recyclerView 연결하기
            it.mLiveData.observe(viewLifecycleOwner) {
                if (adapter.itemCount != 0) {
                    binding.progressBar.visibility = View.GONE
                } else {
                    binding.progressBar.visibility = View.VISIBLE
                }
                adapter.notifyDataSetChanged()
            }
        }


        rv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                // 마지막 스크롤된 항목 위치
                val lastVisibleItemPosition =
                    (recyclerView.layoutManager as GridLayoutManager?)!!.findLastCompletelyVisibleItemPosition()
                // 항목 전체 개수
                val itemTotalCount = recyclerView.adapter!!.itemCount - 2
                if (lastVisibleItemPosition >= itemTotalCount) {
                    adapter.data.getNextData()
                }
            }
        })

        binding.btnFilter.setOnClickListener {
            if (binding.statusLayout.visibility == View.VISIBLE) {
                binding.statusLayout.visibility = View.GONE
            } else {
                binding.statusLayout.visibility = View.VISIBLE
            }
        }

        binding.all.setOnClickListener {
            viewModel.getAllCustomAuctionList()
            setColorButton()
        }
        binding.sold.setOnClickListener {
            viewModel.getSoldAuctionList()
            setColorButton()
        }
        binding.endedSale.setOnClickListener {
            viewModel.getCancelAuctionList()
            setColorButton()
        }
        binding.onSale.setOnClickListener {
            viewModel.getOnSaleAuctionList()
            setColorButton()
        }


        binding.swipeLayout.setOnRefreshListener {
            adapter.data.refreshData()
            Timber.d("refresh Data")
            binding.swipeLayout.isRefreshing = false
        }
    }

    private fun setColorButton() {
        binding.all.backgroundTintList = null
        binding.onSale.backgroundTintList = null
        binding.endedSale.backgroundTintList = null
        binding.sold.backgroundTintList = null

        when (viewModel.liveType.value) {
            NftFragmentViewModel.Type.All -> {
                binding.all.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.s_red)
            }
            NftFragmentViewModel.Type.OnSale -> {
                binding.onSale.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.s_blue)
            }
            NftFragmentViewModel.Type.Cancel -> {
                binding.endedSale.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.s_gray)
            }
            NftFragmentViewModel.Type.Sold -> {
                binding.sold.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.s_green)
            }
        }
    }


    override fun onStart() {
        super.onStart()
        if(!isHidden) {
            setPreviousButton.set(false)
            showBottomNav.show(true)
        }
        Timber.e("NftMainFragment OnStart")


        val prefs = Global.prefs.getPrefs()
        if (prefs != null && prefs.getBoolean("nftMain_update", false)) {
            viewModel.refreshData()
            prefs.edit().remove("nftMain_update").apply()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        INSTANCE = null
    }

}