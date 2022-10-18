package com.draw.free.viewmodel

import android.content.res.Resources

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.draw.free.R

class LoadingDialogModel(description: String = Resources.getSystem().getString(R.string.defaultLoadingMsg)) : ViewModel() {
    val mDescription = MutableLiveData<String>();

    init {
        mDescription.value = description
    }

}