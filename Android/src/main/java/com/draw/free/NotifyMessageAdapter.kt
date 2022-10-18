package com.draw.free

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.draw.free.customView.ProfileView
import com.draw.free.util.*
import com.draw.free.viewmodel.NotifyFragmentViewModel
import java.lang.RuntimeException

class NotifyMessageAdapter(val requestManager: RequestManager, val viewModel : NotifyFragmentViewModel) : RecyclerView.Adapter<NotifyMessageAdapter.BaseHolder>() {
    lateinit var notifymessages: CustomList<NotifyMessage>

    override fun getItemViewType(position: Int): Int {
        return when (notifymessages.getDataByPosition(position)) {
            is RequestFollow -> {
                0
            }

            else -> throw RuntimeException("없는 타입")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseHolder {
        when (viewType) {
            0 -> {
                return RequestFollowVH(LayoutInflater.from(parent.context).inflate(R.layout.item_request_follow, parent, false))
            }
        }

        throw RuntimeException("없는 타입")
    }

    override fun onBindViewHolder(holder: BaseHolder, position: Int) {
        holder.bind(notifymessages.getDataByPosition(position))
    }

    override fun getItemCount(): Int {
        return notifymessages.getItemCount()
    }


    abstract class BaseHolder(v: View) : RecyclerView.ViewHolder(v) {
        abstract fun bind(item: NotifyMessage)
    }

    inner class RequestFollowVH(val v : View) : BaseHolder(v) {
        private var profileView : ProfileView = v.findViewById(R.id.pv)
        private var description : TextView = v.findViewById(R.id.tv_description_request)
        private var time : TextView = v.findViewById(R.id.tv_time)
        private var btnAcccept : TextView = v.findViewById(R.id.button_accept)
        private var btnRefuse : TextView = v.findViewById(R.id.button_refuse)

        override fun bind(item: NotifyMessage) {
            val request = item as RequestFollow
            
            // 설명
            description.text = v.context.getString(R.string.follow_request_message, request.targetUser.accountId)

            // 프로필
            requestManager.load(request.targetUser.pfPicture)
                .placeholder(R.drawable.pf_picture_default)
                .circleCrop()
                .error(R.drawable.pf_picture_default).into(profileView.getContent())

            btnAcccept.setOnClickListener {
                viewModel.acceptFollow(request.getKey(), notifymessages)
            }

            btnRefuse.setOnClickListener {
                viewModel.refuseFollow(request.getKey(), notifymessages)
            }

            // 시간값은 현재 없어서, 안보이게 처리함
            time.visibility = View.GONE
        }

    }


}