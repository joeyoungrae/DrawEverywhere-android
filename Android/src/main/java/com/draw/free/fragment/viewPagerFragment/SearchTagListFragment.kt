package com.draw.free.fragment.viewPagerFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.draw.free.viewmodel.viewPagerFragmentViewModel.WalletListFragmentViewModel.Type
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.draw.free.*
import com.draw.free.databinding.FragmentAuctionListBinding
import com.draw.free.databinding.FragmentSearchListBinding
import com.draw.free.fragment.BaseInnerFragment
import com.draw.free.fragment.WalletFragment
import com.draw.free.interfaceaction.IOpenPost
import com.draw.free.model.Auction
import com.draw.free.model.Post
import com.draw.free.util.CustomList
import com.draw.free.viewmodel.SearchFragmentViewModel
import com.draw.free.viewmodel.viewPagerFragmentViewModel.WalletListFragmentViewModel
import timber.log.Timber

class SearchTagListFragment : BaseInnerFragment<FragmentSearchListBinding>() {

    private lateinit var requestManager: RequestManager
    private lateinit var adapter: HashTagAdapter
    private lateinit var viewModel: SearchFragmentViewModel

    init {
        layoutId = R.layout.fragment_search_list
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)

        viewModel = ViewModelProvider(requireParentFragment())[SearchFragmentViewModel::class.java]


        // 글라이더 requestManager
        requestManager = Glide.with(this)

        // 레이아웃 매니저 설정 및 어댑터 생성
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())



        // 어댑터 연결
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())

        viewModel.hashTagsByKeyword.observe(viewLifecycleOwner) {
            adapter = HashTagAdapter(it)
            adapter.openTag = { hashTag ->
                viewModel.openTag(hashTag)
            }
            // 옵저버
            binding.recyclerView.adapter = adapter


            adapter.data.mLiveData.observe(viewLifecycleOwner) {
                binding.recyclerView.adapter = adapter
                adapter!!.notifyDataSetChanged()

                if (adapter.itemCount != 0) {
                    binding.swipeLayout.isEnabled = true
                    binding.txtZeroPost.visibility = View.GONE

                    binding.swipeLayout.setOnRefreshListener {
                        adapter.data.refreshData()
                        binding.swipeLayout.isRefreshing = false
                    }
                } else {
                    binding.swipeLayout.isEnabled = false
                    binding.txtZeroPost.visibility = View.VISIBLE

                }
            }
        }


        // 스크롤에 따라 다음 데이터 가져오도록 설정
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                // 마지막 스크롤된 항목 위치
                val lastVisibleItemPosition =
                    (recyclerView.layoutManager as LinearLayoutManager).findLastCompletelyVisibleItemPosition()
                // 항목 전체 개수
                val itemTotalCount = recyclerView.adapter!!.itemCount - 2
                if (lastVisibleItemPosition >= itemTotalCount) {
                    adapter.data.getNextData()
                }
            }
        })



        return binding.root
    }


}