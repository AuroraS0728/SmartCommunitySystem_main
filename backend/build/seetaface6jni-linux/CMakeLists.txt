cmake_minimum_required(VERSION 3.10)
project(SeetaFace6JNI LANGUAGES CXX)

set(CMAKE_BUILD_TYPE Release)
set(CMAKE_CXX_STANDARD 11)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_POSITION_INDEPENDENT_CODE ON)

find_package(JNI REQUIRED)

set(SEETAFACE_INCLUDE_DIR "${PROJECT_SOURCE_DIR}/include")
set(SEETAFACE_LIB_DIR "${PROJECT_SOURCE_DIR}/lib64")

include_directories(
    ${JNI_INCLUDE_DIRS}
    "${SEETAFACE_INCLUDE_DIR}"
    "${SEETAFACE_INCLUDE_DIR}/seeta"
)

link_directories("${SEETAFACE_LIB_DIR}")

aux_source_directory("${PROJECT_SOURCE_DIR}/src" SRC_LIST)

add_library(SeetaFace6JNI SHARED ${SRC_LIST})

target_link_libraries(SeetaFace6JNI
    SeetaFaceDetector600
    SeetaFaceLandmarker600
    SeetaFaceRecognizer610
    SeetaFaceAntiSpoofingX600
    SeetaMaskDetector200
    SeetaEyeStateDetector200
    SeetaQualityAssessor300
    SeetaPoseEstimation600
    SeetaAuthorize
    tennis
    tennis_haswell
    tennis_pentium
    tennis_sandy_bridge
    stdc++
)

set_target_properties(SeetaFace6JNI PROPERTIES
    OUTPUT_NAME "SeetaFace6JNI"
    BUILD_RPATH "${SEETAFACE_LIB_DIR}"
    INSTALL_RPATH "$ORIGIN"
)
