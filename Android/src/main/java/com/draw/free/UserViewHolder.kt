package com.draw.free

import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.draw.free.customView.ProfileView
import com.draw.free.model.UserProfile
import timber.log.Timber

class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val profile: ProfileView = itemView.findViewById(R.id.profile)
    private val accountId: TextView = itemView.findViewById(R.id.accountId)
    private val name: TextView = itemView.findViewById(R.id.name)
    val btnChgRelation: Button = itemView.findViewById(R.id.btnRelation)
    var userProfile: UserProfile? = null


    fun bind(item: UserProfile?) {

        if (item == null) {
            return
        }

        userProfile = item

        accountId.text = item.accountId
        if (item.pfName.isNullOrEmpty()) {
            name.visibility = View.GONE
        } else {
            name.visibility = View.VISIBLE
            name.text = item.pfName
        }


        when (item.relation) {
            "Mine" -> {
                btnChgRelation.visibility = View.GONE
            }
            "Follower" -> {
                btnChgRelation.backgroundTintList = ContextCompat.getColorStateList(itemView.context, R.color.solid_dark_gray)
                btnChgRelation.visibility = View.VISIBLE
                btnChgRelation.text = itemView.context.getString(R.string.unfollow)
            }
            "Requested" -> {
                btnChgRelation.backgroundTintList = ContextCompat.getColorStateList(itemView.context, R.color.solid_dark_gray)
                btnChgRelation.visibility = View.VISIBLE
                btnChgRelation.text = itemView.context.getString(R.string.cancel_request_follow)
            }
            "None" -> {
                btnChgRelation.backgroundTintList = null
                btnChgRelation.visibility = View.VISIBLE
                btnChgRelation.text = itemView.context.getString(R.string.follow)
            }
        }


        if (item.pfPicture.isNullOrEmpty()) {
            Glide.with(profile)
                .load(R.drawable.pf_picture_default)
                .circleCrop()
                .into(profile.getContent())
        } else {
            Glide.with(profile)
                .load(item.pfPicture)
                .placeholder(R.drawable.pf_picture_default)
                .circleCrop()
                .into(profile.getContent())
        }

    }

    fun updateRelation(relation: String) {

        userProfile?.relation = relation

        when (relation) {
            "Follower" -> {
                btnChgRelation.text = itemView.context.getString(R.string.unfollow)
                btnChgRelation.backgroundTintList = ContextCompat.getColorStateList(itemView.context, R.color.solid_dark_gray)

            }
            "Requested" -> {
                btnChgRelation.text = itemView.context.getString(R.string.unfollowRequest)
                btnChgRelation.backgroundTintList = ContextCompat.getColorStateList(itemView.context, R.color.solid_dark_gray)
            }
            "None" -> {
                btnChgRelation.text = itemView.context.getString(R.string.follow)
                btnChgRelation.backgroundTintList = null
            }
        }
    }
}