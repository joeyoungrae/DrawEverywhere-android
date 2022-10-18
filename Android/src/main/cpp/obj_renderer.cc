//
// Created by 조영래 on 2022-05-17.
//

#include "obj_renderer.h"

namespace draw_ar {

    ObjRenderer::~ObjRenderer() {
        LOGI("Destroyed ObjRenderer Class");

    }

    void ObjRenderer::CreateOnGLThread() {
        util::CheckGlError("Line Renderer Created 전 체크");

        glGenBuffers(1, &mVertexBuffer);
        glGenBuffers(1, &mIndicesBuffer);


        util::CheckGlError("Buffer 메모리 할당 확인");

        // 프로그램 생성
        mProgram = util::CreateProgram(util::ProgramType_OBJECT_RENDERER);
        if (!mProgram) {
            LOGE("Could not create program.");
        }

        util::CheckGlError("Create obj Renderer");
    }

    void ObjRenderer::Draw(const glm::mat4 &viewMatrix, const glm::mat4 &projectionMatrix,
                           const glm::mat4 &modelMatrix, const float alpha) {

        glUseProgram(mProgram);

        glEnable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFuncSeparate(GL_SRC_ALPHA, GL_DST_ALPHA,
                            GL_ZERO, GL_ONE);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        glBindBuffer(GL_ARRAY_BUFFER, mVertexBuffer);

        // vertex
        glVertexAttribPointer(glGetAttribLocation(mProgram, "position"), 3, GL_FLOAT, false,
                              sizeof(glm::vec3), nullptr);
        glVertexAttribPointer(glGetAttribLocation(mProgram, "color"), 3, GL_FLOAT, false,
                              sizeof(glm::vec3),
                              (GLvoid *) (mO->mVertexs.size() * sizeof(glm::vec3)));

        glEnableVertexAttribArray(glGetAttribLocation(mProgram, "position"));
        glEnableVertexAttribArray(glGetAttribLocation(mProgram, "color"));

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, mIndicesBuffer);


        glUniform1f(glGetUniformLocation(mProgram, "alpha"), alpha);

        glUniformMatrix4fv(glGetUniformLocation(mProgram, "vpMatrix"), 1, GL_FALSE,
                           glm::value_ptr(projectionMatrix * viewMatrix * modelMatrix));


        glDrawElements(GL_TRIANGLES, mO->mIndicies.size(), GL_UNSIGNED_SHORT, 0);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        glDisableVertexAttribArray(glGetAttribLocation(mProgram, "position"));
        glDisableVertexAttribArray(glGetAttribLocation(mProgram, "color"));



        glDisable(GL_BLEND);
        glDisable(GL_DEPTH_TEST);
        glUseProgram(0);
    }

    void ObjRenderer::UploadToBuffer(GlbObject* o) {

        mO = o;

        // 색깔 + vertex -> vertex 색깔 순서로
        unsigned int totalLength = o->mVertexs.size() + o->mColors.size();
        glBindBuffer(GL_ARRAY_BUFFER, mVertexBuffer);
        glBufferData(GL_ARRAY_BUFFER, totalLength * sizeof(glm::vec3), nullptr, GL_STATIC_DRAW);
        glBufferSubData(GL_ARRAY_BUFFER, 0, o->mVertexs.size() * sizeof(glm::vec3), &o->mVertexs[0]);
        glBufferSubData(GL_ARRAY_BUFFER, o->mVertexs.size() * sizeof(glm::vec3),
                        o->mColors.size() * sizeof(glm::vec3), &o->mColors[0]);
        glBindBuffer(GL_ARRAY_BUFFER, 0);


        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, mIndicesBuffer);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, o->mIndicies.size() * sizeof(unsigned short),
                     &o->mIndicies[0], GL_STATIC_DRAW);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
    }
}