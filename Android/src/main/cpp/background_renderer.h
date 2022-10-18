
#ifndef AR_BACKGROUND_RENDERER
#define AR_BACKGROUND_RENDERER

#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>
#include <cstdlib>
#include "arcore_c_api.h"
#include "common_util.h"


namespace draw_ar {



    class BackgroundRenderer {
    public:
        BackgroundRenderer() = default;
        ~BackgroundRenderer() = default;

        void CreateOnGLThread();
        // Camera 화면 렌더링

        void Draw(const ArSession *session, const ArFrame *frame);

        // GL_TEXTURE_EXTERNAL_OES Texture 가져오기
        GLuint GetTextureId() const;


    private:
        static constexpr int numVertices = 4;


        GLuint mCamera_textureId;
        GLuint mProgram;

        GLuint mCameraPosition_attrib;
        GLuint mCameraTexcoord_attrib;
        GLuint mCameraTexture_uniform;

        bool mUvs_initialized = false;
        float mTransformed_uvs[numVertices * 2];



    };
}

#endif