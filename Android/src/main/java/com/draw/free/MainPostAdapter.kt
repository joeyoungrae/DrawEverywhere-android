package com.draw.free

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.draw.free.customView.ProfileView
import com.draw.free.interfaceaction.IOpenPost
import com.draw.free.util.*
import timber.log.Timber
import java.lang.RuntimeException

class MainPostAdapter(val context: Context, val requestManager: RequestManager, val toOpenPage: Any?) : RecyclerView.Adapter<MainPostAdapter.BaseHolder>() {
    private var curMainContents: MainContentsList = MainContentsList()
    var openPost: IOpenPost? = null
    lateinit var openProfile : (accountID : String) -> Unit

    fun updateMainContents(updateList: MainContentsList) {
        curMainContents = updateList
    }

    override fun getItemViewType(position: Int): Int {
        return when (curMainContents.get(position)) {
            is FixedContents -> {
                0
            }
            is HashTagContents -> {
                1
            }
            is UserContents -> {
                2
            }
            is FixedNftContents -> {
                3
            }
            else -> throw RuntimeException("없는 타입")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseHolder {
        when (viewType) {
            0 -> {
                return PopularFixedHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_fixedcontents_posts, parent, false))
            }
            1 -> {
                return PopularHashTagHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_outline_posts, parent, false))
            }
            2 -> {
                return PopularUserPostHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_staruser_posts, parent, false))
            }
            3 -> {
                return FixedNftHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_fixedcontents_posts, parent, false))
            }
        }

        throw RuntimeException("없는 타입")
    }

    override fun onBindViewHolder(holder: BaseHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int {
        return curMainContents.getSize()
    }

    abstract class BaseHolder(v: View) : RecyclerView.ViewHolder(v) {
        abstract fun bind(position: Int)
    }

    inner class FixedNftHolder(v: View) : BaseHolder(v) {
        lateinit var data: FixedNftContents
        private val tagName: TextView = itemView.findViewById(R.id.posts_tag)
        private val rv = itemView.findViewById<RecyclerView>(R.id.rv_post)
        private val progressBar = itemView.findViewById<ProgressBar>(R.id.progress_bar)

        override fun bind(position: Int) {
            data = (curMainContents.get(position) as FixedNftContents)

            val adapter: NftAdapter
            when (data.typeName) {
                FixedNftContentsTheme.RECENTLY_MINTED -> {
                    tagName.text = Global.getContext().getString(R.string.recently_minted)
                    adapter = NftAdapter(context, data.convertCustomNftList(), NftAdapter.ShowType.RECENTLY_MINTED, requestManager, toOpenPage)
                }
            }
            if (adapter.itemCount != 0) {
                progressBar.visibility = View.GONE
            } else {
                progressBar.visibility = View.VISIBLE
            }
            rv.adapter = adapter
        }

    }

    inner class PopularFixedHolder(v: View) : BaseHolder(v) {
        lateinit var data: FixedContents
        private val tagName: TextView = itemView.findViewById(R.id.posts_tag)
        private val rv = itemView.findViewById<RecyclerView>(R.id.rv_post)
        private val progressBar = itemView.findViewById<ProgressBar>(R.id.progress_bar)

        override fun bind(position: Int) {
            data = (curMainContents.get(position) as FixedContents)

            val adapter: PostAdapter
            when (data.typeName) {
                FixedPostContentsTheme.TRENDING_DRAWINGS -> {
                    tagName.text = Global.getContext().getString(R.string.trending_drawings)
                    adapter = PostAdapter(data.convertCustomPostList(), PostAdapter.ShowType.TRENDING_DRAWINGS, openPost!!, requestManager)
                }
                FixedPostContentsTheme.LIKED_POSTS -> {
                    tagName.text = Global.getContext().getString(R.string.liked_posts)
                    adapter = PostAdapter(data.convertCustomPostList(), PostAdapter.ShowType.LIKED_POSTS, openPost!!, requestManager)
                }
            }

            if (data.posts.isNotEmpty()) {
                progressBar.visibility = View.GONE
            } else {
                progressBar.visibility = View.VISIBLE
            }

            rv.adapter = adapter
        }
    }

    inner class PopularHashTagHolder(v: View) : BaseHolder(v) {
        lateinit var data: HashTagContents
        private val tagName: TextView = itemView.findViewById(R.id.posts_tag)
        private val rv = itemView.findViewById<RecyclerView>(R.id.rv_post)

        override fun bind(position: Int) {
            data = (curMainContents.get(position) as HashTagContents)

            tagName.text = data.hashtag

            val adapter = PostAdapter(data.convertCustomPostList(), PostAdapter.ShowType.GIF, openPost!!, requestManager)
            rv.adapter = adapter
        }

    }

    inner class PopularUserPostHolder(v: View) : BaseHolder(v) {
        lateinit var data: UserContents
        private val profile: ProfileView = itemView.findViewById(R.id.profile)
        private val accountId: TextView = itemView.findViewById(R.id.tv_account_id)

        private val rv = itemView.findViewById<RecyclerView>(R.id.rv_post)

        override fun bind(position: Int) {
            data = (curMainContents.get(position) as UserContents)
            accountId.text = data.user.accountId

            itemView.setOnClickListener {
                openProfile(data.user.accountId)
            }

            if (data.user.pfPicture.isNullOrEmpty() || data.user.pfPicture.equals("null")) {
                requestManager.load(R.drawable.pf_picture_default).placeholder(R.drawable.pf_picture_default).circleCrop().into(profile.getContent())
            } else {
                requestManager.load(data.user.pfPicture).placeholder(R.drawable.pf_picture_default).circleCrop().into(profile.getContent())
            }

            val adapter = PostAdapter(data.convertCustomPostList(), PostAdapter.ShowType.GIF, openPost!!, requestManager)
            rv.adapter = adapter
        }

    }
}