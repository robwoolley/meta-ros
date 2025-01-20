# Generated by superflore -- DO NOT EDIT
#
# Copyright Open Source Robotics Foundation

inherit ros_distro_humble
inherit ros_superflore_generated

DESCRIPTION = "ROS2 controllers for KUKA robots"
AUTHOR = "Aron Svastits <svastits1@gmail.com>"
HOMEPAGE = "https://wiki.ros.org"
SECTION = "devel"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://package.xml;beginline=7;endline=7;md5=82f0323c08605e5b6f343b05213cf7cc"

ROS_CN = "kuka_drivers"
ROS_BPN = "kuka_controllers"

ROS_BUILD_DEPENDS = ""

ROS_BUILDTOOL_DEPENDS = " \
    ament-cmake-native \
"

ROS_EXPORT_DEPENDS = ""

ROS_BUILDTOOL_EXPORT_DEPENDS = ""

ROS_EXEC_DEPENDS = " \
    fri-configuration-controller \
    fri-state-broadcaster \
    joint-group-impedance-controller \
    kuka-control-mode-handler \
    kuka-event-broadcaster \
"

# Currently informational only -- see http://www.ros.org/reps/rep-0149.html#dependency-tags.
ROS_TEST_DEPENDS = ""

DEPENDS = "${ROS_BUILD_DEPENDS} ${ROS_BUILDTOOL_DEPENDS}"
# Bitbake doesn't support the "export" concept, so build them as if we needed them to build this package (even though we actually
# don't) so that they're guaranteed to have been staged should this package appear in another's DEPENDS.
DEPENDS += "${ROS_EXPORT_DEPENDS} ${ROS_BUILDTOOL_EXPORT_DEPENDS}"

RDEPENDS:${PN} += "${ROS_EXEC_DEPENDS}"

# matches with: https://github.com/ros2-gbp/kuka_drivers-release/archive/release/humble/kuka_controllers/0.9.2-1.tar.gz
ROS_BRANCH ?= "branch=release/humble/kuka_controllers"
SRC_URI = "git://github.com/ros2-gbp/kuka_drivers-release;${ROS_BRANCH};protocol=https"
SRCREV = "21479b92744c277dc37e2c7589937a6f0f289525"
S = "${WORKDIR}/git"

ROS_BUILD_TYPE = "ament_cmake"

inherit ros_${ROS_BUILD_TYPE}
