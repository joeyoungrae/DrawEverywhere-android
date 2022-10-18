#ifndef PLACE_AR_APPLICATION_H
#define PLACE_AR_APPLICATION_H

#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>
#include <android/asset_manager.h>
#include <jni.h>
#include <jni_interface.h>
#include <string>
#include <cmath>

#include "arcore_c_api.h"
#include "background_renderer.h"
#include "glm.h"
#include "stroke_model.h"
#include "dependence_util.h"
#include "obj_renderer.h"
#include "glb_object.h"
#include "plane_renderer.h"

using namespace std;

namespace draw_ar {
    enum MouseEvent {
        MouseEvent_DOWN,
        MouseEvent_MOVE,
        MouseEvent_UP
    };

    enum Mode {
        Mode_INIT,
        Mode_PREVIEW,
        Mode_SEARCH,
        Mode_PLACE,
        Mode_SAVE,
    };



    class PlaceArApplication {
    public:


        explicit PlaceArApplication(AAssetManager *asset_manager);
        ~PlaceArApplication();

        // Activity 생명주기 관련
        void OnResume(JNIEnv *env, void *context, void *activity);


        void OnPause();


        // RecordableSurfaceView 관련
        void loadGlb(string fileName);

        void OnSurfaceCreated();

        void OnSurfaceDestroyed();

        void OnContextCreated();

        void OnPreDrawFrame();

        void OnDrawFrame();


        void ConfigureSession();


        void OnDisplayGeometryChanged(int displayRotation, int width, int height);

        void OnTouchScreen(float x, float y, MouseEvent event);
        void MakeDisplayMessage(string message);

        void SetMode(Mode mode);

#ifdef ANDROID

        void NotifyModeListener();

#endif

        void MakeToastMessage(string message);

        void ResetRotation();

        void SetScale(int value);


    private:
        GlbObject mObj;
        bool mNewObjLoaded = false;

        Mode mMode = Mode_INIT;

        ArSession *mArSession = nullptr;
        ArFrame *mArFrame = nullptr;
        BackgroundRenderer mBackgroundRenderer;
        ObjRenderer mObjRenderer;
        hello_ar::PlaneRenderer mPlaneRenderer;

        bool mbInstallRequested = false;

        int mWidth = 1;
        int mHeight = 1;
        int mDisplayRotation = 0;

        glm::vec2 mStartPosition = glm::vec2(0.0);

        glm::mat4 mViewMatrix;
        glm::mat4 mProjectionMatrix;
        glm::mat4 mModelMatrix = glm::mat4(2);
        glm::vec3 mTranslateVector;

        ArAnchor *mCurrentAnchor = nullptr;

        std::atomic<float> mSize { 2 };
        std::atomic<float> mCurrentSize { 2};

        //float mCurrentSize = 2;
        std::atomic<glm::vec2> mAxisVector { glm::vec2(1.0) };
        std::atomic<float> mVectorSize { 0.0f };





#ifdef ANDROID
        //ModeInterface mModeInterface;
#endif


    };


} //  namespace draw_ar


#endif // if_def_ANDROID
