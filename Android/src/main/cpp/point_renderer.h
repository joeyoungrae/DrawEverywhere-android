#ifndef POINT_RENDERER_H
#define POINT_RENDERER_H

#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>
#include <cstdlib>
#include <vector>

#include "arcore_c_api.h"
#include "common_util.h"
#include "dependence_util.h"
#include "glm.h"

namespace draw_ar {
    class PointRenderer {
    public:
        PointRenderer() = default;

        ~PointRenderer() = default;

        void CreateOnGLThread();

        void Draw(const glm::mat4 &viewMatrix, const glm::mat4 &projectionMatrix, const glm::mat4 &modelMatrix);

    private:
        GLuint mBuffer;
        GLuint mProgram;

        GLuint mPointAttribute;

        std::vector<glm::vec3> points;
        size_t time = 0;


    };
}

#endif