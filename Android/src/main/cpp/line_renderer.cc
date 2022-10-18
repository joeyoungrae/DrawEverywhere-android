
#include "line_renderer.h"
#include <type_traits>
#include <typeinfo>

#define OFFSETOF(TYPE, ELEMENT) ((size_t)&(((TYPE *)0)->ELEMENT))

#include <string>

namespace draw_ar {
    void LineRenderer::LoadTexture(const char* path, int textureIndex) {
        assert (textureIndex < 2);

        glGenTextures(1, &mTexture[textureIndex]);
        glBindTexture(GL_TEXTURE_2D, mTexture[textureIndex]);

        // 축소등으로, 텍스쳐의 원래 크기보다 작은 경우 보간법을 이용하여 작은 비율로 적용됨.
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        // WRAP_S, T는 X & Y와 같으며, GL_CAMP_TO_EDGE는, 기본 범위 바깥의 텍스쳐 좌표를 사용할 때, 출력 값
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

        if (!util::LoadPngFile(GL_TEXTURE_2D, path)) {
            LOGE("실패함");
        };

        glGenerateMipmap(GL_TEXTURE_2D);

    }


    void LineRenderer::CreateOnGLThread() {
        util::CheckGlError("Line Renderer Created 전 체크");

        glGenBuffers(1, &mBuffer);

        util::CheckGlError("Buffer 메모리 할당 확인");

        // 프로그램 생성
        mProgram = util::CreateProgram(util::ProgramType_LINE_RENDERER);
        if (!mProgram) {
            LOGE("Could not create program.");
        }

        mPointer.position = glGetAttribLocation(mProgram, "position");
        mPointer.previous = glGetAttribLocation(mProgram, "previous");
        mPointer.next = glGetAttribLocation(mProgram, "next");
        mPointer.width = glGetAttribLocation(mProgram, "width");
        mPointer.side = glGetAttribLocation(mProgram, "side");
        mPointer.color = glGetAttribLocation(mProgram, "color");
        mPointer.length = glGetAttribLocation(mProgram, "length");
        mPointer.totalLength = glGetAttribLocation(mProgram, "totalLength");
        mPointer.lineType = glGetAttribLocation(mProgram, "type");




        LoadTexture("models/linecap.png", 0);
        LoadTexture("models/pattern1.png", 1);

/*        glGenTextures(1, &mTexture[0]);
        glBindTexture(GL_TEXTURE_2D, mTexture[0]);

        /*
         * GL_TEXTURE_2D : 텍스쳐 타겟을 설정
         * 0 : mipmap 레벨을 수동으로 지정하고 싶을 때
         * GL_RGB : 텍스쳐가 가져야 할 포맷
         * WIDTH, HEIGHT, 너비와 높이
         * 0 : 관습적 (반드시 지정해야함)
         * GL_RGB : 원본 이미지가 가진 값
         * GL_UNSIGNED_BYTE : 원본 이미지의 포맷과 데이터 타입
         * data : 실제 데이터
         */
        //glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, 255, 255, 0, GL_RGB, GL_UNSIGNED_BYTE, data)
/*
        // 축소등으로, 텍스쳐의 원래 크기보다 작은 경우 보간법을 이용하여 작은 비율로 적용됨.
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        // WRAP_S, T는 X & Y와 같으며, GL_CAMP_TO_EDGE는, 기본 범위 바깥의 텍스쳐 좌표를 사용할 때, 출력 값
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

        if (!util::LoadPngFile(GL_TEXTURE_2D, "models/linecap.png")) {
            LOGE("실패함");
        };

        glGenerateMipmap(GL_TEXTURE_2D);

        */

        glBindTexture(GL_TEXTURE_2D, 0);

        util::CheckGlError("Create Line Renderer");
    }

    PacketAttrib LineRenderer::CreateNewPacketAttrib(const glm::vec3 pos, const glm::vec3 previous, const glm::vec3 next,
                                                     float side, float width, const glm::vec4 color,
                                                     float length, float totalLength, float lineType) {
        PacketAttrib nPacketAttrib{
                pos,
                previous,
                next,
                side,
                width,
                color,
                length,
                totalLength,
                lineType
        };


        return nPacketAttrib;
    }

