# Generated by superflore -- DO NOT EDIT
#
# Copyright Open Source Robotics Foundation

inherit ros_distro_rolling
inherit ros_superflore_generated

DESCRIPTION = "MoveIt plugin to generate industrial trajectories PTP, LIN, CIRC and sequences thereof."
AUTHOR = "Christian Henkel <c.henkel@pilz.de>"
HOMEPAGE = "http://moveit.ros.org"
SECTION = "devel"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://package.xml;beginline=11;endline=11;md5=4633480cdd27d7906aaf3ef4b72014b2"

ROS_CN = "moveit"
ROS_BPN = "pilz_industrial_motion_planner"

ROS_BUILD_DEPENDS = " \
    generate-parameter-library \
    geometry-msgs \
    moveit-common \
    moveit-core \
    moveit-msgs \
    moveit-ros-move-group \
    moveit-ros-planning \
    moveit-ros-planning-interface \
    orocos-kdl-vendor \
    pluginlib \
    rclcpp \
    tf2 \
    tf2-eigen \
    tf2-eigen-kdl \
    tf2-geometry-msgs \
    tf2-kdl \
    tf2-ros \
"

ROS_BUILDTOOL_DEPENDS = " \
    ament-cmake-native \
    eigen3-cmake-module-native \
"

ROS_EXPORT_DEPENDS = " \
    generate-parameter-library \
    geometry-msgs \
    moveit-common \
    moveit-core \
    moveit-msgs \
    moveit-ros-move-group \
    moveit-ros-planning \
    moveit-ros-planning-interface \
    orocos-kdl-vendor \
    pluginlib \
    rclcpp \
    tf2 \
    tf2-eigen \
    tf2-eigen-kdl \
    tf2-geometry-msgs \
    tf2-kdl \
    tf2-ros \
"

ROS_BUILDTOOL_EXPORT_DEPENDS = " \
    eigen3-cmake-module-native \
"

ROS_EXEC_DEPENDS = " \
    generate-parameter-library \
    geometry-msgs \
    moveit-common \
    moveit-core \
    moveit-msgs \
    moveit-ros-move-group \
    moveit-ros-planning \
    moveit-ros-planning-interface \
    orocos-kdl-vendor \
    pluginlib \
    rclcpp \
    tf2 \
    tf2-eigen \
    tf2-eigen-kdl \
    tf2-geometry-msgs \
    tf2-kdl \
    tf2-ros \
"

# Currently informational only -- see http://www.ros.org/reps/rep-0149.html#dependency-tags.
ROS_TEST_DEPENDS = " \
    ament-cmake-gmock \
    ament-cmake-gtest \
    ament-lint-auto \
    ament-lint-common \
    boost \
    launch-param-builder \
    moveit-configs-utils \
    moveit-resources-panda-moveit-config \
    moveit-resources-prbt-moveit-config \
    moveit-resources-prbt-pg70-support \
    moveit-resources-prbt-support \
    pilz-industrial-motion-planner-testutils \
    ros-testing \
"

DEPENDS = "${ROS_BUILD_DEPENDS} ${ROS_BUILDTOOL_DEPENDS}"
# Bitbake doesn't support the "export" concept, so build them as if we needed them to build this package (even though we actually
# don't) so that they're guaranteed to have been staged should this package appear in another's DEPENDS.
DEPENDS += "${ROS_EXPORT_DEPENDS} ${ROS_BUILDTOOL_EXPORT_DEPENDS}"

RDEPENDS:${PN} += "${ROS_EXEC_DEPENDS}"

# matches with: https://github.com/ros2-gbp/moveit2-release/archive/release/rolling/pilz_industrial_motion_planner/2.9.0-2.tar.gz
ROS_BRANCH ?= "branch=release/rolling/pilz_industrial_motion_planner"
SRC_URI = "git://github.com/ros2-gbp/moveit2-release;${ROS_BRANCH};protocol=https"
SRCREV = "5147b17f3b9c34d086fa444b144d5df4078dbf3d"
S = "${WORKDIR}/git"

ROS_BUILD_TYPE = "ament_cmake"

inherit ros_${ROS_BUILD_TYPE}
