cmake_minimum_required (VERSION 2.8)
project (SeetaFace2JNI)

#MESSAGE(STATUS "platform: ${CMAKE_SYSTEM_NAME}")

set(CMAKE_BUILD_TYPE "Release")
set(CMAKE_CXX_FLAGS_RELEASE "${CMAKE_CXX_FLAGS} -std=c++11 -g -o2")


if (CMAKE_SYSTEM_NAME MATCHES "Darwin")
    set(CMAKE_CXX_FLAGS_RELEASE "${CMAKE_CXX_FLAGS_RELEASE} -DGTEST_USE_OWN_TR1_TUPLE=1")
endif (CMAKE_SYSTEM_NAME MATCHES "Darwin")

add_definitions(${CMAKE_CXX_FLAGS_RELEASE})

# set __VIOL_LOG__ macro
# add_definitions(-D__VIPL_LOG__)

set(JDK_INCLUDE_DIR /usr/local/jdk1.8.0_131/include)
#set(OPENCV_INCLUDE_DIR /usr/local/opencv/include)
set(SEETAFACEJNI_INCLUDE_DIR ${PROJECT_SOURCE_DIR}/include)
set(SEETAFACE2_INCLUDE_DIR ${PROJECT_SOURCE_DIR}/include/seeta)

#set(SEETAFACEJNI_SRC_DIR ${PROJECT_SOURCE_DIR}/src)

# use opencv
#set(CMAKE_PREFIX_PATH ${CMAKE_PREFIX_PATH} "/usr/local/opencv")
#find_package(OpenCV 3.4.5 REQUIRED)

include_directories(${JDK_INCLUDE_DIR})
include_directories(${JDK_INCLUDE_DIR}/linux)
#include_directories(${OPENCV_INCLUDE_DIR}/opencv2)
include_directories(${SEETAFACEJNI_INCLUDE_DIR})
include_directories(${SEETAFACE2_INCLUDE_DIR})
#include_directories(${SEETAFACEJNI_SRC_DIR})

#add_subdirectory(src)

set(SEETAFACE2_LIB_DIR ${PROJECT_SOURCE_DIR}/lib/linux/x64)
link_directories(${SEETAFACE2_LIB_DIR})

aux_source_directory(./src SRC_LIST)
add_library(SeetaFace2JNI SHARED ${SRC_LIST})
target_link_libraries(SeetaFace2JNI -lSeetaFaceDetector200 -lSeetaFaceRecognizer200 -lSeetaPointDetector200 -lSeetaFaceCropper200)
target_link_libraries(SeetaFace2JNI -lstdc++)
