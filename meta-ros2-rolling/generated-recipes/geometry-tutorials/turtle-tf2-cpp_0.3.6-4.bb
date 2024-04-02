# Generated by superflore -- DO NOT EDIT
#
# Copyright Open Source Robotics Foundation

inherit ros_distro_rolling
inherit ros_superflore_generated

DESCRIPTION = "turtle_tf2_cpp demonstrates how to write a ROS2 C++ tf2 broadcaster and listener with the turtlesim. The turtle_tf2_listener commands turtle2 to follow turtle1 around as you drive turtle1 using the keyboard."
AUTHOR = "Alejandro Hernández Cordero <alejandro@openrobotics.org>"
ROS_AUTHOR = "Shyngyskhan Abilkassov <abilkasov@gmail.com>"
HOMEPAGE = "https://wiki.ros.org"
SECTION = "devel"
# Original license in package.xml, joined with "&" when multiple license tags were used:
#         "Apache License, Version 2.0"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://package.xml;beginline=11;endline=11;md5=e8978a5103d23266fc6f8ec03dc9eb16"

ROS_CN = "geometry_tutorials"
ROS_BPN = "turtle_tf2_cpp"

ROS_BUILD_DEPENDS = " \
    geometry-msgs \
    launch \
    launch-ros \
    message-filters \
    rclcpp \
    tf2 \
    tf2-geometry-msgs \
    tf2-ros \
    turtlesim \
"

ROS_BUILDTOOL_DEPENDS = " \
    ament-cmake-native \
"

ROS_EXPORT_DEPENDS = " \
    geometry-msgs \
    launch \
    launch-ros \
    message-filters \
    rclcpp \
    tf2 \
    tf2-geometry-msgs \
    tf2-ros \
    turtlesim \
"

ROS_BUILDTOOL_EXPORT_DEPENDS = ""

ROS_EXEC_DEPENDS = " \
    geometry-msgs \
    launch \
    launch-ros \
    message-filters \
    rclcpp \
    tf2 \
    tf2-geometry-msgs \
    tf2-ros \
    turtlesim \
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

# matches with: https://github.com/ros2-gbp/geometry_tutorials-release/archive/release/rolling/turtle_tf2_cpp/0.3.6-4.tar.gz
ROS_BRANCH ?= "branch=release/rolling/turtle_tf2_cpp"
SRC_URI = "git://github.com/ros2-gbp/geometry_tutorials-release;${ROS_BRANCH};protocol=https"
SRCREV = "c18ff88b9b385ee005312a4ce362f077c8c2b234"
S = "${WORKDIR}/git"

ROS_BUILD_TYPE = "ament_cmake"

inherit ros_${ROS_BUILD_TYPE}
