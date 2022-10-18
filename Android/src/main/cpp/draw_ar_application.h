#ifndef DRAW_AR_APPLICATION_H
#define DRAW_AR_APPLICATION_H


#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>
#ifdef ANDROID

#include <android/asset_manager.h>
#include <jni.h>
#include <jni_interface.h>
#endif

#include <fstream>
#include <stack>
#include <memory>
#include <set>
#include <string>
#include <unordered_map>
#include <queue>
#include <atomic>
#include <mutex> // atomic 이 나은지 mutex 가 맞는지 모르겠음. 다만 atomic 배열이 원하는대로 사용이 안되서 어쩔 수 없이 mutex를 사용함.
#include <array>

#include "arcore_c_api.h"
#include "background_renderer.h"
#include "glm.h"
#include "stroke_model.h"
#include "dependence_util.h"
#include "line_renderer.h"
#include "point_renderer.h"
#include "saveGlb.h"



namespace draw_ar {
    enum MouseEvent {
        MouseEvent_DOWN,
        MouseEvent_MOVE,
        MouseEvent_UP
    };

    enum Mode {
        Mode_INIT,
        Mode_SEARCH,
        Mode_DRAW,
        Mode_SAVE,
    };

    class DrawArApplication {
    public:


#ifdef ANDROID
        struct ModeInterface {
            jobject InitModeCallBack;
            jobject SearchModeCallBack;
            jobject DrawModeCallBack;
            jobject SaveModeCallBack;

        };


        // Android 전용 - 생성자, 안드로이드가 아닌 경우 삭제할 것.
        explicit DrawArApplication(AAssetManager *asset_manager);

#else // else ANDROID
        // IOS 전용 - 생성자
        explicit DrawArApplication() = default;
#endif

        ~DrawArApplication();

        // Activity 생명주기 관련
#ifdef ANDROID

        void OnResume(JNIEnv *env, void *context, void *activity);

#else
        void OnResume();
#endif // else ANDROID

        void OnPause();


        // RecordableSurfaceView 관련
        void OnSurfaceCreated();

        void OnSurfaceDestroyed();

        void OnContextCreated();

        void OnPreDrawFrame();

        void OnDrawFrame();


        void ConfigureSession();


        void OnDisplayGeometryChanged(int displayRotation, int width, int height);

        void OnTouchScreen(float x, float y, MouseEvent event);

        void SetLineColor(unsigned int rValue, unsigned int gValue, unsigned int bValue);

        void SetLineWidth(unsigned int zeroTo100);
        void SetLineType(int type);

        void ClearDrawing();
        void SetMode(Mode mode);
        void SaveStrokeInTemp();

        void LoadStrokeByTemp();

#ifdef ANDROID
        void NotifyModeListener();
        void NotifyUndoRedoPossible();
#endif
        void MakeToastMessage(string message);
        void MakeDisplayMessage(string message);


        void UnDo();
        void ReDo();

    private:
        Mode mMode = Mode_INIT;

        ArSession *mArSession = nullptr;
        ArFrame *mArFrame = nullptr;
        BackgroundRenderer mBackgroundRenderer;
        LineRenderer mLineRenderer;

        bool mbInstallRequested = false;

        int mWidth = 1;
        int mHeight = 1;
        int mDisplayRotation = 0;

        glm::mat4 mViewMatrix;
        glm::mat4 mProjectionMatrix;

        std::vector<Stroke> mStrokes;
        std::stack<Stroke> mStackStroke;


        std::mutex accessTouchQueue;

        glm::vec2 mTouchQueue[10];
        unsigned int mTouchSize = 0;

        std::atomic<bool> mbNStroke { false };
        std::atomic<bool> mbTouchDown { false } ;

        struct LineProperty {
            float lineWidth = -1;
            glm::vec4 color = glm::vec4(-0.1f, 0.0f, 0.0f, 0.0f);
            unsigned int lineType = 0;
        } lineProperty;

        ArAnchor* mCurrentAnchor = nullptr;

#ifdef ANDROID
        ModeInterface mModeInterface;
#endif


    };


} //  namespace draw_ar


#endif // if_def_ANDROID
