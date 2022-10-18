package com.draw.free.fragment

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.draw.free.Global
import com.draw.free.MainPostAdapter
import com.draw.free.R
import com.draw.free.databinding.FragmentHomeBinding
import com.draw.free.interfaceaction.IOpenPost
import com.draw.free.model.Nft
import com.draw.free.model.Post
import com.draw.free.util.CustomList
import com.draw.free.viewmodel.HomeFragmentViewModel
import timber.log.Timber


class HomeMainFragment : BaseInnerFragment<FragmentHomeBinding>() {
    init {
        layoutId = R.layout.fragment_home
    }


    lateinit var requestManager: RequestManager
    lateinit var adapter: MainPostAdapter

    interface MainAction {
        fun openPost(postId: String, customPostList: CustomList<Post>)
        fun openNft(nft: Nft)
        fun openNotify()
        fun openProfile(accountId: String)
    }

    companion object {
        private var INSTANCE: HomeMainFragment? = null;

        fun getInstance(): HomeMainFragment {
            if (INSTANCE == null) {
                INSTANCE = HomeMainFragment();
            }

            return INSTANCE!!;
        }
    }

    private lateinit var viewModel: HomeFragmentViewModel

    var mainAction: MainAction? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rv = binding.rvHomeOutline
        viewModel = ViewModelProvider(this).get(HomeFragmentViewModel::class.java)
        binding.viewmodel = viewModel

        val iOpenPost = object : IOpenPost {
            override fun open(postId: String, customPostList: CustomList<Post>) {
                mainAction!!.openPost(postId, customPostList)
            }
        }


        requestManager = Glide.with(this)
        adapter = MainPostAdapter(requireContext(), requestManager, mainAction)
        adapter.openProfile = {
            mainAction?.openProfile(it)
        }
        adapter.openPost = iOpenPost
        rv.adapter = adapter


        // 홈화면에 띄워지는 포스트들 가져오기
        viewModel.getInitialMainPosts()

        viewModel.mCurMainContents.observe(viewLifecycleOwner) {
            // 홈화면 포스트 컨텐츠가 변경되었을 경우 업데이트
            adapter.updateMainContents(it)
            Timber.d("__업데이트__")
            adapter.notifyDataSetChanged()
        }

        // notify가져오기
        Global.notifyLists.mLiveData.observe(viewLifecycleOwner) {
            if (it.isEmpty()) {
                binding.redDot.visibility = View.GONE
                Timber.e("비어 있음")
            } else {
                binding.redDot.visibility = View.VISIBLE
                Timber.e("확인 안한 메세지가 남아 있음.")
            }
        }

        Global.notifyLists.refreshData()

        binding.notify.setOnClickListener {
            mainAction!!.openNotify()
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)

        if (!hidden) {
            // 홈화면에 띄워지는 포스트들 가져오기
            viewModel.getInitialMainPosts()
            Global.notifyLists.refreshData()
        }
    }

    override fun onStart() {
        super.onStart()

        Timber.e("homeMainFragment")

        if(!isHidden) {
            setPreviousButton.set(false)
            showBottomNav.show(true)
        }

        val prefs = Global.prefs.getPrefs()
        if (prefs.getBoolean("homeMainFragment_update", false)) {
            viewModel.getInitialMainPosts(true)
            prefs.edit().remove("homeMainFragment_update").apply()
            Timber.e("포스트 추가되서 메인 새로고침")
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        INSTANCE = null
    }

}