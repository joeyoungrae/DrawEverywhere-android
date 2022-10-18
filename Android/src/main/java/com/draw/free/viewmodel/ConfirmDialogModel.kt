package com.draw.free.viewmodel

import androidx.lifecycle.ViewModel
import com.draw.free.interfaceaction.ToNextWork

class ConfirmDialogModel(description: String) : ViewModel() {
    val mDescription : String = description
    var clickYes : ToNextWork? = null
}