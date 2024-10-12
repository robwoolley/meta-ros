# Generated by superflore -- DO NOT EDIT
#
# Copyright Open Source Robotics Foundation

inherit ros_distro_iron
inherit ros_superflore_generated

DESCRIPTION = "ROS node for GNSS/IMU Novatel receivers with RTK precision using an NTRIP HTTP source (based on mrpt-hwdrivers)"
AUTHOR = "José Luis Blanco-Claraco <joseluisblancoc@gmail.com>"
ROS_AUTHOR = "José Luis Blanco-Claraco"
HOMEPAGE = "https://github.com/mrpt-ros-pkg/mrpt_sensors"
SECTION = "devel"
LICENSE = "BSD"
LIC_FILES_CHKSUM = "file://package.xml;beginline=12;endline=12;md5=d566ef916e9dedc494f5f793a6690ba5"

ROS_CN = "mrpt_sensors"
ROS_BPN = "mrpt_sensor_gnss_novatel"

ROS_BUILD_DEPENDS = " \
    ament-lint-auto \
    ament-lint-common \
    cv-bridge \
    geometry-msgs \
    mrpt-libhwdrivers \
    mrpt-libros-bridge \
    mrpt-msgs \
    mrpt-sensorlib \
    nav-msgs \
    rclcpp \
    rclcpp-components \
    ros-environment \
    sensor-msgs \
    std-msgs \
    stereo-msgs \
    tf2 \
    tf2-ros \
"

ROS_BUILDTOOL_DEPENDS = " \
    ament-cmake-native \
"

ROS_EXPORT_DEPENDS = " \
    ament-lint-auto \
    ament-lint-common \
    cv-bridge \
    geometry-msgs \
    mrpt-libhwdrivers \
    mrpt-libros-bridge \
    mrpt-msgs \
    mrpt-sensorlib \
    nav-msgs \
    rclcpp \
    rclcpp-components \
    sensor-msgs \
    std-msgs \
    stereo-msgs \
    tf2 \
    tf2-ros \
"

ROS_BUILDTOOL_EXPORT_DEPENDS = ""

ROS_EXEC_DEPENDS = " \
    ament-lint-auto \
    ament-lint-common \
    cv-bridge \
    geometry-msgs \
    mrpt-libhwdrivers \
    mrpt-libros-bridge \
    mrpt-msgs \
    mrpt-sensorlib \
    nav-msgs \
    rclcpp \
    rclcpp-components \
    sensor-msgs \
    std-msgs \
    stereo-msgs \
    tf2 \
    tf2-ros \
"

# Currently informational only -- see http://www.ros.org/reps/rep-0149.html#dependency-tags.
ROS_TEST_DEPENDS = ""

DEPENDS = "${ROS_BUILD_DEPENDS} ${ROS_BUILDTOOL_DEPENDS}"
# Bitbake doesn't support the "export" concept, so build them as if we needed them to build this package (even though we actually
# don't) so that they're guaranteed to have been staged should this package appear in another's DEPENDS.
DEPENDS += "${ROS_EXPORT_DEPENDS} ${ROS_BUILDTOOL_EXPORT_DEPENDS}"

RDEPENDS:${PN} += "${ROS_EXEC_DEPENDS}"

# matches with: https://github.com/ros2-gbp/mrpt_sensors-release/archive/release/iron/mrpt_sensor_gnss_novatel/0.2.3-1.tar.gz
ROS_BRANCH ?= "branch=release/iron/mrpt_sensor_gnss_novatel"
SRC_URI = "git://github.com/ros2-gbp/mrpt_sensors-release;${ROS_BRANCH};protocol=https"
SRCREV = "ccd987ee41934d557b4b56384db0bdba236fc3ee"
S = "${WORKDIR}/git"

ROS_BUILD_TYPE = "ament_cmake"

inherit ros_${ROS_BUILD_TYPE}
