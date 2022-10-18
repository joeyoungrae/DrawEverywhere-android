
#include "dependence_util.h"

namespace draw_ar {
    namespace util {

        namespace Dependence_Setting {
#ifdef ANDROID
            AAssetManager *asset_manager;
#endif
        }

        // 사용시 Dependence_Setting의 asset_manager를 먼저 Setting 해줘야 함.
        bool openFile(const char *file_name, std::string *output_file) {
            AAsset *asset = AAssetManager_open(Dependence_Setting::asset_manager, file_name,
                                               AASSET_MODE_STREAMING);
            if (asset == nullptr) {
                LOGE("Failed open File %s", file_name);
                return false;
            }

            off_t file_size = AAsset_getLength(asset);
            output_file->resize(file_size);
            int ret = AAsset_read(asset, &output_file->front(), file_size);

            if (ret <= 0) {
                AAsset_close(asset);
                LOGE("Failed to read file: %s", file_name);
                return false;
            }

            AAsset_close(asset);
            return true;
        }

#ifdef ANDROID

        void ThrowJavaException(JNIEnv *env, const char *msg) {
            jclass c = env->FindClass("java/lang/RuntimeException");
            env->ThrowNew(c, msg);
        }

        std::string getCacheDir() {
            JNIEnv *env = GetJniEnv();

            static struct JNIData {
                jclass helper_class;
                jmethodID load_getDir_method;
            } jniIds = [env]() -> JNIData {

                constexpr char kHelperClassName[] =
                        "com/draw/free/ar/util/JniInterface";
                constexpr char kGetCacheDirMethodName[] = "getCacheDir";
                constexpr char kGetCacheDirMethodSignature[] =
                        "()Ljava/lang/String;";

                jclass helper_class = FindClass(kHelperClassName);
                if (helper_class) {
                    helper_class = static_cast<jclass>(env->NewGlobalRef(helper_class));
                    jmethodID load_getDir_method = env->GetStaticMethodID(
                            helper_class, kGetCacheDirMethodName, kGetCacheDirMethodSignature);

                    return {helper_class, load_getDir_method};
                }
                LOGE("dependence_util::Could not find Java helper class %s",
                     kHelperClassName);
                return {};
            }();

            if (!jniIds.helper_class) {
                LOGE("invalid Helper Class");
                return "";
            }

            jstring cacheDir = (jstring) env->CallStaticObjectMethod(jniIds.helper_class,
                                                                     jniIds.load_getDir_method);
            const char *path = env->GetStringUTFChars(cacheDir, 0);
            return std::string(path);
        }

        bool pLoadPngFile(int target, const std::string &path) {
            JNIEnv *env = GetJniEnv();

            static struct JNIData {
                jclass helper_class;
                jmethodID load_image_method;
                jmethodID load_texture_method;
            } jniIds = [env]() -> JNIData {

                constexpr char kHelperClassName[] =
                        "com/draw/free/ar/util/JniInterfaceForPlace";
                constexpr char kLoadImageMethodName[] = "loadImage";
                constexpr char kLoadImageMethodSignature[] =
                        "(Ljava/lang/String;)Landroid/graphics/Bitmap;";
                constexpr char kLoadTextureMethodName[] = "loadTexture";
                constexpr char kLoadTextureMethodSignature[] =
                        "(ILandroid/graphics/Bitmap;)V";
                jclass helper_class = FindClass(kHelperClassName);
                if (helper_class) {
                    helper_class = static_cast<jclass>(env->NewGlobalRef(helper_class));
                    jmethodID load_image_method = env->GetStaticMethodID(
                            helper_class, kLoadImageMethodName, kLoadImageMethodSignature);
                    jmethodID load_texture_method = env->GetStaticMethodID(
                            helper_class, kLoadTextureMethodName, kLoadTextureMethodSignature);
                    return {helper_class, load_image_method, load_texture_method};
                }
                LOGE("hello_ar::util::Could not find Java helper class %s",
                     kHelperClassName);
                return {};
            }();

            if (!jniIds.helper_class) {
                return false;
            }

            jstring j_path = env->NewStringUTF(path.c_str());
            jobject image_obj = env->CallStaticObjectMethod(
                    jniIds.helper_class, jniIds.load_image_method, j_path);

            if (j_path) {
                env->DeleteLocalRef(j_path);
            }

            env->CallStaticVoidMethod(jniIds.helper_class, jniIds.load_texture_method,
                                      target, image_obj);
            return true;
        }


        bool LoadPngFile(int target, const std::string &path) {
            JNIEnv *env = GetJniEnv();

            static struct JNIData {
                jclass helper_class;
                jmethodID load_image_method;
                jmethodID load_texture_method;
            } jniIds = [env]() -> JNIData {

                constexpr char kHelperClassName[] =
                        "com/draw/free/ar/util/JniInterface";
                constexpr char kLoadImageMethodName[] = "loadImage";
                constexpr char kLoadImageMethodSignature[] =
                        "(Ljava/lang/String;)Landroid/graphics/Bitmap;";
                constexpr char kLoadTextureMethodName[] = "loadTexture";
                constexpr char kLoadTextureMethodSignature[] =
                        "(ILandroid/graphics/Bitmap;)V";
                jclass helper_class = FindClass(kHelperClassName);
                if (helper_class) {
                    helper_class = static_cast<jclass>(env->NewGlobalRef(helper_class));
                    jmethodID load_image_method = env->GetStaticMethodID(
                            helper_class, kLoadImageMethodName, kLoadImageMethodSignature);
                    jmethodID load_texture_method = env->GetStaticMethodID(
                            helper_class, kLoadTextureMethodName, kLoadTextureMethodSignature);
                    return {helper_class, load_image_method, load_texture_method};
                }
                LOGE("hello_ar::util::Could not find Java helper class %s",
                     kHelperClassName);
                return {};
            }();

            if (!jniIds.helper_class) {
                return false;
            }

            jstring j_path = env->NewStringUTF(path.c_str());
            jobject image_obj = env->CallStaticObjectMethod(
                    jniIds.helper_class, jniIds.load_image_method, j_path);

            if (j_path) {
                env->DeleteLocalRef(j_path);
            }

            env->CallStaticVoidMethod(jniIds.helper_class, jniIds.load_texture_method,
                                      target, image_obj);
            return true;
        }

#endif

        // OpenGl Error 발생 체크
        void CheckGlError(const char *operation) {
            bool anyError = false;

            for (GLint error = glGetError(); error; error = glGetError()) {
                LOGE("%s() glError (0x%x)\n", operation, error);
                anyError = true;
            }
            if (anyError) {
                abort();
            }
        }

        glm::vec3 GetPlaneNormal(const ArSession &ar_session, const ArPose &plane_pose) {
            float plane_pose_raw[7] = {0.f};
            ArPose_getPoseRaw(&ar_session, &plane_pose, plane_pose_raw);
            glm::quat plane_quaternion(plane_pose_raw[3], plane_pose_raw[0],
                                       plane_pose_raw[1], plane_pose_raw[2]);
            // Get normal vector, normal is defined to be positive Y-position in local
            // frame.
            return glm::rotate(plane_quaternion, glm::vec3(0., 1.f, 0.));
        }

    }
}

