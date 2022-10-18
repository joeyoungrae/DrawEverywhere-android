package com.draw.free.setting

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.draw.free.Global
import com.draw.free.R
import com.draw.free.databinding.ActivitySeeSeedPhraseBinding
import com.draw.free.dialog.YesOrNoDialog
import com.draw.free.interfaceaction.ToNextWork
import com.draw.free.viewmodel.YesOrNoDialogModel
import timber.log.Timber

class SeeSeedPhraseActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySeeSeedPhraseBinding
    private lateinit var seedPhrase: String

    @OptIn(ExperimentalUnsignedTypes::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent.hasExtra("seedPhrase")) {
            seedPhrase = intent.getStringExtra("seedPhrase")!!
            Global.prefs.seedPhrase = seedPhrase
        } else {
            Timber.e("인텐트 파라미터 없음")
            finish()
        }

        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE);


        // 바인딩
        binding = DataBindingUtil.setContentView(this, R.layout.activity_see_seed_phrase)
        binding.tvSeedPhrase.text = seedPhrase



        // 종료 버튼
        binding.btnCancel.setOnClickListener {
            checkRealExit()
        }
    }

    override fun onBackPressed() {
        // backPressed를 막아버림
    }

    private fun checkRealExit() {
        val dialogModel = YesOrNoDialogModel("로그아웃을 하거나 시드구문은 분실하면 다시 찾을 수 없습니다.\n정말로 화면을 닫을까요?")
        dialogModel.clickYes = object : ToNextWork {
            override fun next() {
                onBackPressed()
                finish()
            }

        }
        val exitActivity = YesOrNoDialog(this, dialogModel);
        exitActivity.show()
    }
}