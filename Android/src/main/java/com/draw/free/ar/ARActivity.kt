package com.draw.free.ar

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.media.MediaRecorder
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.draw.free.Global
import com.draw.free.R
import com.draw.free.ar.util.JniInterfaceForPlace
import com.draw.free.ar.util.PlaceModeListener
import com.draw.free.ar.viewModel.ARActivityViewModel
import com.draw.free.customView.RecordButton
import com.draw.free.databinding.ActivityArBinding
import com.draw.free.dialog.AudioPermission
import com.draw.free.dialog.ConfirmDialog
import com.draw.free.interfaceaction.ICallback
import com.draw.free.interfaceaction.ToNextWork
import com.draw.free.viewmodel.ConfirmDialogModel
import com.google.ar.core.ArCoreApk
import com.uncorkedstudios.android.view.recordablesurfaceview.RecordableSurfaceView
import timber.log.Timber
import java.io.IOException
import java.lang.RuntimeException


class ARActivity : AppCompatActivity(), RecordableSurfaceView.RendererCallbacks {

    private var mHeight: Int = 0
    private var mWidth: Int = 0

    private lateinit var binding: ActivityArBinding
    private lateinit var surface: RecordableSurfaceView
    private var nativeApplication: Long = -1
    private lateinit var recordButton : RecordButton

    private lateinit var glbFileName: String
    private lateinit var postId: String

    private lateinit var launcher: ActivityResultLauncher<Intent>


    var mainHandler: Handler = Handler(Looper.getMainLooper())
    val viewModel: ARActivityViewModel by viewModels()


    private val makeDisplay = object : ICallback {
        @SuppressLint("Recycle")
        override fun callback(message: String?) {
            mainHandler.post {
                if (message != null) {
                    binding.displayMessage.visibility = View.VISIBLE
                    binding.displayMessage.text = message
                }
                val fadeOut = ObjectAnimator.ofFloat(binding.displayMessage, "alpha", 1f, .0f)
                fadeOut.duration = 2000

                val mAnimationSet = AnimatorSet()
                mAnimationSet.play(fadeOut)
                mAnimationSet.start();
            }
        }
    }

    private val modeListener = object : PlaceModeListener {
        override fun InitModeCallBack() {
            Timber.d("????????????")
            mainHandler.post { binding.displayMode.text = "?????? ??????"
                recordButton.visibility = View.INVISIBLE
            }
        }

        override fun SearchModeCallBack() {
            makeDisplay.callback("????????? ??????????????????.\n?????? ????????? ?????? ??? ????????? ??????????????????.")

            mainHandler.post {
                binding.displayMode.text = "?????? ???"
                binding.btnReSearch.visibility = View.INVISIBLE
                binding.switchMode.visibility = View.VISIBLE

                binding.btnPrevReset.visibility = View.INVISIBLE
                binding.btnPlus.visibility = View.INVISIBLE
                binding.btnMinus.visibility = View.INVISIBLE
                binding.btnFinish.visibility = View.INVISIBLE
                recordButton.visibility = View.INVISIBLE
                binding.btnBack.visibility = View.VISIBLE
                binding.recordButton.visibility = View.INVISIBLE;
            }

        }

        override fun PreViewModeCallBack() {
            makeDisplay.callback("????????? ??????????????? ????????? ????????? ??? ????????????.\n ????????? ???????????? ????????? search ??? ???????????????. ")

            mainHandler.post {
                binding.displayMode.text = "?????? ??????"
                binding.btnPrevReset.visibility = View.VISIBLE
                binding.btnReSearch.visibility = View.INVISIBLE
                binding.switchMode.visibility = View.VISIBLE
                binding.btnPlus.visibility = View.VISIBLE
                binding.btnMinus.visibility = View.VISIBLE
                recordButton.visibility = View.INVISIBLE
                binding.btnFinish.visibility = View.INVISIBLE
                binding.btnBack.visibility = View.VISIBLE
                binding.recordButton.visibility = View.INVISIBLE;
            }


        }

        override fun PlaceModeCallBack() {
            //makeDisplay.callback("????????? ?????????????????????.\n????????? ?????? ????????? ?????? ????????? ??? ????????????.\n????????? ?????? ???????????? ???????????????. ")
            makeDisplay.callback("????????? ?????????????????????.\n????????? ?????? ????????? ?????? ????????? ??? ????????????.")

            mainHandler.post {
                binding.displayMode.text = "?????? ??????"
                binding.switchMode.visibility = View.INVISIBLE
                binding.btnReSearch.visibility = View.VISIBLE
                binding.btnPrevReset.visibility = View.INVISIBLE
                recordButton.visibility = View.INVISIBLE
                binding.btnFinish.visibility = View.VISIBLE
                binding.btnBack.visibility = View.VISIBLE
                binding.recordButton.visibility = View.INVISIBLE;
            }
        }

        override fun SaveModeCallBack() {
            mainHandler.post {
                binding.displayMode.text = "?????? ??????"
                recordButton.visibility = View.VISIBLE

                binding.btnBack.visibility = View.INVISIBLE
                binding.recordButton.visibility = View.VISIBLE;
                binding.recordButton.isEnabled = true
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_ar)
        nativeApplication = JniInterfaceForPlace.createNativeApplication(Global.getAssetManager());

        recordButton = binding.recordButton
        glbFileName = intent.getStringExtra("id") ?: ""
        postId = intent.getStringExtra("postId") ?: ""

        if (glbFileName.isNullOrEmpty()) {
            Global.makeToast("???????????? ?????? ???????????????.")
            finish()
        }


        checkAR()
        initialView()

        JniInterfaceForPlace.mPlaceModeListener = modeListener
        JniInterfaceForPlace.NNDady = makeDisplay


        binding.btnPrevReset.setOnClickListener {
            JniInterfaceForPlace.resetRotation(nativeApplication)
        }

        binding.btnReSearch.setOnClickListener {
            JniInterfaceForPlace.setMode(nativeApplication, 1);
        }

        binding.switchMode.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                buttonView.text = "Preview"
                JniInterfaceForPlace.setMode(nativeApplication, 2);
            } else {
                buttonView.text = "Search"
                JniInterfaceForPlace.setMode(nativeApplication, 1);
            }
        }

