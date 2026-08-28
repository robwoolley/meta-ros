# Every ROS recipe, generated or not, must contain "inherit ros_distro_${ROS_DISTRO}".
#
# Copyright (c) 2020 LG Electronics, Inc.

ROS_DISTRO = "lyrical"

inherit ${ROS_DISTRO_TYPE}_distro

# Widespread bug in this distro's generated recipes: 236 of the 241 that
# reference rosidl-default-runtime at all only declare it in
# ROS_EXEC_DEPENDS (RDEPENDS), not ROS_BUILD_DEPENDS/ROS_EXPORT_DEPENDS
# (which feed DEPENDS) -- unlike the 3 that get it right (e.g.
# builtin-interfaces' equivalent rosidl-core-runtime). Any package
# exporting a CMake config that transitively requires
# rosidl_default_runtime (which nearly all ament_cmake interface
# packages do, via ament_cmake_export_dependencies-extras.cmake) fails
# at its *consumer's* configure time: "Could not find a package
# configuration file provided by rosidl_default_runtime". See
# ros-distro.inc's earlier note (this must live in a class that every
# recipe actually `inherit`s -- a `require`d .conf/.inc file's
# anonymous python is never registered for per-recipe execution, unlike
# a bbclass's).
python() {
    if d.getVar('PN') == 'rosidl-default-runtime':
        return
    if 'rosidl-default-runtime' in (d.getVar('ROS_EXEC_DEPENDS') or '').split():
        d.appendVar('DEPENDS', ' rosidl-default-runtime')
}

# Same shape of bug, different package: ament-cmake-native's own DEPENDS
# only stages ament-cmake-target-dependencies-native into the *native*
# sysroot, never the target-arch ament-cmake-target-dependencies package
# into a cross-compiling consumer's sysroot. Any recipe whose CMakeLists.txt
# calls ament_target_dependencies() -- defined by
# ament_cmake_target_dependencies-extras.cmake -- fails to configure with
# "Unknown CMake command \"ament_target_dependencies\"" because that macro
# is never found. Recipes declare one of three ament-cmake-family buildtools
# in ROS_BUILDTOOL_DEPENDS (REP-0140) depending on which convenience wrapper
# their package.xml names -- ament_cmake, ament_cmake_auto, or
# ament_cmake_ros -- all of which need the same macro staged.
python() {
    if d.getVar('PN') == 'ament-cmake-target-dependencies':
        return
    buildtool_deps = (d.getVar('ROS_BUILDTOOL_DEPENDS') or '').split()
    ament_cmake_family = {'ament-cmake-native', 'ament-cmake-auto-native', 'ament-cmake-ros-native'}
    if ament_cmake_family & set(buildtool_deps):
        d.appendVar('DEPENDS', ' ament-cmake-target-dependencies')
}
