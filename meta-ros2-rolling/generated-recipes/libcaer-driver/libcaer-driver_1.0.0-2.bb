# Generated by superflore -- DO NOT EDIT
#
# Copyright Open Source Robotics Foundation

inherit ros_distro_rolling
inherit ros_superflore_generated

DESCRIPTION = "ROS2 driver for event base sensors using libcaer"
AUTHOR = "Bernd Pfrommer <bernd.pfrommer@gmail.com>"
HOMEPAGE = "https://wiki.ros.org"
SECTION = "devel"
LICENSE = "Apache-2"
LIC_FILES_CHKSUM = "file://package.xml;beginline=8;endline=8;md5=4083d50af96f9bddbe68e0de85b0a7db"

ROS_CN = "libcaer_driver"
ROS_BPN = "libcaer_driver"

ROS_BUILD_DEPENDS = " \
    camera-info-manager \
    event-camera-msgs \
    image-transport \
    libcaer \
    rclcpp \
    rclcpp-components \
    sensor-msgs \
    std-srvs \
"

ROS_BUILDTOOL_DEPENDS = " \
    ament-cmake-auto-native \
    ament-cmake-native \
    ament-cmake-ros-native \
    ros-environment-native \
"

ROS_EXPORT_DEPENDS = " \
    camera-info-manager \
    event-camera-msgs \
    image-transport \
    libcaer \
    rclcpp \
    rclcpp-components \
    sensor-msgs \
    std-srvs \
"

ROS_BUILDTOOL_EXPORT_DEPENDS = ""

ROS_EXEC_DEPENDS = " \
    camera-info-manager \
    event-camera-msgs \
    image-transport \
    libcaer \
    rclcpp \
    rclcpp-components \
    sensor-msgs \
    std-srvs \
"

# Currently informational only -- see http://www.ros.org/reps/rep-0149.html#dependency-tags.
ROS_TEST_DEPENDS = " \
    ament-cmake-clang-format \
    ament-cmake-copyright \
    ament-cmake-cppcheck \
    ament-cmake-cpplint \
    ament-cmake-flake8 \
    ament-cmake-lint-cmake \
    ament-cmake-xmllint \
"

DEPENDS = "${ROS_BUILD_DEPENDS} ${ROS_BUILDTOOL_DEPENDS}"
# Bitbake doesn't support the "export" concept, so build them as if we needed them to build this package (even though we actually
# don't) so that they're guaranteed to have been staged should this package appear in another's DEPENDS.
DEPENDS += "${ROS_EXPORT_DEPENDS} ${ROS_BUILDTOOL_EXPORT_DEPENDS}"

RDEPENDS:${PN} += "${ROS_EXEC_DEPENDS}"

# matches with: https://github.com/ros2-gbp/libcaer_driver-release/archive/release/rolling/libcaer_driver/1.0.0-2.tar.gz
ROS_BRANCH ?= "branch=release/rolling/libcaer_driver"
SRC_URI = "git://github.com/ros2-gbp/libcaer_driver-release;${ROS_BRANCH};protocol=https"
SRCREV = "723e3e9cc8938f7089d18a78b1410a58a47879d1"
S = "${WORKDIR}/git"

ROS_BUILD_TYPE = "ament_cmake"

inherit ros_${ROS_BUILD_TYPE}
