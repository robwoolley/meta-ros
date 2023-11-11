# Generated by superflore -- DO NOT EDIT
#
# Copyright Open Source Robotics Foundation

inherit ros_distro_iron
inherit ros_superflore_generated

DESCRIPTION = "C++ Interface for defining setup steps for MoveIt Setup Assistant"
AUTHOR = "David V. Lu!! <davidvlu@gmail.com>"
ROS_AUTHOR = "David V. Lu!! <davidvlu@gmail.com>"
HOMEPAGE = "https://wiki.ros.org"
SECTION = "devel"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://package.xml;beginline=8;endline=8;md5=4633480cdd27d7906aaf3ef4b72014b2"

ROS_CN = "moveit"
ROS_BPN = "moveit_setup_framework"

ROS_BUILD_DEPENDS = " \
    ament-index-cpp \
    fmt \
    moveit-common \
    moveit-core \
    moveit-ros-planning \
    moveit-ros-visualization \
    pluginlib \
    rclcpp \
    rviz-common \
    rviz-rendering \
    srdfdom \
    urdf \
"

ROS_BUILDTOOL_DEPENDS = " \
    ament-cmake-native \
"

ROS_EXPORT_DEPENDS = " \
    ament-index-cpp \
    fmt \
    moveit-common \
    moveit-core \
    moveit-ros-planning \
    moveit-ros-visualization \
    pluginlib \
    rclcpp \
    rviz-common \
    rviz-rendering \
    srdfdom \
    urdf \
"

ROS_BUILDTOOL_EXPORT_DEPENDS = ""

ROS_EXEC_DEPENDS = " \
    ament-index-cpp \
    fmt \
    moveit-common \
    moveit-core \
    moveit-ros-planning \
    moveit-ros-visualization \
    pluginlib \
    rclcpp \
    rviz-common \
    rviz-rendering \
    srdfdom \
    urdf \
"

# Currently informational only -- see http://www.ros.org/reps/rep-0149.html#dependency-tags.
ROS_TEST_DEPENDS = " \
    ament-clang-format \
    ament-cmake-lint-cmake \
    ament-cmake-xmllint \
    ament-lint-auto \
"

DEPENDS = "${ROS_BUILD_DEPENDS} ${ROS_BUILDTOOL_DEPENDS}"
# Bitbake doesn't support the "export" concept, so build them as if we needed them to build this package (even though we actually
# don't) so that they're guaranteed to have been staged should this package appear in another's DEPENDS.
DEPENDS += "${ROS_EXPORT_DEPENDS} ${ROS_BUILDTOOL_EXPORT_DEPENDS}"

RDEPENDS:${PN} += "${ROS_EXEC_DEPENDS}"

# matches with: https://github.com/ros2-gbp/moveit2-release/archive/release/iron/moveit_setup_framework/2.8.0-1.tar.gz
ROS_BRANCH ?= "branch=release/iron/moveit_setup_framework"
SRC_URI = "git://github.com/ros2-gbp/moveit2-release;${ROS_BRANCH};protocol=https"
SRCREV = "8b1aa7c31d32bed8cd2de6b5231f1abcb5ad1351"
S = "${WORKDIR}/git"

ROS_BUILD_TYPE = "ament_cmake"

inherit ros_${ROS_BUILD_TYPE}