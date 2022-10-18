package com.draw.free.viewmodel.viewPagerFragmentViewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.draw.free.model.Auction
import com.draw.free.model.Nft
import com.draw.free.network.RetrofitClient
import com.draw.free.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WalletListFragmentViewModel : ViewModel() {
    enum class Type {
        OWNED_NFT, MINTED_NFT, AUCTION
    }
    var targetAddress: String = ""
    lateinit var customOwnedNftList: CustomList<Nft>
    lateinit var customMintedNftList: CustomList<Nft>
    lateinit var customAuctionList: CustomList<Auction>

    private val _balance = MutableLiveData<Double>()
    val balance
        get() = _balance;

    fun setOwnedNftList() {
        customOwnedNftList = CustomList<Nft>(10, {pageSize: Int, offset: String ->
            return@CustomList RetrofitClient.getSolanaService().getAddressOwnedNfts(address = targetAddress).data
        }) { it.id }
    }

    fun setMintedNftList() {
        customMintedNftList = CustomList<Nft>(10, {pageSize: Int, offset: String ->
            return@CustomList RetrofitClient.getSolanaService().getAddressMintedNfts(offset = offset, size = pageSize, address = targetAddress).data
        }) { it.id }
    }

    fun setUserAuctionList() {
        customAuctionList = CustomList<Auction>(10, api = {pageSize: Int, offset: String ->
            return@CustomList RetrofitClient.getSolanaService().getAddressAuctions(offset = offset, size = pageSize, address = targetAddress).data }, {it.id})
    }

    fun setBalance() {
        CoroutineScope(Dispatchers.IO).launch {
            val getBalance = RetrofitClient.getSolanaService().getBalance(address = targetAddress)
            launch(Dispatchers.Main) {
                _balance.postValue(getBalance);
            }
        }
    }

}