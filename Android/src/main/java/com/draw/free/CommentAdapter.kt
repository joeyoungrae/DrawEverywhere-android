package com.draw.free

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.draw.free.customView.ProfileView
import com.draw.free.model.Comment
import com.draw.free.util.Util
import com.draw.free.viewmodel.CommentFragmentViewModel
import timber.log.Timber


class CommentAdapter(val viewModel: CommentFragmentViewModel) :
    PagingDataAdapter<Comment, CommentViewHolder>(COMMENT_COMPARATOR) {
    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        return CommentViewHolder(viewModel,
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_horizontal_comment, parent, false)
        )
    }


    companion object {
        private val PAYLOAD_SCORE = Any()
        val COMMENT_COMPARATOR = object : DiffUtil.ItemCallback<Comment>() {
            override fun areContentsTheSame(oldItem: Comment, newItem: Comment): Boolean {
                return oldItem.myLike == newItem.myLike
            }


            override fun areItemsTheSame(oldItem: Comment, newItem: Comment): Boolean {
                return oldItem.id == newItem.id
            }

            override fun getChangePayload(oldItem: Comment, newItem: Comment): Any? {
                Timber.d("getChangePayLoad")

                return if (oldItem.likes != newItem.likes) {
                    Timber.d("업데이트 반영됨")
                    PAYLOAD_SCORE
                } else {
                    Timber.d("업데이트 반영 안됨")
                    null
                }
            }
        }

    }
}

class CommentViewHolder(val viewModel: CommentFragmentViewModel, view: View) :
    RecyclerView.ViewHolder(view) {

    private val thumbnail: ProfileView = itemView.findViewById(R.id.profileView)
    private val displayName: TextView = itemView.findViewById(R.id.display_name)
    private val content: TextView = itemView.findViewById(R.id.content)
    private val time: TextView = itemView.findViewById(R.id.tv_time)
    private val delete: TextView = itemView.findViewById(R.id.tv_delete)
    private val countRecommend: TextView = itemView.findViewById(R.id.count_recommend)
    private val likes: ImageView = itemView.findViewById(R.id.btn_thumbup)
    private var item: Comment? = null

    fun bind(item: Comment?) {
        this.item = item
        if (item == null) {
            return
        }

        displayName.text = item.producer.producerAccountID
        countRecommend.text = item.likes.toString()

        if (!item.producer.producerPfPicture.isNullOrEmpty() && item.producer.producerPfPicture != "null") {
            Glide.with(itemView.context)
                .load(item.producer.producerPfPicture)
                .placeholder(R.drawable.pf_picture_default)
                .circleCrop()
                .into(thumbnail.getContent())
        } else {
            Glide.with(itemView.context)
                .load(R.drawable.pf_picture_default)
                .circleCrop()
                .into(thumbnail.getContent())
        }

        time.text = Util.calculateDisplayTime(item.createdAt)

        content.text = item.comment
        likes.isActivated = item.myLike

        likes.setOnClickListener {
            viewModel.recommendComment(item.id, countRecommend, likes)
        }

        if ((Global.userProfile != null) && (item.producer.producerId == Global.userProfile?.uniqueId)) {
            delete.visibility = View.VISIBLE
            delete.setOnClickListener {
                viewModel.deleteComment(itemView.context, item.id)
            }
        } else {
            delete.visibility = View.GONE
        }

    }
}
