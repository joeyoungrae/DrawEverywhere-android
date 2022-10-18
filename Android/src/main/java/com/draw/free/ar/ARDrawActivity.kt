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
            Timber.e("들어옴 데이터")

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

                // 주변을 비추어 주세요 안내문
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

                // 녹화 버튼
                binding.recordButton.visibility = View.VISIBLE;
                binding.recordButton.isEnabled = true
            }
        }
    }

    /*
        그림이 없을 때 -> canUndo, false
        이유는, 그림이 없기 때문에 undo할 수 없음
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
            undo한 경우 Redu 스택이 쌓임
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


    //<editor-fold desc="생명주기">
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_ar_draw)



        // AR Core를 지원하는지 확인한다.
        checkAR()
        initialView()

        val dialogModelForCloseActivity = YesOrNoDialogModel("정말로 나가시겠어요?\n현재까지의 드로잉이 모두 삭제됩니다")
        dialogModelForCloseActivity.clickYes = object : ToNextWork {
            override fun next() {
                finish()
            }
        }

        val dialogForClose = YesOrNoDialog(this, dialogModelForCloseActivity);

        binding.btnBack.setOnClickListener {
            dialogForClose.show()
        }


        val dialogModel = YesOrNoDialogModel("정말로 그림을 삭제하시겠어요?")
        dialogModel.clickYes = object : ToNextWork {
            override fun next() {
                JniInterface.clearDrawing(nativeApplication)
                Global.makeToast("드로잉이 삭제되었습니다")
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


        val saveModel = YesOrNoDialogModel("임시 저장을 할 경우\n기존에 저장된 데이터가 사라집니다")
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
            // 로그인 되지 않은 경우 로그인을 할 수 있어야 함.
//            if (Global.userProfile == null) {
//                Global.makeToast("드로잉을 저장하려면 로그인이 필요합니다!")
//                return@setOnClickListener
//            }

            if (AudioPermission.hasRecordAudioPermission()) {
                JniInterface.setSaveMode(nativeApplication);
            } else {
                val activity = this
                val dialogModelConfirm = ConfirmDialogModel("동영상 녹화를 위해 음성 녹음 권한이 필요합니다")
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

        // 선 굵기 설정 뷰
        binding.btnWidth.setOnClickListener {
            binding.btnWidth.toggle()
            if (binding.btnWidth.isChecked) {
                binding.uiColor.uiColor.visibility = View.INVISIBLE
                binding.uiLine.uiLine.visibility = View.VISIBLE
            } else {
                binding.uiLine.uiLine.visibility = View.INVISIBLE
            }
        }

        // 선 색상 설정 뷰
        binding.btnColor.setOnClickListener {
            binding.btnColor.toggle()
            if (binding.btnColor.isChecked) {
                binding.uiLine.uiLine.visibility = View.INVISIBLE
                binding.uiColor.uiColor.visibility = View.VISIBLE
            } else {
                binding.uiColor.uiColor.visibility = View.INVISIBLE
            }
        }


        val loadModel = YesOrNoDialogModel("저장된 드로잉을 불러올 경우\n현재 드로잉이 사라집니다")
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

        // pallet 관련
        setOnCLickListenerPallet()

        binding.recordButton.reset()
        binding.recordButton.setListener(object : RecordButton.Listener {
            override fun requestRecordingStart(): Boolean {
                prepareForRecording()
                val result = startRecording()
                if (result) {
                    Global.makeToast("녹화를 시작합니다.")
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
                    Global.makeToast("영상 녹화를 취소합니다.")
                } catch (e: RuntimeException) {
                    Global.makeToast("영상 녹화를 취소하기 위하여 멈추는 동안 문제가 발생했습니다.")
                }

            }

        })

        JniInterface.assetManager = Global.getAssetManager()//assets
        JniInterface.mModeListener = modeListener;
        JniInterface.mRedoUndoValidCheck = undoRedoListener;
        JniInterface.mCallback = makeDisplay
        

        nativeApplication = JniInterface.createNativeApplication(Global.getAssetManager());
        
        // TODO 적절한 위치로 바꾸기 -> OnCreate마다 발생은 이상함
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
                    Timber.d("저장 성공")
                    finish()
                } else {
                    Timber.d("저장 안함")
                }
                // RESULT_OK일 때 실행할 코드...
            }
            JniInterface.setDrawMode(nativeApplication);

        }
    }

    private fun checkAR() {
        // 장치가 ARCore를 지원하는지 여부 확인 -> 네트워크 리소스 쿼리 필요할 수도 있다. (캐싱된 결과는 즉시 사용 가능)
        val availability = ArCoreApk.getInstance().checkAvailability(this)
        Timber.d("설치 여부 확인 $availability")

        // 확인이 필요한 경우
        if (availability.isTransient) {
            Timber.d("처리 중")
        }

        if (availability.isSupported) {
            Timber.d("AR 지원함")
        } else {
            Timber.d("AR 지원하지 않음")
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



        Timber.d("세션 중지")
    }

    override fun onStop() {
        super.onStop()
        surface.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("세션 종료함")


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
                Timber.d("선 두께 : ${binding.uiLine.seekBar.progress}")
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
                    // 그냥 취소
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
