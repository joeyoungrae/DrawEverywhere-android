package com.draw.free.ar

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.draw.free.Global
import com.draw.free.R

import com.draw.free.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.widget.*
import com.draw.free.databinding.ActivityEditPostBinding
import com.draw.free.dialog.LoadingDialog
import com.draw.free.model.Post
import com.draw.free.network.BaseResponse
import com.draw.free.viewmodel.EditPostActivityViewModel
import com.draw.free.viewmodel.LoadingDialogModel
import timber.log.Timber

class EditPostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditPostBinding

    private lateinit var mTitle: EditText
    private lateinit var mContent: EditText
    private lateinit var mPlaceDescription: EditText
    private lateinit var mIsClosed : CheckedTextView

    private lateinit var viewModel : EditPostActivityViewModel

    private var loadingDialog : LoadingDialog? = null

    private lateinit var post : Post

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_post)

        if (intent == null || !intent.hasExtra("post") ) {
            Timber.e("값이 없습니다.")
            finish()
            return
        }
         val t = intent.getParcelableExtra<Post>("post")

        if (t == null) {
            Global.makeToast("유효하지 않은 포스트입니다")
            finish()
        }

        post = t!!

        viewModel = EditPostActivityViewModel()

        // 포스트 정보 가져오기
        viewModel.getPost.observe(this) { getSuccess ->
            if (!getSuccess) {
                Global.makeToast("유효하지 않은 포스트 입니다")
                finish()
                return@observe
            }

            // 원래 포스트 내용 채우기
            mTitle = binding.etTitle
            mContent = binding.etContent
            mIsClosed = binding.chkClosed
            mPlaceDescription = binding.etPlace


            mTitle.setText(viewModel.post.title)
            mContent.setText(viewModel.post.content)
            mPlaceDescription.setText((viewModel.post.place)?:"")
            when (viewModel.post.viewBy) {
                "all" -> mIsClosed.isChecked = false
                "none" -> mIsClosed.isChecked = true
            }

            // 로딩창
            if (loadingDialog == null) {
                val customProgressDialog = LoadingDialog(this, LoadingDialogModel("업로드 중입니다"));
                customProgressDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
                customProgressDialog.setCancelable(false);
            }

            // 뒤로가기 버튼
            binding.btnBack.setOnClickListener {
                finish()
            }

            // 완료 버튼
            binding.btnRegister.setOnClickListener {
                register()
            }
        }

        viewModel.setPost(post)

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

        loadingDialog?.show();
        // Retrofit 으로 전송

            RetrofitClient.getPostService().editPost(post.id, mTitle.text.toString(), mContent.text.toString(),
                if (mIsClosed.isChecked) "none" else "all", mPlaceDescription.text.toString(), "Y").enqueue(BaseResponse { response ->
                runOnUiThread {
                    loadingDialog?.dismiss();
                }

                if (response.isSuccessful && response.code() == 200) {
                    val prefs = Global.prefs.getPrefs()
                    prefs.edit().putBoolean("myProfileFragment_update", true).apply()
                    prefs.edit().putBoolean("homeMainFragment_update", true).apply()

                    Global.makeToast("포스트가 수정되었습니다")
                    finish();
                    return@BaseResponse true
                }

                return@BaseResponse false
            })

    }
}