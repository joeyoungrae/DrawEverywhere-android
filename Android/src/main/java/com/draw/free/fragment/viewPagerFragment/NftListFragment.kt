package com.draw.free.fragment.viewPagerFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.draw.free.viewmodel.viewPagerFragmentViewModel.WalletListFragmentViewModel.Type
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.draw.free.Global
import com.draw.free.NftAdapter
import com.draw.free.databinding.FragmentNftListBinding
import com.draw.free.fragment.WalletFragment
import com.draw.free.model.Nft
import com.draw.free.util.CustomList
import com.draw.free.viewmodel.viewPagerFragmentViewModel.WalletListFragmentViewModel
import timber.log.Timber

class NftListFragment : Fragment() {
    lateinit var action: WalletFragment.WalletAction

    lateinit var targetAddress: String
    lateinit var type: Type
    lateinit var viewModel: WalletListFragmentViewModel


    private var _binding: FragmentNftListBinding? = null
    val binding
        get() = _binding!!
    private lateinit var requestManager: RequestManager
    private lateinit var adapter: NftAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(requireParentFragment())[WalletListFragmentViewModel::class.java]
        viewModel.targetAddress = targetAddress

        // 데이터 가져오기
        when (type) {
            Type.OWNED_NFT -> {
                viewModel.setOwnedNftList()
                viewModel.customOwnedNftList.getNextData()
            }
            Type.MINTED_NFT -> {
                viewModel.setMintedNftList()
                viewModel.customMintedNftList.getNextData()
            }
            else -> {
                viewModel.setOwnedNftList()
                viewModel.customOwnedNftList.getNextData()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // 뷰 바인딩
        _binding = FragmentNftListBinding.inflate(inflater, container, false)

        // 글라이더 requestManager
        requestManager = Glide.with(this)

        // 레이아웃 매니저 설정 및 어댑터 생성
        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        adapter = when (type) {
            Type.OWNED_NFT -> {
                NftAdapter(null, viewModel.customOwnedNftList, NftAdapter.ShowType.OWNED_NFT, requestManager, action)
            }
            Type.MINTED_NFT -> {
                NftAdapter(null, viewModel.customMintedNftList, NftAdapter.ShowType.MINTED_NFT, requestManager, action)
            }
            else -> {
                NftAdapter(null, viewModel.customOwnedNftList, NftAdapter.ShowType.OWNED_NFT, requestManager, action)
            }
        }

        // 어댑터 연결
        binding.recyclerView.adapter = adapter

        // 아이템 없는 경우에 대한 옵저버 연결
        val customList : CustomList<Nft> by lazy {
            when (type) {
                Type.MINTED_NFT -> {
                    return@lazy viewModel.customMintedNftList
                }
                else -> {
                    return@lazy viewModel.customOwnedNftList
                }
            }
        }

        customList.mLiveData.observe(viewLifecycleOwner) {
            if (it.isEmpty()) {
                binding.txtZeroNft.visibility = View.VISIBLE
                binding.swipeLayout.isEnabled = false
            }
            else {
                binding.txtZeroNft.visibility = View.INVISIBLE

                binding.swipeLayout.setOnRefreshListener {
                    adapter.data.refreshData()
                    binding.swipeLayout.isRefreshing = false
                }
            }
            adapter.notifyDataSetChanged()
        }

        // 스크롤에 따라 다음 데이터 가져오도록 설정
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                // 마지막 스크롤된 항목 위치
                val lastVisibleItemPosition = (recyclerView.layoutManager as GridLayoutManager).findLastCompletelyVisibleItemPosition()
                // 항목 전체 개수
                val itemTotalCount = recyclerView.adapter!!.itemCount - 2
                if (lastVisibleItemPosition >= itemTotalCount) {
                    viewModel.customOwnedNftList.getNextData()
                }
            }
        })

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        val prefs = Global.prefs.getPrefs()
        if (prefs != null && prefs.getBoolean("nftListFragment_update", false)) {
            prefs.edit().remove("nftListFragment_update").apply()
            adapter.data.refreshData()

            Timber.e("nft 변경 사항 있어서 새로고침")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}