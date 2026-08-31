# Copyright (c) 2026 Wind River Systems, Inc.
#
# In lyrical, ament_target_dependencies() has been removed outright, not
# just gated behind an extra find_package() call: grepping this build's
# entire sysroot (both native and target) for "macro(ament_target_dependencies"
# or "function(ament_target_dependencies" finds nothing at all, including
# inside ament_cmake_target_dependencies itself -- its own
# ament_cmake_target_dependencies-extras.cmake only pulls in
# ament_get_recursive_properties.cmake, a helper the macro used to call
# internally, not the macro itself (see
# https://github.com/ament/ament_cmake/pull/572). So find_package()'ing
# that package, however it's staged, was never going to fix this.
#
# Most of the ROS ecosystem's CMakeLists.txt files haven't been migrated
# off ament_target_dependencies() yet, so provide a compatible
# reimplementation instead of patching 25+ individual upstream files.
# Modern ament_cmake packages (confirmed via cv_bridge et al.'s
# export_*Export.cmake) export a <pkg>::<pkg> imported target and no
# longer set the legacy <pkg>_INCLUDE_DIRS/_LIBRARIES variables at all, but
# non-ROS dependencies found via a plain CMake Find module (e.g. OpenCV,
# which aruco_ros also passes to ament_target_dependencies) still only set
# those legacy variables -- so this covers both.
#
# Injected via CMAKE_PROJECT_INCLUDE -- see ros_distro_lyrical.bbclass.
if(NOT COMMAND ament_target_dependencies)
  macro(ament_target_dependencies target)
    cmake_parse_arguments(_atd "" "" "PUBLIC;PRIVATE;INTERFACE" ${ARGN})
    foreach(_atd_dep ${_atd_UNPARSED_ARGUMENTS} ${_atd_PUBLIC} ${_atd_PRIVATE} ${_atd_INTERFACE})
      if(TARGET ${_atd_dep}::${_atd_dep})
        target_link_libraries(${target} PUBLIC ${_atd_dep}::${_atd_dep})
      elseif(TARGET ${_atd_dep})
        target_link_libraries(${target} PUBLIC ${_atd_dep})
      else()
        if(DEFINED ${_atd_dep}_INCLUDE_DIRS)
          target_include_directories(${target} PUBLIC ${${_atd_dep}_INCLUDE_DIRS})
        endif()
        if(DEFINED ${_atd_dep}_LIBRARIES)
          target_link_libraries(${target} PUBLIC ${${_atd_dep}_LIBRARIES})
        endif()
        if(DEFINED ${_atd_dep}_DEFINITIONS)
          target_compile_definitions(${target} PUBLIC ${${_atd_dep}_DEFINITIONS})
        endif()
      endif()
    endforeach()
  endmacro()
endif()
