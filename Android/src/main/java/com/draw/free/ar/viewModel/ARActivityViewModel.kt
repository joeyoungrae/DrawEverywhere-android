package com.draw.free.ar.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.draw.free.Global
import com.draw.free.interfaceaction.ToNextWork
import timber.log.Timber
import java.io.File

class ARActivityViewModel : ViewModel() {

    //<editor-fold desc="동영상 기능">
    fun createVideoOutputFile(): File? {
        val tempFile: File
        val dir = File(Global.getContext().cacheDir, "captures")

        Timber.d("dir : $dir")

        if (!dir.exists()) {
            dir.mkdirs()
        }

        val filename = ("temp")
        tempFile = File(dir, "$filename.mp4")


        return tempFile
    }


    //</editor-fold>
    fun saveDrawing(next: ToNextWork?) {
        next?.next();
    }

}