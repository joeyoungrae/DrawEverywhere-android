package com.draw.free.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.draw.free.model.Auction
import com.draw.free.network.RetrofitClient
import com.draw.free.util.CustomList
import java.util.concurrent.atomic.AtomicBoolean


class NftFragmentViewModel : ViewModel() {
    var getAllAuctionList: CustomList<Auction>? = null
    var getOnSaleAuctionList: CustomList<Auction>? = null
    var getCancelAuctionList: CustomList<Auction>? = null
    var getSoldAuctionList: CustomList<Auction>? = null

    enum class Type {
        All, OnSale, Cancel, Sold
    }

    private var _liveType = MutableLiveData<Type>()
    val liveType
        get() = _liveType

    private var _customListLive = MutableLiveData<CustomList<Auction>>()
    val customListLive
        get() = _customListLive

    fun initialList() {
        when (liveType.value) {
            Type.All -> {
                getAllCustomAuctionList()
            }
            Type.OnSale -> {
                getOnSaleAuctionList()
            }
            Type.Cancel -> {
                getCancelAuctionList()
            }
            Type.Sold -> {
                getSoldAuctionList()
            }
            else -> {
                getAllCustomAuctionList()
            }
        }
    }





    fun getAllCustomAuctionList() {
        _liveType.value = Type.All
        if (getAllAuctionList != null) {
            _customListLive.postValue(getAllAuctionList)
            //customListLive.value!!.refreshData()

        } else {
            getAllAuctionList = CustomList(10, api = { pageSize: Int, offset: String ->
                return@CustomList RetrofitClient.getSolanaService()
                    .getAuctionsAll(offset = offset, size = pageSize).data
            }, { it.id })
            _customListLive.postValue(getAllAuctionList)
            getAllAuctionList?.getNextData()
        }
    }

    fun getOnSaleAuctionList() {
        _liveType.value = Type.OnSale
        if (getOnSaleAuctionList != null) {
            _customListLive.postValue(getOnSaleAuctionList)
           // customListLive.value!!.refreshData()

        } else {
            getOnSaleAuctionList = CustomList(10, api = { pageSize: Int, offset: String ->
                return@CustomList RetrofitClient.getSolanaService()
                    .getAuctionsOnSale(offset = offset, size = pageSize).data
            }, { it.id })
            _customListLive.postValue(getOnSaleAuctionList)
            getOnSaleAuctionList?.getNextData()
        }
    }

    fun getCancelAuctionList() {
        _liveType.value = Type.Cancel
        if (getCancelAuctionList != null) {
            _customListLive.postValue(getCancelAuctionList)
            //customListLive.value!!.refreshData()

        } else {
            getCancelAuctionList = CustomList(10, api = { pageSize: Int, offset: String ->
                return@CustomList RetrofitClient.getSolanaService()
                    .getAuctionsCancel(offset = offset, size = pageSize).data
            }, { it.id })
            _customListLive.postValue(getCancelAuctionList)
            getCancelAuctionList?.getNextData()
        }
    }

    fun getSoldAuctionList() {
        _liveType.value = Type.Sold
        if (getSoldAuctionList != null) {
            _customListLive.postValue(getSoldAuctionList)
            //customListLive.value!!.refreshData()

        } else {
            getSoldAuctionList = CustomList(10, api = { pageSize: Int, offset: String ->
                return@CustomList RetrofitClient.getSolanaService()
                    .getAuctionsSold(offset = offset, size = pageSize).data
            }, { it.id })
            _customListLive.postValue(getSoldAuctionList)
            getSoldAuctionList?.getNextData()
        }
    }


    fun refreshData() {
        _customListLive.value?.refreshData()
    }
}