package com.draw.free.fragment

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.draw.free.Global
import com.draw.free.MainActivity
import com.draw.free.R
import com.draw.free.databinding.FragmentFragmentContainerBinding
import com.draw.free.model.Auction
import timber.log.Timber

import com.draw.free.model.Nft
import com.draw.free.model.Post
import com.draw.free.util.CustomList
import com.draw.free.viewmodel.UserRelationListViewModel


enum class FragmentType(val tagName: String, val tagValue: Int) {
    Home("Home", 1),
    NFT("NFT", 2),
    Search("Search", 3),
    Profile("Profile", 4)
}

enum class OrderType {
    Pop, PostDetail, Profile, Wallet, RelationUserListFollowing,
    RelationUserListFollow, PostListByTag, Search, MyProfile, EditProfile, Setting,
    HomeMain, NftMain, NftDetail, AuctionDetail, SettingPrivacy, Notify

}


abstract class BaseContainerFragment : Fragment() {

    override fun onAttach(context: Context) {
        super.onAttach(context)
        showBottomNav = (context as MainActivity).showBottomNav
    }

    //<editor-fold desc="Action 관련">
    private val homeMainAction = object : HomeMainFragment.MainAction {
        override fun openPost(postId: String, customPostList: CustomList<Post>) {
            action(OrderType.PostDetail, postId, customPostList)
        }

        override fun openNft(nft: Nft) {
            action(OrderType.NftDetail, nft = nft)
        }

        override fun openNotify() {
            action(OrderType.Notify)
        }

        override fun openProfile(accountId: String) {
            action(OrderType.Profile, accountId)
        }
    }

    private val postDetailAction = object : PostDetailFragment.PostDetailAction {
        override fun openProfile(accountId: String) {
            action(OrderType.Profile, accountId)
        }

        override fun pop() {
            action(OrderType.Pop)
        }

        override fun openNft(nft: Nft) {
            Timber.d("NFT 열기")
            action(OrderType.NftDetail, nft = nft)
        }
    }

    private val profileAction = object : ProfileFragment.ProfileAction {
        override fun edit() {
            action(OrderType.EditProfile)
        }

        override fun logout() {

            Timber.d("로그아웃 처리")
            action(OrderType.Pop)
        }

        override fun openPost(postId: String, customPostList: CustomList<Post>) {
            Timber.d("포스트 열기")
            action(OrderType.PostDetail, postId, customPostList)
        }

        override fun openRelation(targetId: String, type: UserRelationListViewModel.ListType) {
            Timber.d("open__ $targetId")

            if (type == UserRelationListViewModel.ListType.Follow) {
                action(OrderType.RelationUserListFollow, targetId)
            } else if (type == UserRelationListViewModel.ListType.Following) {
                action(OrderType.RelationUserListFollowing, targetId)
            }
        }

        override fun openSetting() {
            action(OrderType.Setting)
        }

        override fun openWallet(targetAddress: String) {
            action(OrderType.Wallet, targetAddress)
        }
    }

    private val relationAction = object : UserRelationListFragment.RelationAction {
        override fun openProfile(accountId: String) {
            action(OrderType.Profile, accountId)
        }
    }

    private val profileEditAction = object : ProfileEditFragment.ProfileEditAction {
        override fun close() {
            action(OrderType.Pop)
        }
    }

    private val settingAction = object : SettingFragment.SettingAction {
        override fun logout() {
            while (childFragmentManager.backStackEntryCount != 0) {
                childFragmentManager.popBackStackImmediate()
            }

            Global.clear()
            Global.userProfile = null
            Global.googleSignUp.mGoogleSignInClient.signOut()


            action(OrderType.MyProfile)
        }

        override fun settingPrivacy() {
            action(OrderType.SettingPrivacy)
        }
    }

    private val walletAction = object : WalletFragment.WalletAction {

        override fun openProfile(accountId: String) {
            Timber.d("프로필 열기")
            action(OrderType.Profile, accountId)
        }

        override fun openNft(nft: Nft) {
            Timber.d("NFT 열기")
            action(OrderType.NftDetail, nft = nft)
        }

        override fun openAuction(auction: Auction) {
            action(OrderType.AuctionDetail, auction.nft.mint, auction = auction)
        }

    }

