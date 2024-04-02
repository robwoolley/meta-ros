# Generated by superflore -- DO NOT EDIT
#
# Copyright Open Source Robotics Foundation

inherit ros_distro_rolling
inherit ros_superflore_generated

DESCRIPTION = "The ROS 2 command line tools for ROS2 Control."
AUTHOR = "Bence Magyar <bence.magyar.robotics@gmail.com>"
ROS_AUTHOR = "Victor Lopez <victor.lopez@pal-robotics.com>"
HOMEPAGE = "https://wiki.ros.org"
SECTION = "devel"
# Original license in package.xml, joined with "&" when multiple license tags were used:
#         "Apache License 2.0"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://package.xml;beginline=11;endline=11;md5=12c26a18c7f493fdc7e8a93b16b7c04f"

ROS_CN = "ros2_control"
ROS_BPN = "ros2controlcli"

ROS_BUILD_DEPENDS = " \
    controller-manager \
    controller-manager-msgs \
    rcl-interfaces \
    rclpy \
    ros2cli \
    ros2node \
    ros2param \
"

ROS_BUILDTOOL_DEPENDS = ""

ROS_EXPORT_DEPENDS = " \
    controller-manager \
    controller-manager-msgs \
    rcl-interfaces \
    rclpy \
    ros2cli \
    ros2node \
    ros2param \
"

ROS_BUILDTOOL_EXPORT_DEPENDS = ""

ROS_EXEC_DEPENDS = " \
    controller-manager \
    controller-manager-msgs \
    python3-pygraphviz \
    rcl-interfaces \
    rclpy \
    ros2cli \
    ros2node \
    ros2param \
    rosidl-runtime-py \
"

# Currently informational only -- see http://www.ros.org/reps/rep-0149.html#dependency-tags.
ROS_TEST_DEPENDS = " \
    ament-copyright \
    ament-flake8 \
    ament-pep257 \
    ament-xmllint \
"

DEPENDS = "${ROS_BUILD_DEPENDS} ${ROS_BUILDTOOL_DEPENDS}"
# Bitbake doesn't support the "export" concept, so build them as if we needed them to build this package (even though we actually
# don't) so that they're guaranteed to have been staged should this package appear in another's DEPENDS.
DEPENDS += "${ROS_EXPORT_DEPENDS} ${ROS_BUILDTOOL_EXPORT_DEPENDS}"

RDEPENDS:${PN} += "${ROS_EXEC_DEPENDS}"

# matches with: https://github.com/ros2-gbp/ros2_control-release/archive/release/rolling/ros2controlcli/4.5.0-2.tar.gz
ROS_BRANCH ?= "branch=release/rolling/ros2controlcli"
SRC_URI = "git://github.com/ros2-gbp/ros2_control-release;${ROS_BRANCH};protocol=https"
SRCREV = "18456d171ce8eac201db9a06bb4002d1efe1dcf5"
S = "${WORKDIR}/git"

ROS_BUILD_TYPE = "ament_python"

inherit ros_${ROS_BUILD_TYPE}
