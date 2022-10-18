package com.draw.free.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.draw.free.network.BaseResponse
import com.draw.free.network.RetrofitClient
import com.naver.maps.geometry.LatLng
import org.bouncycastle.oer.its.Latitude
import org.bouncycastle.oer.its.Longitude

class PostPositionViewModel : ViewModel() {


    val positionLiveData = MutableLiveData<LatLng>()

    fun getPosition(postId : String) {
        RetrofitClient.getPostService().getLocationFromPostID(postId).enqueue(BaseResponse() { response ->
            if (response.isSuccessful && response.code() == 200) {
                val r = response.body()!!
                val p  = LatLng(r.latitude, r.longitude)
                positionLiveData.postValue(p)
                return@BaseResponse true
            }


            return@BaseResponse false
        })
    }
}