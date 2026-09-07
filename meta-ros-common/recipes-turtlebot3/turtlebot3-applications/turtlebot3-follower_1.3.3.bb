# Copyright (c) 2026 Wind River Systems, Inc.
#
# Hand-authored: see turtlebot3-yolo-object-detection_1.3.3.bb for the full
# rationale (no bloom release for this repo/distro combination exists to
# generate from). One of 7 packages in the turtlebot3_applications monorepo.
DESCRIPTION = "Person/object-following example for TurtleBot3"
HOMEPAGE = "https://github.com/ROBOTIS-GIT/turtlebot3_applications"
SECTION = "devel"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://package.xml;beginline=10;endline=10;md5=3dce4ba60d7e51ec64f3c3dc18672dd3"

ROS_CN = "turtlebot3_applications"
ROS_BPN = "turtlebot3_follower"

SRC_URI = "git://github.com/ROBOTIS-GIT/turtlebot3_applications.git;branch=main;protocol=https"
SRCREV = "2f0310775daded07bed9be87a6602e0e6e1bae1a"

S = "${UNPACKDIR}/${BP}/turtlebot3_follower"

# package.xml declares no dependencies at all beyond the ament_cmake
# buildtool, but CMakeLists.txt directly find_package()s all of these -- a
# package.xml under-declaration, not something this recipe got wrong.
#
# NOTE: nav2-msgs has no recipe in this distro at all -- the entire Nav2
# stack was never generated for lyrical (confirmed: exists for
# humble/jazzy/kilted; rosdistro confirms lyrical has a real upstream
# release too, so this is a generation gap, not an upstream one).
# Backfilling all of Nav2 is a separate, much larger undertaking than
# turtlebot3 itself (see turtlebot3_2.3.7.bb's equivalent note re.
# turtlebot3-navigation2), so this package is left unbuilt/unreferenced by
# turtlebot3-applications for now. Every other dependency below is already
# available.
ROS_BUILD_DEPENDS = " \
    geometry-msgs \
    nav2-msgs \
    nav-msgs \
    rclcpp \
    std-msgs \
    tf2 \
    tf2-geometry-msgs \
    tf2-ros \
"

ROS_BUILDTOOL_DEPENDS = " \
    ament-cmake-native \
"

ROS_EXPORT_DEPENDS = ""

ROS_BUILDTOOL_EXPORT_DEPENDS = ""

ROS_EXEC_DEPENDS = " \
    geometry-msgs \
    nav2-msgs \
    nav-msgs \
    rclcpp \
    std-msgs \
    tf2 \
    tf2-geometry-msgs \
    tf2-ros \
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
