#ifndef AR_LINE_RENDERER
#define AR_LINE_RENDERER

#define BUFFER_OFFSET(i) ((GLvoid*)(i * sizeof(GLfloat)))
//#define BUFFER_OFFSET(i) ((GLvoid*)(i))


#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>
#include <cstdlib>
#include <vector>

#include "common_util.h"
#include "dependence_util.h"
#include "glm.h"
#include "stroke_model.h"

namespace draw_ar {


#pragma pack(1) // 구조체 패딩 제거
    struct PacketAttrib {
        glm::vec3 position;
        glm::vec3 previous;
        glm::vec3 next;
        float side;
        float width;
        glm::vec4 color;
        float length;
        float totalLength;
        float lineType;
    };
#pragma pack(0)

    struct AttributePointer {
        int position;
        int previous;
        int next;
        int side;
        int width;
        int color;
        int length;
        int totalLength;
        int lineType;
    };

    class LineRenderer {
    public:
        LineRenderer() = default;

        ~LineRenderer();

        void CreateOnGLThread();

        void UploadToBuffer(const std::vector<Stroke>& strokes);

        void Draw(const glm::mat4& viewMatrix, const glm::mat4& projectionMatrix, const glm::mat4 &modelMatrix);

    private:
        GLuint mBuffer;
        GLuint mTexture[2];
        GLuint mProgram;
        GLuint mSetBufferCount;

        std::vector<PacketAttrib> mPacketAttrib;
        AttributePointer mPointer;

        PacketAttrib CreateNewPacketAttrib(const glm::vec3 pos, const glm::vec3 previous, const glm::vec3 next,
                                           float side, float width, const glm::vec4 color,
                                           float length, float totalLength, float lineType);

        void LoadTexture(const char *path, int textureIndex);
    };
}

#endif