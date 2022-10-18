package com.draw.free.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import com.draw.free.Global
import com.draw.free.databinding.ActivitySettingBinding
import com.draw.free.dialog.YesOrNoDialog
import com.draw.free.interfaceaction.ToNextWork
import com.draw.free.network.BaseResponse
import com.draw.free.network.RetrofitClient
import com.draw.free.util.Util
import com.draw.free.viewmodel.YesOrNoDialogModel
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import timber.log.Timber

class SettingFragment : BaseInnerFragment<ActivitySettingBinding>() {
    companion object {
        private var INSTANCE: SettingFragment? = null;

        fun getInstance(): SettingFragment {
            if (INSTANCE == null) {
                INSTANCE = SettingFragment();
            }

            return INSTANCE!!;
        }
    }

    interface SettingAction {
        fun logout()
        fun settingPrivacy()
    }


    lateinit var settingAction: SettingAction

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val loginOut = binding.tvLogin

        binding.tvSetPrivacy.setOnClickListener {
            settingAction.settingPrivacy()
        }

        val activ = activity

        binding.license.setOnClickListener {
            OssLicensesMenuActivity.setActivityTitle("오픈소스 라이선스")
            startActivity(Intent(activ, OssLicensesMenuActivity::class.java))
        }

        binding.secession.setOnClickListener {
            val dialogModel = YesOrNoDialogModel("정말로 탈퇴하시겠습니까?\n탈퇴 시 포스트가 삭제되어 더이상 노출되지 않습니다." +
                    "\n팔로우 상태를 잃습니다." +
                    "\n지갑과 회원 아이디 간의 연결이 끊기게 됩니다." +
                    "\n자세한 것은 이용약관을 참고해주세요.\n")
            dialogModel.clickYes = object : ToNextWork {
                override fun next() {
                    RetrofitClient.getUserService().deleteUser().enqueue(BaseResponse { response ->
                        when (response.code()) {
                            200 -> {
                                Timber.d("서버 로그아웃 성공")
                                settingAction.logout()


                                return@BaseResponse true
                            }

                            400 -> {
                                Timber.e("서버 로그아웃 실패")
                                Timber.e("accessToken 없음")
                                settingAction.logout()

                                return@BaseResponse true
                            }
                        }




                        return@BaseResponse false
                    })
                }
            }

            YesOrNoDialog(requireContext(), dialogModel).show()
        }

        binding.terms.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            val uri: Uri = Uri.parse("https://sites.google.com/view/draweverywhere/%ED%99%88/termsofservice?authuser=0")
            intent.data = uri
            startActivity(intent)
        }

        binding.policy.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            val uri: Uri = Uri.parse("https://sites.google.com/view/draweverywhere/%ED%99%88/privacypolicy?authuser=0")
            intent.data = uri
            startActivity(intent)
        }

        loginOut.setOnClickListener {

            RetrofitClient.getUserService().logout().enqueue(BaseResponse { response ->
                when (response.code()) {
                    200 -> {
                        Timber.d("서버 로그아웃 성공")
                        settingAction.logout()


                        return@BaseResponse true
                    }

                    400 -> {
                        Timber.e("서버 로그아웃 실패")
                        Timber.e("accessToken 없음")
                        settingAction.logout()

                        return@BaseResponse true
                    }
                }


                return@BaseResponse false
            })
        }

        val content = StringBuilder()
        content.append("1. 문의 내용 : ")
        content.append("\n2. 사용자 정보")
        content.append("\n아이디 : ")
        content.append("${Global.userProfile?.accountId!!}")
        content.append("\n\n- 기기 정보")
        content.append("\n제조사 : ")
        content.append(Util.getManufacturer())
        content.append("\n브랜드 : ")
        content.append(Util.getDeviceBrand())
        content.append("\n모델명 : ")
        content.append(Util.getDeviceModel())
        content.append("\nAndroid OS 버전 : ")
        content.append(Util.getDeviceOs())
        content.append("\ndeviceSDK 버전 : ")
        content.append(Util.getDeviceSdk())

        binding.inquire.setOnClickListener {
            val intent = Intent(Intent.ACTION_SEND)
            val address = arrayOf("draweverywhereservice@gmail.com")
            intent.putExtra(Intent.EXTRA_EMAIL, address)

            intent.type = "plain/text"
            intent.setPackage("com.google.android.gm")

            intent.putExtra(Intent.EXTRA_SUBJECT, "DrawEveryWhere 문의사항입니다.")
            intent.putExtra(Intent.EXTRA_TEXT, content.toString())
            startActivity(intent)
        }

    }

    override fun onStart() {
        super.onStart()
        if (!isHidden) {
            setPreviousButton.set(isSet = true)
            showBottomNav.show(true)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        INSTANCE = null
    }


}