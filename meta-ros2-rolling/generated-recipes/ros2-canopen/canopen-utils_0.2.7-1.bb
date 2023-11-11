# Generated by superflore -- DO NOT EDIT
#
# Copyright Open Source Robotics Foundation

inherit ros_distro_rolling
inherit ros_superflore_generated

DESCRIPTION = "Utils for working with ros2_canopen."
AUTHOR = "Christoph Hellmann Santos <christoph.hellmann.santos@ipa.fraunhofer.de>"
HOMEPAGE = "https://wiki.ros.org"
SECTION = "devel"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://package.xml;beginline=8;endline=8;md5=82f0323c08605e5b6f343b05213cf7cc"

ROS_CN = "ros2_canopen"
ROS_BPN = "canopen_utils"

ROS_BUILD_DEPENDS = " \
    canopen-interfaces \
    lifecycle-msgs \
    rclpy \
    std-msgs \
"

ROS_BUILDTOOL_DEPENDS = ""

ROS_EXPORT_DEPENDS = " \
    canopen-interfaces \
    lifecycle-msgs \
    rclpy \
    std-msgs \
"

ROS_BUILDTOOL_EXPORT_DEPENDS = ""

ROS_EXEC_DEPENDS = " \
    canopen-interfaces \
    lifecycle-msgs \
    rclpy \
    std-msgs \
"

# Currently informational only -- see http://www.ros.org/reps/rep-0149.html#dependency-tags.
ROS_TEST_DEPENDS = " \
    ament-lint-auto \
"

DEPENDS = "${ROS_BUILD_DEPENDS} ${ROS_BUILDTOOL_DEPENDS}"
# Bitbake doesn't support the "export" concept, so build them as if we needed them to build this package (even though we actually
# don't) so that they're guaranteed to have been staged should this package appear in another's DEPENDS.
DEPENDS += "${ROS_EXPORT_DEPENDS} ${ROS_BUILDTOOL_EXPORT_DEPENDS}"

RDEPENDS:${PN} += "${ROS_EXEC_DEPENDS}"

# matches with: https://github.com/ros2-gbp/ros2_canopen-release/archive/release/rolling/canopen_utils/0.2.7-1.tar.gz
ROS_BRANCH ?= "branch=release/rolling/canopen_utils"
SRC_URI = "git://github.com/ros2-gbp/ros2_canopen-release;${ROS_BRANCH};protocol=https"
SRCREV = "e9d888e5d09486859e2f4c44bffc476913211288"
S = "${WORKDIR}/git"

ROS_BUILD_TYPE = "ament_python"

inherit ros_${ROS_BUILD_TYPE}
