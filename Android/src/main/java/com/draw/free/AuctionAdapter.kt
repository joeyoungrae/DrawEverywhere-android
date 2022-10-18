package com.draw.free

import android.annotation.SuppressLint
import android.view.*
import androidx.annotation.CallSuper
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.target.Target
import com.draw.free.databinding.ItemAuctionBinding
import com.draw.free.databinding.ItemMyAuctionBinding
import com.draw.free.fragment.NftMainFragment
import com.draw.free.fragment.WalletFragment
import com.draw.free.model.Auction
import com.draw.free.util.CustomList
import java.text.Format
import java.text.SimpleDateFormat

class AuctionAdapter(val data: CustomList<Auction>, var type: ShowType, val requestManager: RequestManager, val toOpenAuction : Any?) : RecyclerView.Adapter<AuctionAdapter.AuctionVH>() {
    var mData: List<Auction> = data.getData()

    @SuppressLint("SimpleDateFormat")
    val format: Format = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")

    enum class ShowType {
        WALLET_AUCTION, NFT_TAB
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AuctionVH {
        return when (type) {
            ShowType.WALLET_AUCTION -> {
                WalletAuctionVH(
                    DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_my_auction, parent, false)
                )
            }
            ShowType.NFT_TAB -> {
                NftTabAuctionVH(
                    DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_auction, parent, false)
                )
            }
        }
    }

    override fun onBindViewHolder(holder: AuctionVH, position: Int) {
        holder.bind(mData[position])

        // 다음 gif 썸네일 이미지 5개 미리 로드해두기
        if (position <= mData.size) {
            val endPosition = if (position + 5 > mData.size) {
                mData.size
            } else {
                position + 5
            }
            mData.subList(position, endPosition).map { it.nft.thumbnail }.forEach {
                preload(it)
            }
        }
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    abstract inner class AuctionVH(view: View) : RecyclerView.ViewHolder(view) {
        private lateinit var auction: Auction

        @CallSuper
        open fun bind(auction: Auction) {
            this.auction = auction
        }
    }

    inner class WalletAuctionVH(private val binding: ItemMyAuctionBinding) : AuctionVH(binding.root) {
        override fun bind(auction: Auction) {
            super.bind(auction)
            // 클릭 이벤트
            itemView.setOnClickListener {
                // 상세보기 화면으로 이동
                (toOpenAuction!! as WalletFragment.WalletAction).openAuction(auction)
            }
            // 상태 마크 숨기기
            binding.onSale.visibility = View.GONE
            binding.endedSale.visibility = View.GONE
            binding.sold.visibility = View.GONE
            binding.notSettled.visibility = View.GONE
            binding.settled.visibility = View.GONE

            // 썸네일
            requestManager.load(auction.nft.thumbnail).error(R.drawable.icon_error).into(binding.thumbnail)
            // nft 타이틀
            binding.txtTitle.text = auction.nft.data.name
            // nft 심볼
            binding.txtSymbol.text = auction.nft.data.symbol
            // nft 가격
            binding.txtPrice.text = String.format("%.9f", auction.price)
            // 판매 등록 날짜
            binding.txtCreatedAt.text = format.format(auction.createdAt)
            // 상태 마크 보이기
            if (auction.state == "started") {
                binding.onSale.visibility = View.VISIBLE // 판매 중
            }
            if (auction.state == "ended" && auction.buyer == null) {
                binding.endedSale.visibility = View.VISIBLE // 본인이 판매 종료한 경우
            }
            if (auction.state == "ended" && auction.buyer != null) {
                binding.sold.visibility = View.VISIBLE // 다른 유저가 구매한 경우
                if (auction.isSettled) {
                    binding.settled.visibility = View.VISIBLE // 정산 완료
                } else {
                    binding.notSettled.visibility = View.VISIBLE // 정산 미완료
                }
            }
        }
    }

    inner class NftTabAuctionVH(private val binding: ItemAuctionBinding) : AuctionVH(binding.root) {
        override fun bind(auction: Auction) {
            super.bind(auction)
            // 클릭 이벤트
            itemView.setOnClickListener {
                // auction 상세보기 화면으로 이동
                (toOpenAuction!! as NftMainFragment.MainAction).openAuction(auction)
            }
            // 썸네일
            requestManager.load(auction.nft.thumbnail).error(R.drawable.icon_error).into(binding.thumbnail)
            // nft 타이틀
            binding.tvTitle.text = auction.nft.data.name
            // nft 심볼
            binding.tvSymbol.text = auction.nft.data.symbol
            // 판매 가격
            binding.tvPrice.text = String.format("%.9f", auction.price)
        }
    }


    private fun preload(imageUrl: String) {
        // 글라이더 이미지 미리 로드
        requestManager.load(imageUrl).preload(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
    }
}