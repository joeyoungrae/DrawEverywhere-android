package com.draw.free.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.draw.free.model.Auction
import com.draw.free.network.BaseResponse
import com.draw.free.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class AuctionDetailFragmentViewModel : ViewModel() {

    lateinit var mint: String
    val auction = MutableLiveData<Auction>()

    fun setAuction() {
        RetrofitClient.getSolanaService().getStartedAuctionByMint(mint)
            .enqueue(BaseResponse { response ->
                if (response.code() == 200) {
                    auction.postValue(response.body())
                }

                return@BaseResponse true
            })
    }

    fun getUserProfileById(accountId : String) {
        RetrofitClient.getUserService().getUserProfile(accountId).enqueue(BaseResponse { response ->
            if (response.code() == 200) {

            }

            return@BaseResponse false
        })
    }
}