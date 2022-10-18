package com.draw.free.dialog

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.draw.free.Global
import android.content.Intent
import android.net.Uri
import android.provider.Settings;


class CameraPermission {
    companion object {
        // const -> 컴파일 단게에서 결정 됨, val 런타임 단계에서 결정 됨
        private const val CAMERA_PERMISSION_CODE = 3;
        private const val CAMERA_PERMISSION = Manifest.permission.CAMERA

        // 카메라 권한이 있는지 확인하는 코드
        fun hasCameraPermission() : Boolean {
            return ContextCompat.checkSelfPermission(Global.getContext(), CAMERA_PERMISSION) == PackageManager.PERMISSION_GRANTED
        }

        // 필요한 권한이 있는지 확인하고 없을 시 요청함
        fun requestCameraPermission(activity: Activity) {
            ActivityCompat.requestPermissions(
                activity, Array<String>(1){ CAMERA_PERMISSION}, CAMERA_PERMISSION_CODE
            )
        }

        // 권한에 대한 근거를 보여줄 필요가 있는지 확인함
        fun shouldShowRequestPermissionRationale(activity: Activity) : Boolean {
            return ActivityCompat.shouldShowRequestPermissionRationale(activity, CAMERA_PERMISSION);
        }

        fun launchPermissionSettings(activity: Activity) {
            val intent = Intent()
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            intent.data = Uri.fromParts("package", activity.packageName, null)
            activity.startActivity(intent)
        }
    }


}