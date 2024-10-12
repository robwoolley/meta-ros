# Generated by superflore -- DO NOT EDIT
#
# Copyright Open Source Robotics Foundation

inherit ros_distro_rolling
inherit ros_superflore_generated

DESCRIPTION = "Successor of ros_type_introspection"
AUTHOR = "Davide Faconti <davide.faconti@gmail.com>"
ROS_AUTHOR = "Davide Faconti <davide.faconti@gmail.com>"
HOMEPAGE = "https://wiki.ros.org"
SECTION = "devel"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://package.xml;beginline=8;endline=8;md5=58e54c03ca7f821dd3967e2a2cd1596e"

ROS_CN = "rosx_introspection"
ROS_BPN = "rosx_introspection"

ROS_BUILD_DEPENDS = " \
    ${ROS_UNRESOLVED_DEP-rapidjson-dev} \
    ament-index-cpp \
    fastcdr \
    rclcpp \
    rosbag2-cpp \
"

ROS_BUILDTOOL_DEPENDS = " \
    ament-cmake-native \
"

ROS_EXPORT_DEPENDS = " \
    ${ROS_UNRESOLVED_DEP-rapidjson-dev} \
    ament-index-cpp \
    fastcdr \
    rclcpp \
    rosbag2-cpp \
"

ROS_BUILDTOOL_EXPORT_DEPENDS = ""

ROS_EXEC_DEPENDS = " \
    ${ROS_UNRESOLVED_DEP-rapidjson-dev} \
    ament-index-cpp \
    fastcdr \
    rclcpp \
    rosbag2-cpp \
"

# Currently informational only -- see http://www.ros.org/reps/rep-0149.html#dependency-tags.
ROS_TEST_DEPENDS = " \
    geometry-msgs \
    sensor-msgs \
"

DEPENDS = "${ROS_BUILD_DEPENDS} ${ROS_BUILDTOOL_DEPENDS}"
# Bitbake doesn't support the "export" concept, so build them as if we needed them to build this package (even though we actually
# don't) so that they're guaranteed to have been staged should this package appear in another's DEPENDS.
DEPENDS += "${ROS_EXPORT_DEPENDS} ${ROS_BUILDTOOL_EXPORT_DEPENDS}"

RDEPENDS:${PN} += "${ROS_EXEC_DEPENDS}"

# matches with: https://github.com/ros2-gbp/rosx_introspection-release/archive/release/rolling/rosx_introspection/1.0.2-1.tar.gz
ROS_BRANCH ?= "branch=release/rolling/rosx_introspection"
SRC_URI = "git://github.com/ros2-gbp/rosx_introspection-release;${ROS_BRANCH};protocol=https"
SRCREV = "fff7bdfa85870357de451d78417f9502ac96f413"
S = "${WORKDIR}/git"

ROS_BUILD_TYPE = "ament_cmake"

inherit ros_${ROS_BUILD_TYPE}
