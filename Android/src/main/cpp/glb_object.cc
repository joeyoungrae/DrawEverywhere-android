//
// Created by 조영래 on 2022-05-17.
//

#include "glb_object.h"

namespace draw_ar {
    GlbObject::~GlbObject() {



        if (mNodes != nullptr)
        {
            delete[] mNodes;
            LOGI("nodes destroyed");
            mNodes = nullptr;
        }

        if (mBufferViews != nullptr)
        {
            delete[] mBufferViews;
            LOGI("bufferViews destroyed");
            mBufferViews = nullptr;
        }

        if (mAccessors != nullptr)
        {
            delete[] mAccessors;
            LOGI("accessors destroyed");
            mAccessors = nullptr;
        }


    }



    void GlbObject::loadGLB(string fileName) {
        // Binary 파싱하기
        LOGI("로더__시작");


        std::ifstream rs;

        rs.open(fileName.c_str(), std::fstream::binary | std::fstream::in);

        LOGI("파일 열기");

        // JSON 앞까지 만들기
        char *header = new char[21];
        rs.read(header, 20);
        header[20] = 0;

        unsigned int jsonLength;
        unsigned int fileLength;

        memcpy(&fileLength, &(header[8]), sizeof(unsigned int));
        memcpy(&jsonLength, &(header[12]), sizeof(unsigned int));

        cout << "fileLength : " << fileLength << endl;
        cout << "jsonLength : " << jsonLength << endl;

        delete[] header;

        // JSON 길이만큼 불러오기
        char *jsonBody = new char[jsonLength + 1];
        rs.read(jsonBody, jsonLength);
        jsonBody[jsonLength] = '\0';

        LOGI("헤더 처리");

        // 뒤에 패딩있는 경우 제거하기
        for (int i = jsonLength; i != 0; --i)
        {
            if (jsonBody[i] == '}')
            {
                cout << "end index : " << i << endl;
                break;
            }
            else
            {
                cout << "i " << i << endl;
                jsonBody[i] = '\0';
            }
        }

        string jsonBodyStr(jsonBody);

        delete[] jsonBody;

        // Json부분, 앞에서 Binary 헤더 처리
        Json::Value root;
        Json::Reader reader;

        reader.parse(jsonBodyStr, root); // json >> root; 두 구문 모두 json을 파싱하는데 사용할 수 있다.

        // Senes_nodes 저장하기
        mNodesSize = root["scenes"][0]["nodes"].size();

        mNodes = new int[mNodesSize];

        // nodes_속성 관련하여 저장하기 -> 현재 자체 glb전용이라 하나
        // camera 속성 관련하여 저장하기 -> 현재 자체 glb전용이라 없음.

        // mesh 속성 저장하기 -> 자체 glb 전용이라 mesh가 하나로 가정
        mPositionIndex = root["meshes"][0]["primitives"][0]["attributes"]["POSITION"].asUInt();

        if (root["meshes"][0]["primitives"][0]["attributes"].isMember("COLOR_0"))
        {
            mColorIndex = root["meshes"][0]["primitives"][0]["attributes"]["COLOR_0"].asUInt();
            ++mBufferViewLength;
        }

        mIndiciesIndex = root["meshes"][0]["primitives"][0]["indices"].asUInt();

        // buffer -> 자체 glb 전용이라 buffer가 하나로 가정
        mByteLength = root["buffers"][0]["byteLength"].asUInt();

        auto bufferViewsJSON = root["bufferViews"];
        mBufferViews = new BufferViews_t[mBufferViewLength];
        for (int i = 0; i < mBufferViewLength; ++i)
        {
            mBufferViews[i].buffer = bufferViewsJSON[i]["buffer"].asUInt();
            mBufferViews[i].byteLength = bufferViewsJSON[i]["byteLength"].asUInt();
            mBufferViews[i].byteOffset = bufferViewsJSON[i]["byteOffset"].asUInt();
            mBufferViews[i].target = bufferViewsJSON[i]["target"].asUInt();
        }

        auto accessorsJSON = root["accessors"];
        mAccessors = new Accessors_t[mBufferViewLength];
        for (int i = 0; i < mBufferViewLength; ++i)
        {
            mAccessors[i].bufferView = accessorsJSON[i]["bufferView"].asUInt();
            mAccessors[i].byteOffset = accessorsJSON[i]["byteOffset"].asUInt();
            mAccessors[i].componentType = accessorsJSON[i]["componentType"].asUInt();
            mAccessors[i].count = accessorsJSON[i]["count"].asUInt();

            string typeString = accessorsJSON[i]["type"].asString();
            memcpy(reinterpret_cast<char *>(mAccessors[i].type), typeString.c_str(), typeString.length());
            mAccessors[i].type[typeString.length()] = '\0';
        }

        unsigned int binaryLength;
        rs.read(reinterpret_cast<char *>(&binaryLength), sizeof(unsigned int));
        cout << "binary size : " << binaryLength << endl;
        char *binary = new char[binaryLength];

        // 값 버리는 용도 (BIN)
        unsigned int trashValue;
        rs.read(reinterpret_cast<char *>(&trashValue), sizeof(unsigned int));
        rs.read(binary, binaryLength);

        // BufferView 기준으로 필요한 데이터 읽기 (1) INDICIES
        BufferViews_t indiciesInfo = mBufferViews[mAccessors[mIndiciesIndex].bufferView];
        char *pIndicies = binary + indiciesInfo.byteOffset;

        unsigned int indiciesLength = indiciesInfo.byteLength / sizeof(unsigned short);
        mIndicies.resize(indiciesLength);
        memcpy(reinterpret_cast<char *>(&(mIndicies[0])), pIndicies, indiciesInfo.byteLength);


        // BufferView 기준으로 필요한 데이터 읽기 (2) POSITION
        BufferViews_t positionInfo = mBufferViews[mAccessors[mPositionIndex].bufferView];
        unsigned int positionLength = positionInfo.byteLength / (sizeof(glm::vec3));

        mVertexs.resize(positionLength);

        char *pPositions = binary + positionInfo.byteOffset;
        memcpy(reinterpret_cast<char *>(&(mVertexs[0])), pPositions, positionInfo.byteLength);


        // BufferView 기준으로 필요한 데이터 읽기 (3) COLOR
        BufferViews_t colorInfo = mBufferViews[mAccessors[mColorIndex].bufferView];
        unsigned int colorLength = colorInfo.byteLength / (sizeof(glm::vec3));
        mColors.resize(colorLength);

        char *pColors = binary + colorInfo.byteOffset;
        memcpy(reinterpret_cast<char *>(&(mColors[0])), pColors, colorInfo.byteLength);

        delete[] binary;
        rs.close();

        moveCenter();
    }

