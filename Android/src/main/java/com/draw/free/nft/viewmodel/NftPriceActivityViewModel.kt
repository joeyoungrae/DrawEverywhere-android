package com.draw.free.nft.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import timber.log.Timber

class NftPriceActivityViewModel : ViewModel() {
    enum class CheckPriceType {
        Empty, NotNumberFormat, NotInRange, WrongDecimal, Ok
    }

    val mPrice = MutableLiveData<String>()

    fun checkPrice() : CheckPriceType {
        if (mPrice.value.isNullOrEmpty()) {
            return CheckPriceType.Empty
        }
        try {
            val dPrice: Double = mPrice.value!!.toDouble()

            if (dPrice <= 0.0) {
                return CheckPriceType.NotInRange
            }

            val stringList = mPrice.value!!.split(".")
            if (stringList.size > 1 && stringList[1].length > 9) {
                return CheckPriceType.WrongDecimal
            }

            return CheckPriceType.Ok

        } catch (e: NumberFormatException) {
            return CheckPriceType.NotNumberFormat
        }
    }
}