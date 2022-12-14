package com.draw.free.ar


import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaRecorder
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.draw.free.Global
import com.draw.free.R
import com.draw.free.ar.util.JniInterface
import com.draw.free.ar.util.ModeListener
import com.draw.free.ar.util.UndoRedoValidCheck
import com.draw.free.ar.viewModel.ARActivityViewModel
import com.draw.free.customView.RecordButton
import com.draw.free.databinding.ActivityArDrawBinding
import com.draw.free.dialog.AudioPermission
import com.draw.free.dialog.ConfirmDialog
import com.draw.free.dialog.YesOrNoDialog
import com.draw.free.interfaceaction.ICallback
import com.draw.free.interfaceaction.ToNextWork
import com.draw.free.viewmodel.ConfirmDialogModel
import com.draw.free.viewmodel.YesOrNoDialogModel
import com.google.ar.core.*
import com.skydoves.colorpickerview.ColorEnvelope
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.flag.BubbleFlag
import com.skydoves.colorpickerview.flag.FlagMode
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import com.uncorkedstudios.android.view.recordablesurfaceview.RecordableSurfaceView
import timber.log.Timber
import java.io.IOException


class ARDrawActivity : AppCompatActivity(), RecordableSurfaceView.RendererCallbacks {
    private lateinit var binding: ActivityArDrawBinding
    private lateinit var surface: RecordableSurfaceView
    private lateinit var launcher: ActivityResultLauncher<Intent>

    private var colorViews = Array<ImageView?>(4) { null }
    private var colorEdgeViews = Array<ImageView?>(4) { null }

    private var currentColorIndex = 0
    private var selectIndexColorViews = -1
    private var lastIndexColorViews = 0

    private var nativeApplication: Long = -1

    private var mWidth: Int = 0
    private var mHeight: Int = 0

    val viewModel: ARActivityViewModel by viewModels()


    private val makeDisplay = object : ICallback {
        @SuppressLint("Recycle")
        override fun callback(message: String?) {
            Timber.e("????????? ?????????")

            runOnUiThread {
                binding.displayDebug.text = message
            }
        }
    }

    private val modeListener = object : ModeListener {
        override fun InitModeCallBack() {
            runOnUiThread {
                binding.toolbarBottom.visibility = View.GONE
                binding.toolbarTop.visibility = View.GONE
                binding.recordButton.visibility = View.GONE
                binding.uiLine.uiLine.visibility = View.GONE
                binding.uiColor.uiColor.visibility = View.GONE
            }
        }

        override fun SearchModeCallBack() {
            runOnUiThread {
                binding.toolbarBottom.visibility = View.GONE
                binding.toolbarTop.visibility = View.GONE
                binding.recordButton.visibility = View.GONE
                binding.uiLine.uiLine.visibility = View.GONE
                binding.uiColor.uiColor.visibility = View.GONE

                // ????????? ????????? ????????? ?????????
                binding.txtNotice.visibility = View.VISIBLE
            }
        }

        override fun DrawModeCallBack() {
            runOnUiThread {
                binding.toolbarBottom.visibility = View.VISIBLE
                binding.toolbarTop.visibility = View.VISIBLE

                binding.txtNotice.visibility = View.GONE
                binding.recordButton.visibility = View.GONE
            }
        }

        override fun SaveModeCallBack() {
            runOnUiThread {
                binding.toolbarBottom.visibility = View.GONE
                binding.toolbarTop.visibility = View.GONE
                binding.uiLine.uiLine.visibility = View.GONE
                binding.uiColor.uiColor.visibility = View.GONE

                // ?????? ??????
                binding.recordButton.visibility = View.VISIBLE;
                binding.recordButton.isEnabled = true
            }
        }
    }

    /*
        ????????? ?????? ??? -> canUndo, false
        ?????????, ????????? ?????? ????????? undo??? ??? ??????
     */
    private val undoRedoListener = object : UndoRedoValidCheck {
        override fun undoCheck(canUndo: Boolean) {
            runOnUiThread {
                if (canUndo) {
                    binding.btnUndo.visibility = View.VISIBLE;
                    binding.btnClear.isEnabled = true
                    binding.btnTempSave.isEnabled = true
                    binding.btnFinish.isEnabled = true
                } else {
                    binding.btnUndo.visibility = View.INVISIBLE;
                    binding.btnClear.isEnabled = false
                    binding.btnTempSave.isEnabled = false
                    binding.btnFinish.isEnabled = false
                }
            }
        }

        /*
            undo??? ?????? Redu ????????? ??????
        */
        override fun redoCheck(canRedo: Boolean) {
            runOnUiThread {
                if (canRedo) {
                    binding.btnRedo.visibility = View.VISIBLE;
                } else {
                    binding.btnRedo.visibility = View.INVISIBLE;
                }
            }
        }

    }


