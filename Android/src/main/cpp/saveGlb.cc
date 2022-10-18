#include "saveGlb.h"


namespace draw_ar {
    unsigned int saveGlb::getBinaryBytes() {
        unsigned int bytes;
        unsigned int vc = mVertexs.size();
        bytes = mIndices.size() * sizeof(unsigned short);

        // padding code
        if (bytes % 4 != 0) {
            bytes = (bytes / 4 + 1) * 4;
        }
        bytes += vc * 3 * sizeof(float) * 2; // vertex and color

        return bytes;
    }

    string saveGlb::saveGlbFile() {
        stringstream stream;

        // json

        stream << "{\"scene\":0,\"scenes\":[{\"nodes\":[0, 1]}],";
        stream << "\"nodes\":[{\"mesh\":0},{\"camera\":0}],";
        stream << "\"cameras\":[{\"type\":\"perspective\",\"perspective\":{\"aspectRatio\":1.0,\"yfov\":0.7,\"zfar\":100,\"znear\":0.001}}],";
        stream << "\"meshes\":[{\"primitives\":[{\"attributes\":{\"POSITION\":1,\"COLOR_0\":2},\"indices\":0}]}],";
        stream << "\"buffers\":[{\"byteLength\":";
        stream << getBinaryBytes();
        stream << "}],";

        // bufferViews
        stream << "\"bufferViews\":[{";
        stream << "\"buffer\":0,";
        stream << "\"byteOffset\":0,";
        stream << "\"byteLength\":" << mIndices.size() * sizeof(unsigned short) << ",";
        stream << "\"target\":34963";
        stream << "},";

        unsigned int offset = mIndices.size() * sizeof(unsigned short); //(mVertexs.size() - 2) * 3 * sizeof(unsigned short) * 2;
        if (offset % 4 != 0) {
            offset = (offset / 4 + 1) * 4;
        }

        stream << "{";
        stream << "\"buffer\":0,";
        stream << "\"byteOffset\":" << offset << ",";
        stream << "\"byteLength\":" << mVertexs.size() * 3 * sizeof(float) << ",";
        stream << "\"target\":34962";
        stream << "},";

        // mColor
        stream << "{";
        stream << "\"buffer\":0,";
        stream << "\"byteOffset\":" << offset + mVertexs.size() * 3 * sizeof(float) << ",";
        stream << "\"byteLength\":" << mVertexs.size() * 3 * sizeof(float) << ",";
        stream << "\"target\":34962";
        stream << "}],";

        // accessor
        stream << "\"accessors\":[{";
        stream << "\"bufferView\":0,";
        stream << "\"byteOffset\":0,";
        stream << "\"componentType\":5123,";
        //stream << "\"count\":" << (mVertexs.size() - 2) * 3 * 2 << ",";
        stream << "\"count\":" << mIndices.size() << ",";
        stream << "\"type\":\"SCALAR\",";
        stream << "\"max\":[" << mVertexs.size() - 1 << "],\"min\":[0]},{";

        stream << "\"bufferView\":1,";
        stream << "\"byteOffset\":0,";
        stream << "\"componentType\":5126,";
        stream << "\"count\":" << mVertexs.size() << ",";
        stream << "\"type\":\"VEC3\",";
        stream << "\"max\":[" << mMaxVec.x << "," << mMaxVec.y << "," << mMaxVec.z << "],";
        stream << "\"min\":[" << mMinVec.x << "," << mMinVec.y << "," << mMinVec.z << "]},{";

        // color
        stream << "\"bufferView\":2,";
        stream << "\"byteOffset\":0,";
        stream << "\"componentType\":5126,";
        stream << "\"count\":" << mVertexs.size() << ",";
        stream << "\"type\":\"VEC3\",";
        stream << "\"max\":[" << mMaxCol.x << "," << mMaxCol.y << "," << mMaxCol.z << "],";
        stream << "\"min\":[" << mMinCol.x << "," << mMinCol.y << "," << mMinCol.z << "]}],";

        // asset
        stream << "\"asset\":{"
               << "\"generator\":\"DrawEverywhere.3D\","
                  "\"version\":\"2.0\"}}";

        string json = stream.str();


        unsigned int count = 0;
        for (unsigned int i = 0; i < json.size(); ++i) {
            //if (!isspace(json[i]))
                ++count;
        }

        LOGE("json Size : %u, %u", json.size(), count);


        // bufferDump
        unsigned int padding = 0;
        unsigned int arrangeSize = count;

        if (count % 4 != 0) {
            padding = 4 - (count % 4);
            arrangeSize += padding;
        }

        void *header = nullptr;
        header = malloc(20);
        char *pCurHeader = reinterpret_cast<char *>(header);

        unsigned int magic = 0x46546C67;
        unsigned int version = 2;
        unsigned int paddingByte = 0x20202020;
        unsigned int fileLength = arrangeSize + getBinaryBytes() + 28;

        memcpy(pCurHeader, &magic, sizeof(unsigned int));
        pCurHeader += sizeof(unsigned int);

        memcpy(pCurHeader, &version, sizeof(unsigned int));
        pCurHeader += sizeof(unsigned int);

        // 전체 길이
        memcpy(pCurHeader, &fileLength, sizeof(unsigned int));
        pCurHeader += sizeof(unsigned int);

        // json 길이
        memcpy(pCurHeader, &arrangeSize, sizeof(unsigned int));
        pCurHeader += sizeof(unsigned int);
        unsigned int jsonSignature = 0x4E4F534A;

        memcpy(pCurHeader, &jsonSignature, sizeof(unsigned int));
        pCurHeader += sizeof(unsigned int);

        char *pInitial = reinterpret_cast<char *>(header);

        unsigned int byteLengths = getBinaryBytes();
        unsigned int binarySignature = 0x004E4942;

        void *const memory = malloc(sizeof(char) * getBinaryBytes());
        char *pMemory = reinterpret_cast<char *>(memory);

        // buffer Scope
        {
            unsigned int offset = 0;
            unsigned int vc = mVertexs.size();

            for (unsigned int u = 0; u < mIndices.size(); ++u) {
                unsigned short s = mIndices[u];

                memcpy(pMemory, &s, sizeof(unsigned short));
                pMemory += sizeof(unsigned short);
                offset += sizeof(unsigned short);
            }


            // padding
            if (offset % 4 != 0) {
                pMemory += (4 - (offset % 4));
            }

            // vertex
            for (unsigned int i = 0; i < vc; ++i) {
                point_t p = mVertexs[i];
                float f[3] = {p.x, p.y, p.z};
                memcpy(pMemory, &f, sizeof(float) * 3);
                pMemory += 3 * sizeof(float);
            }

            // color
            for (unsigned int i = 0; i < vc; ++i) {
                point_t p = mColors[i];
                float f[3] = {p.x, p.y, p.z};
                memcpy(pMemory, &f, sizeof(float) * 3);
                pMemory += 3 * sizeof(float);
            }
        }


        LOGE("%s/draw.glb", util::getCacheDir().c_str());

        ofstream writeFile(util::getCacheDir() + "/draw.glb");
        if (writeFile.is_open()) {

            for (unsigned int i = 0; i < 20; ++i) {
                writeFile << pInitial[i];
            }

            writeFile.write(json.c_str(), sizeof(char) * json.size());
            writeFile.write(reinterpret_cast<char *>(&paddingByte), sizeof(char) * padding);
            writeFile.write(reinterpret_cast<char *>(&byteLengths), sizeof(unsigned int));
            writeFile.write(reinterpret_cast<char *>(&binarySignature), sizeof(unsigned int));
            writeFile.write(reinterpret_cast<char *>(memory), sizeof(char) * getBinaryBytes());
            writeFile.close();
        }

        if (header != nullptr) {
            free(header);
        }

        free(memory);

        return stream.str();
    }