        binding.btnPlus.setOnClickListener {
            JniInterfaceForPlace.setScale(nativeApplication, 0);
        }

        binding.btnMinus.setOnClickListener {
            JniInterfaceForPlace.setScale(nativeApplication, 1);
        }

        binding.btnBack.setOnClickListener {
            finish()
        }

        val activity = this

        binding.btnFinish.setOnClickListener {
//            if (Global.userProfile == null) {
//                Global.makeToast("???????????? ??????????????? ???????????? ????????????!")
//                return@setOnClickListener
//            }

            if (AudioPermission.hasRecordAudioPermission()) {
                JniInterfaceForPlace.setSaveMode(nativeApplication);
            } else {

                val dialogModelForCloseActivity = ConfirmDialogModel("????????? ????????? ?????? ?????? ?????? ????????? ???????????????")
                dialogModelForCloseActivity.clickYes = object : ToNextWork {
                    override fun next() {
                        if (AudioPermission.shouldShowRequestPermissionRationale(activity)) {
                            AudioPermission.requestRecordAudioPermission(activity)
                        } else {
                            AudioPermission.launchPermissionSettings(activity)
                        }
                    }
                }

                val dialogForClose = ConfirmDialog(this, dialogModelForCloseActivity);

                dialogForClose.show()
            }
        }

        binding.btnHelp.setOnClickListener {
            makeDisplay.callback()
        }

