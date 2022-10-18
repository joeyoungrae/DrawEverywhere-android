#include "draw_ar_application.h"

namespace draw_ar {


#ifdef ANDROID

    DrawArApplication::DrawArApplication(AAssetManager *assetManager) {
        util::Dependence_Setting::asset_manager = assetManager;
    }

#endif

    DrawArApplication::~DrawArApplication() {
        util::Dependence_Setting::asset_manager = nullptr;

        if (mCurrentAnchor != nullptr) {
            ArAnchor_release(mCurrentAnchor);
        }

        if (mArSession != nullptr) {
            ArSession_destroy(mArSession);
            ArFrame_destroy(mArFrame);
        }


        LOGI("Destroyed Application Class");

    }

    //<editor-fold desc="Activity 생명주기">
#ifdef ANDROID

    void DrawArApplication::OnResume(JNIEnv *env, void *context, void *activity) {
        if (mArSession == nullptr) {
            ArInstallStatus install_status;
            bool bRequestedInstall = !mbInstallRequested;

            CHECKANDTHROW(
                    ArCoreApk_requestInstall(env, activity, bRequestedInstall, &install_status) == AR_SUCCESS,
                    env, "Please install Google Play Services for AR (ARCore).");

            switch (install_status) {
                case AR_INSTALL_STATUS_INSTALLED:
                    break;
                case AR_INSTALL_STATUS_INSTALL_REQUESTED:
                    mbInstallRequested = true;
                    return;
            }

            CHECKANDTHROW(ArSession_create(env, context, &mArSession) == AR_SUCCESS,
                          env, "Failed to create AR session.");

            ConfigureSession();
            ArFrame_create(mArSession, &mArFrame);

            ArSession_setDisplayGeometry(mArSession, mDisplayRotation, mWidth,
                                         mHeight);

            if (mMode == Mode_INIT) {
                SetMode(Mode_SEARCH);
                //SetMode(Mode_DRAW);
            }

        }

        const ArStatus status = ArSession_resume(mArSession);
        CHECKANDTHROW(status == AR_SUCCESS, env, "Failed to resume AR session.");
    }

#else // 아직 NOT UPDATE
    void DrawArApplication::OnResume() {
        if (mArSession == nullptr) {
            ArInstallStatus install_status;
            bool bRequestedInstall = !mbInstallRequested;

            switch (install_status) {
                case AR_INSTALL_STATUS_INSTALLED:
                    break;
                case AR_INSTALL_STATUS_INSTALL_REQUESTED:
                    mbInstallRequested = true;
                    return;
            }

            ConfigureSession();
            ArFrame_create(mArSession, &mArFrame);

            ArSession_setDisplayGeometry(mArSession, mDisplayRotation, mWidth,
                                         mHeight);
        }

        const ArStatus status = ArSession_resume(mArSession);
    }
#endif

    void DrawArApplication::OnPause() {
        if (mArSession != nullptr) {
            ArSession_pause(mArSession);
        }
    }
    //</editor-fold>

    void DrawArApplication::MakeDisplayMessage(string message) {
        JNIEnv *env = GetJniEnv();

        static struct JNIData {
            jclass helper_class;
            jmethodID modeCallBack;
        } jniIds = [env]() -> JNIData {
            constexpr char helperClassName[] =
                    "com/draw/free/ar/util/JniInterface";
            constexpr char methodName[] = "makeDisplay";
            constexpr char methodSignature[] =
                    "(Ljava/lang/String;)V";

            jclass helper_class = FindClass(helperClassName);
            if (helper_class) {
                helper_class = static_cast<jclass>(env->NewGlobalRef(helper_class));
                jmethodID modeCallBack = env->GetStaticMethodID(
                        helper_class, methodName, methodSignature);
                return {helper_class, modeCallBack};
            }
            LOGE("util::Could not find Java helper class %s", helperClassName);
            return {};
        }();

        if (!jniIds.helper_class) {
            return;
        }

        jstring str;
        str = env->NewStringUTF(message.c_str());


        env->CallStaticVoidMethod(jniIds.helper_class, jniIds.modeCallBack, str);
        if (str) {
            env->DeleteLocalRef(str);
        }
    }

