package com.draw.free

import android.os.Handler
import android.os.Looper
import android.view.*
import android.view.View.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.target.Target
import com.draw.free.customView.ProfileView
import com.draw.free.dialog.CommentBottomFragmentDialog
import com.draw.free.interfaceaction.IOpenProfile
import com.draw.free.model.Nft
import com.draw.free.model.Post
import com.draw.free.util.CustomList
import com.draw.free.viewmodel.PostDetailFragmentViewModel
import com.google.android.exoplayer2.ui.StyledPlayerView

class PostVideoAdapter(
    val data: CustomList<Post>,
    val vm: PostDetailFragmentViewModel,
    val fm: FragmentManager,
    val requestManager: RequestManager
) : RecyclerView.Adapter<PostVideoAdapter.VpVHPostVideo>() {
    var mData: List<Post> = data.getData()
    lateinit var openNft: ((postId: String) -> Unit)

    val handler = Handler(Looper.getMainLooper())

    companion object {
        var openProfile: IOpenProfile? = null
    }

    fun updateItem(newData: List<Post>) {
        mData = newData
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VpVHPostVideo {
        // parent: 뷰페이저
        return VpVHPostVideo(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.unused_item_post_detail_video, parent, false)
        )
    }

    override fun onBindViewHolder(holder: VpVHPostVideo, position: Int) {
        holder.bind(mData[position])
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    inner class VpVHPostVideo(view: View) : RecyclerView.ViewHolder(view) {
        var parent: View = view

        var playerView: StyledPlayerView = view.findViewById(R.id.playerView)
        var volumeControl: ImageView = view.findViewById(R.id.volume_control)
        var thumbnail: ImageView = view.findViewById(R.id.iv_thumbnail)

        var title: TextView = view.findViewById(R.id.tv_title)
        var profile: ProfileView = view.findViewById(R.id.pv_profile)
        var producerAccountId: TextView = view.findViewById(R.id.tv_account_id)
        var content: TextView = view.findViewById(R.id.tv_content)
        var place: TextView = view.findViewById(R.id.tv_place)

        var btnSetDrawing: LinearLayout = view.findViewById(R.id.btn_place)
        var cntLikes: TextView = view.findViewById(R.id.cnt_likes)
        var cntComments: TextView = view.findViewById(R.id.cnt_comments)
        var btnLike: LinearLayout = view.findViewById(R.id.btn_like)
        var btnLikeImage: ImageView = view.findViewById(R.id.btn_like_iv)
        var btnComment: LinearLayout = view.findViewById(R.id.btn_comment)
        var btnMore: ImageView = view.findViewById(R.id.btn_more)
        var nftMark: ImageView = view.findViewById(R.id.nft_mark)
        var originalMark: LinearLayout = view.findViewById(R.id.original_mark)
        var btnGoToMint: LinearLayout = view.findViewById(R.id.btn_go_to_mint)
        var position : LinearLayout = view.findViewById(R.id.btn_position)

        var status = ""

        fun bind(item: Post?) {
            if (item == null) {
                return
            }

            if (item.status == "freezed") {
                return
            }

            // 비디오 썸네일
            requestManager.load(item.thumbnail).into(thumbnail)
            thumbnail.visibility = VISIBLE

            // 텍스트 데이터 입력
            title.text = item.title
            content.text = item.content
            producerAccountId.text = item.producer.producerAccountID
            if (item.place != null) {
                place.text = item.place
                place.visibility = VISIBLE
            } else {
                place.visibility = INVISIBLE
            }
            cntLikes.text = "${item.likes}"
            cntComments.text = "${item.comments}"

            // nft 발행 여부 표시
            if (item.isMinted) {
                nftMark.visibility = VISIBLE
            } else {
                nftMark.visibility = GONE
            }

            // 오리지널 포스트 표시
            if (item.isOriginal) {
                originalMark.visibility = VISIBLE
                position.visibility = VISIBLE
            } else {
                originalMark.visibility = GONE
                position.visibility = INVISIBLE
            }

            // TODO 바꾸기
              position.setOnClickListener {
                vm.startPositionActivity(item)
            }

            // 좋아요 색깔 변경
            if (Global.userProfile != null) {
                btnLikeImage.isActivated = item.myLike
            } else {
                btnLikeImage.isActivated = false
            }

            // 본인의 포스트이고, 직접 그린 그림인데, NFT 발행이 안된 경우
            if (item.isMine && !item.isMinted && item.isOriginal) {
                btnGoToMint.visibility = VISIBLE
            } else {
                btnGoToMint.visibility = GONE
            }

            // 작성자 프로필 사진
            if (item.producer.producerPfPicture.isNullOrEmpty()) {
                requestManager
                    .load(R.drawable.pf_picture_default)
                    .circleCrop()
                    .into(profile.getContent())
            } else {
                requestManager
                    .load(item.producer.producerPfPicture)
                    .placeholder(R.drawable.pf_picture_default)
                    .circleCrop()
                    .into(profile.getContent())
            }

            // 포스트 내용 클릭
            content.setOnClickListener {
                if (content.maxLines == 3) {
                    // 포스트 내용 전체 보이기
                    content.maxLines = 100
                } else {
                    // 포스트 내용 숨기기
                    content.maxLines = 3
                }
            }
            // 좋아요 버튼 클릭
            btnLike.setOnClickListener {
                vm.likePost(item.id, cntLikes, btnLikeImage, data)
            }
            // 댓글 버튼 클릭
            btnComment.setOnClickListener {
                val f = CommentBottomFragmentDialog()
                f.passValue = {
                    val p = data.getPostByKey(f.postId!!)
                    if (p != null) {
                        p.comments = it

                        handler.post {
                            cntComments.text = "$it"
                        }

                    }
                }
                f.postId = item.id
                f.show(fm, "comment")
            }

            btnMore.visibility = VISIBLE

            // 더보기 버튼 클릭
            btnMore.setOnClickListener {
                vm.processButtonMore(item)
            }
            // 작성자 프로필 이미지 클릭
            profile.setOnClickListener {
                openProfile?.open(item.producer.producerAccountID)
            }
            // 배치하기 버튼 클릭
            btnSetDrawing.setOnClickListener {
                vm.openPlaceActivity(item)
            }
            // NFT 마크 클릭
            nftMark.setOnClickListener {
                openNft(item.id)
            }
            // nft 발행 버튼 클릭
            btnGoToMint.setOnClickListener {
                vm.openMintingActivity(item)
            }
            // 뷰에 해당 뷰홀더를 태그로 저장
            parent.tag = this

        }
    }

    private fun preload(imageUrl: String) {
        requestManager.load(imageUrl).preload(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
    }

}
