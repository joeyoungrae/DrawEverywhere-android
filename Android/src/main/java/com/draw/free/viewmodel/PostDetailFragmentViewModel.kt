package com.draw.free.viewmodel

import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.draw.free.Global
import com.draw.free.R
import com.draw.free.interfaceaction.ICallback
import com.draw.free.model.Nft
import com.draw.free.model.Post
import com.draw.free.network.BaseResponse
import com.draw.free.network.RetrofitClient
import com.draw.free.network.dao.PostService

import com.draw.free.util.CustomList

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import org.json.JSONObject
import timber.log.Timber
import java.io.DataInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class PostDetailFragmentViewModel : ViewModel() {
    lateinit var openPostEdit: ((post: Post) -> Unit)


    var startPlaceActivity: ICallback? = null
    var startMintingActivity: ICallback? = null
    lateinit var startPositionActivity: ((Post) -> Unit)

    private val _targetPost: MutableLiveData<Post> = MutableLiveData()
    val targetPost: LiveData<Post>
        get() = _targetPost

    var targetPostId: String = ""

    private val postDao: PostService = RetrofitClient.getPostService()

    private val _likeResponse: MutableLiveData<String> = MutableLiveData()
    val likeResponse: LiveData<String>
        get() = _likeResponse

    fun processButtonMore(item: Post) {
        openPostEdit(item)
    }

    fun removePost(postId: String, afterPost: () -> Unit) {

            postDao.deletePost(postId).enqueue(BaseResponse<ResponseBody>() { response ->
                if (response.code() == 200) {
                    afterPost()
                    return@BaseResponse true

                } else {
                    return@BaseResponse false
                }

            })

    }

    fun getNftByPostId(postId : String, after: (nft : Nft) -> Unit) {
        RetrofitClient.getSolanaService().getNftByPostId(postId).enqueue(BaseResponse { response ->
            if (response.code() == 200) {
                after(response.body()!!)
            }

            return@BaseResponse false
        })

    }


    fun openPlaceActivity(post: Post) {
        CoroutineScope(Dispatchers.IO).launch {

            val url = URL(post.drawing)
            val conn: HttpURLConnection = url.openConnection() as HttpURLConnection

            val dis = DataInputStream(conn.getInputStream())

            val d = File(Global.getContext().cacheDir, "model")

            Timber.d("dir : $d")

            if (!d.exists()) {
                d.mkdirs()
            }

            val fw = FileOutputStream(File("$d/${post.id}.glb"))

            val buffer = ByteArray(1024)

            val offset = 0
            var bytes: Int
            while (dis.read(buffer, offset, buffer.size).also { bytes = it } > 0) {
                fw.write(buffer, 0, bytes)
            }
            fw.close()

            startPlaceActivity?.callback(post.id)
        }
    }

    fun openMintingActivity(post: Post) {
        val json = JSONObject()
        json.put("postId", post.id)
        json.put("thumbnail", post.thumbnail)
        startMintingActivity?.callback(json.toString())
    }

    fun likePost(
        targetId: String,
        goodCount: TextView,
        iv: ImageView,
        customList: CustomList<Post>
    ) {
        if (Global.userProfile == null) {
            Global.makeToast("로그인이 필요한 기능입니다.")
            return
        }


            postDao.likePost(targetId).enqueue(BaseResponse<String>() { response ->
                if (response.code() == 200) {
                    val message = response.body()

                    Timber.d("성공 $message")

                    var value = goodCount.text.toString().toInt()
                    when (message) {
                        "UnLike" -> {
                            value -= 1
                            goodCount.post { goodCount.text = "$value" }
                            val scale = AnimationUtils.loadAnimation(
                                Global.getContext(),
                                R.anim.reverse_scale
                            )
                            iv.post {
                                iv.startAnimation(scale)
                                iv.isActivated = false
                            }

                            if (customList.findPositionByKey(targetId) != -1) {
                                customList.getDataByPosition(customList.findPositionByKey(targetId)).likes -= 1;
                                customList.getDataByPosition(customList.findPositionByKey(targetId)).myLike =
                                    false
                            }
                        }
                        "Like" -> {
                            value += 1
                            goodCount.post { goodCount.text = "$value" }
                            val scale =
                                AnimationUtils.loadAnimation(Global.getContext(), R.anim.swing)
                            iv.post {
                                iv.startAnimation(scale)
                                iv.isActivated = true
                            }

                            if (customList.findPositionByKey(targetId) != -1) {
                                customList.getDataByPosition(customList.findPositionByKey(targetId)).likes += 1;
                                customList.getDataByPosition(customList.findPositionByKey(targetId)).myLike =
                                    true
                            }
                        }
                    }
                    return@BaseResponse true
                } else {
                    return@BaseResponse false

                }
            })

    }


}