    void LineRenderer::UploadToBuffer(const std::vector<Stroke> &strokes) {


        //<editor-fold desc="For문">

        if (!mPacketAttrib.empty()) {
            mPacketAttrib.clear();
        }

        for (auto &stroke : strokes) {

            auto points = stroke.GetPoints();
            GLfloat totalLength = stroke.GetTotalLength();
            float length = 0;
            float lineType = stroke.GetLineType();

            for (int i = 0; i < points.size(); ++i) {

                unsigned int prevI = i - 1 < 0 ? 0 : i - 1;
                unsigned int nextI = i + 1 > points.size() - 1 ? points.size() - 1 : i + 1;

                const glm::vec3 cur = points.at(i);
                const glm::vec3 prev = points.at(prevI);
                const glm::vec3 next = points.at(nextI);

                length += glm::length(cur - prev);

                // 이전 선의 끝점과 현재 선의 첫점이 이루는 삼각형의 넓이를 0으로 만들어서 렌더링시 안보이게 함.
                if (i == 0) {
                    mPacketAttrib.push_back(CreateNewPacketAttrib(cur, next, prev, 1.0f, stroke.GetLineWidth(), stroke.GetColor(), length, totalLength, lineType));
                }

                // 삼각형으로 선의 두께를 생성함
                mPacketAttrib.push_back(CreateNewPacketAttrib(cur, next, prev, 1.0f, stroke.GetLineWidth(), stroke.GetColor(), length, totalLength, lineType));
                mPacketAttrib.push_back(CreateNewPacketAttrib(cur, next, prev, -1.0f, stroke.GetLineWidth(), stroke.GetColor(), length, totalLength, lineType));

                if (i == points.size() - 1) {
                    mPacketAttrib.push_back(CreateNewPacketAttrib(cur, next, prev, -1.0f, stroke.GetLineWidth(), stroke.GetColor(), length, totalLength, lineType));
                }

            }
        }

        mSetBufferCount = mPacketAttrib.size();

        glBindBuffer(GL_ARRAY_BUFFER, mBuffer);

        // 버퍼랑 사이즈는 맞음
        glBufferData(GL_ARRAY_BUFFER, mSetBufferCount * sizeof(PacketAttrib), nullptr, GL_DYNAMIC_DRAW);
        glBufferSubData(GL_ARRAY_BUFFER, 0, mSetBufferCount * sizeof(PacketAttrib), &mPacketAttrib[0]);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        util::CheckGlError("SubData Check from GEN_BIND_BUFFER");
    }


    // projectModel, viewModel 추가하기
    void LineRenderer::Draw(const glm::mat4 &viewMatrix, const glm::mat4 &projectionMatrix, const glm::mat4 &modelMatrix) {
        if (mPacketAttrib.empty()) {
            return;
        }

        glUseProgram(mProgram);


        glEnable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFuncSeparate(GL_SRC_ALPHA, GL_DST_ALPHA,
                            GL_ZERO, GL_ONE);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glBindBuffer(GL_ARRAY_BUFFER, mBuffer);

        glVertexAttribPointer(mPointer.position, 3, GL_FLOAT, false, sizeof(PacketAttrib), nullptr);
        glVertexAttribPointer(mPointer.previous, 3, GL_FLOAT, false, sizeof(PacketAttrib), BUFFER_OFFSET(3));
        glVertexAttribPointer(mPointer.next, 3, GL_FLOAT, false, sizeof(PacketAttrib), BUFFER_OFFSET(6));
        glVertexAttribPointer(mPointer.side, 1, GL_FLOAT, false, sizeof(PacketAttrib), BUFFER_OFFSET(9));
        glVertexAttribPointer(mPointer.width, 1, GL_FLOAT, false, sizeof(PacketAttrib), BUFFER_OFFSET(10));
        glVertexAttribPointer(mPointer.color, 4, GL_FLOAT, false, sizeof(PacketAttrib), BUFFER_OFFSET(11));
        glVertexAttribPointer(mPointer.length, 1, GL_FLOAT, false, sizeof(PacketAttrib), BUFFER_OFFSET(15));
        glVertexAttribPointer(mPointer.totalLength, 1, GL_FLOAT, false, sizeof(PacketAttrib), BUFFER_OFFSET(16));
        glVertexAttribPointer(mPointer.lineType, 1, GL_FLOAT, false, sizeof(PacketAttrib), BUFFER_OFFSET(17));

        glBindBuffer(GL_ARRAY_BUFFER, 0);

        glEnableVertexAttribArray(mPointer.position);
        glEnableVertexAttribArray(mPointer.previous);
        glEnableVertexAttribArray(mPointer.next);

        glEnableVertexAttribArray(mPointer.side);
        glEnableVertexAttribArray(mPointer.width);
        glEnableVertexAttribArray(mPointer.color);

        glEnableVertexAttribArray(mPointer.length);
        glEnableVertexAttribArray(mPointer.totalLength);
        glEnableVertexAttribArray(mPointer.lineType);


        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, mTexture[0]);
        glUniform1i(glGetUniformLocation(mProgram, "sampler"), 0);

        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, mTexture[1]);
        glUniform1i(glGetUniformLocation(mProgram, "sampler2"), 1);



        util::CheckGlError("before UniformMatrix4fv");

        glUniformMatrix4fv(glGetUniformLocation(mProgram, "vpMatrix"), 1, GL_FALSE, glm::value_ptr(projectionMatrix * viewMatrix));

        util::CheckGlError("after UniformMatrix4fv");


        glDrawArrays(GL_TRIANGLE_STRIP, 0, mSetBufferCount);


        glDisableVertexAttribArray(mPointer.position);
        glDisableVertexAttribArray(mPointer.previous);
        glDisableVertexAttribArray(mPointer.next);
        glDisableVertexAttribArray(mPointer.side);
        glDisableVertexAttribArray(mPointer.width);
        glDisableVertexAttribArray(mPointer.color);

        glDisableVertexAttribArray(mPointer.length);
        glDisableVertexAttribArray(mPointer.totalLength);
        glDisableVertexAttribArray(mPointer.lineType);


        glBindBuffer(GL_ARRAY_BUFFER, 0);

        glDisable(GL_BLEND);
        glDisable(GL_DEPTH_TEST);
        glUseProgram(0);
        // 2nd Stroke
        util::CheckGlError("Line Renderer Drawing");

    }


    LineRenderer::~LineRenderer() {
        // mPacketAttrib delete 코드

            LOGI("Destroyed LineRenderer Class");
    }
}