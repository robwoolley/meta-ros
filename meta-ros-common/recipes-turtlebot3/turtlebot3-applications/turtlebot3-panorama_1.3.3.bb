# Copyright (c) 2026 Wind River Systems, Inc.
#
# Hand-authored: see turtlebot3-yolo-object-detection_1.3.3.bb for the full
# rationale (no bloom release for this repo/distro combination exists to
# generate from). One of 7 packages in the turtlebot3_applications monorepo.
DESCRIPTION = "Panorama capture example for TurtleBot3"
HOMEPAGE = "https://github.com/ROBOTIS-GIT/turtlebot3_applications"
SECTION = "devel"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://package.xml;beginline=10;endline=10;md5=4633480cdd27d7906aaf3ef4b72014b2"

ROS_CN = "turtlebot3_applications"
ROS_BPN = "turtlebot3_panorama"

SRC_URI = "git://github.com/ROBOTIS-GIT/turtlebot3_applications.git;branch=main;protocol=https"
SRCREV = "2f0310775daded07bed9be87a6602e0e6e1bae1a"

S = "${UNPACKDIR}/${BP}/turtlebot3_panorama"

# package.xml declares no dependencies at all beyond the ament_cmake
# buildtool, but CMakeLists.txt directly find_package()s all of these -- a
# package.xml under-declaration, not something this recipe got wrong.
ROS_BUILD_DEPENDS = " \
    boost \
    cv-bridge \
    geometry-msgs \
    image-transport \
    nav-msgs \
    opencv \
    rclcpp \
    sensor-msgs \
    tf2 \
    tf2-geometry-msgs \
    turtlebot3-applications-msgs \
"

ROS_BUILDTOOL_DEPENDS = " \
    ament-cmake-native \
"

ROS_EXPORT_DEPENDS = ""

ROS_BUILDTOOL_EXPORT_DEPENDS = ""

ROS_EXEC_DEPENDS = " \
    cv-bridge \
    geometry-msgs \
    image-transport \
    nav-msgs \
    opencv \
    rclcpp \
    sensor-msgs \
    tf2 \
    tf2-geometry-msgs \
    turtlebot3-applications-msgs \
"

ROS_TEST_DEPENDS = ""

DEPENDS = "${ROS_BUILD_DEPENDS} ${ROS_BUILDTOOL_DEPENDS}"
# Bitbake doesn't support the "export" concept, so build them as if we needed them to build this package (even though we actually
# don't) so that they're guaranteed to have been staged should this package appear in another's DEPENDS.
DEPENDS += "${ROS_EXPORT_DEPENDS} ${ROS_BUILDTOOL_EXPORT_DEPENDS}"

RDEPENDS:${PN} += "${ROS_EXEC_DEPENDS}"

ROS_BUILD_TYPE = "ament_cmake"

inherit ros_distro_lyrical
inherit ros_component
inherit ros_${ROS_BUILD_TYPE}

# Boost 1.90 removed the compiled boost_system stub library and its CMake
# package entirely -- header-only since 1.69. Same fix already used
# elsewhere in this tree (e.g. hls-lfcd-lds-driver).
#
# tf2 0.45.7 (this layer set's version) renamed its headers from .h to
# .hpp; upstream panorama.cpp (written against an older tf2) still includes
# the old .h names, which no longer exist at all. Same fix as
# turtlebot3-node_2.3.7.bb.
do_configure:prepend() {
    sed -i \
        -e 's/find_package(Boost REQUIRED COMPONENTS system)/find_package(Boost REQUIRED)/' \
        ${S}/CMakeLists.txt

    if ! grep -q "tf2/LinearMath/Quaternion.hpp" ${S}/src/panorama.cpp; then
        sed -i \
            -e 's#tf2/LinearMath/Quaternion\.h#tf2/LinearMath/Quaternion.hpp#' \
            -e 's#tf2/utils\.h#tf2/utils.hpp#' \
            ${S}/src/panorama.cpp
    fi
}
