# Generated by superflore -- DO NOT EDIT
#
# Copyright Open Source Robotics Foundation

inherit ros_distro_humble
inherit ros_superflore_generated

DESCRIPTION = "Rotation Shim Controller"
AUTHOR = "Steve Macenski <stevenmacenski@gmail.com>"
HOMEPAGE = "https://wiki.ros.org"
SECTION = "devel"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://package.xml;beginline=8;endline=8;md5=82f0323c08605e5b6f343b05213cf7cc"

ROS_CN = "navigation2"
ROS_BPN = "nav2_rotation_shim_controller"

ROS_BUILD_DEPENDS = " \
    angles \
    geometry-msgs \
    nav2-common \
    nav2-core \
    nav2-costmap-2d \
    nav2-msgs \
    nav2-util \
    pluginlib \
    rclcpp \
    tf2 \
"

ROS_BUILDTOOL_DEPENDS = " \
    ament-cmake-native \
"

ROS_EXPORT_DEPENDS = " \
    angles \
    geometry-msgs \
    nav2-common \
    nav2-core \
    nav2-costmap-2d \
    nav2-msgs \
    nav2-util \
    pluginlib \
    rclcpp \
    tf2 \
"

ROS_BUILDTOOL_EXPORT_DEPENDS = ""

ROS_EXEC_DEPENDS = " \
    angles \
    geometry-msgs \
    nav2-common \
    nav2-core \
    nav2-costmap-2d \
    nav2-msgs \
    nav2-util \
    pluginlib \
    rclcpp \
    tf2 \
"

# Currently informational only -- see http://www.ros.org/reps/rep-0149.html#dependency-tags.
ROS_TEST_DEPENDS = " \
    ament-cmake-gtest \
    ament-lint-auto \
    ament-lint-common \
    nav2-controller \
    nav2-regulated-pure-pursuit-controller \
"

DEPENDS = "${ROS_BUILD_DEPENDS} ${ROS_BUILDTOOL_DEPENDS}"
# Bitbake doesn't support the "export" concept, so build them as if we needed them to build this package (even though we actually
# don't) so that they're guaranteed to have been staged should this package appear in another's DEPENDS.
DEPENDS += "${ROS_EXPORT_DEPENDS} ${ROS_BUILDTOOL_EXPORT_DEPENDS}"

RDEPENDS:${PN} += "${ROS_EXEC_DEPENDS}"

# matches with: https://github.com/SteveMacenski/navigation2-release/archive/release/humble/nav2_rotation_shim_controller/1.1.3-1.tar.gz
ROS_BRANCH ?= "branch=release/humble/nav2_rotation_shim_controller"
SRC_URI = "git://github.com/SteveMacenski/navigation2-release;${ROS_BRANCH};protocol=https"
SRCREV = "2320784f3b664caa76910bb5dd2c9a14bad651a3"
S = "${WORKDIR}/git"

ROS_BUILD_TYPE = "ament_cmake"

inherit ros_${ROS_BUILD_TYPE}
