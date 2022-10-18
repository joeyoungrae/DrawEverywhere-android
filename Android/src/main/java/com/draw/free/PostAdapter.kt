package com.draw.free

import android.view.*
import androidx.annotation.CallSuper
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.target.Target
import com.draw.free.databinding.ItemFatThumbnailPostBinding
import com.draw.free.databinding.ItemGridThumbnailPostBinding
import com.draw.free.databinding.ItemThumbnailPostBinding
import com.draw.free.databinding.ItemThumbnailWithProfilePostBinding
import com.draw.free.interfaceaction.IOpenPost
import com.draw.free.model.Post
import com.draw.free.util.CustomList

class PostAdapter(
    val data: CustomList<Post>,
    var type: ShowType,
    val postOpen: IOpenPost,
    val requestManager: RequestManager
) : RecyclerView.Adapter<PostAdapter.PostVH>() {
    var mData: List<Post> = data.getData()

    enum class ShowType {
        GIF, JPEG, FAT_GIF, TRENDING_DRAWINGS, LIKED_POSTS, PROFILE_POSTS
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostVH {
        when (type) {
            ShowType.GIF -> {
                return PostGIFVH(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.item_thumbnail_post,
                        parent,
                        false
                    )
                )
            }
            ShowType.JPEG -> {
                return PostJPEGVH(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.item_thumbnail_post,
                        parent,
                        false
                    )
                )
            }
            ShowType.FAT_GIF -> {
                return FatPostGIFVH(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.item_fat_thumbnail_post,
                        parent,
                        false
                    )
                )
            }
            ShowType.TRENDING_DRAWINGS -> {
                return TrendingDrawingsVH(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.item_thumbnail_with_profile_post,
                        parent,
                        false
                    )
                )
            }
            ShowType.LIKED_POSTS -> {
                return LikedPostsVH(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.item_thumbnail_with_profile_post,
                        parent,
                        false
                    )
                )
            }
            ShowType.PROFILE_POSTS -> {
                return ProfilePostsVH(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.item_grid_thumbnail_post,
                        parent,
                        false
                    )
                )
            }
            else -> {
                return PostGIFVH(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.item_thumbnail_post,
                        parent,
                        false
                    )
                )
            }
        }
    }

    override fun onBindViewHolder(holder: PostVH, position: Int) {
        holder.bind(mData[position])

        // 다음 gif 썸네일 이미지 5개 미리 로드해두기
        if (position <= mData.size) {
            val endPosition = if (position + 5 > mData.size) {
                mData.size
            } else {
                position + 5
            }
            mData.subList(position, endPosition).map { it.animatedThumbnail }.forEach {
                preload(it)
            }
        }
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    abstract inner class PostVH(view: View) : RecyclerView.ViewHolder(view) {
        private lateinit var post: Post

        @CallSuper
        open fun bind(post: Post) {
            this.post = post
        }
    }

    inner class FatPostGIFVH(private val binding: ItemFatThumbnailPostBinding) :
        PostVH(binding.root) {
        override fun bind(post: Post) {
            super.bind(post)
            itemView.setOnClickListener {
                postOpen.open(post.id, data)
            }
            requestManager.load(post.animatedThumbnail).error(R.drawable.icon_error)
                .into(binding.thumbnail)
        }
    }

    inner class PostGIFVH(private val binding: ItemThumbnailPostBinding) : PostVH(binding.root) {
        override fun bind(post: Post) {
            super.bind(post)
            itemView.setOnClickListener {
                postOpen.open(post.id, data)
            }
            requestManager.load(post.animatedThumbnail).error(R.drawable.icon_error)
                .into(binding.thumbnail)
        }
    }

    inner class PostJPEGVH(private val binding: ItemThumbnailPostBinding) : PostVH(binding.root) {
        override fun bind(post: Post) {
            super.bind(post)
            itemView.setOnClickListener {
                postOpen.open(post.id, data)
            }
            requestManager.load(post.thumbnail).error(R.drawable.icon_error).into(binding.thumbnail)
        }
    }

    inner class TrendingDrawingsVH(private val binding: ItemThumbnailWithProfilePostBinding) :
        PostVH(binding.root) {
        override fun bind(post: Post) {
            super.bind(post)
            itemView.setOnClickListener {
                postOpen.open(post.id, data)
            }
            // 썸네일
            requestManager.load(post.animatedThumbnail).error(R.drawable.icon_error)
                .into(binding.thumbnail)
            // 프로필 사진
            if (post.producer.producerPfPicture.isNullOrEmpty() || post.producer.producerPfPicture.equals("null")) {
                requestManager.load(R.drawable.pf_picture_default).circleCrop()
                    .into(binding.pfPicture.getContent())
            } else {
                requestManager.load(post.producer.producerPfPicture)
                    .placeholder(R.drawable.pf_picture_default)
                    .circleCrop()
                    .into(binding.pfPicture.getContent())
            }
            // 계정 id
            binding.accountId.text = post.producer.producerAccountID
            // 그림 타이틀
            binding.subText.text = post.title
        }
    }

    inner class LikedPostsVH(private val binding: ItemThumbnailWithProfilePostBinding) :
        PostVH(binding.root) {
        override fun bind(post: Post) {
            super.bind(post)
            itemView.setOnClickListener {
                postOpen.open(post.id, data)
            }
            // 썸네일
            requestManager.load(post.animatedThumbnail).error(R.drawable.icon_error)
                .into(binding.thumbnail)
            // 프로필 사진
            if (post.producer.producerPfPicture.isNullOrEmpty()) {
                requestManager.load(R.drawable.pf_picture_default).circleCrop()
                    .into(binding.pfPicture.getContent())
            } else {
                requestManager.load(post.producer.producerPfPicture)
                    .placeholder(R.drawable.pf_picture_default)
                    .circleCrop()
                    .into(binding.pfPicture.getContent())
            }
            // 계정 id
            binding.accountId.text = post.producer.producerAccountID
            // 좋아요 수
            binding.subText.text =
                Global.getContext().getString(R.string.likes) + post.likes.toString()
        }
    }

    inner class ProfilePostsVH(private val binding: ItemGridThumbnailPostBinding) :
        PostVH(binding.root) {
        override fun bind(post: Post) {
            super.bind(post)
            if (post.status != "freezed") {
                itemView.setOnClickListener {
                    postOpen.open(post.id, data)
                }

                requestManager.load(post.animatedThumbnail).error(R.drawable.icon_error)
                    .into(binding.thumbnail)
            } else {
                requestManager.load(R.drawable.loading_video)
                    .into(binding.thumbnail)
            }
        }
    }


    private fun preload(imageUrl: String) {
        // 글라이더 이미지 미리 로드
        requestManager.load(imageUrl).preload(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
    }
}