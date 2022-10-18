package com.draw.free.ar


import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferNetworkLossHandler
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import com.draw.free.Global
import com.draw.free.R
import com.draw.free.ar.util.JniInterface2
import com.draw.free.databinding.ActivityUploadPostBinding
import com.draw.free.dialog.ConfirmDialog
import com.draw.free.dialog.LoadingDialog
import com.draw.free.interfaceaction.ToNextWork
import com.draw.free.network.BaseResponse
import com.draw.free.network.RetrofitClient
import com.draw.free.viewmodel.ConfirmDialogModel
import com.draw.free.viewmodel.LoadingDialogModel
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.FusedLocationSource
import timber.log.Timber
import java.io.File
import kotlin.system.exitProcess


// TODO: 네이버 지도 적용하기: https://navermaps.github.io/android-map-sdk/guide-ko/2-1.html

class UploadPostActivity : AppCompatActivity(), OnMapReadyCallback {

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }

    private lateinit var binding: ActivityUploadPostBinding
    private lateinit var mMapFragment: MapFragment
    private lateinit var mNaverMap: NaverMap
    private lateinit var mLocationSource: FusedLocationSource
    private var curMarker: Marker? = null

    private lateinit var mTitle: EditText
    private lateinit var mContent: EditText
    private lateinit var mRegister: TextView
    private lateinit var mPlaceDescription: EditText
    private lateinit var mIsClosed: CheckedTextView

    private var loadingDialog: LoadingDialog? = null

    private var levelStatus = MutableLiveData<SaveLevel>(SaveLevel.INIT)
    val dirPath = "${Global.getContext().cacheDir}/"

    enum class SaveLevel {
        INIT,
        UPLOAD_VIDEO,
        UPLOAD_GLB,
        SAVE
    }

    private val next = object : ToNextWork {
        override fun next() {
            val prefs = Global.prefs.getPrefs()
            prefs.edit().putBoolean("myProfileFragment_update", true).apply()

            val data = Intent()
            data.putExtra("finish save", true)
            setResult(RESULT_OK, data)
            finish()
        }
    }

    private var id = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_upload_post)

        // 네이버 지도
        val fm = supportFragmentManager
        mMapFragment = fm.findFragmentById(R.id.naverMap) as MapFragment?
            ?: MapFragment.newInstance().also {
                fm.beginTransaction().add(R.id.naverMap, it).commit()
            }
        mMapFragment.getMapAsync(this) // API를 호출하기 위한 인터페이스 역할의 NaverMap 객체 준비
        // 사용자 위치 가져오기
        mLocationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)

        // 뷰
        mTitle = binding.etTitle
        mContent = binding.etContent
        mRegister = binding.btnRegister
        mPlaceDescription = binding.etPlace
        mIsClosed = binding.chkClosed


        // 비공개 버튼
        mIsClosed.setOnClickListener {
            mIsClosed.toggle()
        }

        // 업로드 로딩
        if (loadingDialog == null) {
            loadingDialog = LoadingDialog(this, LoadingDialogModel("업로드 중입니다"))
            loadingDialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            loadingDialog!!.setCancelable(false)
        }

        // 뒤로가기 버튼
        binding.btnBack.setOnClickListener {
            finish()
        }



        // 완료 버튼
        mRegister.setOnClickListener {

            register()
        }

        levelStatus.observe(this) {
            when (it) {
                SaveLevel.INIT -> {
                    loadingDialog?.dismiss()
                    id = "${Global.userProfile?.accountId}${System.currentTimeMillis()}"
                }
                SaveLevel.UPLOAD_VIDEO -> {
                    loadingDialog?.show()
                    val videoFile = File(dirPath + "captures/temp.mp4")
                    uploadFile("$id.mp4", videoFile)
                }
                SaveLevel.UPLOAD_GLB -> {
                    val glbFile = File(dirPath + "draw.glb")
                    uploadFile("$id.glb", glbFile)
                }
                SaveLevel.SAVE -> {

                    val curLongitude = curMarker?.position?.longitude.toString()
                    val curLatitude = curMarker?.position?.latitude.toString()

                    val place = mPlaceDescription.text.toString()
                    val title = mTitle.text.toString()
                    val content = mContent.text.toString()
                    val isOriginal = "Y"
                    val viewdBy = if (mIsClosed.isChecked) "none" else "all"

                    runOnUiThread {
                        loadingDialog?.show()
                    }

                    RetrofitClient.getPostService().uploadPost(id, curLongitude, curLatitude, title, content, place, isOriginal, viewdBy).enqueue(BaseResponse(failureResponse = { r, t ->
                        runOnUiThread {
                            loadingDialog?.dismiss()
                        }

                        val file = File("${Global.getContext().cacheDir}/drawTemp.dat")
                        if (file.exists()) {
                            Timber.d("임시 저장한 파일 삭제")
                            file.delete()
                        }

                        next.next()

                        return@BaseResponse false
                    }) { response ->
                        runOnUiThread {
                            loadingDialog?.dismiss()
                        }

                        if (response.code() == 200) {

                            val file = File("${Global.getContext().cacheDir}/drawTemp.dat")
                            if (file.exists()) {
                                Timber.d("임시 저장한 파일 삭제")
                                file.delete()
                            }

                            Global.makeToast("포스트가 등록되었습니다")
                            next.next()

                            return@BaseResponse true
                        }

                        val file = File("${Global.getContext().cacheDir}/drawTemp.dat")
                        if (file.exists()) {
                            Timber.d("임시 저장한 파일 삭제")
                            file.delete()
                        }

                        next.next()
                        Global.makeToast("포스트 등록에 실패했습니다. 응답코드 : ${response.code()}")


                        return@BaseResponse true
                    })
                }
            }
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


    private fun register() {
        if (mTitle.text.isEmpty()) {
            Global.makeToast("제목을 입력해주세요")
            return
        }

        if (mContent.text.isEmpty()) {
            Global.makeToast("드로잉 소개를 입력해주세요")
            return
        }

        if (mPlaceDescription.text.isEmpty()) {
            Global.makeToast("위치명을 입력해주세요")
            return
        }

        if (curMarker == null) {
            Global.makeToast("그림 위치를 표시해주세요.")
            return
        }


        levelStatus.postValue(SaveLevel.UPLOAD_VIDEO)
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

        // 지도 클릭 이벤트 리스너 (길게 누를 경우)
        mNaverMap.setOnMapLongClickListener { _, coordinate ->
            Timber.d("${coordinate.latitude}, ${coordinate.longitude}")
            // 이전 마커 삭제
            if (curMarker != null) {
                curMarker!!.map = null
            }
            // 새로운 마커 생성
            val marker = Marker()
            marker.position = LatLng(coordinate.latitude, coordinate.longitude)
            marker.map = mNaverMap
            // 현재 마커 저장
            curMarker = marker
        }

    }

    fun uploadFile(fileName: String?, file: File?) {
        val awsCredentials: AWSCredentials =
            BasicAWSCredentials(
                JniInterface2.awsAccessKey(),
                JniInterface2.awsSecretKey()
            ) // IAM 생성하며 받은 것 입력
        val s3Client = AmazonS3Client(awsCredentials, Region.getRegion(Regions.AP_NORTHEAST_2))
        val transferUtility = TransferUtility.builder().s3Client(s3Client)
            .context(this@UploadPostActivity.applicationContext).build()
        TransferNetworkLossHandler.getInstance(this@UploadPostActivity.applicationContext)

        val dir by lazy {
            if (levelStatus.value == SaveLevel.UPLOAD_VIDEO) {
                return@lazy "original-videos/${fileName}"
            } else {
                return@lazy "drawings/${fileName}"
            }
        }

        val uploadObserver = transferUtility.upload(
            JniInterface2.awsBucketName(),
            dir,
            file,
        ) // (bucket api, file이름, file객체)

        uploadObserver.setTransferListener(object : TransferListener {
            override fun onStateChanged(id: Int, state: TransferState) {
                if (state === TransferState.COMPLETED) {
                    if (levelStatus.value == SaveLevel.UPLOAD_VIDEO) {
                        levelStatus.postValue(SaveLevel.UPLOAD_GLB)
                    } else if (levelStatus.value == SaveLevel.UPLOAD_GLB) {
                        levelStatus.postValue(SaveLevel.SAVE)
                    }
                }
            }

            override fun onProgressChanged(id: Int, current: Long, total: Long) {
                val done = (current.toDouble() / total * 100.0).toInt()
            }

            override fun onError(id: Int, ex: Exception) {
                levelStatus.postValue(SaveLevel.INIT)
                showErrorDialog()
            }
        })
    }

    private fun showErrorDialog() {
        val dialogModelForCloseActivity = ConfirmDialogModel("포스트 등록에 실패했습니다. 실패했습니다.")
        dialogModelForCloseActivity.clickYes = object : ToNextWork {
            override fun next() {

            }
        }

        val dialogForClose = ConfirmDialog(this@UploadPostActivity, dialogModelForCloseActivity);
        dialogForClose.show()
        dialogForClose.setCancelable(false)
    }
}