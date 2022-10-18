package com.draw.free.fragment

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.draw.free.PostAdapter
import com.draw.free.databinding.FragmentPostListBinding
import com.draw.free.interfaceaction.IOpenPost
import com.draw.free.model.Post
import com.draw.free.util.CustomList
import com.draw.free.viewmodel.PostListFragmentViewModel

class PostListFragment  : BaseInnerFragment<FragmentPostListBinding>() {
    interface PostListAction {
        fun openPost(postId: String, customPostList: CustomList<Post>)
    }


    lateinit var hashTag : String
    lateinit var viewModel : PostListFragmentViewModel
    lateinit var postAction : PostListAction

    private val openPost = object : IOpenPost {
        override fun open(postId: String, customPostList: CustomList<Post>) {
            postAction.openPost(postId, customPostList)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(PostListFragmentViewModel::class.java)
        viewModel.hashtag = hashTag
        binding.tvHashtags.text = hashTag

        val customPostList = viewModel.initialGetCustomPost()

        val adapter = PostAdapter(customPostList, PostAdapter.ShowType.GIF, openPost, Glide.with(this))
        binding.rv.layoutManager = GridLayoutManager(context, 3)
        binding.rv.adapter = adapter

        customPostList.mLiveData.observe(viewLifecycleOwner) {
            adapter.notifyDataSetChanged()
        }

        binding.swipe.setOnRefreshListener {
            adapter.data.refreshData()
            binding.swipe.isRefreshing = false
        }
    }

    override fun onStart() {
        super.onStart()
        if(!isHidden) {
            showBottomNav.show(false)
            setPreviousButton.set(isSet = true, false)
        }
    }

}