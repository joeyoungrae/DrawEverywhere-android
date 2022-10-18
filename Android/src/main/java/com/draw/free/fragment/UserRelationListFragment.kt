package com.draw.free.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.draw.free.R
import com.draw.free.databinding.FragmentUsersRelationlistBinding
import com.draw.free.fragment.viewPagerFragment.RelationUserListFragment

import com.draw.free.viewmodel.UserRelationListViewModel
import com.draw.free.util.MyViewModelFactory

import com.google.android.material.tabs.TabLayoutMediator

import timber.log.Timber

class UserRelationListFragment : BaseInnerFragment<FragmentUsersRelationlistBinding>() {
    lateinit var type: UserRelationListViewModel.ListType
    var targetId = ""
    lateinit var viewModel: UserRelationListViewModel
    lateinit var relationAction: RelationAction

    interface RelationAction {
        fun openProfile(accountId: String)
    }

    init {
        layoutId = R.layout.fragment_users_relationlist
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = MyViewModelFactory(targetId).createUserRelationListModel(UserRelationListViewModel::class.java)
        viewModel.setInitial()
        viewModel.mOpenUserProfile = {
            relationAction.openProfile(it)
        }

        binding.tvAccountId.text = targetId
        Timber.d("targetId : $targetId")

        val adapter = RelationViewPagerAdapter(this)
        binding.viewPager.adapter = adapter
        //binding.viewPager.currentItem = 0


        // 탭 레이아웃과 뷰페이저 연결
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when (position) {
                0 -> {
                    tab.text = getString(R.string.follower)
                }
                1 -> {
                    tab.text = getString(R.string.following)
                }
            }
        }.attach()


        if (type == UserRelationListViewModel.ListType.Following) {
            binding.viewPager.post {
                binding.viewPager.setCurrentItem(1, false)
            }
        }
    }

    inner class RelationViewPagerAdapter(fragment: Fragment?) :
        FragmentStateAdapter(fragment!!) {
        override fun createFragment(position: Int): Fragment {
            val fragment =  when(position)
            {
                0 -> {
                    val r = RelationUserListFragment()
                    (r as RelationUserListFragment).isFollower = true
                    r
                }
                1-> RelationUserListFragment()
                else -> RelationUserListFragment()
            }

            (fragment as RelationUserListFragment).viewModel = viewModel

            return fragment
        }

        override fun getItemCount(): Int {
            return 2
        }
    }

    override fun onStart() {
        super.onStart()
        if(!isHidden) {
            setPreviousButton.set(true)
        }

    }
}