    glm::vec3 GlbObject::getCenter() {
        return mCenterVertex;
    }

    void GlbObject::moveCenter() {
        LOGI("중앙 정렬 진행 전 %f %f %f", mVertexs[0].x, mVertexs[0].y, mVertexs[0].z);

        mMinVertex = mVertexs[0];
        mMaxVertex = mVertexs[0];

        for (int i = 1; i < mVertexs.size(); ++i) {
            for (int j = 0; j < 3; ++j) {
                mMinVertex[j] = mMinVertex[j] > mVertexs[i][j] ? mVertexs[i][j] : mMinVertex[j];
                mMaxVertex[j] = mMaxVertex[j] < mVertexs[i][j] ? mVertexs[i][j] : mMaxVertex[j];
            }
        }

        mCenterVertex = mMaxVertex + mMinVertex;
        mCenterVertex *= 0.5;

        for (int i = 0; i < mVertexs.size(); ++i) {
            mVertexs[i].x -= mCenterVertex.x; //mMinVertex.x - 0.5f;//average.x - 0.5f;
            mVertexs[i].y -= mCenterVertex.y; //mMinVertex.y - 0.5f;//average.y - 0.5f;
            mVertexs[i].z -= mCenterVertex.z; //mVertexs[i].z - mMinVertex.z; //
        }

        LOGI("중앙 정렬 진행 후 %f %f %f", mVertexs[0].x, mVertexs[0].y, mVertexs[0].z);

    }
}
