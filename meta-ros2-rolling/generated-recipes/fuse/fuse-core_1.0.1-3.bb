# Generated by superflore -- DO NOT EDIT
#
# Copyright Open Source Robotics Foundation

inherit ros_distro_rolling
inherit ros_superflore_generated

DESCRIPTION = "The fuse_core package provides the base class interfaces for the various fuse components. Concrete implementations of these     interfaces are provided in other packages."
AUTHOR = "Stephen Williams <swilliams@locusrobotics.com>"
ROS_AUTHOR = "Stephen Williams <swilliams@locusrobotics.com>"
HOMEPAGE = "https://wiki.ros.org"
SECTION = "devel"
LICENSE = "BSD"
LIC_FILES_CHKSUM = "file://package.xml;beginline=12;endline=12;md5=d566ef916e9dedc494f5f793a6690ba5"

ROS_CN = "fuse"
ROS_BPN = "fuse_core"

ROS_BUILD_DEPENDS = " \
    boost \
    ceres-solver \
    fuse-msgs \
    glog \
    libeigen \
    pluginlib \
    rcl-interfaces \
    rclcpp \
    rclcpp-components \
"

ROS_BUILDTOOL_DEPENDS = " \
    ament-cmake-ros-native \
"

ROS_EXPORT_DEPENDS = " \
    boost \
    ceres-solver \
    glog \
    libeigen \
"

ROS_BUILDTOOL_EXPORT_DEPENDS = ""

ROS_EXEC_DEPENDS = " \
    boost \
    ceres-solver \
    fuse-msgs \
    glog \
    libeigen \
    pluginlib \
    rcl-interfaces \
    rclcpp \
    rclcpp-components \
"

# Currently informational only -- see http://www.ros.org/reps/rep-0149.html#dependency-tags.
ROS_TEST_DEPENDS = " \
    ament-cmake-gtest \
    ament-cmake-pytest \
    ament-lint-auto \
    ament-lint-common \
    geometry-msgs \
    launch \
    launch-pytest \
    rclcpp \
"

DEPENDS = "${ROS_BUILD_DEPENDS} ${ROS_BUILDTOOL_DEPENDS}"
# Bitbake doesn't support the "export" concept, so build them as if we needed them to build this package (even though we actually
# don't) so that they're guaranteed to have been staged should this package appear in another's DEPENDS.
DEPENDS += "${ROS_EXPORT_DEPENDS} ${ROS_BUILDTOOL_EXPORT_DEPENDS}"

RDEPENDS:${PN} += "${ROS_EXEC_DEPENDS}"

# matches with: https://github.com/ros2-gbp/fuse-release/archive/release/rolling/fuse_core/1.0.1-3.tar.gz
ROS_BRANCH ?= "branch=release/rolling/fuse_core"
SRC_URI = "git://github.com/ros2-gbp/fuse-release;${ROS_BRANCH};protocol=https"
SRCREV = "a89bf4bca85a5a966919adce8383ec8424f8b32a"
S = "${WORKDIR}/git"

ROS_BUILD_TYPE = "ament_cmake"

inherit ros_${ROS_BUILD_TYPE}
