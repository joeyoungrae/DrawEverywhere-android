package com.draw.free.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.draw.free.Global
import com.draw.free.R
import com.draw.free.databinding.FragmentProfileBinding
import com.draw.free.fragment.viewPagerFragment.ProfilePostListFragment
import com.draw.free.interfaceaction.IOpenPost
import com.draw.free.model.Post
import com.draw.free.setting.RestoreWalletByPhraseActivity
import com.draw.free.util.CustomList
import com.draw.free.viewmodel.ProfileFragmentViewModel
import com.draw.free.viewmodel.ProfileFragmentViewModel.FollowerType
import com.draw.free.viewmodel.UserRelationListViewModel
import com.google.android.material.tabs.TabLayoutMediator
import timber.log.Timber


class ProfileFragment : BaseInnerFragment<FragmentProfileBinding>() {

    lateinit var requestManager: RequestManager
    var isSetting = false

    init {
        layoutId = R.layout.fragment_profile
    }


    interface ProfileAction {
        fun edit()
        fun logout()
        fun openPost(postId: String, customPostList: CustomList<Post>)
        fun openRelation(targetId: String, type: UserRelationListViewModel.ListType)
        fun openSetting()
        fun openWallet(targetAddress: String)
    }

    lateinit var profileAction: ProfileAction
    var targetID: String = ""
    var targetAddress: String = ""
    var isStacked = false
    var canPrevious = true

    var openPost: IOpenPost = object : IOpenPost {
        override fun open(postId: String, customPostList: CustomList<Post>) {
            profileAction.openPost(postId, customPostList)
        }
    }

    private lateinit var viewModel: ProfileFragmentViewModel

