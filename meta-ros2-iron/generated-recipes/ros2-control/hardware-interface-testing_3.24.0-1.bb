# Generated by superflore -- DO NOT EDIT
#
# Copyright Open Source Robotics Foundation

inherit ros_distro_iron
inherit ros_superflore_generated

DESCRIPTION = "ros2_control hardware interface testing"
AUTHOR = "Bence Magyar <bence.magyar.robotics@gmail.com>"
HOMEPAGE = "https://wiki.ros.org"
SECTION = "devel"
# Original license in package.xml, joined with "&" when multiple license tags were used:
#         "Apache License 2.0"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://package.xml;beginline=9;endline=9;md5=12c26a18c7f493fdc7e8a93b16b7c04f"

ROS_CN = "ros2_control"
ROS_BPN = "hardware_interface_testing"

ROS_BUILD_DEPENDS = " \
    control-msgs \
    hardware-interface \
    lifecycle-msgs \
    pluginlib \
    rclcpp-lifecycle \
    ros2-control-test-assets \
"

ROS_BUILDTOOL_DEPENDS = " \
    ament-cmake-native \
"

ROS_EXPORT_DEPENDS = " \
    control-msgs \
    hardware-interface \
    lifecycle-msgs \
    pluginlib \
    rclcpp-lifecycle \
    ros2-control-test-assets \
"

ROS_BUILDTOOL_EXPORT_DEPENDS = ""

ROS_EXEC_DEPENDS = " \
    control-msgs \
    hardware-interface \
    lifecycle-msgs \
    pluginlib \
    rclcpp-lifecycle \
    ros2-control-test-assets \
"

# Currently informational only -- see http://www.ros.org/reps/rep-0149.html#dependency-tags.
ROS_TEST_DEPENDS = " \
    ament-cmake-gmock \
"

DEPENDS = "${ROS_BUILD_DEPENDS} ${ROS_BUILDTOOL_DEPENDS}"
# Bitbake doesn't support the "export" concept, so build them as if we needed them to build this package (even though we actually
# don't) so that they're guaranteed to have been staged should this package appear in another's DEPENDS.
DEPENDS += "${ROS_EXPORT_DEPENDS} ${ROS_BUILDTOOL_EXPORT_DEPENDS}"

RDEPENDS:${PN} += "${ROS_EXEC_DEPENDS}"

# matches with: https://github.com/ros2-gbp/ros2_control-release/archive/release/iron/hardware_interface_testing/3.24.0-1.tar.gz
ROS_BRANCH ?= "branch=release/iron/hardware_interface_testing"
SRC_URI = "git://github.com/ros2-gbp/ros2_control-release;${ROS_BRANCH};protocol=https"
SRCREV = "576ad24a734eb8c5fb4d6114fb3a5840d2308357"
S = "${WORKDIR}/git"

ROS_BUILD_TYPE = "ament_cmake"

inherit ros_${ROS_BUILD_TYPE}
