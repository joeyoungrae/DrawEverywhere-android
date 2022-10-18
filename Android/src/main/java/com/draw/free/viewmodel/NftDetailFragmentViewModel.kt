package com.draw.free.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.draw.free.model.Nft
import com.draw.free.network.BaseResponse
import com.draw.free.network.RetrofitClient

class NftDetailFragmentViewModel : ViewModel() {
    private val _liveNft = MutableLiveData<Nft>()
    val liveNft get() = _liveNft


    fun refreshNft(nftId: String) {
        RetrofitClient.getSolanaService().getNftByMint(nftId).enqueue(BaseResponse {response ->
            if (response.isSuccessful && response.code() == 200) {
                liveNft.postValue(response.body())
            }

            return@BaseResponse false
        })
    }

    fun setNft(nft: Nft) {
        _liveNft.postValue(nft)
    }
}