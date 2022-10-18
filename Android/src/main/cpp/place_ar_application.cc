
#include "place_ar_application.h"

namespace draw_ar {

    PlaceArApplication::PlaceArApplication(AAssetManager *assetManager) {
        util::Dependence_Setting::asset_manager = assetManager;
    }


    PlaceArApplication::~PlaceArApplication() {
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

    void MakeDisplayMessage(string message) {

    }

    //<editor-fold desc="Activity 생명주기">
    void PlaceArApplication::OnResume(JNIEnv *env, void *context, void *activity) {
        if (mArSession == nullptr) {
            ArInstallStatus install_status;
            bool bRequestedInstall = !mbInstallRequested;

            CHECKANDTHROW(
                    ArCoreApk_requestInstall(env, activity, bRequestedInstall, &install_status) ==
                    AR_SUCCESS,
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
                SetMode(Mode_PREVIEW);
            }

        }

        const ArStatus status = ArSession_resume(mArSession);
        CHECKANDTHROW(status == AR_SUCCESS, env, "Failed to resume AR session.");
    }

    void PlaceArApplication::OnPause() {
        if (mArSession != nullptr) {
            ArSession_pause(mArSession);
        }
    }
    //</editor-fold>

    //<editor-fold desc="RendererCallBack">
    void PlaceArApplication::OnDrawFrame() {
        if (mArSession == nullptr) {
            LOGE("PlaceARApplication::Session is Null");
            return;
        }

        if (ArSession_update(mArSession, mArFrame) != AR_SUCCESS) {
            LOGE("PlaceARApplication::OnDrawFrame ArSession_update error");
        }

        //<editor-fold desc="Ar Camera Projection">
        ArCamera *arCamera;
        ArFrame_acquireCamera(mArSession, mArFrame, &arCamera);

        ArCamera_getProjectionMatrix(mArSession, arCamera, /*near=*/0.001f, /*far=*/100.f,
                                     glm::value_ptr(mProjectionMatrix));
        ArCamera_getViewMatrix(mArSession, arCamera, glm::value_ptr(mViewMatrix));

        glClear(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);

        mBackgroundRenderer.Draw(mArSession, mArFrame);

        ArTrackingState cameraTrackingState;
        ArCamera_getTrackingState(mArSession, arCamera, &cameraTrackingState);

        // Tracking 하지 않는 경우 탈출 처리
        if (cameraTrackingState != AR_TRACKING_STATE_TRACKING) {
            ArTrackingFailureReason reason;
            ArCamera_getTrackingFailureReason(mArSession, arCamera, &reason);
            ArCamera_release(arCamera);
            return;
        }
        ArCamera_release(arCamera);
        //</editor-fold>


        switch (mMode) {
            case Mode_SEARCH: {
                ArTrackableList *planeList = nullptr;
                ArTrackableList_create(mArSession, &planeList);
                ArSession_getAllTrackables(mArSession, AR_TRACKABLE_PLANE, planeList);

                int32_t planeListSize = 0;
                ArTrackableList_getSize(mArSession, planeList, &planeListSize);

                for (int i = 0; i < planeListSize; ++i) {
                    ArTrackable *arTrackable = nullptr;
                    ArTrackableList_acquireItem(mArSession, planeList, i, &arTrackable);

                    ArTrackingState out_tracking_state;
                    ArTrackable_getTrackingState(mArSession, arTrackable,
                                                 &out_tracking_state);

                    ArTrackableType arTrackableType = AR_TRACKABLE_NOT_VALID;
                    ArTrackable_getType(mArSession, arTrackable, &arTrackableType);

                    if (arTrackableType == AR_TRACKABLE_PLANE) {
                        ArPlane *arPlane = ArAsPlane(arTrackable);

                        ArPose *planePose = nullptr;
                        ArPose_create(mArSession, nullptr, &planePose);

                        ArPlane_getCenterPose(mArSession, arPlane, planePose);

                        ArAnchor *anchor = nullptr;
                        ArTrackable_acquireNewAnchor(mArSession, arTrackable, planePose, &anchor);

                        mPlaneRenderer.Draw(mProjectionMatrix, mViewMatrix, *mArSession, arPlane);
                        ArPose_destroy(planePose);
                    }

                    ArTrackable_release(arTrackable);
                }

                ArTrackableList_destroy(planeList);
                planeList = nullptr;
            }
                break;
            case Mode_PREVIEW: {
                float scaleSize = mSize.load();
                float currentSize = mCurrentSize.load();

                if (scaleSize != currentSize) {
                    float ss;

                    if (currentSize > scaleSize) {
                        ss = 0.5f;
                    } else {
                        ss = 2.0f;
                    }

                    mCurrentSize.store(scaleSize);
                    mModelMatrix = glm::scale(mModelMatrix, glm::vec3(ss, ss, ss));
                }

                glm::vec2 vectorAxis = mAxisVector.load();
                float vectorSize = mVectorSize.load();

                // Scale 적용하기

                glm::mat4 targetMatrix = mModelMatrix;

                // x로 회전
                if (vectorAxis.x != 0.0f || vectorAxis.y != 0.0f) {
                    glm::mat4 instanceMatrix = glm::rotate(mModelMatrix, glm::radians(vectorSize),
                                                           glm::vec3(vectorAxis.x, vectorAxis.y,
                                                                     0.0f));
                    targetMatrix = instanceMatrix;
                }


                glm::mat4 lookView = glm::lookAt(glm::vec3(1, 1, 10),
                                                 glm::vec3(0.0f, 0.0f, 0.0f),
                                                 glm::vec3(0, 1, 0));

                glm::mat4 orthoMatrix = glm::ortho(-1.0f, 1.0f, -1.0f, 1.0f, 0.01f, 100.0f);


                mObjRenderer.Draw(lookView, orthoMatrix, targetMatrix, 0.5);
            }
                break;
            case Mode_PLACE:
            case Mode_SAVE: {
                if (mCurrentAnchor == nullptr) {
                    MakeToastMessage("배치 대상이 없어서 다시 탐색합니다.");
                    SetMode(Mode_SEARCH);
                } else {
                    ArPose *pose;
                    ArPose_create(mArSession, nullptr, &pose);
                    glm::mat4 anchorMatrix(1.0f);

                    ArAnchor_getPose(mArSession, mCurrentAnchor, pose);
                    ArPose_getMatrix(mArSession, pose, glm::value_ptr(anchorMatrix));
                    ArPose_destroy(pose);

                    anchorMatrix *= mModelMatrix;
                    mObjRenderer.Draw(mViewMatrix, mProjectionMatrix, anchorMatrix, 1.0);
                }
            }
                break;
        }
    }

    void PlaceArApplication::OnTouchScreen(float x, float y, MouseEvent mouseEvent) {
        switch (mouseEvent) {
            case MouseEvent_DOWN:
                if (mMode == Mode_PREVIEW) {
                    mStartPosition = glm::vec2(x, y);
                } else if (mMode == Mode_SEARCH) {
                    if (mArFrame != nullptr && mArSession != nullptr) {
                        ArHitResultList *hit_result_list = nullptr;
                        ArHitResultList_create(mArSession, &hit_result_list);
                        CHECK(hit_result_list);

                        ArFrame_hitTest(mArSession, mArFrame, x, y, hit_result_list);


                        int32_t hit_result_list_size = 0;
                        ArHitResultList_getSize(mArSession, hit_result_list,
                                                &hit_result_list_size);
                        ArHitResult *ar_hit_result = nullptr;
                        for (int32_t i = 0; i < hit_result_list_size; ++i) {
                            ArHitResult *ar_hit = nullptr;
                            ArHitResult_create(mArSession, &ar_hit);
                            ArHitResultList_getItem(mArSession, hit_result_list, i, ar_hit);

                            if (ar_hit == nullptr) {
                                LOGE("PlaceArApplication::OnTouched ArHitResult 리스트에서 아이템을 가져오는 것에서 에러가 발생함 error");
                                return;
                            }

                            ArTrackable *ar_trackable = nullptr;
                            ArHitResult_acquireTrackable(mArSession, ar_hit, &ar_trackable);
                            ArTrackableType ar_trackable_type = AR_TRACKABLE_NOT_VALID;
                            ArTrackable_getType(mArSession, ar_trackable, &ar_trackable_type);

                            if (AR_TRACKABLE_PLANE == ar_trackable_type) {
                                ArPose *hit_pose = nullptr;
                                ArPose_create(mArSession, nullptr, &hit_pose);
                                ArHitResult_getHitPose(mArSession, ar_hit, hit_pose);
                                int32_t in_polygon = 0;
                                ArPlane *ar_plane = ArAsPlane(ar_trackable);
                                ArPlane_isPoseInPolygon(mArSession, ar_plane, hit_pose,
                                                        &in_polygon);

                                ArPose *camera_pose = nullptr;
                                ArPose_create(mArSession, nullptr, &camera_pose);
                                ArCamera *ar_camera;
                                ArFrame_acquireCamera(mArSession, mArFrame, &ar_camera);
                                ArCamera_getPose(mArSession, ar_camera, camera_pose);
                                ArCamera_release(ar_camera);

                                ArPose_destroy(hit_pose);
                                ArPose_destroy(camera_pose);

                                ar_hit_result = ar_hit;
                                break;
                            }
                        }

                        if (ar_hit_result) {
                            ArAnchor *anchor = nullptr;
                            if (ArHitResult_acquireNewAnchor(mArSession, ar_hit_result, &anchor) !=
                                AR_SUCCESS) {
                                return;
                            }

                            ArTrackingState tracking_state = AR_TRACKING_STATE_STOPPED;
                            ArAnchor_getTrackingState(mArSession, anchor, &tracking_state);
                            if (tracking_state != AR_TRACKING_STATE_TRACKING) {
                                ArAnchor_release(anchor);
                                return;
                            }

                            ArTrackable *ar_trackable = nullptr;
                            ArHitResult_acquireTrackable(mArSession, ar_hit_result, &ar_trackable);

                            mCurrentAnchor = anchor;
                            ArPose *pose;
                            ArPose_create(mArSession, nullptr, &pose);
                            glm::mat4 anchorMatrix(1.0f);

                            ArAnchor_getPose(mArSession, anchor, pose);
                            ArPose_getMatrix(mArSession, pose, glm::value_ptr(anchorMatrix));
                            ArPose_destroy(pose);

                            mTranslateVector = glm::vec3(anchorMatrix[3][0], anchorMatrix[3][1],
                                                         anchorMatrix[3][2]);

                            LOGE("anchor 부착 성공");
                            SetMode(Mode_PLACE);


                            ArHitResult_destroy(ar_hit_result);
                            ArHitResultList_destroy(hit_result_list);
                        }
                    }
                }
                break;

            case MouseEvent_MOVE:
                if (mMode == Mode_PREVIEW) {
                    float valueX = (mStartPosition.x - x) / mWidth * 90;
                    float valueY = (mStartPosition.y - y) / mHeight * 90;


                    mAxisVector.store(glm::vec2(valueY, valueX));
                    valueX *= valueX;
                    valueY *= valueY;

                    float degree = (valueX + valueY) / 10.0f;

                    degree = degree < 270.0f ? degree : 270.0f;
                    mVectorSize.store(degree);
                }

                break;

            case MouseEvent_UP:
                if (mMode == Mode_PREVIEW) {
                    glm::vec2 vectorAxis = mAxisVector.load();
                    float vectorSize = mVectorSize.load();
                    glm::mat4 instanceMatrix = glm::rotate(mModelMatrix, glm::radians(vectorSize),
                                                           glm::vec3(vectorAxis.x, vectorAxis.y,
                                                                     0.0f));

                    LOGE("축 x : %f y : %f ", vectorAxis.y, vectorAxis.x);
                    mAxisVector.store(glm::vec2(0.0f, 0.0f));

                    mModelMatrix = instanceMatrix;
                }
                break;

            default:
                break;
        }
    }


    void PlaceArApplication::OnPreDrawFrame() {
        if (mNewObjLoaded) {
            mNewObjLoaded = false;
            mObjRenderer.UploadToBuffer(&mObj);
        }

    }

    void PlaceArApplication::OnSurfaceCreated() {

    }

    void PlaceArApplication::OnSurfaceDestroyed() {
        // ClearGL
        // ClearReference
    }

    void PlaceArApplication::OnContextCreated() {
        mBackgroundRenderer.CreateOnGLThread();
        mObjRenderer.CreateOnGLThread();
        mPlaneRenderer.InitializeGlContent(util::Dependence_Setting::asset_manager);

        ArSession_setCameraTextureName(mArSession, mBackgroundRenderer.GetTextureId());

    }
    //</editor-fold>

    void PlaceArApplication::ConfigureSession() {
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

    void PlaceArApplication::OnDisplayGeometryChanged(int displayRotation, int width, int height) {
        LOGI("OnSurfaceChanged(%d, %d)", width, height);

        glViewport(0, 0, width, height);
        mDisplayRotation = displayRotation;
        mWidth = width;
        mHeight = height;
        if (mArSession != nullptr) {
            ArSession_setDisplayGeometry(mArSession, displayRotation, width, height);
        }
    }

    void PlaceArApplication::SetScale(int value) {
        float s = mSize.load();


        if (s <= 2 && value != 0) {
            MakeToastMessage("크기를 더 이상 줄일 수 없습니다.");
            return;
        } else if (s >= 10 && value == 0) {
            MakeToastMessage("크기를 더 이상 늘릴 수 없습니다.");
            return;
        }


        if (value == 0) {
            mSize.store(s + 2.0f);
        } else {
            mSize.store(s - 2.0f);
        }
    }

    void PlaceArApplication::SetMode(Mode mode) {
        // 저장시 파일저장;
        mMode = mode;

        if (mMode == Mode_SEARCH) {
            ArAnchor_release(mCurrentAnchor);
            mCurrentAnchor = nullptr;
        }


        if (mMode == Mode_SAVE) {
            LOGI("SAVE COMPLETE");
        }


        ConfigureSession();
        NotifyModeListener();
    }


    void PlaceArApplication::NotifyModeListener() {
        JNIEnv *env = GetJniEnv();

        static struct JNIData {
            jclass helper_class;
            jmethodID modeCallBack;
        } jniIds = [env]() -> JNIData {
            constexpr char helperClassName[] =
                    "com/draw/free/ar/util/JniInterfaceForPlace";
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
            case Mode_PREVIEW:
                modeType = env->NewStringUTF("PREVIEW");
                break;
            case Mode_PLACE:
                modeType = env->NewStringUTF("PLACE");
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


    void PlaceArApplication::MakeDisplayMessage(string message) {
        JNIEnv *env = GetJniEnv();

        static struct JNIData {
            jclass helper_class;
            jmethodID modeCallBack;
        } jniIds = [env]() -> JNIData {
            constexpr char helperClassName[] =
                    "com/draw/free/ar/util/JniInterfaceForPlace";
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

    void PlaceArApplication::MakeToastMessage(string message) {
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

    void PlaceArApplication::loadGlb(string fileName) {
        string fileDir = util::getCacheDir() + "/model/" + fileName;

        mObj.loadGLB(fileDir);
        mNewObjLoaded = true;
        LOGE("LOAD END");
    }

    void PlaceArApplication::ResetRotation() {
        mModelMatrix = glm::mat4(1.0f);
    }


}