    void saveGlb::addVertex(float x, float y, float z) {
        if (mVertexs.size() > 0) {
            mMaxVec.x = mMaxVec.x < x ? x : mMaxVec.x;
            mMaxVec.y = mMaxVec.y < y ? y : mMaxVec.y;
            mMaxVec.z = mMaxVec.z < z ? z : mMaxVec.z;

            mMinVec.x = mMinVec.x > x ? x : mMinVec.x;
            mMinVec.y = mMinVec.y > y ? y : mMinVec.y;
            mMinVec.z = mMinVec.z > z ? z : mMinVec.z;
        } else {
            mMaxVec.x = x;
            mMaxVec.y = y;
            mMaxVec.z = z;

            mMinVec.x = x;
            mMinVec.y = y;
            mMinVec.z = z;
        }

        point_t p;

        p.x = x;
        p.y = y;
        p.z = z;

        mVertexs.push_back(p);
        mColors.push_back(mColor);
    }

    void saveGlb::setColor(float x, float y, float z) {
        if (mVertexs.size() > 0) {
            mMaxCol.x = mMaxCol.x < x ? x : mMaxCol.x;
            mMaxCol.y = mMaxCol.y < y ? y : mMaxCol.y;
            mMaxCol.z = mMaxCol.z < z ? z : mMaxCol.z;

            mMinCol.x = mMinCol.x > x ? x : mMinCol.x;
            mMinCol.y = mMinCol.y > y ? y : mMinCol.y;
            mMinCol.z = mMinCol.z > z ? z : mMinCol.z;
        } else {
            mMaxCol = {x, y, z};
            mMinCol = {x, y, z};
        }

        mColor = {x, y, z};
    }

