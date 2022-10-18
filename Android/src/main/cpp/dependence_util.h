/* 개별 유틸 */

#ifndef AR_UTIL_H
#define AR_UTIL_H


#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>
#include <string>
#include "arcore_c_api.h"
#include "glm.h"

#ifdef ANDROID
#include <android/asset_manager.h>
    #include <jni.h>
    #include "jni_interface.h"


#include <android/log.h>


#ifndef LOGI
#define LOGI(...) \
  __android_log_print(ANDROID_LOG_INFO, "draw_ar", __VA_ARGS__)
#endif  // LOGI

#ifndef LOGE
#define LOGE(...) \
  __android_log_print(ANDROID_LOG_ERROR, "draw_ar", __VA_ARGS__)
#endif  // LOGE

#ifndef CHECK
#define CHECK(condition)                                                   \
  if (!(condition)) {                                                      \
    LOGE("*** CHECK FAILED at %s:%d: %s", __FILE__, __LINE__, #condition); \
    abort();                                                               \
  }
#endif  // CHECK

#ifndef CHECKANDTHROW
#define CHECKANDTHROW(condition, env, msg, ...)                            \
  if (!(condition)) {                                                      \
    LOGE("*** CHECK FAILED at %s:%d: %s", __FILE__, __LINE__, #condition); \
    util::ThrowJavaException(env, msg);                                    \
    return ##__VA_ARGS__;                                                  \
  }
#endif
#endif

namespace draw_ar {

    namespace util {

        class ScopedArPose {
        public:
            explicit ScopedArPose(const ArSession* session) {
                ArPose_create(session, nullptr, &pose_);
            }
            ~ScopedArPose() { ArPose_destroy(pose_); }
            ArPose* GetArPose() { return pose_; }
            // Delete copy constructors.
            ScopedArPose(const ScopedArPose&) = delete;
            void operator=(const ScopedArPose&) = delete;

        private:
            ArPose* pose_;
        };

        glm::vec3 GetPlaneNormal(const ArSession& ar_session, const ArPose& plane_pose);



        namespace Dependence_Setting {
#ifdef ANDROID
            extern AAssetManager *asset_manager;
#endif
        };


        // 파일 여는 코드, Platform 별로 차이가 있을 수 있으므로 적절하게 수정할 것
        bool openFile(const char *file_name, std::string *output_file);
        bool LoadPngFile(int target, const std::string& path);

        bool pLoadPngFile(int target, const std::string &path); // 나중에 통합하기

        std::string getCacheDir();


#ifdef ANDROID

        void ThrowJavaException(JNIEnv *env, const char *msg);

#endif

        void CheckGlError(const char *operation);
    }


}

#endif