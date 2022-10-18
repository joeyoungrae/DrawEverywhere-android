
#include "stroke_model.h"
#include "dependence_util.h"

namespace draw_ar {

    Stroke::Stroke(GLfloat lineWidth, glm::vec4 &color, float lineType) {
        if (lineWidth > 0) {
            mLineWidth = lineWidth;
        }

        if (color.x >= 0) {
            mColor = color;
        }

        mLineType = lineType;
    }

    Stroke::~Stroke() { Clear(); }


    void Stroke::AddPoint(const glm::vec3 &point) {
        unsigned int pSize = mPoints.size();

        glm::vec3 filterP = point;

        // biquad filter initial
        if (mPoints.empty()) {
            for (int i = 0; i < 3; ++i) {
                mBiquadfilter[i] = Biquad(0, 0.07f, 0.707, 0);
            }

            for (int i = 0; i < 1500; i++) {
                mBiquadfilter[0].process(point.x);
                mBiquadfilter[1].process(point.y);
                mBiquadfilter[2].process(point.z);
            }
        }


        filterP.x = mBiquadfilter[0].process(point.x);;
        filterP.y = mBiquadfilter[1].process(point.y);;
        filterP.z = mBiquadfilter[2].process(point.z);;


        if (!mPoints.empty()) {
            const glm::vec3 lastPoint = mPoints.back();

            // 점 사이의 거리가 너무 짧은 경우
            if (glm::length(point - lastPoint) < mLineWidth / 10) {
                return;
            }
        }

        //mPoints.emplace_back(point);
        mPoints.emplace_back(filterP);

        // 불필요한 점 삭제
        if (pSize > 3) {
            glm::vec3 p1 = mPoints[pSize - 3];
            glm::vec3 p2 = mPoints[pSize - 2];
            glm::vec3 p3 = mPoints[pSize - 1];

            glm::vec3 p1_2 = p2 - p1;
            glm::vec3 p2_3 = p3 - p2;

            float dir = angle(p1_2, p2_3);

            if (dir < 0.05) {
                mPoints.erase(mPoints.begin() + (pSize - 2));
            } else {
                SubDivideSection(pSize - 3, 0.3f, 0);
            }

        }

    }

    void Stroke::SubDivideSection(unsigned int s, float maxAngle, int iteration) {
        if (iteration == 6) {
            return;
        }

        glm::vec3 p1 = mPoints[s];
        glm::vec3 p2 = mPoints[s + 1];
        glm::vec3 p3 = mPoints[s + 2];

        glm::vec3 p1_2 = p2 - p1;
        glm::vec3 p2_3 = p3 - p2;

        float dir = angle(p1_2, p2_3);

        if (dir > maxAngle) {
            p1_2 *= 0.5;
            p2_3 *= 0.5;
            p1 += p1_2;
            p2 += p2_3;

            mPoints.insert(mPoints.begin() + (s + 1), p1);
            mPoints.insert(mPoints.begin() + (s + 3), p2);

            SubDivideSection(s + 2, maxAngle, iteration + 1);
            SubDivideSection(s, maxAngle, iteration + 1);
        }
    }

    float angle(glm::vec3 v1, glm::vec3 v2) {
        double xx = v1.y * v2.z - v1.z * v2.y;
        double yy = v1.z * v2.x - v1.x * v2.z;
        double zz = v1.x * v2.y - v1.y * v2.x;
        double cross = sqrt(xx*xx + yy*yy + zz*zz);

        return (float)abs(atan2(cross, v1.x * v2.x + v1.y * v2.y + v1.z * v2.z));
    }


    void Stroke::Clear() {
        mPoints.clear();
    }

    std::vector<glm::vec3> Stroke::GetPoints() const {
        return mPoints;
    }

    float Stroke::GetLineType() const {
        return mLineType;
    }


    GLfloat Stroke::GetLineWidth() const {
        return mLineWidth;
    }

    glm::vec4 Stroke::GetColor() const {
        return mColor;
    }

    float Stroke::GetTotalLength() const {
        float totalLength = 0.0f;

        for (unsigned int i = 1; i < mPoints.size(); ++i) {
            glm::vec3 previous = mPoints[i - 1];
            totalLength += glm::length(mPoints[i] - previous);
        }

        return totalLength;
    }


}