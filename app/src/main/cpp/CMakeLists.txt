cmake_minimum_required(VERSION 3.22.1)
project("sr_mods_engine")

add_library(
        sr_mods_engine
        SHARED
        native-lib.cpp
)

find_library(
        log-lib
        log
)

target_link_libraries(
        sr_mods_engine
        ${log-lib}
)