    private var isMine = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isSetting || targetID.isEmpty()) {
            Timber.e("this")
            // Setting ??? ProfileFragment ?????? MyProfile ??? ????????? ?????? ?????? ?????? ???.
            binding.btnSetting.visibility = View.VISIBLE // ?????? ?????? ?????????
            isSetting = true
            isMine = true
        } else {
            binding.btnSetting.visibility = View.GONE
        }

        if (targetID.isEmpty() || isMine) {
            targetID = Global.userProfile!!.accountId
        }


        viewModel = ViewModelProvider(this)[ProfileFragmentViewModel::class.java]
        viewModel.lastTarget = targetID
        requestManager = Glide.with(this)

        if (viewModel.targetUserProfile.value == null || isMine) {
            viewModel.getUserProfile()
        } else if (viewModel.targetUserProfile.value != null) {
            viewModel.lastTarget = viewModel.targetUserProfile.value!!.accountId
        } else {
            viewModel.getUserProfile()
        }


        binding.btnEditProfile.setOnClickListener { // ????????? ?????? ??????
            if (viewModel.targetUserProfile.value == null) {
                return@setOnClickListener
            }
            profileAction.edit()
        }
        binding.btnFollow.setOnClickListener { // ?????????, ????????? ?????? ??????
            if (viewModel.targetUserProfile.value == null) {
                return@setOnClickListener
            }
            viewModel.followUser() // ?????? ????????? follower ??? ???????????? ?????? ?????? ????????? ?????????
        }

        binding.btnSetting.setOnClickListener { // ?????? ??????
            if (viewModel.targetUserProfile.value == null) {
                return@setOnClickListener
            }
            profileAction.openSetting()
        }
        binding.btnWallet.setOnClickListener {
            if (viewModel.targetUserProfile.value?.relation.equals("Mine") && (Global.prefs.walletSecretKeyCipher.isNullOrEmpty() || Global.prefs.walletPassword.isNullOrEmpty())) {
                startActivity(Intent(context, RestoreWalletByPhraseActivity::class.java))
                return@setOnClickListener
            } else {
                profileAction.openWallet(
                    viewModel.targetUserProfile.value?.walletAddress!!
                )
            }
        }



        viewModel.targetUserProfile.observe(viewLifecycleOwner) {
            binding.tvAccountId.text = it.accountId
            if (!it.pfName.isNullOrEmpty() || !it.pfDescription.equals("null")) {
                binding.tvPfName.text = it.pfName
            } else {
                binding.tvPfName.text = "????????? ??????????????????."
            }
            if (!it.pfDescription.isNullOrEmpty() || !it.pfDescription.equals("null")) {
                binding.tvPfDescription.text = it.pfDescription
            } else {
                binding.tvPfName.text = "?????? ????????? ??????????????????.."
            }

            if (it.pfPicture.isNullOrEmpty()) {
                requestManager
                    .load(R.drawable.pf_picture_default)
                    .circleCrop()
                    .into(binding.pvProfile.getContent())
            } else {
                requestManager
                    .load(it.pfPicture)
                    .placeholder(R.drawable.pf_picture_default)
                    .circleCrop()
                    .into(binding.pvProfile.getContent())
            }

            binding.tvCntFollower.text = it.numberOfFollowers.toString()
            binding.tvCntFollowing.text = it.numberOfFollowings.toString()

            if (it.relation != "Mine") {
                binding.btnFollow.visibility = View.VISIBLE // ????????? ?????? ?????????
                binding.btnEditProfile.visibility = View.INVISIBLE // ????????? ?????? ?????? ?????????
            }

            val btn = binding.btnFollow
            // ??? ????????? ??????
            when (it.relation) {
                "Mine" -> {
                    binding.btnFollow.visibility = View.INVISIBLE // ????????? ?????? ?????????
                    binding.btnEditProfile.visibility = View.VISIBLE // ????????? ?????? ?????? ?????????
                    viewModel.setCanShowPost(true)
                }
                "Follower" -> {
                    btn.text = getString(R.string.unfollow)
                    btn.backgroundTintList = ContextCompat.getColorStateList(
                        requireContext(),
                        R.color.solid_dark_gray
                    )
                    viewModel.setCanShowPost(true)

                }
                "Requested" -> {
                    btn.text = getString(R.string.unfollowRequest)
                    btn.backgroundTintList = ContextCompat.getColorStateList(
                        requireContext(),
                        R.color.solid_dark_gray
                    )
                    if (it.accountType != "private") {
                        viewModel.setCanShowPost(true)
                    } else {
                        viewModel.setCanShowPost(false)
                    }
                }
                "None" -> {
                    btn.text = getString(R.string.follow)
                    btn.backgroundTintList = null
                    if (it.accountType != "private") {
                        viewModel.setCanShowPost(true)
                    } else {
                        viewModel.setCanShowPost(false)
                    }
                }
            }
        }

        // ?????? ????????? ????????????
        viewModel.setCustomPostList()


        val adapter = ProfilePostViewPager(this)
        binding.viewPager.adapter = adapter
        binding.viewPager.currentItem = 0


        // ??? ??????????????? ???????????? ??????
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when (position) {
                0 -> {
                    tab.icon = resources.getDrawable(R.drawable.icon_grid, null)
                }
                1 -> {
                    tab.icon = resources.getDrawable(R.drawable.icon_heart, null)
                }
            }
        }.attach()


        viewModel.isSecretAccount.observe(viewLifecycleOwner) {
            if (!it) {
                Timber.e("?????????")

                binding.tvCntFollower.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.gray
                    )
                )
                binding.tvCntFollowing.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.gray
                    )
                )
            } else {
                binding.vTouchFollower.setOnClickListener { // ????????? ?????? ??????
                    profileAction.openRelation(
                        viewModel.lastTarget,
                        UserRelationListViewModel.ListType.Follow
                    )
                }
                binding.vTouchFollowing.setOnClickListener { // ????????? ?????? ??????
                    profileAction.openRelation(
                        viewModel.lastTarget,
                        UserRelationListViewModel.ListType.Following
                    )
                }
            }
        }

    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)

        if (!hidden) {
            showBottomNav.show(isShowed = true)
        }

        // ????????? ??????????????? ??????
        val prefs = Global.prefs.getPrefs()
        if (isMine && prefs != null && prefs.getBoolean("myProfileFragment_update", false)) {
            viewModel.writePosts?.refreshData()
            prefs.edit().remove("myProfileFragment_update").apply()
            Timber.e("????????? ???????????? ????????????")
        }
    }

    override fun onStart() {
        super.onStart()
        if (!isHidden) {
            Timber.d("?????? ?????? $canPrevious")
            setPreviousButton.set(isSet = canPrevious, isLight = false)
            //showBottomNav.show(!isStacked)
            showBottomNav.show(true)
        }

        // ????????? ??????????????? ??????
        val prefs = Global.prefs.getPrefs()
        if (isMine && prefs.getBoolean("myProfileFragment_update", false)) {
            viewModel.writePosts.refreshData()
            prefs.edit().remove("myProfileFragment_update").apply()
            Timber.e("????????? ???????????? ????????????")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.destroyViewModel()
    }


    // ???????????? ?????????
    inner class ProfilePostViewPager(fragment: Fragment?) :
        FragmentStateAdapter(fragment!!) {
        override fun createFragment(position: Int): Fragment {
            val fragment = when (position) {
                0 -> {
                    val p = ProfilePostListFragment()
                    (p as ProfilePostListFragment).type = ProfilePostListFragment.Type.Write
                    (p as ProfilePostListFragment).openPost = openPost
                    p
                }
                1 -> {
                    val p = ProfilePostListFragment()
                    (p as ProfilePostListFragment).type = ProfilePostListFragment.Type.Like
                    (p as ProfilePostListFragment).openPost = openPost
                    p
                }
                else -> PostListFragment()
            }
            return fragment
        }

        override fun getItemCount(): Int {
            return 2
        }
    }

}
