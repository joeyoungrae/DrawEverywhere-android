package com.draw.free

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

import com.draw.free.model.UserProfile
import com.draw.free.util.CustomList
import timber.log.Timber

class UserAdapter(val data : CustomList<UserProfile>) : RecyclerView.Adapter<UserViewHolder>() {
    lateinit var openProfile : (accountId: String) -> Unit
    lateinit var follow : (String, (String) -> Unit ) -> Unit
    var handler = Handler(Looper.getMainLooper())

    var mData: List<UserProfile> = data.getData()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        return UserViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_user_profile, parent, false)
        )
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(mData[position])

        if (holder.userProfile != null) {
            holder.btnChgRelation.setOnClickListener {
                val updateRelation = {status : String ->
                    handler.post { holder.updateRelation(status) }
                }

                Timber.e("클릭함")

                follow(holder.userProfile!!.accountId, updateRelation);
            }

            holder.itemView.setOnClickListener {
                openProfile(mData[position].accountId)
            }
        }
    }

    override fun getItemCount(): Int {
        return mData.size
    }

}