    private val nftMainAction = object : NftMainFragment.MainAction {
        override fun openAuction(auction: Auction) {
            Timber.d("옥션 열기")
            action(OrderType.AuctionDetail, auction.nft.mint, auction = auction)
        }
    }

    private val searchAction = object : SearchFragment.SearchAction {
        override fun openPost(postId: String, customPostList: CustomList<Post>) {
            action(OrderType.PostDetail, postId, customPostList)
        }

        override fun openTag(hashtag: String) {
            action(OrderType.PostListByTag, hashtag)
        }

        override fun openProfile(accountId: String) {
            action(OrderType.Profile, accountId)
        }
    }

    private val postListAction = object : PostListFragment.PostListAction {
        override fun openPost(postId: String, customPostList: CustomList<Post>) {
            action(OrderType.PostDetail, postId, customPostList)
        }
    }

    private val nftDetailAction = object : NftDetailFragment.NftDetailAction {
        override fun openWalletFragment(targetWalletAddress: String) {
            action(OrderType.Wallet, targetWalletAddress)
        }

        override fun openAuctionDetail(mint: String) {
            Timber.d("옥션 열기")
            action(OrderType.AuctionDetail, mint, auction = null)
        }

        override fun pop() {
            action(OrderType.Pop)
        }

    }


    private val auctionDetailAction = object : AuctionDetailFragment.AuctionDetailAction {
        override fun openWallet(targetWalletAddress: String) {
            action(OrderType.Wallet, targetWalletAddress)
        }

        override fun pop() {
            action(OrderType.Pop)
        }

    }

    //</editor-fold>

    private fun containerPop() {
        childFragmentManager.popBackStackImmediate()
    }


    fun action(
        orderType: OrderType,
        extraValue: String = "",
        customPostList: CustomList<Post>? = null,
        wallet: String? = null,
        nft: Nft? = null,
        auction: Auction? = null
    ) {
        if (orderType == OrderType.Pop) {
            containerPop()
            return
        }

        changeFragment(orderType, extraValue, customPostList, wallet, nft, auction)
    }


    interface SetPreviousButton {
        fun set(isSet: Boolean, isLight: Boolean = false)
    }