    //<editor-fold desc="RendererCallBack">
    void DrawArApplication::OnDrawFrame() {
        // 카메라 업데이트
        if (mArSession == nullptr) {
            LOGE("DrawARApplication::Session is Null");
            return;
        }


        if (ArSession_update(mArSession, mArFrame) != AR_SUCCESS) {
            LOGE("DrawARApplication::OnDrawFrame ArSession_update error");
        }

        ArCamera *arCamera;
        ArFrame_acquireCamera(mArSession, mArFrame, &arCamera);

        ArCamera_getProjectionMatrix(mArSession, arCamera, /*near=*/0.001f, /*far=*/100.f, glm::value_ptr(mProjectionMatrix));
        ArCamera_getViewMatrix(mArSession, arCamera, glm::value_ptr(mViewMatrix));


        glClear(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);

        mBackgroundRenderer.Draw(mArSession, mArFrame);

        ArTrackingState cameraTrackingState;
        ArCamera_getTrackingState(mArSession, arCamera, &cameraTrackingState);
        ArCamera_release(arCamera);

        // Tracking 하지 않는 경우 탈출 처리
        if (cameraTrackingState != AR_TRACKING_STATE_TRACKING) {
            //LOGE("DrawARApplication::OnDrawFrame Not Tracking");
            return;
        }

        //LineRenderer.Draw(mViewMatrix, mProjectionMatrix, mProjectionMatrix);


        //<editor-fold desc="Plane 추적 및 그리기">
        if (mMode == Mode_SEARCH) {
            ArTrackableList *planeList = nullptr;
            ArTrackableList_create(mArSession, &planeList);
            ArSession_getAllTrackables(mArSession, AR_TRACKABLE_PLANE, planeList);

            int32_t planeListSize = 0;
            ArTrackableList_getSize(mArSession, planeList, &planeListSize);

            for (int i = 0; i < planeListSize; ++i) {
                ArTrackable *arTrackable = nullptr;
                ArTrackableList_acquireItem(mArSession, planeList, i, &arTrackable);

                ArTrackableType arTrackableType = AR_TRACKABLE_NOT_VALID;


                ArTrackable_getType(mArSession, arTrackable, &arTrackableType);

                if (arTrackableType == AR_TRACKABLE_PLANE) {
                    ArPlane *arPlane = ArAsPlane(arTrackable);

                    ArPose *planePose = nullptr;
                    ArPose_create(mArSession, nullptr, &planePose);

                    ArPlane_getCenterPose(mArSession, arPlane, planePose);

                    ArAnchor *anchor = nullptr;
                    ArTrackable_acquireNewAnchor(mArSession, arTrackable, planePose, &anchor);
                    if (anchor != nullptr) {
                        mCurrentAnchor = anchor;
                        LOGE("anchor 부착 성공");
                        SetMode(Mode_DRAW);
                        break;
                    } else {
                        LOGE("anchor 부착 실패");
                    }

                    ArPose_destroy(planePose);
                }


                ArTrackable_release(arTrackable);
            }

            ArTrackableList_destroy(planeList);
            planeList = nullptr;
        }
        //</editor-fold>

        if (mCurrentAnchor != nullptr) {
            glm::mat4 modelMatrix(0.0f);
            ArPose *pose;
            ArPose_create(mArSession, nullptr, &pose);

            const ArAnchor &anchor = *mCurrentAnchor;

            ArAnchor_getPose(mArSession, &anchor, pose);
            ArPose_getMatrix(mArSession, pose, glm::value_ptr(modelMatrix));

            ArPose_destroy(pose);
            mLineRenderer.Draw(mViewMatrix, mProjectionMatrix, modelMatrix);
        }

    }


    void DrawArApplication::OnPreDrawFrame() {
        // Stroke가 마구잡이로 생기는 문제 발생

        if (mbNStroke.load()) {
            mbNStroke.store(false);
            mStrokes.emplace_back(Stroke(lineProperty.lineWidth, lineProperty.color, lineProperty.lineType));
        }


        if (mTouchSize > 0) {
            while (!mStackStroke.empty()) {
                mStackStroke.pop();
            }
        }


        accessTouchQueue.lock();
        for (unsigned int i = 0; i < mTouchSize; ++i) {
            glm::vec2 touchCoordinate = mTouchQueue[i];
            glm::vec3 p = util::ScreenToWorldCoordinate(mViewMatrix, mProjectionMatrix, touchCoordinate.x, touchCoordinate.y, (float) mWidth, (float) mHeight);
            Stroke *lastStroke = &mStrokes.back();
            lastStroke->AddPoint(p);
        }
        if (mTouchSize > 0 && mStrokes.back().GetPoints().size() < 3 && !mbTouchDown) {
            mStrokes.pop_back();
            LOGE("비어서 버림");
        }

        mTouchSize = 0;
        accessTouchQueue.unlock();


        NotifyUndoRedoPossible();

        mLineRenderer.UploadToBuffer(mStrokes);

    }

