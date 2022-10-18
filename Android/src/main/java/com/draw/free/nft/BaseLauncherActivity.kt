package com.draw.free.nft

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import timber.log.Timber

open class BaseLauncherActivity : AppCompatActivity() {

    private var canBackPressed = true
    protected lateinit var launcher: ActivityResultLauncher<Intent>

    override fun onBackPressed() {
        if (canBackPressed) {
            super.onBackPressed()
        } else {
            Timber.e("can't back pressed")
        }
    }

    protected fun setBackPressed(boolean : Boolean) {
        canBackPressed = boolean
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 스크린 꺼지지 않도록
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

    }

    override fun onStart() {
        super.onStart()
        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                if (data != null && data.getBooleanExtra("finish", false)) {
                    data.putExtra("finish", true)
                    setResult(RESULT_OK, data)
                    Timber.d("처리 성공")
                    finish()
                } else {
                    Timber.d("처리 안함")
                }
                // RESULT_OK일 때 실행할 코드...
            }
        }
    }
}