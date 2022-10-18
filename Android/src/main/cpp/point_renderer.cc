#include "point_renderer.h"

namespace draw_ar {


    const GLfloat gTriangleVertices[] = { 0.0f, 0.5f, -0.5f, -0.5f,
                                          0.5f, -0.5f, 1.0f, 1.0f };

    void PointRenderer::CreateOnGLThread() {
        util::CheckGlError("Point Renderer Created 전 체크");

        glGenTextures(1, &mBuffer);
        glBindBuffer(GL_ARRAY_BUFFER, mBuffer);
        glBufferData(GL_ARRAY_BUFFER, 24, nullptr, GL_DYNAMIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        util::CheckGlError("Buffer 메모리 할당 확인");

        // 프로그램 생성
        mProgram = util::CreateProgram(util::ProgramType_POINT_RENDERER);
        if (!mProgram) {
            LOGE("Could not create program.");
        }

        util::CheckGlError("Create Point Renderer");

        mPointAttribute = glGetAttribLocation(mProgram, "vPosition");
        util::CheckGlError("glGetAttribLocation");

        glBindBuffer(GL_ARRAY_BUFFER, mBuffer);
        glBufferData(GL_ARRAY_BUFFER, 32, nullptr, GL_DYNAMIC_DRAW);
        glBufferSubData(GL_ARRAY_BUFFER, 0, 32, &gTriangleVertices[0]);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }


    void PointRenderer::Draw(const glm::mat4 &viewMatrix, const glm::mat4 &projectionMatrix, const glm::mat4 &modelMatrix) {

        static float grey;
        grey += 0.01f;
        if (grey > 1.0f) {
            grey = 0.0f;
        }
        util::CheckGlError("glClearColor");
        //glClear( GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);
        util::CheckGlError("glClear");

        glUseProgram(mProgram);
        util::CheckGlError("glUseProgram");

        glBindBuffer(GL_ARRAY_BUFFER, mBuffer);
        glVertexAttribPointer(mPointAttribute, 2, GL_FLOAT, GL_FALSE, 0, nullptr);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        util::CheckGlError("glVertexAttribPointer");
        glEnableVertexAttribArray(mPointAttribute);
        util::CheckGlError("glEnableVertexAttribArray");

        glUniformMatrix4fv(glGetUniformLocation(mProgram, "pvmMatrix"), 1, GL_FALSE, glm::value_ptr(projectionMatrix * viewMatrix));



        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
        util::CheckGlError("glDrawArrays");
        glDisableVertexAttribArray(mPointAttribute);

    }
}