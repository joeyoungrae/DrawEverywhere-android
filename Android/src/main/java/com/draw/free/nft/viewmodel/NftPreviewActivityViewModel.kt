package com.draw.free.nft.viewmodel

import android.provider.Settings
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.draw.free.Global
import com.draw.free.model.ParcelableNftMetadata
import com.draw.free.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import timber.log.Timber

class NftPreviewActivityViewModel : ViewModel() {
    enum class NftApiType {
        Mint, Sell, Buy, EndSale, SettleBill
    }

    lateinit var type: NftApiType
    lateinit var walletAddress: String
    lateinit var metadata: ParcelableNftMetadata

    var auctionCache: String? = null

    val mExpectedCost = MutableLiveData<String>()
    val mBalance = MutableLiveData<String>()

    fun setExpectedCost() {
        when (type) {
            NftApiType.Mint -> {
                CoroutineScope(Dispatchers.IO).launch {
                    val postId = MultipartBody.Part.createFormData("post_id", metadata.post!!)
                    val name = MultipartBody.Part.createFormData("name", metadata.name!!)
                    val symbol = MultipartBody.Part.createFormData("symbol", metadata.symbol!!)
                    val desc = MultipartBody.Part.createFormData("desc", metadata.description!!)
                    val creativeFee = MultipartBody.Part.createFormData(
                        "creative_fee",
                        metadata.sellerFeeBasisPoints!!
                    )

                    val getExpectedCost = RetrofitClient.getSolanaService()
                        .getExpectedMintingCost(postId, name, symbol, desc, creativeFee)

                    launch(Dispatchers.Main) {
                        mExpectedCost.postValue(String.format("%.9f", getExpectedCost))
                    }
                }
            }
            NftApiType.Sell -> {
                CoroutineScope(Dispatchers.IO).launch {
                    val fAddress = MultipartBody.Part.createFormData("address", Global.userProfile!!.walletAddress!!)
                    val getExpectedCost = RetrofitClient.getSolanaService().getExpectedSellingCost(fAddress)

                    launch(Dispatchers.Main) {
                        mExpectedCost.postValue(String.format("%.9f", getExpectedCost))
                    }
                }
            }
            NftApiType.Buy -> {
                CoroutineScope(Dispatchers.IO).launch {
                    val fAuctionCache = MultipartBody.Part.createFormData("auction_cache", auctionCache!!)
                    val getExpectedCost = RetrofitClient.getSolanaService().getExpectedBuyingCost(fAuctionCache)

                    launch(Dispatchers.Main) {
                        mExpectedCost.postValue(String.format("%.9f", getExpectedCost))
                    }
                }
            }
            NftApiType.EndSale -> {
                CoroutineScope(Dispatchers.IO).launch {
                    val getExpectedCost = RetrofitClient.getSolanaService().getExpectedEndingSaleCost()

                    launch(Dispatchers.Main) {
                        mExpectedCost.postValue(String.format("%.9f", getExpectedCost))
                    }
                }
            }
            NftApiType.SettleBill -> {
                CoroutineScope(Dispatchers.IO).launch {
                    val getExpectedCost = RetrofitClient.getSolanaService().getExpectedSettlingBillCost()

                    launch(Dispatchers.Main) {
                        mExpectedCost.postValue(String.format("%.9f", getExpectedCost))
                    }
                }
            }
        }
    }

    fun setWalletBalance() {
        CoroutineScope(Dispatchers.IO).launch {
            val getBalance = RetrofitClient.getSolanaService().getBalance(address = walletAddress)

            launch(Dispatchers.Main) {
                mBalance.postValue(String.format("%.9f", getBalance))
            }
        }
    }

    fun checkEnoughBalance() : Boolean {
        // 잔액과 예상 비용 비교
        return mBalance.value!!.toDouble() >= mExpectedCost.value!!.toDouble()
    }

}