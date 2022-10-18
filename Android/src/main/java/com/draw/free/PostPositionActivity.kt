package com.draw.free

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.draw.free.model.Post
import com.draw.free.viewmodel.PostPositionViewModel
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.FusedLocationSource


class PostPositionActivity : AppCompatActivity(), OnMapReadyCallback {

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }

    private lateinit var mMapFragment: MapFragment
    private lateinit var mLocationSource: FusedLocationSource
    private lateinit var mNaverMap: NaverMap
    private lateinit var targetPost : Post
    private lateinit var viewmodel : PostPositionViewModel
    private var activity : Activity? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_position_post)

        if (intent == null || !intent.hasExtra("post")) {
            finish()
            return
        }

        activity = this

        targetPost = intent.getParcelableExtra<Post>("post")!!
        viewmodel = ViewModelProvider(this).get(PostPositionViewModel::class.java)

        findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            finish()
        }

        findViewById<TextView>(R.id.tv_place).text = targetPost.content
        findViewById<TextView>(R.id.tv_place).setOnClickListener {
            if (viewmodel.positionLiveData.value == null) {
                return@setOnClickListener
            }

            val cameraUpdate = CameraUpdate.scrollTo(LatLng(viewmodel.positionLiveData.value!!.latitude, viewmodel.positionLiveData.value!!.longitude))
                .reason(3)
                .animate(CameraAnimation.Easing, 2000)
            mNaverMap.moveCamera(cameraUpdate)
        }

        val fm = supportFragmentManager
        mMapFragment = fm.findFragmentById(R.id.naverMap) as MapFragment?
            ?: MapFragment.newInstance().also {
                fm.beginTransaction().add(R.id.naverMap, it).commit()
            }
        mMapFragment.getMapAsync(this) // API를 호출하기 위한 인터페이스 역할의 NaverMap 객체 준비
        // 사용자 위치 가져오기
        mLocationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)

        viewmodel.getPosition(targetPost.id)

    }

    override fun onMapReady(naverMap: NaverMap) {
        // 네이버 지도가 준비되었을 때
        mNaverMap = naverMap

        mNaverMap.locationSource = mLocationSource

        // 지도 사용자 상호작용 가능 설정
        mNaverMap.uiSettings.isLocationButtonEnabled = true
        mNaverMap.uiSettings.isZoomGesturesEnabled = true
        mNaverMap.uiSettings.isScrollGesturesEnabled = true
        mNaverMap.uiSettings.isStopGesturesEnabled = true
        mNaverMap.uiSettings.isRotateGesturesEnabled = true


        viewmodel.positionLiveData.observe(this) {
            Glide.with(activity!!)
                .asBitmap()
                .load(targetPost.thumbnail)
                .dontTransform()
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        val marker = Marker()
                        marker.position = LatLng(it.latitude, it.longitude)
                        marker.width = 200
                        marker.height = 200
                        marker.icon = OverlayImage.fromBitmap(resource)
                        marker.map = mNaverMap


                        val cameraUpdate = CameraUpdate.scrollTo(LatLng(it.latitude, it.longitude))
                            .reason(3)
                            .animate(CameraAnimation.Easing, 2000)
                        /*.finishCallback(() -> {
                            Toast.makeText(context, "완료", Toast.LENGTH_SHORT).show();
                        })
                        .cancelCallback(() -> {
                            Toast.makeText(context, "취소", Toast.LENGTH_SHORT).show();
                        });*/
                        mNaverMap.moveCamera(cameraUpdate)
                    }



                    override fun onLoadCleared(placeholder: Drawable?) {

                    }

                })
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        // 사용자 위치 가져오기 결과 처리
        if (mLocationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            if (!mLocationSource.isActivated) { // 권한 거부됨
                mNaverMap.locationTrackingMode = LocationTrackingMode.None
            }
            return
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}