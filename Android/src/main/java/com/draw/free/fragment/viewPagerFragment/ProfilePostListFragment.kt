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
import com.draw.free.AuctionAdapter
import com.draw.free.NftAdapter
import com.draw.free.PostAdapter
import com.draw.free.R
import com.draw.free.databinding.FragmentAuctionListBinding
import com.draw.free.databinding.FragmentProfilePostListBinding
import com.draw.free.databinding.FragmentSearchListBinding
import com.draw.free.fragment.BaseInnerFragment
import com.draw.free.fragment.WalletFragment
import com.draw.free.interfaceaction.IOpenPost
import com.draw.free.model.Auction
import com.draw.free.model.Post
import com.draw.free.util.CustomList
import com.draw.free.viewmodel.ProfileFragmentViewModel
import com.draw.free.viewmodel.SearchFragmentViewModel
import com.draw.free.viewmodel.viewPagerFragmentViewModel.WalletListFragmentViewModel
import timber.log.Timber

class ProfilePostListFragment : BaseInnerFragment<FragmentProfilePostListBinding>() {
    enum class Type {
        Write, Like
    }


    private lateinit var requestManager: RequestManager
    private lateinit var adapter: PostAdapter
    private lateinit var viewModel: ProfileFragmentViewModel
    lateinit var type: Type
    lateinit var openPost: IOpenPost

    init {
        layoutId = R.layout.fragment_profile_post_list
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        viewModel = ViewModelProvider(requireParentFragment())[ProfileFragmentViewModel::class.java]


        // ???????????? requestManager
        requestManager = Glide.with(this)


        if (type == Type.Write) {
            adapter = PostAdapter(
                viewModel.writePosts,
                PostAdapter.ShowType.PROFILE_POSTS,
                openPost,
                requestManager
            )
        } else {
            adapter = PostAdapter(
                viewModel.likePosts,
                PostAdapter.ShowType.PROFILE_POSTS,
                openPost,
                requestManager
            )
        }


        viewModel.isSecretAccount.observe(viewLifecycleOwner) {
            if (!it) {
                binding.txtZeroPost.visibility = View.INVISIBLE
                binding.txtSecretPost.visibility = View.VISIBLE

                Timber.e("__?????????__")
            } else {
                binding.txtSecretPost.visibility = View.INVISIBLE

                Timber.e("__??????__")

                when (type) {
                    Type.Write -> {
                        viewModel.writePosts.getNextData()
                    }
                    Type.Like -> {
                        viewModel.likePosts.getNextData()
                    }
                }


                binding.recyclerView.adapter = adapter
                binding.swipeLayout.isEnabled = false

                adapter.data.mLiveData.observe(viewLifecycleOwner) {
                    if (it.isEmpty()) {
                        binding.txtZeroPost.visibility = View.VISIBLE
                        binding.swipeLayout.isEnabled = false
                    } else {
                        binding.txtZeroPost.visibility = View.INVISIBLE
                        binding.swipeLayout.isEnabled = true

                        binding.swipeLayout.setOnRefreshListener {
                            adapter.data.refreshData()
                            binding.swipeLayout.isRefreshing = false
                        }
                    }

                    adapter.notifyDataSetChanged()
                }


                // ???????????? ?????? ?????? ????????? ??????????????? ??????
                binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        super.onScrolled(recyclerView, dx, dy)
                        // ????????? ???????????? ?????? ??????
                        val lastVisibleItemPosition =
                            (recyclerView.layoutManager as LinearLayoutManager).findLastCompletelyVisibleItemPosition()
                        // ?????? ?????? ??????
                        val itemTotalCount = recyclerView.adapter!!.itemCount - 2
                        if (lastVisibleItemPosition >= itemTotalCount) {
                            Timber.e("this")
                        }
                    }
                })
            }
        }





        return binding.root
    }


}