package com.draw.free.viewmodel

import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.draw.free.Global
import com.draw.free.fragment.ProfileEditFragment
import com.draw.free.model.UserProfile
import com.draw.free.network.BaseResponse
import com.draw.free.network.RetrofitClient
import com.draw.free.network.dao.UserService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import timber.log.Timber
import java.io.File

class ProfileEditFragmentViewModel : ViewModel() {

    val mAccountId = MutableLiveData(Global.userProfile!!.accountId);
    val mNickname = MutableLiveData(Global.userProfile!!.pfName);
    val mDescription = MutableLiveData(Global.userProfile!!.pfDescription);
    var isRemoveImage: Boolean = false
    var resultUri: Uri? = null
        set(value) {
            isRemoveImage = false
            field = value
        }


    var targetUserProfile : UserProfile = Global.userProfile!!
    private val userDao: UserService = RetrofitClient.getUserService()


    lateinit var closeFragment: ProfileEditFragment.ProfileEditAction

    private val handler = Handler(Looper.getMainLooper())

    fun onCompleteEdit() {
            val accountIDIsChanged: MultipartBody.Part by lazy {
                if (mAccountId.value == null || mAccountId.value == Global.userProfile!!.accountId) {
                    MultipartBody.Part.createFormData("account_id_changed", "N")
                } else {
                    MultipartBody.Part.createFormData("account_id_changed", "Y")
                }
            }

            val nicknameIsChanged: MultipartBody.Part by lazy {
                if (mNickname.value == null || mNickname.value == Global.userProfile!!.pfName) {
                    MultipartBody.Part.createFormData("pf_name_changed", "N")
                } else {
                    MultipartBody.Part.createFormData("pf_name_changed", "Y")
                }
            }
            val descIsChanged: MultipartBody.Part by lazy {
                if (mDescription.value == null || mDescription.value == Global.userProfile!!.pfDescription) {
                    MultipartBody.Part.createFormData("pf_desc_changed", "N")
                } else {
                    MultipartBody.Part.createFormData("pf_desc_changed", "Y")
                }
            }
            val pictureChanged: MultipartBody.Part by lazy {
                if (resultUri == null && !isRemoveImage) {
                    Timber.d("_11")
                    MultipartBody.Part.createFormData("pf_pic_changed", "N")
                } else {
                    Timber.d("2_2")
                    MultipartBody.Part.createFormData("pf_pic_changed", "Y")
                }
            }

            val accountId: MultipartBody.Part by lazy {
                MultipartBody.Part.createFormData("account_id", mAccountId.value ?: "")
            }

            val name: MultipartBody.Part by lazy {
                MultipartBody.Part.createFormData("pf_name", mNickname.value ?: "")
            }
            val desc: MultipartBody.Part by lazy {
                MultipartBody.Part.createFormData("pf_description", mDescription.value ?: "")
            }
            val picture: MultipartBody.Part by lazy {
                when {
                    isRemoveImage -> {
                        MultipartBody.Part.createFormData("multipart/form-data".toMediaTypeOrNull().toString(), "")
                    }
                    resultUri != null -> {
                        val file = File(resultUri!!.path!!)
                        Timber.d("Path : ${resultUri!!.path!!}, type : Path : ${file.isFile}")
                        val fileReqBody = file.asRequestBody("image/*".toMediaType())
                        MultipartBody.Part.createFormData("pf_picture", file.name, fileReqBody)
                    }
                    else -> {
                        MultipartBody.Part.createFormData("multipart/form-data".toMediaTypeOrNull().toString(), "")
                    }
                }
            }

            userDao.setProfile(accountIDIsChanged, nicknameIsChanged, descIsChanged, pictureChanged, accountId, name, desc, picture).enqueue(BaseResponse { response ->
                if (response.code() == 200) {
                    Global.userProfile = response.body()!!

                    handler.post {
                        Global.makeToast("정상적으로 변경되었습니다.")
                        closeFragment.close()
                    }

                    return@BaseResponse true

                } else {
                    Timber.d("실패함")
                    return@BaseResponse false
                }
            })




    }

    fun onCancelEdit() {
        closeFragment.close()
    }

}