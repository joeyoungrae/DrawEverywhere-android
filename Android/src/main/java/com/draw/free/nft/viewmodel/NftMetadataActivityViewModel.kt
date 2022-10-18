package com.draw.free.nft.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.draw.free.model.ParcelableNftMetadata
import timber.log.Timber

class NftMetadataActivityViewModel : ViewModel() {

    enum class CreatorFeeType {
        Empty, NotNumberFormat, NotInRange, WrongDecimal, Ok
    }
    lateinit var postId: String
    lateinit var creatorId: String
    lateinit var thumbnail: String
    val mTitle = MutableLiveData<String>()
    val mSymbol = MutableLiveData<String>()
    val mDesc = MutableLiveData<String>()
    val mCreatorFee = MutableLiveData<String>()

    fun checkTitleLength(): Boolean {
        // 32 bytes 이하인지 확인
        if (mTitle.value.isNullOrEmpty()) {
            return false
        }
        return mTitle.value!!.toByteArray(Charsets.UTF_8).size < 32
    }

    fun checkSymbolLength(): Boolean {
        // 10 bytes 이하인지 확인
        if (mSymbol.value.isNullOrEmpty()) {
            return false
        }
        return mSymbol.value!!.toByteArray(Charsets.UTF_8).size < 10
    }

    fun checkDescLength(): Boolean {
        // 100자 이하인지 확인
        if (mDesc.value.isNullOrEmpty()) {
            return false
        }
        return mDesc.value!!.length < 100
    }

    fun checkCreatorFee(): CreatorFeeType {
        if (mCreatorFee.value.isNullOrEmpty()) {
            return CreatorFeeType.Empty
        }
        try {
            val dCreatorFee: Double = mCreatorFee.value!!.toDouble()

            if (dCreatorFee < 0.0 || dCreatorFee > 50.0) {
                return CreatorFeeType.NotInRange
            }

            val stringList = mCreatorFee.value!!.split(".")
            if (stringList.size > 1 && stringList[1].length > 2) {
                return CreatorFeeType.WrongDecimal
            }

            return CreatorFeeType.Ok

        } catch (e: NumberFormatException) {
            Timber.e("숫자로 형변환 불가: ${mCreatorFee.value}")
            return CreatorFeeType.NotNumberFormat
        }
    }


    fun checkAll() : ParcelableNftMetadata? {
        return if (checkTitleLength() && checkSymbolLength() && checkDescLength() && checkCreatorFee() == CreatorFeeType.Ok) {
            val creatorFee = String.format("%.2f", mCreatorFee.value!!.toDouble())
            ParcelableNftMetadata(postId, thumbnail, mTitle.value!!, mSymbol.value!!, mDesc.value!!, creatorFee, creatorId)

        } else {
            null
        }
    }

}