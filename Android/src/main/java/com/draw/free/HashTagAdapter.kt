package com.draw.free

import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.draw.free.databinding.ItemHashtagPostCountBinding
import com.draw.free.model.PostHashTag
import com.draw.free.util.CustomList


class HashTagAdapter(val data: CustomList<PostHashTag>) : RecyclerView.Adapter<HashTagAdapter.PostHashtagVH>() {


    lateinit var openTag : (hashTag : String) -> Unit

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostHashtagVH {

        return PostHashtagVH(
            DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_hashtag_post_count, parent, false)
        )
    }

    override fun onBindViewHolder(holder: PostHashtagVH, position: Int) {
        holder.bind(data.getDataByPosition(position))
    }

    override fun getItemCount(): Int {
        return data.getItemCount()
    }

    // ViewHolder
    inner class PostHashtagVH(
        val binding: ItemHashtagPostCountBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: PostHashTag) {
            binding.tagName.text = data.hashtag
            binding.tagCount.text = "게시물 ${data.count}"

            itemView.setOnClickListener {
                openTag(data.hashtag)
            }
        }

    }

}