    void DrawArApplication::OnSurfaceCreated() {

    }

    void DrawArApplication::OnSurfaceDestroyed() {
        // ClearGL
        // ClearReference
    }

    void DrawArApplication::OnContextCreated() {
        mBackgroundRenderer.CreateOnGLThread();
        mLineRenderer.CreateOnGLThread();


        ArSession_setCameraTextureName(mArSession, mBackgroundRenderer.GetTextureId());

    }
    //</editor-fold>

    void DrawArApplication::ConfigureSession() {
        ArConfig *arConfig = nullptr;
        ArConfig_create(mArSession, &arConfig);
        ArConfig_setDepthMode(mArSession, arConfig, AR_DEPTH_MODE_DISABLED);

        if (mMode == Mode_SEARCH) {
            ArConfig_setPlaneFindingMode(mArSession, arConfig, AR_PLANE_FINDING_MODE_HORIZONTAL);
        } else {
            ArConfig_setPlaneFindingMode(mArSession, arConfig, AR_PLANE_FINDING_MODE_DISABLED);
        }


        CHECK(arConfig);
        CHECK(ArSession_configure(mArSession, arConfig) == AR_SUCCESS);
        ArConfig_destroy(arConfig);
    }

    void DrawArApplication::OnDisplayGeometryChanged(int displayRotation, int width, int height) {
        LOGI("OnSurfaceChanged(%d, %d)", width, height);

       // glViewport(0, 0, width, height);
        mDisplayRotation = displayRotation;
        mWidth = width;
        mHeight = height;
        if (mArSession != nullptr) {
            ArSession_setDisplayGeometry(mArSession, displayRotation, width, height);
        }
    }

    // 렌더링 확인 후에 onTouchScreen + 3 작업으로 나눠야 함.

    void DrawArApplication::OnTouchScreen(float x, float y, MouseEvent mouseEvent) {
        switch (mouseEvent) {
            case MouseEvent_DOWN:
                // Draw Phase일 때만 그림을 그림
                mbNStroke.store(mMode == Mode_DRAW);
                mbTouchDown.store(mMode == Mode_DRAW);
                break;

            case MouseEvent_MOVE:
                if (mbTouchDown.load()) {
                    accessTouchQueue.lock();
                    unsigned int index = mTouchSize >= 10 ? 9 : mTouchSize;
                    mTouchQueue[index] = glm::vec2(x, y);
                    ++mTouchSize;
                    accessTouchQueue.unlock();
                }
                break;

            case MouseEvent_UP:
                mbTouchDown.store(false);
                break;

            default:
                break;
        }
    }

    void DrawArApplication::SetMode(Mode mode) {
        // 저장시 파일저장;
        mMode = mode;
        if (mMode == Mode_SAVE) {
            LOGI("SAVE MODE");
            saveGlb o(mStrokes);
            o.saveGlbFile();
            LOGI("SAVE COMPLETE");
        }


        ConfigureSession();
#ifdef ANDROID
        NotifyModeListener();
#endif
    }

    void DrawArApplication::SetLineColor(unsigned int rValue, unsigned int gValue, unsigned int bValue) {
        rValue &= 255;
        gValue &= 255;
        bValue &= 255;

        lineProperty.color = glm::vec4(rValue / 255.0f, gValue / 255.0f, bValue / 255.0f, 1.0f);
    }

    void DrawArApplication::SetLineWidth(unsigned int zeroTo100) {

        int validPercent = zeroTo100;
        float minSize = Stroke::MIN_LINE_WIDTH;
        float maxSize = Stroke::MAX_LINE_WIDTH;

        lineProperty.lineWidth = (maxSize - minSize) * (validPercent / 100.0f) + minSize;

    }

    void DrawArApplication::ClearDrawing() {
        mStrokes.clear();
        while (!mStackStroke.empty()) {
            mStackStroke.pop();
        }

        NotifyUndoRedoPossible();
    }

#ifdef ANDROID

