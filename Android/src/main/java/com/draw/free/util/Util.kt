package com.draw.free.util

import android.content.Context
import android.os.Build
import android.provider.Settings
import timber.log.Timber
import java.text.Format
import java.text.SimpleDateFormat
import java.util.*


class Util {
    companion object {
        fun calculateDisplayTime(date: Date): String {
            var value = (System.currentTimeMillis() - date.time) / 1000

            Timber.d("time :$value")
            if (value < 60) {
                if (value < 0) {
                    value = 0
                }

                return "$value 초 전"
            }

            value /= 60
            if (value < 60) { // 분
                return "$value 분 전"
            }

            value /= 60
            if (value < 60) { // 시간
                return "$value 시간 전"
            }

            value /= 24
            if (value < 24) { // 일
                return "$value 일 전"
            }


            // 수정 필요
            val format: Format = SimpleDateFormat("yyyy MM dd HH:mm:ss")
            return format.format(date).toString()
        }

        /**
         * device id 가져오기
         * @param context
         * @return
         */
        fun getDeviceId(context: Context): String? {
            return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        }

        /**
         * device 제조사 가져오기
         * @return
         */
        fun getManufacturer(): String? {
            return Build.MANUFACTURER
        }

        /**
         * device 브랜드 가져오기
         * @return
         */
        fun getDeviceBrand(): String? {
            return Build.BRAND
        }

        /**
         * device 모델명 가져오기
         * @return
         */
        fun getDeviceModel(): String? {
            return Build.MODEL
        }

        /**
         * device Android OS 버전 가져오기
         * @return
         */
        fun getDeviceOs(): String? {
            return Build.VERSION.RELEASE
        }

        /**
         * device SDK 버전 가져오기
         * @return
         */
        fun getDeviceSdk(): Int {
            return Build.VERSION.SDK_INT
        }
    }
}