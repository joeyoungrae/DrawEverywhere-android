package com.draw.free.viewmodel

import androidx.lifecycle.ViewModel
import com.draw.free.interfaceaction.ToNextWork

class YesOrNoDialogModel(description: String) : ViewModel() {
    val mDescription : String = description
    var clickYes : ToNextWork? = null
    var clickNo : ToNextWork? = null
}