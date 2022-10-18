package com.draw.free.fragment

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.draw.free.Global
import com.draw.free.NotifyMessageAdapter
import com.draw.free.databinding.FragmentNotifyBinding
import com.draw.free.viewmodel.NotifyFragmentViewModel
import com.draw.free.viewmodel.PostDetailFragmentViewModel
import timber.log.Timber

class NotifyFragment : BaseInnerFragment<FragmentNotifyBinding>() {
    private lateinit var recyclerView : RecyclerView
    private lateinit var viewModel : NotifyFragmentViewModel
    private lateinit var adapter : NotifyMessageAdapter


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = binding.rvNotifyList
        viewModel = ViewModelProvider(this)[NotifyFragmentViewModel::class.java]

        val requestManager = Glide.with(this)
        adapter = NotifyMessageAdapter(requestManager, viewModel)
        adapter.notifymessages = Global.notifyLists

        adapter.notifymessages.mLiveData.observe(viewLifecycleOwner) {
            adapter.notifyDataSetChanged()
        }
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter


    }

    override fun onStart() {
        super.onStart()
        if(!isHidden) {
            showBottomNav.show(true)
            setPreviousButton.set(isSet = true, false)
        }
    }

}