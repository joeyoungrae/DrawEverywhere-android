

#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>
#include <jni.h>
#include "place_ar_application.h"
#include "jni_interface.h"

#define JNI_METHOD(return_type, method_name) \
  JNIEXPORT return_type JNICALL              \
      Java_com_draw_free_ar_util_JniInterfaceForPlace_##method_name


extern "C" {
namespace {
    static JavaVM *g_vm = nullptr;

    inline jlong jptr(draw_ar::PlaceArApplication *native_draw_ar_application) {
        return reinterpret_cast<intptr_t>(native_draw_ar_application);
    }

    inline draw_ar::PlaceArApplication *native(jlong ptr) {
        return reinterpret_cast<draw_ar::PlaceArApplication *>(ptr);
    }

} // namespace



JNI_METHOD(jlong, createNativeApplication)
(JNIEnv *env, jclass, jobject j_asset_manager) {
    AAssetManager *asset_manager = AAssetManager_fromJava(env, j_asset_manager);
    return jptr(new draw_ar::PlaceArApplication(asset_manager));
}

JNI_METHOD(void, destroyNativeApplication)
(JNIEnv *, jclass, jlong native_application) {
    delete native(native_application);
}

JNI_METHOD(void, onPause)
(JNIEnv *, jclass, jlong native_application) {
    native(native_application)->OnPause();
}

JNI_METHOD(void, onResume)
(JNIEnv *env, jclass, jlong native_application, jobject context,
 jobject activity) {
    native(native_application)->OnResume(env, context, activity);
}

JNI_METHOD(void, onContextCreated)
(JNIEnv *, jclass, jlong native_application) {
    native(native_application)->OnContextCreated();
}

JNI_METHOD(void, onDrawFrame)
(JNIEnv *, jclass, jlong native_application) {
    native(native_application)->OnDrawFrame();
}

JNI_METHOD(void, onPreDrawFrame)
(JNIEnv *, jclass, jlong native_application) {
    native(native_application)->OnPreDrawFrame();
}

JNI_METHOD(void, onDisplayGeometryChanged)
(JNIEnv *, jclass, jlong native_application, int displayRotation, int width, int height) {
    native(native_application)->OnDisplayGeometryChanged(displayRotation, width, height);
}

JNI_METHOD(void, onTouchScreen)
(JNIEnv *, jclass, jlong native_application, float x, float y, int options) {
    if (options == 0) {
        native(native_application)->OnTouchScreen(x, y, draw_ar::MouseEvent_DOWN);
    } else if (options == 1) {
        native(native_application)->OnTouchScreen(x, y, draw_ar::MouseEvent_MOVE);
    } else if (options == 2) {
        native(native_application)->OnTouchScreen(x, y, draw_ar::MouseEvent_UP);
    }
}

JNI_METHOD(void, resetRotation)
(JNIEnv *, jclass, jlong native_application) {
    native(native_application)->ResetRotation();
}


JNI_METHOD(void, loadObj)
(JNIEnv *env, jclass clazz, jlong native_application, jstring fileName) {
    const char *nativeString = env->GetStringUTFChars(fileName, 0);

    // use your string


    native(native_application)->loadGlb(nativeString);
    env->ReleaseStringUTFChars(fileName, nativeString);
}

JNI_METHOD(void, setMode)
(JNIEnv *env, jclass clazz, jlong native_application, jint value) {
    switch (value) {
        case 0 :
            native(native_application)->SetMode(draw_ar::Mode_INIT);
            break;
        case 1:
            native(native_application)->SetMode(draw_ar::Mode_SEARCH);
            break;
        case 2:
            native(native_application)->SetMode(draw_ar::Mode_PREVIEW);
            break;
        case 3:
            native(native_application)->SetMode(draw_ar::Mode_SEARCH);
            break;
    }
}

JNI_METHOD(void, setScale)
(JNIEnv *env, jclass clazz, jlong native_application, jint value) {

    native(native_application)->SetScale(value);
}

extern "C"
JNI_METHOD(void, setSaveMode)
(JNIEnv *env, jclass clazz, jlong native_applicatoin) {
    native(native_applicatoin)->SetMode(draw_ar::Mode_SAVE);
}




extern "C"
JNIEXPORT void JNICALL
Java_com_rai_de_ar_util_JniInterface_testCallBack(JNIEnv *env, jclass clazz, jobject callback) {
    jclass iCallBackEmpty = env->GetObjectClass(callback);
    jmethodID method = env->GetMethodID(iCallBackEmpty, "callback", "()V");
    env->CallVoidMethod(callback, method);
}

jint JNI_OnLoad(JavaVM *vm, void *) {
    g_vm = vm;
    return JNI_VERSION_1_6;
}

JNIEnv *GetJniEnv() {
    JNIEnv *env;
    jint result = g_vm->AttachCurrentThread(&env, nullptr);
    return result == JNI_OK ? env : nullptr;
}

jclass FindClass(const char *classname) {
    JNIEnv *env = GetJniEnv();
    return env->FindClass(classname);
}

}
