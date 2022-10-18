

#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>
#include <jni.h>
#include "draw_ar_application.h"

#define JNI_METHOD(return_type, method_name) \
  JNIEXPORT return_type JNICALL              \
      Java_com_draw_free_ar_util_JniInterface_##method_name


extern "C" {
namespace {
    static JavaVM *g_vm = nullptr;

    inline jlong jptr(draw_ar::DrawArApplication *native_draw_ar_application) {
        return reinterpret_cast<intptr_t>(native_draw_ar_application);
    }

    inline draw_ar::DrawArApplication *native(jlong ptr) {
        return reinterpret_cast<draw_ar::DrawArApplication *>(ptr);
    }

} // namespace

jint JNI_OnLoad(JavaVM *vm, void *) {
    g_vm = vm;
    return JNI_VERSION_1_6;
}

//createNativeApplication
JNI_METHOD(jlong, createNativeApplication)
(JNIEnv *env, jclass, jobject j_asset_manager) {
    AAssetManager *asset_manager = AAssetManager_fromJava(env, j_asset_manager);


    return jptr(new draw_ar::DrawArApplication(asset_manager));
}

//destroyNativeApplication
JNI_METHOD(void, destroyNativeApplication)
(JNIEnv *, jclass, jlong native_application) {
    delete native(native_application);
}

//onPause
JNI_METHOD(void, onPause)
(JNIEnv *, jclass, jlong native_application) {
    native(native_application)->OnPause();
}

//onResume
JNI_METHOD(void, onResume)
(JNIEnv *env, jclass, jlong native_application, jobject context,
 jobject activity) {
    native(native_application)->OnResume(env, context, activity);
}

//onContextCreated
JNI_METHOD(void, onContextCreated)
(JNIEnv *, jclass, jlong native_application) {
    native(native_application)->OnContextCreated();
}

//onDrawFrame
JNI_METHOD(void, onDrawFrame)
(JNIEnv *, jclass, jlong native_application) {
    native(native_application)->OnDrawFrame();
}

//onPreDrawFrame
JNI_METHOD(void, onPreDrawFrame)
(JNIEnv *, jclass, jlong native_application) {
    native(native_application)->OnPreDrawFrame();
}

//onDisplayGeometryChanged
JNI_METHOD(void, onDisplayGeometryChanged)
(JNIEnv *, jclass, jlong native_application, int displayRotation, int width, int height) {
    native(native_application)->OnDisplayGeometryChanged(displayRotation, width, height);
}

//onTouchScreen
JNI_METHOD(void, onTouchScreen)
(JNIEnv *, jclass , jlong native_application, float x, float y, int options) {
    if (options == 0) {
        native(native_application)->OnTouchScreen(x, y, draw_ar::MouseEvent_DOWN);
    } else if (options == 1) {
        native(native_application)->OnTouchScreen(x, y, draw_ar::MouseEvent_MOVE);
    } else if (options == 2) {
        native(native_application)->OnTouchScreen(x, y, draw_ar::MouseEvent_UP);
    }

}
//setLineColor
JNI_METHOD(void, setLineColor)
(JNIEnv *env, jclass clazz, jlong native_application, jint r, jint g, jint b) {
    native(native_application)->SetLineColor(r, g, b);
}

//clearDrawing
JNI_METHOD(void, clearDrawing)
(JNIEnv *env, jclass clazz, jlong native_application) {
    native(native_application)->ClearDrawing();
}

//setLineWidth
JNI_METHOD(void, setLineWidth)
(JNIEnv *env, jclass clazz, jlong native_application, jint width_percent) {
    native(native_application)->SetLineWidth(width_percent);
}

//setLineType
JNI_METHOD(void, setLineType)
(JNIEnv *env, jclass clazz, jlong native_application, jint lineType) {
    native(native_application)->SetLineType(lineType);
}

//undo
JNI_METHOD(void, undo)
(JNIEnv *env, jclass clazz, jlong native_application) {
    native(native_application)->UnDo();
}

//redo
JNI_METHOD(void, redo)
(JNIEnv *env, jclass clazz, jlong native_application) {
    native(native_application)->ReDo();
}


//setSaveMode
extern "C"
JNI_METHOD(void, setSaveMode)
(JNIEnv *env, jclass clazz, jlong native_applicatoin) {
    native(native_applicatoin)->SetMode(draw_ar::Mode_SAVE);
}

//setDrawMode
extern "C"
JNI_METHOD(void, setDrawMode)
(JNIEnv *env, jclass clazz, jlong native_applicatoin) {
    native(native_applicatoin)->SetMode(draw_ar::Mode_DRAW);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_rai_de_ar_util_JniInterface_testCallBack(JNIEnv *env, jclass clazz, jobject callback) {
    jclass iCallBackEmpty = env->GetObjectClass(callback);
    jmethodID method = env->GetMethodID(iCallBackEmpty, "callback", "()V");
    env->CallVoidMethod(callback, method);

}

//saveTempDraw
JNI_METHOD(void, saveTempDraw)
(JNIEnv *env, jclass clazz, jlong native_applicatoin) {
    native(native_applicatoin)->SaveStrokeInTemp();
}

//loadTempDraw
JNI_METHOD(void, loadTempDraw)
(JNIEnv *env, jclass clazz, jlong native_applicatoin) {
    native(native_applicatoin)->LoadStrokeByTemp();
}

} // extern C

JNIEnv *GetJniEnv() {
    JNIEnv *env;
    jint result = g_vm->AttachCurrentThread(&env, nullptr);
    return result == JNI_OK ? env : nullptr;
}

jclass FindClass(const char *classname) {
    JNIEnv *env = GetJniEnv();
    return env->FindClass(classname);
}



