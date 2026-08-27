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