    private fun changeFragment(
        orderType: OrderType,
        extraValue: String,
        postData: CustomList<Post>?,
        wallet: String?,
        nft: Nft?,
        auction: Auction?
    ) {
        lateinit var fragment: Fragment
        lateinit var type: String

        when (orderType) {
            OrderType.SettingPrivacy -> {
                fragment = BaseInnerFragment.createSettingPrivacyFragment()
                type = "SettingPrivacy"
            }
            OrderType.Notify -> {
                fragment = BaseInnerFragment.createNotifyFragment()
                type = "Notify"
            }
            OrderType.HomeMain -> {
                fragment = BaseInnerFragment.createHomeFragment(homeMainAction)
                type = "HomeMain"
            }
            OrderType.PostDetail -> {
                assert(extraValue.isNotEmpty()) { " 대상이 되는 postId가 필요합니다. " }
                fragment = BaseInnerFragment.createPostDetailFragment(
                    postDetailAction,
                    extraValue,
                    postData!!
                )
                type = "PostDetail"
            }
            OrderType.Profile -> {
                assert(extraValue.isNotEmpty()) { "대상이 되는 profileId가 필요합니다 " }
                fragment =
                    BaseInnerFragment.createProfileFragment(profileAction, extraValue, true, wallet)
                type = "profile"
            }
            OrderType.RelationUserListFollow -> {
                fragment = BaseInnerFragment.createUserRelationListFragment(
                    relationAction,
                    UserRelationListViewModel.ListType.Follow,
                    extraValue
                )
                type = "relation"
            }
            OrderType.RelationUserListFollowing -> {
                fragment = BaseInnerFragment.createUserRelationListFragment(
                    relationAction,
                    UserRelationListViewModel.ListType.Following,
                    extraValue
                )
                type = "relation"
            }
            OrderType.Wallet -> {
                fragment = BaseInnerFragment.createWalletFragment(walletAction, extraValue)
                type = "wallet"

            }
            OrderType.Search -> {
                fragment = BaseInnerFragment.createSearchFragment(searchAction)
                type = "Search"
            }
            OrderType.PostListByTag -> {
                fragment = BaseInnerFragment.createPostListByTagFragment(postListAction, extraValue)
                type = "PostByTag"
            }
            OrderType.MyProfile -> {
                if (Global.userProfile != null) {
                    fragment = BaseInnerFragment.createProfileFragment(profileAction, "", false, "")
                    fragment.canPrevious = false
                    type = "myProfile"
                } else {
                    val afterLogin = object : LoginFragment.LoginFragmentAction {
                        override fun next() {
                            action(OrderType.MyProfile)
                            Timber.e("afterLogin")
                        }
                    }
                    fragment = BaseInnerFragment.createLoginFragment(afterLogin)
                    type = "login"
                }
            }
            OrderType.EditProfile -> {
                fragment = BaseInnerFragment.createProfileEditFragment(profileEditAction)
                type = "profileEdit"
            }

            OrderType.Setting -> {
                fragment = BaseInnerFragment.createSettingFragment(settingAction)
                type = "setting"
            }
            OrderType.NftMain -> {
                fragment = BaseInnerFragment.createNftFragment(nftMainAction)
                type = "NftMain"
            }
            OrderType.NftDetail -> {
                assert(nft != null) { " 대상이 되는 nft가 필요합니다. " }
                fragment = BaseInnerFragment.createNftDetailFragment(
                    nftDetailAction,
                    nft!!
                )
                type = "NftDetail"
            }
            OrderType.AuctionDetail -> {
                fragment = if (auction != null) {
                    BaseInnerFragment.createAuctionDetailFragment(
                        auctionDetailAction,
                        extraValue,
                        auction
                    )
                } else {
                    BaseInnerFragment.createAuctionDetailFragment(
                        auctionDetailAction,
                        extraValue,
                        null
                    )
                }
                type = "AuctionDetail"
            }

            else -> {}
        }


        // Type 중복시 생성 안되게 (나중에)
        // Commit 으로 중복 fragment 삭제 (나중에)

        val transaction = childFragmentManager.beginTransaction();
        transaction.replace(R.id.lazy_fragment_binding_view, fragment, type);

        val fragments = childFragmentManager.fragments;

        if (fragments.isNotEmpty() && type != "myProfile" && type != "login") {
            Timber.d("backStack 추가함")
            transaction.addToBackStack(null);
        } else {
            Timber.d("Type : $type")
        }

        transaction.commit()
    }


    companion object {
        fun createHomeFragment(): HomeContainer {
            return HomeContainer()
        }

        fun createNftFragment(): NftContainer {
            return NftContainer()
        }

        fun createSearchFragment(): SearchContainer {
            return SearchContainer()
        }

        fun createProfileFragment(): ProfileContainer {
            return ProfileContainer()
        }


    }

    lateinit var showBottomNav: MainActivity.ShowBottomNav // 호출자에서 구현

    private var _binding: FragmentFragmentContainerBinding? = null
    private val binding
        get() = _binding!!

    private lateinit var popButton: ImageView

    val previousButton = object : SetPreviousButton {
        override fun set(isSet: Boolean, isLight: Boolean) {
            popButton.bringToFront()

            if (_binding == null) {
                return
            }

            if (isSet) {
                popButton.visibility = View.VISIBLE
            } else {
                popButton.visibility = View.GONE
            }

            if (isLight) {
                binding.popButton.imageTintList = ColorStateList.valueOf(Color.WHITE)
            } else {
                binding.popButton.imageTintList = ColorStateList.valueOf(Color.BLACK)
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        assert(showBottomNav != null) { "필수 파라미터 부재, showBottomNav" }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFragmentContainerBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        popButton = binding.popButton

        popButton.setOnClickListener {
            containerPop()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}

