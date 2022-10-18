//
// Created by 조영래 on 2022-05-17.
//

#pragma once
#include <iostream>
#include <fstream>
#include <stdlib.h>
#include <vector>

#include "glm.h"
#include "json/json.h"
#include "json/json-forwards.h"
#include "dependence_util.h"

using namespace std;

namespace draw_ar {
    typedef struct BufferViews {
        unsigned int buffer = -1;
        unsigned int byteOffset = -1;
        unsigned int byteLength = -1;
        unsigned int target = -1;
    } BufferViews_t;

    typedef struct Accessors {
        unsigned int bufferView = -1;
        unsigned int byteOffset = -1;
        unsigned int componentType = -1;
        unsigned int count = -1;
        unsigned char type[7];
    } Accessors_t;

    class GlbObject {
    public:
        ~GlbObject();
        void loadGLB(string fileName);
        glm::vec3 getCenter();

    private:
        void moveCenter();

        int *mNodes = nullptr;
        BufferViews_t *mBufferViews = nullptr;
        Accessors_t *mAccessors = nullptr;

        unsigned int mNodesSize = 0;
        unsigned int mPositionIndex = -1;
        unsigned int mIndiciesIndex = -1;
        unsigned int mColorIndex = -1;

        unsigned int mByteLength = -1;
        unsigned int mBufferViewLength = 2;

        vector<unsigned short> mIndicies;
        vector<glm::vec3> mVertexs;
        vector<glm::vec3> mColors;

        glm::vec3 mMaxVertex;
        glm::vec3 mMinVertex;
        glm::vec3 mCenterVertex;

        friend class ObjRenderer;
    };


}

