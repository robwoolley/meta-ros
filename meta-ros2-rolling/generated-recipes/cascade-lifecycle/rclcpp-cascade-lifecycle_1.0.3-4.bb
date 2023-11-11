# Generated by superflore -- DO NOT EDIT
#
# Copyright Open Source Robotics Foundation

inherit ros_distro_rolling
inherit ros_superflore_generated

DESCRIPTION = "Provides a mechanism to make trees of lifecycle nodes to propagate state changes"
AUTHOR = "fmrico <fmrico@gmail.com>"
HOMEPAGE = "https://wiki.ros.org"
SECTION = "devel"
# Original license in package.xml, joined with "&" when multiple license tags were used:
#         "Apache License, Version 2.0"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://package.xml;beginline=8;endline=8;md5=e8978a5103d23266fc6f8ec03dc9eb16"

ROS_CN = "cascade_lifecycle"
ROS_BPN = "rclcpp_cascade_lifecycle"

ROS_BUILD_DEPENDS = " \
    cascade-lifecycle-msgs \
    lifecycle-msgs \
    rclcpp \
    rclcpp-lifecycle \
"

ROS_BUILDTOOL_DEPENDS = " \
    ament-cmake-native \
"

ROS_EXPORT_DEPENDS = " \
    cascade-lifecycle-msgs \
    lifecycle-msgs \
    rclcpp \
    rclcpp-lifecycle \
"

ROS_BUILDTOOL_EXPORT_DEPENDS = ""

ROS_EXEC_DEPENDS = " \
    cascade-lifecycle-msgs \
    lifecycle-msgs \
    rclcpp \
    rclcpp-lifecycle \
"

# Currently informational only -- see http://www.ros.org/reps/rep-0149.html#dependency-tags.
ROS_TEST_DEPENDS = " \
    ament-cmake-gtest \
    ament-lint-auto \
    ament-lint-common \
"

DEPENDS = "${ROS_BUILD_DEPENDS} ${ROS_BUILDTOOL_DEPENDS}"
# Bitbake doesn't support the "export" concept, so build them as if we needed them to build this package (even though we actually
# don't) so that they're guaranteed to have been staged should this package appear in another's DEPENDS.
DEPENDS += "${ROS_EXPORT_DEPENDS} ${ROS_BUILDTOOL_EXPORT_DEPENDS}"

RDEPENDS:${PN} += "${ROS_EXEC_DEPENDS}"

# matches with: https://github.com/ros2-gbp/cascade_lifecycle-release/archive/release/rolling/rclcpp_cascade_lifecycle/1.0.3-4.tar.gz
ROS_BRANCH ?= "branch=release/rolling/rclcpp_cascade_lifecycle"
SRC_URI = "git://github.com/ros2-gbp/cascade_lifecycle-release;${ROS_BRANCH};protocol=https"
SRCREV = "01c3ef66e88b6799ebbd0982c0a83a3a9e4e446f"
S = "${WORKDIR}/git"

ROS_BUILD_TYPE = "ament_cmake"

inherit ros_${ROS_BUILD_TYPE}
