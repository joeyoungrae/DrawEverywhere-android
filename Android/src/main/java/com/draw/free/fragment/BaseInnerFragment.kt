package com.draw.free.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import com.draw.free.MainActivity
import com.draw.free.R
import com.draw.free.model.Auction
import com.draw.free.model.Nft
import com.draw.free.model.Post
import com.draw.free.util.CustomList
import com.draw.free.viewmodel.UserRelationListViewModel

open class BaseInnerFragment<T : ViewDataBinding> : Fragment() {

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (parentFragment is BaseContainerFragment) {
            showBottomNav = (parentFragment as BaseContainerFragment).showBottomNav
            setPreviousButton = (parentFragment as BaseContainerFragment).previousButton
        }
    }


    companion object {
        fun createNotifyFragment(): NotifyFragment {
            val temp = NotifyFragment()
            temp.layoutId = R.layout.fragment_notify
            return temp
        }

        fun createSettingPrivacyFragment(): SettingPrivacyFragment {
            val temp = SettingPrivacyFragment()
            temp.layoutId = R.layout.fragment_setting_privacy

            return temp
        }


        fun createHomeFragment(mainAction: HomeMainFragment.MainAction): HomeMainFragment {
            val temp = HomeMainFragment.getInstance()
            temp.mainAction = mainAction

            return temp
        }

        fun createNftDetailFragment(
            nftDetailAction: NftDetailFragment.NftDetailAction,
            nft: Nft
        ): NftDetailFragment {
            val temp = NftDetailFragment()
            temp.layoutId = R.layout.fragment_nft_detail
            temp.nftDetailAction = nftDetailAction
            temp.nft = nft

            return temp
        }

        fun createAuctionDetailFragment(
            auctionDetailAction: AuctionDetailFragment.AuctionDetailAction,
            mint: String,
            auction: Auction?
        ): AuctionDetailFragment {
            val temp = AuctionDetailFragment()
            temp.layoutId = R.layout.fragment_auction_detail
            temp.auctionDetailAction = auctionDetailAction
            temp.mint = mint
            temp.auction = auction

            return temp
        }


        fun createNftFragment(mainAction: NftMainFragment.MainAction): NftMainFragment {
            val temp = NftMainFragment.getInstance()
            temp.layoutId = R.layout.fragment_nft
            temp.mainAction = mainAction

            return temp
        }

        fun createSearchFragment(searchAction: SearchFragment.SearchAction): SearchFragment {
            val temp = SearchFragment()
            temp.layoutId = R.layout.fragment_search
            temp.searchAction = searchAction

            return temp
        }

        fun createPostDetailFragment(
            postDetailAction: PostDetailFragment.PostDetailAction,
            postId: String,
            postList: CustomList<Post>
        ): PostDetailFragment {
            val temp = PostDetailFragment()
            temp.customPostList = postList
            temp.targetPostId = postId
            temp.postDetailAction = postDetailAction

            return temp
        }

        fun createProfileFragment(
            profileAction: ProfileFragment.ProfileAction,
            userId: String,
            isStacked: Boolean = false,
            wallet: String? = null
        ): ProfileFragment {
            val temp = ProfileFragment()
            temp.targetID = userId
            temp.targetAddress = wallet ?: ""
            temp.isStacked = isStacked
            temp.profileAction = profileAction

            return temp
        }

        fun createProfileEditFragment(profileEditAction: ProfileEditFragment.ProfileEditAction): ProfileEditFragment {
            val temp = ProfileEditFragment.getInstance()
            temp.layoutId = R.layout.fragment_profile_edit
            temp.profileEditAction = profileEditAction

            return temp
        }

        fun createUserRelationListFragment(
            relationAction: UserRelationListFragment.RelationAction,
            type: UserRelationListViewModel.ListType,
            userId: String
        ): UserRelationListFragment {
            val temp = UserRelationListFragment()
            temp.targetId = userId
            temp.type = type
            temp.relationAction = relationAction

            return temp
        }

        fun createPostListByTagFragment(
            action: PostListFragment.PostListAction,
            hashTag: String
        ): PostListFragment {
            val temp = PostListFragment()
            temp.layoutId = R.layout.fragment_post_list
            temp.postAction = action
            temp.hashTag = hashTag

            return temp
        }

        fun createLoginFragment(afterLogin: LoginFragment.LoginFragmentAction): LoginFragment {
            val temp = LoginFragment()
            temp.layoutId = R.layout.activity_login
            temp.afterLogin = afterLogin

            return temp
        }

        fun createSettingFragment(settingAction: SettingFragment.SettingAction): SettingFragment {
            val temp = SettingFragment()
            temp.layoutId = R.layout.activity_setting
            temp.settingAction = settingAction

            return temp
        }

        fun createWalletFragment(
            walletAction: WalletFragment.WalletAction,
            address: String
        ): WalletFragment {
            val temp = WalletFragment()
            temp.targetAddress = address
            temp.layoutId = R.layout.fragment_wallet
            temp.walletAction = walletAction
            return temp
        }


    }

    var layoutId = -1
    lateinit var showBottomNav: MainActivity.ShowBottomNav // 호출자에서 구현
    lateinit var setPreviousButton: BaseContainerFragment.SetPreviousButton

    private var _binding: T? = null
    val binding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
         assert(layoutId != -1) { "Inner Fragment 의 레이아웃을 지정해주지 않음." }
        _binding = DataBindingUtil.inflate<T>(inflater, layoutId, container, false)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}