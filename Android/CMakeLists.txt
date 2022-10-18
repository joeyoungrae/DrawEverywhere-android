
cmake_minimum_required(VERSION 3.4.1)

# ArCore Library 추가
add_library(arcore SHARED IMPORTED)
set_target_properties(arcore PROPERTIES IMPORTED_LOCATION
        ${ARCORE_LIBPATH}/${ANDROID_ABI}/libarcore_sdk_c.so
        INTERFACE_INCLUDE_DIRECTORIES ${ARCORE_INCLUDE}
        )

# NDK의 형태로 glm 헤더 추가
add_library( glm INTERFACE )
set_target_properties( glm PROPERTIES INTERFACE_INCLUDE_DIRECTORIES ${GLM_INCLUDE})


# cpp 폴더 내부 전체 파일 추가
#[[file(GLOB SOURCES "src/main/cpp/*.cc")
add_library(draw_everywhere_native SHARED
        ${SOURCES})]]

# c++ 파일 추가
add_library(draw_everywhere_native SHARED
        src/main/cpp/background_renderer.cc
        src/main/cpp/common_util.cc
        src/main/cpp/dependence_util.cc
        src/main/cpp/draw_ar_application.cc
        src/main/cpp/jni_interface.cc
        src/main/cpp/stroke_model.cc
        src/main/cpp/line_renderer.cc
        src/main/cpp/saveGlb.cc
        src/main/cpp/biquad.cc
        src/main/cpp/jsoncpp.cpp)

add_library(place_everywhere_native SHARED
        src/main/cpp/place_ar_application.cc
        src/main/cpp/jni_interface_for_place.cc
        src/main/cpp/dependence_util.cc
        src/main/cpp/common_util.cc
        src/main/cpp/background_renderer.cc
        src/main/cpp/jsoncpp.cpp
        src/main/cpp/point_renderer.cc
        src/main/cpp/obj_renderer.cc
        src/main/cpp/glb_object.cc
        src/main/cpp/plane_renderer.cc)


add_library(library_collection SHARED
        src/main/cpp/abcdefg.cc
        )

# 라이브러리들을 링커처리함
target_include_directories(draw_everywhere_native PRIVATE
        src/main/cpp)
target_link_libraries(draw_everywhere_native
        android
        log
        glm
        GLESv2
        arcore)

target_include_directories(place_everywhere_native PRIVATE
        src/main/cpp)
target_link_libraries(place_everywhere_native
        android
        log
        glm
        GLESv2
        arcore)

target_include_directories(library_collection PRIVATE
        src/main/cpp)
target_link_libraries(library_collection
        android
        log)


