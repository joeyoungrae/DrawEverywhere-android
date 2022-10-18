package com.draw.free

import android.content.Context
import android.view.*
import androidx.annotation.CallSuper
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.target.Target
import com.draw.free.databinding.ItemGridThumbnailPostBinding
import com.draw.free.databinding.ItemNftBinding
import com.draw.free.fragment.HomeMainFragment
import com.draw.free.fragment.WalletFragment
import com.draw.free.model.Nft
import com.draw.free.util.CustomList

class NftAdapter(var context: Context? = null, val data: CustomList<Nft>, var type: ShowType, val requestManager: RequestManager, val toOpenNft : Any?) : RecyclerView.Adapter<NftAdapter.NftVH>() {
    var mData: List<Nft> = data.getData()

    enum class ShowType {
        RECENTLY_MINTED, OWNED_NFT, MINTED_NFT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NftVH {
        when (type) {
            ShowType.RECENTLY_MINTED -> {
                return RecentlyMintedNftVH(
                    DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_nft, parent, false)
                )
            }
            ShowType.OWNED_NFT -> {
                return WalletNftVH(
                    DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_grid_thumbnail_post, parent, false)
                )
            }
            ShowType.MINTED_NFT -> {
                return WalletNftVH(
                    DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_grid_thumbnail_post, parent, false)
                )
            }
        }
    }

    override fun onBindViewHolder(holder: NftVH, position: Int) {
        holder.bind(mData[position], position)

        // 다음 썸네일 이미지 5개 미리 로드해두기
        if (position <= mData.size) {
            val endPosition = if (position + 5 > mData.size) {
                mData.size
            } else {
                position + 5
            }
            mData.subList(position, endPosition).map { it.thumbnail }.forEach {
                preload(it)
            }
        }
    }

    override fun getItemCount(): Int {
        return mData.size
    }


    abstract inner class NftVH(view: View) : RecyclerView.ViewHolder(view) {
        private lateinit var nft: Nft

        @CallSuper
        open fun bind(nft: Nft, position: Int) {
            this.nft = nft
        }
    }


    inner class RecentlyMintedNftVH(private val binding: ItemNftBinding) : NftVH(binding.root) {
        override fun bind(nft: Nft, position: Int) {
            super.bind(nft, position)
            itemView.setOnClickListener {
                // nft 상세보기로 이동
                (toOpenNft!! as HomeMainFragment.MainAction).openNft(nft)
            }
            // NFT 썸네일
            requestManager.load(nft.thumbnail).error(R.drawable.icon_error).into(binding.thumbnail)
            binding.tvTitle.text = nft.data.name
            binding.tvSymbol.text = nft.data.symbol
        }
    }

    inner class WalletNftVH(private val binding: ItemGridThumbnailPostBinding) : NftVH(binding.root) {
        override fun bind(nft: Nft, position: Int) {
            super.bind(nft, position)
            itemView.setOnClickListener {
                (toOpenNft!! as WalletFragment.WalletAction).openNft(nft)
            }
            // NFT 썸네일
            requestManager.load(nft.thumbnail).error(R.drawable.icon_error).into(binding.thumbnail)
        }
    }



    private fun preload(imageUrl: String) {
        // 글라이더 이미지 미리 로드
        requestManager.load(imageUrl).preload(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
    }
}