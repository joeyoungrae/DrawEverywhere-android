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
            // Setting 은 ProfileFragment 에서 MyProfile 로 열었을 때만 접근 가능 함.
            binding.btnSetting.visibility = View.VISIBLE // 설정 버튼 보이기
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


        binding.btnEditProfile.setOnClickListener { // 프로필 수정 버튼
            if (viewModel.targetUserProfile.value == null) {
                return@setOnClickListener
            }
            profileAction.edit()
        }
        binding.btnFollow.setOnClickListener { // 팔로우, 팔로우 취소 버튼
            if (viewModel.targetUserProfile.value == null) {
                return@setOnClickListener
            }
            viewModel.followUser() // 다른 유저와 follower 를 신청하고 그에 따른 결과를 반영함
        }

        binding.btnSetting.setOnClickListener { // 세팅 화면
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
                binding.tvPfName.text = "이름을 설정해주세요."
            }
            if (!it.pfDescription.isNullOrEmpty() || !it.pfDescription.equals("null")) {
                binding.tvPfDescription.text = it.pfDescription
            } else {
                binding.tvPfName.text = "자기 소개를 설정해주세요.."
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
                binding.btnFollow.visibility = View.VISIBLE // 팔로우 버튼 보이기
                binding.btnEditProfile.visibility = View.INVISIBLE // 프로필 수정 버튼 숨기기
            }

            val btn = binding.btnFollow
            // 나 자신인 경우
            when (it.relation) {
                "Mine" -> {
                    binding.btnFollow.visibility = View.INVISIBLE // 팔로우 버튼 숨기기
                    binding.btnEditProfile.visibility = View.VISIBLE // 프로필 수정 버튼 보이기
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

        // 최초 데이터 가져오기
        viewModel.setCustomPostList()


        val adapter = ProfilePostViewPager(this)
        binding.viewPager.adapter = adapter
        binding.viewPager.currentItem = 0


        // 탭 레이아웃과 뷰페이저 연결
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
                Timber.e("도착함")

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
                binding.vTouchFollower.setOnClickListener { // 팔로워 목록 보기
                    profileAction.openRelation(
                        viewModel.lastTarget,
                        UserRelationListViewModel.ListType.Follow
                    )
                }
                binding.vTouchFollowing.setOnClickListener { // 팔로잉 목록 보기
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

        // 쉐어드 프리퍼런스 확인
        val prefs = Global.prefs.getPrefs()
        if (isMine && prefs != null && prefs.getBoolean("myProfileFragment_update", false)) {
            viewModel.writePosts?.refreshData()
            prefs.edit().remove("myProfileFragment_update").apply()
            Timber.e("포스트 추가되서 새로고침")
        }
    }

    override fun onStart() {
        super.onStart()
        if (!isHidden) {
            Timber.d("스택 여부 $canPrevious")
            setPreviousButton.set(isSet = canPrevious, isLight = false)
            //showBottomNav.show(!isStacked)
            showBottomNav.show(true)
        }

        // 쉐어드 프리퍼런스 확인
        val prefs = Global.prefs.getPrefs()
        if (isMine && prefs.getBoolean("myProfileFragment_update", false)) {
            viewModel.writePosts.refreshData()
            prefs.edit().remove("myProfileFragment_update").apply()
            Timber.e("포스트 추가되서 새로고침")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.destroyViewModel()
    }


    // 뷰페이저 어댑터
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
