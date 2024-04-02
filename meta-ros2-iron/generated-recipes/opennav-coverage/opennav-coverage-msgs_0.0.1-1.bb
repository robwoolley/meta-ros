# Generated by superflore -- DO NOT EDIT
#
# Copyright Open Source Robotics Foundation

inherit ros_distro_iron
inherit ros_superflore_generated

DESCRIPTION = "A set of ROS interfaces for complete coverage planning"
AUTHOR = "Steve Macenski <steve@opennav.org>"
HOMEPAGE = "https://wiki.ros.org"
SECTION = "devel"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://package.xml;beginline=8;endline=8;md5=82f0323c08605e5b6f343b05213cf7cc"

ROS_CN = "opennav_coverage"
ROS_BPN = "opennav_coverage_msgs"

ROS_BUILD_DEPENDS = " \
    builtin-interfaces \
    geometry-msgs \
    nav-msgs \
    nav2-msgs \
    nav2-util \
    rclcpp \
    rclcpp-action \
    rclcpp-lifecycle \
    rosidl-default-generators \
    tf2-ros \
"

ROS_BUILDTOOL_DEPENDS = " \
    ament-cmake-native \
"

ROS_EXPORT_DEPENDS = " \
    builtin-interfaces \
    geometry-msgs \
    nav-msgs \
    nav2-msgs \
    nav2-util \
    rclcpp \
    rclcpp-action \
    rclcpp-lifecycle \
    rosidl-default-generators \
    tf2-ros \
"

ROS_BUILDTOOL_EXPORT_DEPENDS = ""

ROS_EXEC_DEPENDS = " \
    builtin-interfaces \
    geometry-msgs \
    nav-msgs \
    nav2-msgs \
    nav2-util \
    rclcpp \
    rclcpp-action \
    rclcpp-lifecycle \
    rosidl-default-generators \
    tf2-ros \
"

# Currently informational only -- see http://www.ros.org/reps/rep-0149.html#dependency-tags.
ROS_TEST_DEPENDS = " \
    ament-lint-auto \
    ament-lint-common \
"

DEPENDS = "${ROS_BUILD_DEPENDS} ${ROS_BUILDTOOL_DEPENDS}"
# Bitbake doesn't support the "export" concept, so build them as if we needed them to build this package (even though we actually
# don't) so that they're guaranteed to have been staged should this package appear in another's DEPENDS.
DEPENDS += "${ROS_EXPORT_DEPENDS} ${ROS_BUILDTOOL_EXPORT_DEPENDS}"

RDEPENDS:${PN} += "${ROS_EXEC_DEPENDS}"

# matches with: https://github.com/open-navigation/opennav_coverage-release/archive/release/iron/opennav_coverage_msgs/0.0.1-1.tar.gz
ROS_BRANCH ?= "branch=release/iron/opennav_coverage_msgs"
SRC_URI = "git://github.com/open-navigation/opennav_coverage-release;${ROS_BRANCH};protocol=https"
SRCREV = "c39e00657fa00a0f0e05437121d1c2ccf56a8e10"
S = "${WORKDIR}/git"

ROS_BUILD_TYPE = "ament_cmake"

inherit ros_${ROS_BUILD_TYPE}
