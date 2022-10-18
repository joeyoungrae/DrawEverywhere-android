

#include "common_util.h"

namespace draw_ar {

    namespace util {

        GLuint CreateProgram(ProgramType programType) {
            std::string vertexShaderContent;
            std::string fragmentShaderContent;
            std::string readVertexContent;
            std::string readFragmentContent;
            std::stringstream t;


            switch (programType) {
                case ProgramType_BACKGROUND_RENDERER:
                    openFile(cameraVertexShaderFilename, &readVertexContent);
                    openFile(cameraFragmentShaderFilename, &readFragmentContent);
                    break;
                case ProgramType_LINE_RENDERER: {
                    openFile(lineVertexShaderFilename, &readVertexContent);
                    openFile(lineFragmentShaderFilename, &readFragmentContent);
                }
                    break;
                case ProgramType_POINT_RENDERER:
                    openFile(pointVertexShaderFilename, &readVertexContent);
                    openFile(pointFragmentShaderFilename, &readFragmentContent);
                    break;
                case ProgramType_PLANE_RENDERER:
                    openFile(planeVertexShaderFilename, &readVertexContent);
                    openFile(planeFragmentShaderFilename, &readFragmentContent);
                    break;
                case ProgramType_OBJECT_RENDERER:
                    openFile(objectShaderFilename, &readVertexContent);
                    openFile(objectFragmentFilename, &readFragmentContent);
                    break;
                default:
                    break;
            }

            for (int i = 0; i < readVertexContent.size(); ++i) {
                t << (char)(readVertexContent.c_str()[i] - 1);
            }
            vertexShaderContent = t.str();
            t.str("");
            for (int i = 0; i < readFragmentContent.size(); ++i) {
                t << (char)(readFragmentContent.c_str()[i] - 1);
            }
            fragmentShaderContent = t.str();

            GLuint vertexShader = LoadShader(GL_VERTEX_SHADER, vertexShaderContent.c_str());
            if (!vertexShader) {
                return 0;
            }

            GLuint fragment_shader = LoadShader(GL_FRAGMENT_SHADER, fragmentShaderContent.c_str());
            if (!fragment_shader) {
                return 0;
            }

            GLuint program = glCreateProgram();
            if (program) {
                glAttachShader(program, vertexShader);
                glAttachShader(program, fragment_shader);
                glLinkProgram(program);
                GLint link_status = GL_FALSE;
                glGetProgramiv(program, GL_LINK_STATUS, &link_status);

                // 링크가 정상적으로 발생하는지 확인
                if (link_status != GL_TRUE) {
                    GLint buf_length = 0;
                    glGetProgramiv(program, GL_INFO_LOG_LENGTH, &buf_length);

                    if (buf_length) {
                        char *buf = reinterpret_cast<char *>(malloc(buf_length));

                        if (buf) {
                            glGetProgramInfoLog(program, buf_length, nullptr, buf);
                            LOGE("Could not link program:\n%s\n", buf);
                            free(buf);
                        }
                    }

                    glDeleteProgram(program);
                    program = 0;
                }
            }


            return program;
        }

        GLuint LoadShader(GLenum shader_type, const char *shader_source) {
            GLuint shader = glCreateShader(shader_type);
            if (!shader) {
                return shader;
            }

            glShaderSource(shader, 1, &shader_source, nullptr);
            glCompileShader(shader);
            GLint compiled = 0;
            glGetShaderiv(shader, GL_COMPILE_STATUS, &compiled);

            if (!compiled) {
                GLint info_len = 0;

                glGetShaderiv(shader, GL_INFO_LOG_LENGTH, &info_len);
                if (!info_len) {
                    return shader;
                }

                char *buf = reinterpret_cast<char *>(malloc(info_len));
                if (!buf) {
                    return shader;
                }

                // Shader Create 실패
                glGetShaderInfoLog(shader, info_len, nullptr, buf);
                LOGE("util::Could not compile shader %d:\n%s\n", shader_type, buf);

                free(buf);
                glDeleteShader(shader);
                shader = 0;
            }

            return shader;
        }

        glm::vec3
        ScreenToWorldCoordinate(const glm::mat4 &viewMatrix, const glm::mat4 &projectionMatrix,
                                const float touchX, const float touchY,
                                const float screenWidth, const float screenHeight) {

            const glm::mat4 viewProjMatrix = projectionMatrix * viewMatrix;

            const float dy = (screenHeight - touchY) * 2.0f / screenHeight - 1.0f;
            const float dx = touchX * 2.0f / screenWidth - 1.0f;

            const glm::vec4 farScreenPoint = glm::vec4(dx, dy, 1.0f, 1.0f);
            const glm::vec4 nearScreenPoint = glm::vec4(dx, dy, -1.0f, 1.0f);

            const glm::mat4 inverseMatrix = glm::inverse(viewProjMatrix);

            const glm::vec4 nearPlanePoint = inverseMatrix * nearScreenPoint;
            const glm::vec4 farPlanePoint = inverseMatrix * farScreenPoint;

            glm::vec3 direction = glm::vec3(farPlanePoint[0], farPlanePoint[1], farPlanePoint[2]) /
                                  farPlanePoint[3];
            glm::vec3 origin = glm::vec3(nearPlanePoint[0], nearPlanePoint[1], nearPlanePoint[2]) /
                               nearPlanePoint[3];

            direction = direction - origin;
            direction = glm::normalize(direction);

            //

            // Scale
            direction *= 0.13f;

            origin = origin + direction;

            return origin;
        }


    } // util close
} // draw_ar close