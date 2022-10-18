package com.draw.free.fragment

import android.os.Bundle
import android.view.View
import com.draw.free.R
import com.draw.free.model.Nft
import com.draw.free.model.Post
import com.draw.free.util.CustomList
import timber.log.Timber


class HomeContainer : BaseContainerFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (childFragmentManager.fragments.size == 0) {
            Timber.d("새로 생성")
            containerMainFragment()
        }
    }

    var mainFragment : HomeMainFragment? = null

    private val homeMainAction = object : HomeMainFragment.MainAction {
        override fun openPost(postId: String, customPostList: CustomList<Post>) {
            action(OrderType.PostDetail, extraValue = postId, customPostList = customPostList)
        }

        override fun openNft(nft: Nft) {
            Timber.d("NFT 열기")
            action(OrderType.NftDetail, nft = nft)
        }

        override fun openNotify() {
            action(OrderType.Notify)
        }

        override fun openProfile(accountId: String) {
            action(OrderType.Profile, accountId)
        }


    }

    private fun containerMainFragment() {
        if (mainFragment == null) {
            mainFragment = BaseInnerFragment.createHomeFragment(homeMainAction)
        }

        val fragment = mainFragment!!


        val transaction = childFragmentManager.beginTransaction();
        transaction.replace(R.id.lazy_fragment_binding_view, fragment, "HomeMain");

        val fragments = childFragmentManager.fragments;

        if (fragments.isNotEmpty()) {
            Timber.d("backStack 추가함")
            transaction.addToBackStack(null);
        }

        transaction.commit()
    }


}