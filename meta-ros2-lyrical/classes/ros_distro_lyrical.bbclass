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

# Related bug, harmless-but-no-longer-sufficient bitbake-level half of the
# fix. Any recipe whose CMakeLists.txt calls ament_target_dependencies()
# fails to configure with "Unknown CMake command
# \"ament_target_dependencies\"". ament-cmake-native's own DEPENDS only
# stages ament-cmake-target-dependencies-native into the *native* sysroot,
# never the target-arch ament-cmake-target-dependencies package into a
# cross-compiling consumer's sysroot -- so stage it here too, keyed off
# whichever of the three ament-cmake-family buildtools (ament_cmake,
# ament_cmake_auto, ament_cmake_ros; REP-0140) the recipe declared in
# ROS_BUILDTOOL_DEPENDS.
#
# Turns out this alone doesn't fix the error: ament_target_dependencies()
# has been removed from ament_cmake_target_dependencies outright in
# lyrical, not merely gated behind find_package()'ing it (confirmed by
# grepping the entire sysroot for its definition -- nothing, not even
# inside that package itself; see
# https://github.com/ament/ament_cmake/pull/572). Left this staging fix in
# place anyway since it's harmless and doesn't hurt. The actual fix is a
# compatible reimplementation of the macro, injected globally via
# CMAKE_PROJECT_INCLUDE -- see ament_target_dependencies_compat.cmake next
# to this bbclass.
python() {
    if d.getVar('PN') == 'ament-cmake-target-dependencies':
        return
    buildtool_deps = (d.getVar('ROS_BUILDTOOL_DEPENDS') or '').split()
    ament_cmake_family = {'ament-cmake-native', 'ament-cmake-auto-native', 'ament-cmake-ros-native'}
    if ament_cmake_family & set(buildtool_deps):
        d.appendVar('DEPENDS', ' ament-cmake-target-dependencies')
}

EXTRA_OECMAKE:append = " -DCMAKE_PROJECT_INCLUDE=${ROS2_LYRICAL_LAYER_DIR}/classes/ament_target_dependencies_compat.cmake"

# generate-parameter-library's package.xml declares generate_parameter_library
# as a regular (non-buildtool) dependency, even though its CMake macro
# generate_parameter_library() runs generate_parameter_library_cpp -- a
# code generator that must execute on the build host -- via find_program()
# at configure time. bitbake's "export" mechanism doesn't cascade past one
# hop (see the recipe's own comment on ROS_BUILDTOOL_EXPORT_DEPENDS), so
# generate-parameter-library-native being built is not enough: consumers
# never get the native tool staged into their own sysroot, and
# find_program() fails with "generate_parameter_library_cpp_BIN must not
# be empty". The actual generate_parameter_library_cpp binary is shipped
# by the sibling generate-parameter-library-py-native package (confirmed
# via tmp/sysroots-components), not generate-parameter-library-native
# itself, which only provides the CMake config.
python() {
    if d.getVar('PN') in ('generate-parameter-library', 'generate-parameter-library-py'):
        return
    if 'generate-parameter-library' in (d.getVar('DEPENDS') or '').split():
        d.appendVar('DEPENDS', ' generate-parameter-library-py-native')
}

# The rosidl-adapter-native fix in ros-distro.inc
# (DEPENDS:append:pn-rosidl-cmake) stages the tool into rosidl-cmake's
# *own* recipe-sysroot-native correctly (verified: its own do_configure's
# "Direct dependencies" now lists rosidl-adapter-native), but that alone
# doesn't reach a package like autoware-planning-msgs three hops away
# (autoware-planning-msgs -> rosidl-default-generators -> rosidl-cmake).
# Unlike target-to-target sysroot staging, a target recipe's *native*
# sysroot only gets what's in that recipe's own DEPENDS -- a dependency's
# own native tool requirements don't cascade into a grandparent consumer's
# recipe-sysroot-native. So every package that transitively needs
# rosidl_generate_interfaces() -- i.e. every one with rosidl-default-generators
# in DEPENDS, the same near-universal condition as the rosidl-default-runtime
# fix above -- needs rosidl-adapter-native added directly to its own DEPENDS.
python() {
    if d.getVar('PN') in ('rosidl-default-generators', 'rosidl-adapter'):
        return
    if 'rosidl-default-generators' in (d.getVar('DEPENDS') or '').split():
        d.appendVar('DEPENDS', ' rosidl-adapter-native')
}
