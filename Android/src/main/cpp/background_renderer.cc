
#include "background_renderer.h"

// 템플릿 메타프로그래밍에 사용되는 라이브러리, 유형의 속성을 검사하거나 변환하는 기능을 제공한다.
#include <type_traits>

namespace draw_ar {

    // Anonymous namespace
    namespace {
        // 카메라 화면을 맡을 사각형 Vertex 좌표 (X,Y)
        const GLfloat vertices[] = {
                -1.0f, -1.0f,
                +1.0f, -1.0f,
                -1.0f, +1.0f,
                +1.0f, +1.0f,
        };

    } // close namespace



    void BackgroundRenderer::CreateOnGLThread() {

        glGenTextures(1, &mCamera_textureId);
        glBindTexture(GL_TEXTURE_EXTERNAL_OES, mCamera_textureId);
        glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        // 프로그램 생성
        mProgram = util::CreateProgram(util::ProgramType_BACKGROUND_RENDERER);
        if (!mProgram) {
            LOGE("Could not create program.");
        }


        // if !mProgram -> {} 프로그램 생성에 실패한 경우

        // Handling
        mCameraTexture_uniform = glGetUniformLocation(mProgram, "sTexture");
        mCameraPosition_attrib = glGetAttribLocation(mProgram, "a_Position");
        mCameraTexcoord_attrib = glGetAttribLocation(mProgram, "a_TexCoord");

        // 오류 체크
        util::CheckGlError("Create Background Renderer");
    }

    void BackgroundRenderer::Draw(const ArSession *session, const ArFrame *frame) {
       static_assert(std::extent<decltype(vertices)>::value == numVertices * 2, "vertices 길이가 잘못됨");

        util::CheckGlError("Before Draw Background Renderer");

        // 디스플레이 회전이 새로 생겼는지 체크함.
        // 회전이 감지될 경우 카메라 이미지의 화면 부분에 대한 uv 좌표를 다시 가져옴
        int32_t geometry_changed = 0;
        ArFrame_getDisplayGeometryChanged(session, frame, &geometry_changed);
        if (geometry_changed != 0 || !mUvs_initialized) {
            ArFrame_transformCoordinates2d(
                    session, frame, AR_COORDINATES_2D_OPENGL_NORMALIZED_DEVICE_COORDINATES,
                    numVertices, vertices, AR_COORDINATES_2D_TEXTURE_NORMALIZED,
                    mTransformed_uvs);
            mUvs_initialized = true;
        }

        int64_t frame_timestamp;
        ArFrame_getTimestamp(session, frame, &frame_timestamp);
        if (frame_timestamp == 0) {
            // 카메라가 아직 첫 번째 프레임을 생성하지 않은 경우 렌더링을 억제한다.
            // 텍스처가 재사용 될 때 이전 세션에서 남은 데이터를 그리는 것을 방지한다.
            return;
        }

        if (mCamera_textureId == -1) {
            return;
        }

        glDepthMask(GL_FALSE);
        glBindTexture(GL_TEXTURE_EXTERNAL_OES, mCamera_textureId);
        glUseProgram(mProgram);
        util::CheckGlError("use Program");


        glUniform1i(mCameraTexture_uniform, 0);

        glVertexAttribPointer(mCameraPosition_attrib, 2, GL_FLOAT, false, 0, vertices);
        glVertexAttribPointer(mCameraTexcoord_attrib, 2, GL_FLOAT, false, 0, mTransformed_uvs);

        glEnableVertexAttribArray(mCameraPosition_attrib);
        glEnableVertexAttribArray(mCameraTexcoord_attrib);

        glDrawArrays(GL_TRIANGLE_STRIP, 0, numVertices);

        glDisableVertexAttribArray(mCameraTexcoord_attrib);
        glDisableVertexAttribArray(mCameraPosition_attrib);

        glUseProgram(0);
        glDepthMask(GL_TRUE);
        // Draw Error 체크

        util::CheckGlError("After Draw Background Renderer");
    }

    GLuint BackgroundRenderer::GetTextureId() const {
        return mCamera_textureId;
    }


}