    saveGlb::saveGlb(std::vector<Stroke> strokes) {

        glm::vec3 center = glm::vec3(mMaxVec.x - mMinVec.x, mMaxVec.y - mMinVec.y, mMaxVec.z - mMinVec.z);

        // stroke 루프
        for (auto &stroke : strokes) {
            auto points = stroke.GetPoints();

            // Stroke 단위 setColor
            setColor(stroke.GetColor().x, stroke.GetColor().y, stroke.GetColor().z);
            int size = points.size();
            float width = stroke.GetLineWidth();
            unsigned int first = mVertexs.size();

            // Stroke 내부의 mPoints 돌기
            for (int i = 0; i < size; ++i) {
                glm::vec3 prev = points[(i - 1) > 0 ? i - 1 : 0];
                glm::vec3 cur = points[i];
                glm::vec3 next = points[(i + 1) < size ? i + 1 : size - 1];

                prev = prev - center;
                cur = cur - center;
                next = next - center;

                glm::vec3 t1 = fixVector(prev, cur, next, 1.0f, width);
                addVertex(t1.x, t1.y, t1.z);

                glm::vec3 t2 = fixVector(prev, cur, next, -1.0f, width);
                addVertex(t2.x, t2.y, t2.z);

            }

            unsigned int end = mVertexs.size();
            addIndices(first, end);
        }
    }

    void saveGlb::addIndices(unsigned int first, unsigned int end) {
        unsigned int f = 0;

        for (f = first; f + 2 < end; ++f) {
            mIndices.push_back(f);
            mIndices.push_back(f + 1);
            mIndices.push_back(f + 2);

            mIndices.push_back(f + 2);
            mIndices.push_back(f + 1);
            mIndices.push_back(f);
        }
        LOGI("f : %u \n", f);
    }

    bool compareVec2(glm::vec2 v1, glm::vec2 v2) {
        glm::vec2 t = v1 - v2;
        t.x = t.x < 0 ? t.x * (-1) : t.x;
        t.y = t.y < 0 ? t.y * (-1) : t.y;

        float e = std::numeric_limits<float>::epsilon();

        return (t.x < e) && (t.y < e);
    }

    glm::vec3 saveGlb::fixVector(glm::vec3 prev, glm::vec3 cur, glm::vec3 next, float side, float width) {

        glm::vec2 prevP = glm::vec2(prev.x, prev.y);
        glm::vec2 curP = glm::vec2(cur.x, cur.y);
        glm::vec2 nextP = glm::vec2(next.x, next.y);

        glm::vec2 dir;

        if (compareVec2(nextP, curP)) {
            dir = curP - prevP;
        } else if (compareVec2(prevP, curP)) {
            dir = nextP - curP;
        } else {
            glm::vec2 cp = curP - prevP;
            glm::vec2 nc = nextP - curP;
            dir = cp + nc;
        }

        glm::vec2 vNormal(-dir.y, dir.x);
        vNormal = glm::normalize(vNormal);

        vNormal *= 0.3 * width;
        vNormal *= side;

        glm::vec3 result(cur.x, cur.y, cur.z);
        result.x += vNormal.x;
        result.y += vNormal.y;

        return result;
    }
}