package com.draw.free.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import androidx.paging.*
import com.draw.free.Global
import com.draw.free.R
import com.draw.free.dialog.YesOrNoDialog
import com.draw.free.interfaceaction.ToNextWork
import com.draw.free.model.Comment
import com.draw.free.network.BaseResponse
import com.draw.free.network.RetrofitClient
import com.draw.free.network.dao.CommentService
import com.draw.free.util.CommentPagingSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import okhttp3.ResponseBody

sealed class CommentEvents {
    data class Remove(val commentId: String) : CommentEvents()
    data class InsertItemHeader(val comment: Comment) : CommentEvents()
}

class CommentFragmentViewModel(val postId: String) : ViewModel() {

    val commentContent = MutableLiveData("");
    private val commentDao: CommentService = RetrofitClient.getCommentService()
    val changedData = MutableLiveData<Boolean>(false)


    private var modificationEvents = MutableStateFlow<List<CommentEvents>>(emptyList())


    private val combined =
        Pager(PagingConfig(pageSize = 10)) { CommentPagingSource(postId, pageIndex) }.flow.cachedIn(
            viewModelScope
        ).combine(modificationEvents) { pagingData, modifications ->
            modifications.fold(pagingData) { acc, event ->
                applyEvents(acc, event)
            }
        }

    fun clearEventsByRefresh() {
        modificationEvents.value = emptyList()
    }

    fun onViewEvent(event: CommentEvents) {
        modificationEvents.value += event
    }

    private fun applyEvents(
        paging: PagingData<Comment>,
        event: CommentEvents
    ): PagingData<Comment> {
        return when (event) {
            is CommentEvents.Remove -> {
                paging.filter { event.commentId != it.id }
            }
            is CommentEvents.InsertItemHeader -> {
                paging.insertHeaderItem(item = event.comment)
            }

        }
    }

    val pagingDataViewStates: LiveData<PagingData<Comment>> = combined.asLiveData()


    private val pageIndex = LinkedHashMap<String?, String?>()

    fun recommendComment(id: String, tv: TextView, iv: ImageView) {
        if (Global.userProfile == null) {
            Global.makeToast("유저 로그인이 필요한 서비스 입니다.")
            return
        }

            commentDao.recommendComment(id).enqueue(BaseResponse<String>() { response ->
                if (response.code() == 200) {
                    if (response.body() == "Like") {
                        val scale = AnimationUtils.loadAnimation(Global.getContext(), R.anim.swing)

                        tv.post { tv.text = "${tv.text.toString().toInt() + 1}" }

                        iv.post {
                            iv.startAnimation(scale)
                            iv.isActivated = true
                        }
                    } else {
                        val scale =
                            AnimationUtils.loadAnimation(Global.getContext(), R.anim.reverse_scale)
                        tv.post { tv.text = "${tv.text.toString().toInt() - 1}" }
                        iv.post {
                            iv.startAnimation(scale)
                            iv.isActivated = false
                        }
                    }

                    return@BaseResponse true


                }

                return@BaseResponse false
            })

    }

    fun deleteComment(context: Context, commentId: String) {
        val dialogViewModel = YesOrNoDialogModel("정말로 삭제하시겠습니까?")
        dialogViewModel.clickYes = object : ToNextWork {
            override fun next() {

                    commentDao.deleteComment(commentId).enqueue(BaseResponse<ResponseBody>() {
                        if (it.code() == 200) {
                            onViewEvent(CommentEvents.Remove(commentId))
                            changedData.postValue(true)
                            return@BaseResponse true
                        } else {
                            Global.makeToast("삭제에 실패하셨습니다.")
                            return@BaseResponse false
                        }
                    })
                }


        }

        val onemoreCheckDialog = YesOrNoDialog(context, dialogViewModel);
        onemoreCheckDialog.show()
    }

    private var handler = Handler(Looper.getMainLooper())

    fun addComment(clear: ToNextWork) {
        if (commentContent.value.isNullOrEmpty() && commentContent.value!!.trim().isNotEmpty()) {
            Global.makeToast("댓글을 입력해주세요.")
            return
        }


        commentDao.addComment(postId, commentContent.value.toString())
            .enqueue(BaseResponse<Comment>() { response ->
                if (response.code() == 200) {
                    onViewEvent(CommentEvents.InsertItemHeader(response.body()!!))
                    changedData.postValue(true)

                    handler.post {
                        clear.next()
                    }

                    return@BaseResponse true
                } else {
                    return@BaseResponse false
                }
            })

    }

}