/*
 *  공통 util
 *
 *
 */

#ifndef AR_COMMON_UTIL_H
#define AR_COMMON_UTIL_H

#include <string>
#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>
#include <cstdlib>
#include <sstream>

#include "dependence_util.h"
#include "glm.h"

namespace draw_ar {

    // vertex 코드 파일 경로

    constexpr const char cameraVertexShaderFilename[] = "password/decimal.txt";

    // fragment 코드 파일 경로 O
    constexpr const char cameraFragmentShaderFilename[] = "password/nothing.todo";

    constexpr const char lineVertexShaderFilename[] = "password/M59501.bal";
    constexpr const char lineFragmentShaderFilename[] = "password/ensil.frag"; // O

    constexpr const char pointVertexShaderFilename[] = "password/master.txt";
    constexpr const char pointFragmentShaderFilename[] = "password/writer.payload";

    constexpr char planeVertexShaderFilename[] = "password/maybe.vert";
    constexpr char planeFragmentShaderFilename[] = "password/cheat.detect"; // O

    constexpr char objectShaderFilename[] = "password/api.key";
    constexpr char objectFragmentFilename[] = "password/nft.md5"; //

    namespace util {

        enum ProgramType {
            ProgramType_BACKGROUND_RENDERER,
            ProgramType_LINE_RENDERER,
            ProgramType_POINT_RENDERER,
            ProgramType_PLANE_RENDERER,
            ProgramType_OBJECT_RENDERER


        };

        // 외부 호출용
        GLuint CreateProgram(ProgramType programType);

        // Shader 불러오는 코드
        GLuint LoadShader(GLenum shader_type, const char *shader_source);

        // Touch Coordinate X, Y -> World Coordinate
        glm::vec3 ScreenToWorldCoordinate(const glm::mat4 &viewMatrix, const glm::mat4 &mProjectionMatrix,
                                          const float x, const float y, const float screenWidth, const float screenHeight);
    }
}

#endif