    //<editor-fold desc="????????????">
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_ar_draw)



        // AR Core??? ??????????????? ????????????.
        checkAR()
        initialView()

        val dialogModelForCloseActivity = YesOrNoDialogModel("????????? ???????????????????\n??????????????? ???????????? ?????? ???????????????")
        dialogModelForCloseActivity.clickYes = object : ToNextWork {
            override fun next() {
                finish()
            }
        }

        val dialogForClose = YesOrNoDialog(this, dialogModelForCloseActivity);

        binding.btnBack.setOnClickListener {
            dialogForClose.show()
        }


        val dialogModel = YesOrNoDialogModel("????????? ????????? ??????????????????????")
        dialogModel.clickYes = object : ToNextWork {
            override fun next() {
                JniInterface.clearDrawing(nativeApplication)
                Global.makeToast("???????????? ?????????????????????")
            }

        }
        val testAlertDialog = YesOrNoDialog(this, dialogModel);

        //<editor-fold desc="setOnClickListener">
        binding.btnHelp.setOnClickListener {
            startActivity(Intent(baseContext, ARDrawTutorialActivity::class.java))
        }


        binding.btnClear.setOnClickListener {
            testAlertDialog.show()
        }


        val saveModel = YesOrNoDialogModel("?????? ????????? ??? ??????\n????????? ????????? ???????????? ???????????????")
        saveModel.clickYes = object : ToNextWork {
            override fun next() {
                JniInterface.saveTempDraw(nativeApplication);
            }

        }
        val saveDialog = YesOrNoDialog(this, saveModel);

        binding.btnTempSave.setOnClickListener {
            saveDialog.show()
        }

        binding.btnFinish.setOnClickListener {
            // ????????? ?????? ?????? ?????? ???????????? ??? ??? ????????? ???.
//            if (Global.userProfile == null) {
//                Global.makeToast("???????????? ??????????????? ???????????? ???????????????!")
//                return@setOnClickListener
//            }

            if (AudioPermission.hasRecordAudioPermission()) {
                JniInterface.setSaveMode(nativeApplication);
            } else {
                val activity = this
                val dialogModelConfirm = ConfirmDialogModel("????????? ????????? ?????? ?????? ?????? ????????? ???????????????")
                dialogModelConfirm.clickYes = object : ToNextWork {
                    override fun next() {
                        if (AudioPermission.shouldShowRequestPermissionRationale(activity)) {
                            AudioPermission.requestRecordAudioPermission(activity)
                        } else {
                            AudioPermission.launchPermissionSettings(activity)
                        }
                    }
                }

                val dialogConfirm = ConfirmDialog(this, dialogModelConfirm);
                dialogConfirm.show()
            }
        }

        binding.btnUndo.setOnClickListener {
            JniInterface.undo(nativeApplication)
        }

        binding.btnRedo.setOnClickListener {
            JniInterface.redo(nativeApplication)
        }

        // ??? ?????? ?????? ???
        binding.btnWidth.setOnClickListener {
            binding.btnWidth.toggle()
            if (binding.btnWidth.isChecked) {
                binding.uiColor.uiColor.visibility = View.INVISIBLE
                binding.uiLine.uiLine.visibility = View.VISIBLE
            } else {
                binding.uiLine.uiLine.visibility = View.INVISIBLE
            }
        }

        // ??? ?????? ?????? ???
        binding.btnColor.setOnClickListener {
            binding.btnColor.toggle()
            if (binding.btnColor.isChecked) {
                binding.uiLine.uiLine.visibility = View.INVISIBLE
                binding.uiColor.uiColor.visibility = View.VISIBLE
            } else {
                binding.uiColor.uiColor.visibility = View.INVISIBLE
            }
        }


        val loadModel = YesOrNoDialogModel("????????? ???????????? ????????? ??????\n?????? ???????????? ???????????????")
        loadModel.clickYes = object : ToNextWork {
            override fun next() {
                JniInterface.loadTempDraw(nativeApplication)
            }
        }
        val loadDialog = YesOrNoDialog(this, loadModel);

        binding.btnGet.setOnClickListener {
            loadDialog.show()
        }
        //</editor-fold>

        // pallet ??????
        setOnCLickListenerPallet()

        binding.recordButton.reset()
        binding.recordButton.setListener(object : RecordButton.Listener {
            override fun requestRecordingStart(): Boolean {
                prepareForRecording()
                val result = startRecording()
                if (result) {
                    Global.makeToast("????????? ???????????????.")
                }
                return result
            }

            override fun requestRecordingStop(): Boolean {
                return stopRecording()
            }

            override fun requestRecordCancel() {
                try {
                    surface.stopRecording()
                    binding.recordButton.reset()
                    prepareForRecording()
                    Global.makeToast("?????? ????????? ???????????????.")
                } catch (e: RuntimeException) {
                    Global.makeToast("?????? ????????? ???????????? ????????? ????????? ?????? ????????? ??????????????????.")
                }

            }

        })

        JniInterface.assetManager = Global.getAssetManager()//assets
        JniInterface.mModeListener = modeListener;
        JniInterface.mRedoUndoValidCheck = undoRedoListener;
        JniInterface.mCallback = makeDisplay
        

        nativeApplication = JniInterface.createNativeApplication(Global.getAssetManager());
        
        // TODO ????????? ????????? ????????? -> OnCreate?????? ????????? ?????????
        JniInterface.setLineWidth(nativeApplication, 35)
    }

    override fun onStart() {
        super.onStart()
        launcher = registerForActivityResult(
            StartActivityForResult()
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
            JniInterface.setDrawMode(nativeApplication);

        }
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

    override fun onResume() {
        super.onResume()

        JniInterface.onResume(nativeApplication, applicationContext, this)
        surface.resume()
    }

    override fun onPause() {
        super.onPause()
        if (binding.recordButton.isRecording()) {
            binding.recordButton.setRecording(false)
        }


        surface.pause()
        if (nativeApplication != -1L) {
            JniInterface.onPause(nativeApplication)
        }



        Timber.d("?????? ??????")
    }

    override fun onStop() {
        super.onStop()
        surface.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("?????? ?????????")


        synchronized(this) {
            JniInterface.destroyNativeApplication(nativeApplication)
        }

        nativeApplication = -1
        JniInterface.mModeListener = null;
        JniInterface.mRedoUndoValidCheck = null;
        JniInterface.mCallback = null;
    }
//</editor-fold>

    //<editor-fold desc="private function">
    private fun initialView() {
        surface = binding.surfaceView
        surface.rendererCallbacks = this
        surface.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY

    }

    private fun setOnCLickListenerPallet() {
        binding.uiLine.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                Timber.d("??? ?????? : ${binding.uiLine.seekBar.progress}")
                JniInterface.setLineWidth(nativeApplication, binding.uiLine.seekBar.progress);
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })

        binding.uiColor.btnMoreColor.setOnClickListener {
            val builder = ColorPickerDialog.Builder(this, R.style.AlertDialogCustom)
            builder.setPositiveButton(getString(R.string.ok), object : ColorEnvelopeListener {
                    override fun onColorSelected(envelope: ColorEnvelope?, fromUser: Boolean) {
                        val it = envelope!!.color
                        var mask = 255;
                        val b = it and mask
                        mask *= 256
                        val g = (it and mask) shr 8
                        mask *= 256
                        val r = (it and mask) shr 16

                        JniInterface.setLineColor(nativeApplication, r, g, b);

                        if (currentColorIndex != -1) {
                            colorEdgeViews[currentColorIndex]?.visibility = View.INVISIBLE
                        }

                        if (selectIndexColorViews != -1) {
                            colorViews[selectIndexColorViews]?.setBackgroundColor(
                                Color.rgb(
                                    r,
                                    g,
                                    b
                                )
                            )
                            colorEdgeViews[selectIndexColorViews]?.visibility = View.VISIBLE
                            currentColorIndex = selectIndexColorViews

                            selectIndexColorViews = -1
                            lastIndexColorViews = 0
                        } else {
                            colorEdgeViews[lastIndexColorViews]?.visibility = View.VISIBLE
                            currentColorIndex = lastIndexColorViews

                            colorViews[lastIndexColorViews]?.setBackgroundColor(Color.rgb(r, g, b))
                            lastIndexColorViews = (++lastIndexColorViews) % 4
                        }
                    }
                })
                .setNegativeButton(getString(R.string.cancel)) { p0, p1 ->
                    // ?????? ??????
                }
                .attachAlphaSlideBar(false)
                .attachBrightnessSlideBar(true)

            val colorPickerView = builder.colorPickerView
            val bubbleFlag = BubbleFlag(this)
            bubbleFlag.flagMode = FlagMode.FADE
            colorPickerView.flagView = bubbleFlag


            if (selectIndexColorViews != -1) {
                colorPickerView.setInitialColor((colorViews[selectIndexColorViews]?.background as ColorDrawable).color)
            } else {
                val i = if (lastIndexColorViews > 0) lastIndexColorViews - 1 else 0
                colorPickerView.setInitialColor((colorViews[i]?.background as ColorDrawable).color)
            }

            builder.show() // shows the dialog
        }


        val colorPicker = binding.uiColor.colorPickerView
        colorPicker.addOnColorSelectedListener {
            var mask = 255;
            val b = it and mask
            mask *= 256
            val g = (it and mask) shr 8
            mask *= 256
            val r = (it and mask) shr 16


            Timber.d("Color Selected  ${Integer.toHexString(it)}")
            Timber.d("r: $r  g: $g b : $b")

            JniInterface.setLineColor(nativeApplication, r, g, b);

            if (currentColorIndex != -1) {
                colorEdgeViews[currentColorIndex]?.visibility = View.INVISIBLE
            }


            if (selectIndexColorViews != -1) {
                colorEdgeViews[selectIndexColorViews]?.visibility = View.VISIBLE
                colorViews[selectIndexColorViews]?.setBackgroundColor(Color.rgb(r, g, b))
                currentColorIndex = selectIndexColorViews
                selectIndexColorViews = -1
                lastIndexColorViews = 0
            } else {
                colorEdgeViews[lastIndexColorViews]?.visibility = View.VISIBLE
                colorViews[lastIndexColorViews]?.setBackgroundColor(Color.rgb(r, g, b))
                currentColorIndex = lastIndexColorViews
                lastIndexColorViews = (++lastIndexColorViews) % 4
            }
        }

        colorViews[0] = binding.uiColor.color1
        colorViews[1] = binding.uiColor.color2
        colorViews[2] = binding.uiColor.color3
        colorViews[3] = binding.uiColor.color4

        colorEdgeViews[0] = binding.uiColor.color1Edge
        colorEdgeViews[1] = binding.uiColor.color2Edge
        colorEdgeViews[2] = binding.uiColor.color3Edge
        colorEdgeViews[3] = binding.uiColor.color4Edge

        for (i in colorViews.indices) {
            colorViews[i]?.setOnClickListener {
                val color = (it.background as ColorDrawable).color

                var mask = 255;
                val b = color and mask
                mask *= 256
                val g = (color and mask) shr 8
                mask *= 256
                val r = (color and mask) shr 16

                JniInterface.setLineColor(nativeApplication, r, g, b);
                //colorPicker.setColor(color, false)

                selectIndexColorViews = i
                colorEdgeViews[currentColorIndex]?.visibility = View.INVISIBLE
                currentColorIndex = selectIndexColorViews
                colorEdgeViews[selectIndexColorViews]?.visibility = View.VISIBLE



            }
        }
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
        val stoppedSuccessfully: Boolean = try {
            surface.stopRecording()
        } catch (e: RuntimeException) {
            false
        }

        if (stoppedSuccessfully) {
            viewModel.saveDrawing(object : ToNextWork {
                override fun next() {
                    runOnUiThread {
                        launcher.launch(Intent(baseContext, PlayBackActivity::class.java))

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
            prepareForRecording()
        }
        return startSuccessful
    }


//</editor-fold>

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) {

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    JniInterface.onTouchScreen(nativeApplication, event.x, event.y, 0)
                }

                MotionEvent.ACTION_MOVE -> {
                    JniInterface.onTouchScreen(nativeApplication, event.x, event.y, 1)
                }

                MotionEvent.ACTION_UP -> {
                    JniInterface.onTouchScreen(nativeApplication, event.x, event.y, 2)
                }
            }
        }

        return false
    }

    //<editor-fold desc="Recoradble SurfaceView Rendering">
    override fun onSurfaceCreated() {

    }

    override fun onSurfaceChanged(width: Int, height: Int) {
        mWidth = width
        mHeight = height

        val displayRotation = 0
        JniInterface.onDisplayGeometryChanged(nativeApplication, displayRotation, width, height)
    }

    override fun onSurfaceDestroyed() {

    }

    override fun onContextCreated() {
        JniInterface.onContextCreated(nativeApplication);
    }

    override fun onPreDrawFrame() {
        if (nativeApplication == 0L) {
            Timber.d("nativeApplication already Destroyed")
            return
        }

        synchronized(this) {
            JniInterface.onPreDrawFrame(nativeApplication);
        }
    }

    override fun onDrawFrame() {
        if (nativeApplication == 0L) {
            Timber.d("nativeApplication already Destroyed")
            return
        }

        synchronized(this) {
            JniInterface.onDrawFrame(nativeApplication);
        }
    }


//</editor-fold>


}