        binding.recordButton.reset()
        binding.recordButton.setListener(object : RecordButton.Listener {
            override fun requestRecordingStart(): Boolean {
                val result = startRecording()
                if (result) {
                    Timber.d("?????? ????????? ???????????????.")
                    Global.makeToast("????????? ???????????????.")
                }
                return result
            }

            override fun requestRecordingStop(): Boolean {
                Timber.d("?????? ????????? ????????????.")
                return stopRecording()
            }

            override fun requestRecordCancel() {
                try {
                    surface.stopRecording()
                    Timber.d("?????? ????????? ???????????????.")
                    Global.makeToast("?????? ????????? ???????????????.")
                } catch (e: RuntimeException) {
                    Global.makeToast("?????? ????????? ???????????? ????????? ????????? ?????? ????????? ??????????????????.")
                }

            }
        })
    }

    private fun prepareForRecording() {
        try {
            val outputFile = viewModel.createVideoOutputFile()

            val errorCode = MediaRecorder.OnErrorListener { _, what, extra ->
                Timber.e(
                    "ERROR",
                    "$what   $extra"
                );
            }

            Timber.e("__width : $mWidth, height : $mHeight")
            surface.initRecorder(outputFile, 1080, 2280, errorCode, null)
            Timber.e("__Complete__init__")

        } catch (e: IOException) {
            Timber.e("${e.printStackTrace()}")
        }
    }

    private fun stopRecording(): Boolean {
        var stoppedSuccessfully: Boolean
        try {
            stoppedSuccessfully = surface.stopRecording()
        } catch (e: RuntimeException) {
            stoppedSuccessfully = false
        }

        if (stoppedSuccessfully) {
            viewModel.saveDrawing(object : ToNextWork {
                override fun next() {
                    runOnUiThread {
                        val intent = Intent(baseContext, PlayBackActivity::class.java)
                        intent.putExtra("type", "POST")
                        intent.putExtra("postId", postId)

                        launcher.launch(intent)

                    }
                }
            })
        } else {
            binding.recordButton.reset()
            prepareForRecording()
        }

        return stoppedSuccessfully
    }

    private fun startRecording(): Boolean {
        val startSuccessful: Boolean = surface.startRecording()
        if (startSuccessful) {

        } else {
            Global.makeToast("Failed Recording Start")
            Timber.e("Failed Recording Start")
            prepareForRecording()
        }
        return startSuccessful
    }

    override fun onStart() {
        super.onStart()
        launcher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                if (data != null && data.getBooleanExtra("finish save", false)) {
                    Timber.d("?????? ??????")
                    finish()
                } else {
                    Timber.d("?????? ??????")
                }
                // RESULT_OK??? ??? ????????? ??????...
            }
            JniInterfaceForPlace.setMode(nativeApplication, 2);

        }
    }

    override fun onResume() {
        super.onResume()

        JniInterfaceForPlace.onResume(nativeApplication, applicationContext, this)


        JniInterfaceForPlace.loadObj(nativeApplication, glbFileName);
        surface.resume()
    }

    override fun onPause() {
        super.onPause()
        surface.pause()
        if (nativeApplication != -1L) {
            JniInterfaceForPlace.onPause(nativeApplication)
        }
    }

    override fun onStop() {
        super.onStop()
        surface.stop()
    }

    override fun onDestroy() {
        super.onDestroy()

        synchronized(this) {
            JniInterfaceForPlace.destroyNativeApplication(nativeApplication)
        }

        JniInterfaceForPlace.mPlaceModeListener = null
        JniInterfaceForPlace.NNDady = null
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) {

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    JniInterfaceForPlace.onTouchScreen(nativeApplication, event.x, event.y, 0)
                }

                MotionEvent.ACTION_MOVE -> {
                    JniInterfaceForPlace.onTouchScreen(nativeApplication, event.x, event.y, 1)
                }

                MotionEvent.ACTION_UP -> {
                    JniInterfaceForPlace.onTouchScreen(nativeApplication, event.x, event.y, 2)
                }
            }
        }

        return false
    }

    private fun checkAR() {
        // ????????? ARCore??? ??????????????? ?????? ?????? -> ???????????? ????????? ?????? ????????? ?????? ??????. (????????? ????????? ?????? ?????? ??????)
        val availability = ArCoreApk.getInstance().checkAvailability(this)
        Timber.d("?????? ?????? ?????? $availability")

        // ????????? ????????? ??????
        if (availability.isTransient) {
            Timber.d("?????? ???")
        }

        if (availability.isSupported) {
            Timber.d("AR ?????????")
        } else {
            Timber.d("AR ???????????? ??????")
        }
    }

    private fun initialView() {
        surface = binding.surfaceview
        surface.rendererCallbacks = this
        surface.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY

    }

    override fun onSurfaceCreated() {
        if (AudioPermission.hasRecordAudioPermission()) {
            prepareForRecording()
        }
    }

    override fun onSurfaceChanged(width: Int, height: Int) {
        mWidth = width
        mHeight = height

        val displayRotation = 0
        JniInterfaceForPlace.onDisplayGeometryChanged(
            nativeApplication,
            displayRotation,
            width,
            height
        )
    }

    override fun onSurfaceDestroyed() {
    }

    override fun onContextCreated() {
        JniInterfaceForPlace.onContextCreated(nativeApplication);
    }

    override fun onPreDrawFrame() {
        if (nativeApplication == 0L) {
            Timber.d("nativeApplication already Destroyed")
            return
        }

        synchronized(this) {
            JniInterfaceForPlace.onPreDrawFrame(nativeApplication); // <- ????????? ??? ????????? ?????? && loadGLB
        }
    }

    override fun onDrawFrame() {
        if (nativeApplication == 0L) {
            Timber.d("nativeApplication already Destroyed")
            return
        }

        synchronized(this) {
            JniInterfaceForPlace.onDrawFrame(nativeApplication);
        }
    }

}