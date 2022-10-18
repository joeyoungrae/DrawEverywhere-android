package com.draw.free.ar

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import com.draw.free.Global
import com.draw.free.R
import com.draw.free.ar.util.JniInterface
import com.draw.free.databinding.ActivityPlayBackBinding
import com.draw.free.dialog.CameraPermission
import com.draw.free.dialog.WriteStoragePermission
import timber.log.Timber
import java.io.*


class PlayBackActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayBackBinding
    private lateinit var launcher: ActivityResultLauncher<Intent>
    private var postType = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_play_back)

        postType = intent.getStringExtra("type")?:"DRAW"

        val dir = "${Global.getContext().cacheDir}/captures/temp.mp4"
        val f = File(dir)
        if (!f.exists()) {
            Global.makeToast("파일이 없습니다.")
            finish()
        }

        val videoView = binding.videoView

        val uri = Uri.parse(f.path)

        videoView.setVideoURI(uri)
        videoView.requestFocus()

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnNext.setOnClickListener {
            if (postType == "DRAW") {
                launcher.launch(Intent(baseContext, UploadPostActivity::class.java))
            } else {
                val postId = intent.getStringExtra("postId")?:"None"
                val intent = Intent(baseContext, UploadOtherARPostActivity::class.java)
                intent.putExtra("postId", postId)
                launcher.launch(intent)
            }
        }

//        binding.btnSave.setOnClickListener {
//            // 화면 터치 불가
//            binding.btnBack.isClickable = false
//            binding.btnNext.isClickable = false
//            saveFile()
//        }
    }

    override fun onStart() {
        super.onStart()
        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                if (data != null && data.getBooleanExtra("finish save", false)) {
                    Timber.d("저장 성공")
                    val resultIntent = Intent();
                    resultIntent.putExtra("finish save", true)
                    setResult(RESULT_OK, resultIntent);
                    finish()
                } else {
                    Timber.d("그냥 끝")
                    finish()
                }
                // RESULT_OK일 때 실행할 코드...
            }
        }
    }

    override fun onResume() {
        super.onResume()

        binding.videoView.setOnPreparedListener(MediaPlayer.OnPreparedListener() {
            it.isLooping = true
            binding.videoView.start()
        })
    }

    override fun onPause() {
        super.onPause()
        binding.videoView.pause()
    }

    private fun saveFile() {
        if (!WriteStoragePermission.hasWritePermission()) {
            CameraPermission.requestCameraPermission(this)
            if (WriteStoragePermission.hasWritePermission()) {
                saveFile()
            } else {
                Global.makeToast("저장 권한이 필요합니다.")
            }
            return
        }

        Timber.d("저장 진행 중")

        val cacheInput = "${Global.getContext().cacheDir}/captures/temp.mp4"
        val f = File(cacheInput)

        val dstPath = ""
        val dst = File(dstPath)

        val input: InputStream = FileInputStream(f)
        input.use { it ->
            val out: OutputStream = FileOutputStream(dst)
            out.use { o ->
                // Transfer bytes from in to out
                val buf = ByteArray(1024)
                var len: Int
                while (it.read(buf).also { len = it } > 0) {
                    o.write(buf, 0, len)
                }
                // 화면 터치 가능
                binding.btnBack.isClickable = true
                binding.btnNext.isClickable = true
                Global.makeToast("동영상이 저장되었습니다")
            }
        }
    }
}