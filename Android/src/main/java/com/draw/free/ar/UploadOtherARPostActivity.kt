package com.draw.free.ar


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.draw.free.Global
import com.draw.free.R
import com.draw.free.databinding.ActivityUploadPostBinding
import com.draw.free.interfaceaction.ToNextWork
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import timber.log.Timber
import java.io.File
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.*
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
import com.draw.free.ar.util.JniInterface2
import com.draw.free.dialog.ConfirmDialog
import com.draw.free.dialog.LoadingDialog
import com.draw.free.network.BaseResponse
import com.draw.free.network.RetrofitClient
import com.draw.free.viewmodel.ConfirmDialogModel
import com.draw.free.viewmodel.LoadingDialogModel
import com.naver.maps.map.MapFragment


class UploadOtherARPostActivity : AppCompatActivity() {

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }

    private lateinit var binding: ActivityUploadPostBinding


    private lateinit var mTitle: EditText
    private lateinit var mContent: EditText
    private lateinit var mRegister: TextView
    private lateinit var mPlaceDescription: EditText
    private lateinit var mIsClosed: CheckedTextView
    private lateinit var postId: String

    private var loadingDialog: LoadingDialog? = null

    private var levelStatus = MutableLiveData<SaveLevel>(SaveLevel.INIT)
    val dirPath = "${Global.getContext().cacheDir}/"


    enum class SaveLevel {
        INIT,
        UPLOAD_VIDEO,
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

        // 뷰
        mTitle = binding.etTitle
        mContent = binding.etContent
        mRegister = binding.btnRegister
        mPlaceDescription = binding.etPlace
        mIsClosed = binding.chkClosed

        binding.etPlace.visibility = View.INVISIBLE
        binding.txtNotice.visibility = View.INVISIBLE

        // 지도
        val fm = supportFragmentManager
        val mMapFragment = fm.findFragmentById(R.id.naverMap) as MapFragment
        mMapFragment.view?.visibility = View.GONE


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

        postId = intent.getStringExtra("postId") ?: "None"



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
                SaveLevel.SAVE -> {


                    val place = mPlaceDescription.text.toString()
                    val title = mTitle.text.toString()
                    val content = mContent.text.toString()
                    val isOriginal = "N"
                    val viewdBy = if (mIsClosed.isChecked) "none" else "all"

                    runOnUiThread {
                        loadingDialog?.show()
                    }

                    RetrofitClient.getPostService().uploadOtherARPost(id, title, content, postId, isOriginal, viewdBy).enqueue(BaseResponse(failureResponse = { r, t ->
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

    fun uploadFile(fileName: String?, file: File?) {
        val awsCredentials: AWSCredentials =
            BasicAWSCredentials(
                JniInterface2.awsAccessKey(),
                JniInterface2.awsSecretKey()
            )
        val s3Client = AmazonS3Client(awsCredentials, Region.getRegion(Regions.AP_NORTHEAST_2))
        val transferUtility = TransferUtility.builder().s3Client(s3Client)
            .context(this@UploadOtherARPostActivity.applicationContext).build()
        TransferNetworkLossHandler.getInstance(this@UploadOtherARPostActivity.applicationContext)

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
        )

        uploadObserver.setTransferListener(object : TransferListener {
            override fun onStateChanged(id: Int, state: TransferState) {
                if (state === TransferState.COMPLETED) {
                    if (levelStatus.value == SaveLevel.UPLOAD_VIDEO) {
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

        val dialogForClose = ConfirmDialog(this@UploadOtherARPostActivity, dialogModelForCloseActivity);
        dialogForClose.show()
        dialogForClose.setCancelable(false)
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


        if (loadingDialog == null) {
            Timber.d("null임")
        } else {
            Timber.d("null 아님")
            loadingDialog?.show()
        }

        levelStatus.postValue(SaveLevel.UPLOAD_VIDEO)
    }


}