    void DrawArApplication::NotifyModeListener() {
        JNIEnv *env = GetJniEnv();

        static struct JNIData {
            jclass helper_class;
            jmethodID modeCallBack;
        } jniIds = [env]() -> JNIData {
            constexpr char helperClassName[] =
                    "com/draw/free/ar/util/JniInterface";
            constexpr char methodName[] = "modeCallBack";
            constexpr char methodSignature[] =
                    "(Ljava/lang/String;)V";

            jclass helper_class = FindClass(helperClassName);
            if (helper_class) {
                helper_class = static_cast<jclass>(env->NewGlobalRef(helper_class));
                jmethodID modeCallBack = env->GetStaticMethodID(
                        helper_class, methodName, methodSignature);
                return {helper_class, modeCallBack};
            }
            LOGE("util::Could not find Java helper class %s", helperClassName);
            return {};
        }();

        if (!jniIds.helper_class) {
            return;
        }

        jstring modeType;

        switch (mMode) {
            case Mode_INIT:
                modeType = env->NewStringUTF("INIT");
                break;
            case Mode_SEARCH:
                modeType = env->NewStringUTF("SEARCH");
                break;
            case Mode_DRAW:
                modeType = env->NewStringUTF("DRAW");
                break;
            case Mode_SAVE:
                modeType = env->NewStringUTF("SAVE");
                break;
        }

        env->CallStaticVoidMethod(jniIds.helper_class, jniIds.modeCallBack, modeType);
        if (modeType) {
            env->DeleteLocalRef(modeType);
        }
    }

    void DrawArApplication::MakeToastMessage(string message) {
        JNIEnv *env = GetJniEnv();

        static struct JNIData {
            jclass helper_class;
            jmethodID makeToastMessage;
        } jniIds = [env]() -> JNIData {
            constexpr char helperClassName[] =
                    "com/draw/free/ar/util/JniInterface";
            constexpr char methodName[] = "makeToastMessage";
            constexpr char methodSignature[] =
                    "(Ljava/lang/String;)V";

            jclass helper_class = FindClass(helperClassName);
            if (helper_class) {
                helper_class = static_cast<jclass>(env->NewGlobalRef(helper_class));
                jmethodID modeCallBack = env->GetStaticMethodID(
                        helper_class, methodName, methodSignature);
                return {helper_class, modeCallBack};
            }
            LOGE("util::Could not find Java helper class %s", helperClassName);
            return {};
        }();

        if (!jniIds.helper_class) {
            return;
        }

        jstring jMessage = env->NewStringUTF(message.c_str());

        env->CallStaticVoidMethod(jniIds.helper_class, jniIds.makeToastMessage, jMessage);
        if (jMessage) {
            env->DeleteLocalRef(jMessage);
        }
    }

    void DrawArApplication::NotifyUndoRedoPossible() {
        JNIEnv *env = GetJniEnv();

        static struct JNIData {
            jclass helper_class;
            jmethodID undoRedoCallBack;
        } jniIds = [env]() -> JNIData {
            constexpr char helperClassName[] =
                    "com/draw/free/ar/util/JniInterface";
            constexpr char methodName[] = "undoRedoCallBack";
            constexpr char methodSignature[] =
                    "(ZZ)V";

            jclass helper_class = FindClass(helperClassName);
            if (helper_class) {
                helper_class = static_cast<jclass>(env->NewGlobalRef(helper_class));
                jmethodID undoRedoCallBack = env->GetStaticMethodID(
                        helper_class, methodName, methodSignature);
                return {helper_class, undoRedoCallBack};
            }
            LOGE("util::Could not find Java helper class %s", helperClassName);
            return {};
        }();

        if (!jniIds.helper_class) {
            return;
        }

        env->CallStaticVoidMethod(jniIds.helper_class, jniIds.undoRedoCallBack, true, !mStrokes.empty());
        env->CallStaticVoidMethod(jniIds.helper_class, jniIds.undoRedoCallBack, false, !mStackStroke.empty());
    }

#endif // ANDROID

    void DrawArApplication::UnDo() {
        if (mStrokes.empty()) {
            return;
        }
        mStackStroke.push(mStrokes.back());
        mStrokes.pop_back();
        NotifyUndoRedoPossible();
    }

    void DrawArApplication::ReDo() {
        if (mStackStroke.empty()) {
            return;
        }
        mStrokes.push_back(mStackStroke.top());
        mStackStroke.pop();
        NotifyUndoRedoPossible();
    }

