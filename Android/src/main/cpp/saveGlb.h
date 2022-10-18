#include <iostream>
#include <vector>
#include <sstream>
#include <cstring>
#include <fstream>

#include "stroke_model.h"
#include "dependence_util.h"

using namespace std;

namespace draw_ar {
    bool compareVec2(glm::vec2 v1, glm::vec2 v2);

    class saveGlb {
    private:
        typedef struct point {
            float x;
            float y;
            float z;
        } point_t;

        vector<point_t> mVertexs;
        vector<point_t> mColors;
        vector<unsigned short> mIndices;

        point_t mColor = {0.0f, 0.0f, 0.0f};

        point_t mMaxVec = {0.0f, 0.0f, 0.0f};
        point_t mMinVec = {0.0f, 0.0f, 0.0f};

        point_t mMaxCol = {0.0f, 0.0f, 0.0f};
        point_t mMinCol = {0.0f, 0.0f, 0.0f};

        void addIndices(unsigned int first, unsigned int end);
        glm::vec3 fixVector(glm::vec3 prev, glm::vec3 cur, glm::vec3 next, float side, float width);



    public:
        saveGlb(std::vector<Stroke> strokes);

        string saveGlbFile();
        void setColor(float x, float y, float z);
        void addVertex(float x, float y, float z);
        unsigned int getBinaryBytes();
    };
}