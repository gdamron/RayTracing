cmake_minimum_required( VERSION 3.1.0 )

project ( RayTracing
    VERSION 2020.10.0
    LANGUAGES CXX
)

set ( CMAKE_CXX_STANDARD 11 )

set ( COMMON_ALL
    src/color.h
    src/hittable.h
    src/ray.h
    src/vec3.h
)

set ( SOURCE_TRACER
    ${COMMON_ALL}
    src/main.cc
)

add_executable(rayTracer   ${SOURCE_TRACER})
