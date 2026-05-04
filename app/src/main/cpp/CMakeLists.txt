cmake_minimum_required(VERSION 3.22.1)
project("spotifyplus")

include_directories(${CMAKE_SOURCE_DIR}/../../../libnode/include/node/)

add_library(libnode SHARED IMPORTED)
set_target_properties(libnode PROPERTIES
    IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/../../../libnode/bin/${ANDROID_ABI}/libnode.so
)

add_library(native-lib SHARED
    native-lib.cpp
    spotifyengine.cpp
    spotifyplus-exports.cpp
)

add_library(spotifyplus_bridge SHARED
    spotifyplus_bridge.cpp
)

target_link_libraries(native-lib
    android
    log
    libnode
)

target_link_libraries(spotifyplus_bridge
    android
    log
    libnode
)