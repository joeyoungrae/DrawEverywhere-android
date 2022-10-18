
#ifndef STROKE_MODEL_H
#define STROKE_MODEL_H

#include <cmath>
#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>
#include <vector>

#include "biquad.h"
#include "glm.h"

namespace draw_ar {
    class Stroke {
    public:
        static constexpr GLfloat MIN_LINE_WIDTH = 0.0011f;
        //static constexpr GLfloat MAX_LINE_WIDTH = 0.02f;
        static constexpr GLfloat MAX_LINE_WIDTH = 0.05f;

        static constexpr GLuint FLOAT_PER_POINT = 3;
        static constexpr GLuint BYTE_PER_FLOAT = 8;
        static constexpr GLuint BYTE_PER_POINT = FLOAT_PER_POINT * BYTE_PER_FLOAT;

        Stroke() = default;

        Stroke(GLfloat lineWidth, glm::vec4 &color, float lineType);

        ~Stroke();

        std::vector<glm::vec3> GetPoints() const;


        GLfloat GetLineWidth() const;

        glm::vec4 GetColor() const;

        void AddPoint(const glm::vec3 &point);

        void Clear();
        void SubDivideSection(unsigned int s, float maxAngle, int iteration);

        float GetTotalLength() const;
        float GetLineType() const;

    private:
        std::vector<glm::vec3> mPoints;
        GLfloat mLineWidth = MIN_LINE_WIDTH;
        glm::vec4 mColor = glm::vec4(1.0f, 1.0f, 1.0f, 1.0f);
        Biquad mBiquadfilter[3];
        float mLineType = 0;


        friend class DrawArApplication;

    };

    float angle(glm::vec3 v1, glm::vec3 v2);

}


#endif