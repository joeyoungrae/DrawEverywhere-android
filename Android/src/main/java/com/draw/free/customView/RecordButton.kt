package com.draw.free.customView

import android.content.Context
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.Nullable
import com.draw.free.Global
import com.draw.free.R
import timber.log.Timber
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class RecordButton : FrameLayout {

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, @Nullable attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    private lateinit var mProgressBar: ProgressBar
    private lateinit var mDescription: TextView
    private lateinit var mStopView: ImageView

    val MAX_DURATION = TimeUnit.SECONDS.toMillis(30)

    // Timer
    private var mHandler: Handler? = null
    private var mRecordingRunnable: Runnable? = null
    private val mTick: Long = 20
    private var mRecordStartTime: Long = -1
    private var mCounterRunning = false


    private fun init() {
        inflate(context, R.layout.view_record_button, this)

        // viewBinding
        mProgressBar = findViewById(R.id.progress_bar)
        mDescription = findViewById(R.id.status_description)
        mStopView = findViewById(R.id.stop_view)

        //
        // set up timer
        //
        mHandler = Handler(Looper.getMainLooper())
        mRecordingRunnable = Runnable {
            val counterDuration: Long = getCurrentCounterDuration()
            val ratio = Math.min((counterDuration / MAX_DURATION.toFloat() * 100).toInt(), 100)

            mProgressBar.progress = ratio //setCurrentDuration(counterDuration, MAX_DURATION)
            if (counterDuration >= MAX_DURATION) {
                setRecording(false)
                mHandler?.removeCallbacks(mRecordingRunnable!!);
            } else if (mCounterRunning) {
                mHandler!!.postDelayed(mRecordingRunnable!!, mTick)
            }
        }
    }

    override fun performClick(): Boolean {
        when (mRecordingState) {
            RecordingState.NOT_RECORDING -> {
                startRecordingPrep()
                contentDescription = ""
                return super.performClick()
            }
            RecordingState.RECORDING -> {
                setRecording(false)
                return super.performClick()
            }
            else -> {

            }
        }

        return false
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        if (isEnabled) {
            when (event.action) {
                MotionEvent.ACTION_DOWN
                -> {
                    if (lockRecording.get()) {
                        // 연타 방어용
                        return false;
                    }
                    lockRecording.set(true)

                    // Touch 막아둠
                    handler.postDelayed(Runnable {
                        lockRecording.set(false) }, 1000)

                    when (mRecordingState) {
                        RecordingState.NOT_RECORDING -> {
                            // 사용자가 녹음 버튼과 상호 작용하고 녹음 프로세스를 초기화합니다.
                            mTapDownTime = SystemClock.elapsedRealtime()
                            mTapTwo = false
                            startRecordingPrep()
                        }
                        RecordingState.RECORDING_REQUESTED -> {
                            // 계속 녹음 준비, 첫 번째 탭으로 처리
                            mTapTwo = true

                        }
                        else ->
                            // 사용자가 녹음을 중지하기 위해 두 번째 탭을 하고 있습니다.
                            // 시작 후 충분한 시간이 경과하면 녹음을 중지할 수 있습니다.
                            mTapTwo = true
                    }
                }
                MotionEvent.ACTION_UP
                -> if (!mTapTwo && SystemClock.elapsedRealtime() - mTapDownTime < RECORDING_PREP_TIME) {
                    // 사용자가 버튼을 누른 상태에서 녹음 버튼에서 손가락을 떼면 취소
                } else if (!isTouchEventInBounds(this, event) && !mTapTwo) {
                    // 사용자가 녹음을 시작하기 위해 탭하고 손을 뗍니다.
                } else if (mRecordingState != RecordingState.RECORDING
                    || SystemClock.elapsedRealtime() - mTapDownTime < RECORDING_PREP_TIME
                ) {
                    if (!isTooShort.get()) {
                        isTooShort.set(true)
                        Global.makeToast("영상 길이가 너무 짧습니다.")
                        Timber.e("displayMessage")
                    }
                    // 사용자가 두 번 탭했거나 녹음이 완료되기 전에 보류를 해제했습니다.
                    // 시작
                    // 녹음 시작 허용
                } else {
                    // 사용자가 두 번째 탭했거나 원래 탭에서 손을 뗍니다.
                    setRecording(false)
                }
            }
        }
        return true
    }

    private fun isTouchEventInBounds(view: View, event: MotionEvent): Boolean {
        val x = event.x.toInt()
        val y = event.y.toInt()
        val outRect = Rect()
        val location = IntArray(2)
        view.getDrawingRect(outRect)
        view.getLocationOnScreen(location)
        return outRect.contains(x, y)
    }


    private fun startCounter() {
        mCounterRunning = true
        mProgressBar.setProgress(
            Math.min(
                (getCurrentCounterDuration() / MAX_DURATION * 100).toInt(),
                100
            )
        )
        mHandler!!.postDelayed(mRecordingRunnable!!, mTick)
    }

    private fun stopCounter() {
        mCounterRunning = false
        mHandler!!.removeCallbacks(mRecordingRunnable!!)
    }

    private val lockRecording = AtomicBoolean(false);
    private val isTooShort = AtomicBoolean(false);

    fun setRecording(recording: Boolean) {
        if (recording) {
            //켜기로 설정
            mRecordStartTime = SystemClock.elapsedRealtime()
            startCounter()

        } else {
            if (mRecordingState == RecordingState.RECORDING && mListener != null) {
                mListener?.requestRecordingStop()
                reset()
            }
        }
        mRecordingState = if (recording) RecordingState.RECORDING else RecordingState.NOT_RECORDING
    }

    fun cancelRecording() {
        if (mRecordingState == RecordingState.RECORDING && mListener != null) {
            mListener?.requestRecordCancel()
        }
        reset()
        mRecordingState = RecordingState.NOT_RECORDING
    }

    private fun startRecordingPrep() {
        mProgressBar.visibility = View.VISIBLE
        mStopView.visibility = View.VISIBLE
        mDescription.visibility = View.GONE

        if (mListener != null) {
            mRecordingState = RecordingState.RECORDING_REQUESTED
            val startSuccessful: Boolean = mListener?.requestRecordingStart() ?: false
            if (startSuccessful) {
                setRecording(true)
            } else {
                reset()
            }
        }
    }

    fun isRecording(): Boolean {
        return mRecordingState == RecordingState.RECORDING
    }

    fun reset() {
        stopCounter()
        mDescription.visibility = View.VISIBLE
        mStopView.visibility = View.GONE
        mProgressBar.visibility = View.GONE
        mRecordStartTime = -1
        mProgressBar.progress = 0
        isTooShort.set(false)

    }


    private fun getCurrentCounterDuration(): Long {
        return if (mRecordStartTime > 0) {
            SystemClock.elapsedRealtime() - mRecordStartTime
        } else {
            0
        }
    }


    private val MAX_TAP_LENGTH: Long = 40
    private var mTapDownTime: Long = -1
    private var mTapTwo: Boolean = false
    private val RECORDING_PREP_TIME: Long = 3000


    fun setListener(listener: Listener) {
        mListener = listener
    }

    private enum class RecordingState {
        NOT_RECORDING, RECORDING_REQUESTED, RECORDING
    }

    private var mListener: Listener? = null

    private var mRecordingState = RecordingState.NOT_RECORDING

    interface Listener {
        fun requestRecordingStart(): Boolean
        fun requestRecordingStop(): Boolean
        fun requestRecordCancel()
    }

}