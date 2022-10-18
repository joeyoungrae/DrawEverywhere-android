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
import com.draw.free.viewmodel.UserRelationListViewModel
import com.draw.free.viewmodel.viewPagerFragmentViewModel.WalletListFragmentViewModel
import timber.log.Timber

class RelationUserListFragment : BaseInnerFragment<FragmentSearchListBinding>() {

    private lateinit var requestManager: RequestManager
    lateinit var viewModel: UserRelationListViewModel
    var isFollower = false

    init {
        layoutId = R.layout.fragment_search_list
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 글라이더 requestManager
        requestManager = Glide.with(this)

        // 레이아웃 매니저 설정 및 어댑터 생성
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.txtZeroPost.visibility = View.GONE



        val adapter: UserAdapter by lazy {
            if (isFollower) {
                binding.txtZeroPost.text = context?.getString(R.string.notice_zero_follower)
                UserAdapter(viewModel.followerList)
            } else {
                binding.txtZeroPost.text = context?.getString(R.string.notice_zero_following)
                UserAdapter(viewModel.followingList)
            }
        }
        adapter.follow = viewModel.followerUser;
        adapter.openProfile = { accountId ->
             viewModel.mOpenUserProfile(accountId)
        }
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

        // 새로고침
        binding.swipeLayout.setOnRefreshListener {
            adapter.data.refreshData()
            binding.swipeLayout.isRefreshing = false
        }

    }


}