    // Stroke 길이 - (Stroke 단위로 저장함), [Stroke : points길이 - points, width, color ]
    void DrawArApplication::SaveStrokeInTemp() {
        if (mStrokes.empty()) {
            MakeToastMessage("저장할 데이터가 없습니다.");
            LOGE("저장할 데이터가 없습니다.");
            return;
        }
        accessTouchQueue.lock();
        string fileName = util::getCacheDir() + "/drawTemp.dat";

        unsigned int size = mStrokes.size();

        std::fstream fs;
        fs.open(fileName.c_str(), std::fstream::binary | std::fstream::out);

        fs.write(reinterpret_cast<char *>(&size), sizeof(unsigned int));
        for (unsigned int i = 0; i < size; ++i) {
            Stroke *s = &(mStrokes[i]);
            unsigned int pointSize = s->mPoints.size();
            fs.write(reinterpret_cast<char *>(&pointSize), sizeof(unsigned int));
            fs.write(reinterpret_cast<char *>(&(s->mPoints[0])), sizeof(glm::vec3) * s->mPoints.size());
            fs.write(reinterpret_cast<char *>(&(s->mLineWidth)), sizeof(GLfloat));
            fs.write(reinterpret_cast<char *>(&(s->mColor)), sizeof(glm::vec4));
            fs.write(reinterpret_cast<char *>(&(s->mLineType)), sizeof(float));
        }
        fs.close();

        LOGI("저장 완료");
        accessTouchQueue.unlock();

    }

    void DrawArApplication::LoadStrokeByTemp() {
        string fileName = util::getCacheDir() + "/drawTemp.dat";
        ifstream f(fileName.c_str());
        if (!f.good()) {
            MakeToastMessage("저장된 파일이 없습니다.");
            return;
        }
        f.close();

        accessTouchQueue.lock();
        LOGE("불러오기 시작.");

        std::ifstream rs;
        rs.open(fileName.c_str(), std::fstream::binary | std::fstream::in);
        unsigned int size = 0;
        rs.read(reinterpret_cast<char *>(&size), sizeof(unsigned int));

        std::vector<Stroke> strokes(size);
        for (unsigned int i = 0; i < size; ++i) {
            unsigned int vectorSize = 0;
            rs.read(reinterpret_cast<char *>(&vectorSize), sizeof(unsigned int));

            Stroke *emptyStroke = &strokes[i];
            emptyStroke->mPoints.resize(vectorSize);

            rs.read(reinterpret_cast<char *>(&(emptyStroke->mPoints[0])), sizeof(glm::vec3) * vectorSize);
            rs.read(reinterpret_cast<char *>(&(emptyStroke->mLineWidth)), sizeof(GLfloat));
            rs.read(reinterpret_cast<char *>(&(emptyStroke->mColor)), sizeof(glm::vec4));
            rs.read(reinterpret_cast<char *>(&(emptyStroke->mLineType)), sizeof(float));
        }
        rs.close();

        mStrokes.clear();
        mStrokes.resize(size);

        for (int i = 0; i < mStrokes.size(); ++i) {
            unsigned int pointSize = strokes[i].mPoints.size();

            mStrokes[i].mPoints.resize(pointSize);
            memcpy(reinterpret_cast<char *>(&(mStrokes[i].mColor)), reinterpret_cast<char *>(&(strokes[i].mColor)), sizeof(glm::vec4));
            memcpy(reinterpret_cast<char *>(&(mStrokes[i].mLineWidth)), reinterpret_cast<char *>(&(strokes[i].mLineWidth)), sizeof(GLfloat));
            memcpy(reinterpret_cast<char *>(&(mStrokes[i].mPoints[0])), reinterpret_cast<char *>(&(strokes[i].mPoints[0])), sizeof(glm::vec3) * pointSize);
            memcpy(reinterpret_cast<char *>(&(mStrokes[i].mLineType)), reinterpret_cast<char *>(&(strokes[i].mLineType)), sizeof(float));
        }


        LOGI("불러오기 완료 : %lu", mStrokes.size());
        accessTouchQueue.unlock();

    }

    void DrawArApplication::SetLineType(int type) {
        if (type == 0) {
            lineProperty.lineType = 0.0f;
        } else if (type == 1) {
            lineProperty.lineType = 1.0f;
        } else {
            LOGE("없는 타입 :%d", type);
        }
    }

}