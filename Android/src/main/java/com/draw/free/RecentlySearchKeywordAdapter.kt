package com.draw.free

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.draw.free.customView.ProfileView
import com.draw.free.util.*
import com.draw.free.viewmodel.NotifyFragmentViewModel
import java.lang.RuntimeException

class RecentlySearchKeywordAdapter(val keywords: ArrayList<String>) : RecyclerView.Adapter<RecentlySearchKeywordAdapter.SearchKeywordVH>() {

    lateinit var touch : (type : InputType, pos : Int, keyword : String) -> Unit

    override fun getItemViewType(position: Int): Int {
        return 0
    }

    enum class InputType {
        Delete, Search
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchKeywordVH {
        when (viewType) {
            0 -> {
                return SearchKeywordVH(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_recently_search_keyword, parent, false)
                )
            }
        }

        throw RuntimeException("없는 타입")
    }

    override fun onBindViewHolder(holder: SearchKeywordVH, position: Int) {
        holder.bind(keywords[position], position)
    }

    override fun getItemCount(): Int {
        return keywords.size
    }


    inner class SearchKeywordVH(v : View) : RecyclerView.ViewHolder(v) {
        private var tvKeyword: TextView = v.findViewById(R.id.txt_keyword)
        private var ivDelete: ImageView = v.findViewById(R.id.iv_delete)

        fun bind(item: String, pos : Int) {
            tvKeyword.text = item
            tvKeyword.setOnClickListener {
                touch(InputType.Search, keywords.size - pos, item)
            }
            ivDelete.setOnClickListener {
                keywords.removeAt(pos)
                touch(InputType.Delete, keywords.size - pos, item)
            }
        }
    }

}