//
// Created by 조영래 on 2022-05-17.
//

#ifndef MY_APPLICATION_OBJ_RENDERER_H
#define MY_APPLICATION_OBJ_RENDERER_H

#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>
#include <cstdlib>
#include <vector>

#include "dependence_util.h"
#include "common_util.h"
#include "glm.h"
#include "glb_object.h"


namespace draw_ar {
    class ObjRenderer {
    public:
        ObjRenderer() = default;

        ~ObjRenderer();

        void CreateOnGLThread();

        void UploadToBuffer(GlbObject* o);

        void Draw(const glm::mat4 &viewMatrix, const glm::mat4 &projectionMatrix,
                  const glm::mat4 &modelMatrix, const float alpha = 0.1);

    private:
        GLuint mVertexBuffer;
        GLuint mIndicesBuffer;
        GLuint mProgram;
        GlbObject* mO;
    };
}


#endif //MY_APPLICATION_OBJ_RENDERER_H

