# Generated by superflore -- DO NOT EDIT
#
# Copyright Open Source Robotics Foundation

inherit ros_distro_humble
inherit ros_superflore_generated

DESCRIPTION = "ublox_msgs contains raw messages for u-blox GNSS devices."
AUTHOR = "Veronica Lane <vmlane@alum.mit.edu>"
ROS_AUTHOR = "Johannes Meyer"
HOMEPAGE = "http://ros.org/wiki/ublox"
SECTION = "devel"
LICENSE = "BSD"
LIC_FILES_CHKSUM = "file://package.xml;beginline=13;endline=13;md5=d566ef916e9dedc494f5f793a6690ba5"

ROS_CN = "ublox"
ROS_BPN = "ublox_msgs"

ROS_BUILD_DEPENDS = " \
    rosidl-default-generators \
    sensor-msgs \
    std-msgs \
    ublox-serialization \
"

ROS_BUILDTOOL_DEPENDS = " \
    ament-cmake-ros-native \
"

ROS_EXPORT_DEPENDS = " \
    sensor-msgs \
    std-msgs \
    ublox-serialization \
"

ROS_BUILDTOOL_EXPORT_DEPENDS = ""

ROS_EXEC_DEPENDS = " \
    sensor-msgs \
    std-msgs \
    ublox-serialization \
"

# Currently informational only -- see http://www.ros.org/reps/rep-0149.html#dependency-tags.
ROS_TEST_DEPENDS = ""

DEPENDS = "${ROS_BUILD_DEPENDS} ${ROS_BUILDTOOL_DEPENDS}"
# Bitbake doesn't support the "export" concept, so build them as if we needed them to build this package (even though we actually
# don't) so that they're guaranteed to have been staged should this package appear in another's DEPENDS.
DEPENDS += "${ROS_EXPORT_DEPENDS} ${ROS_BUILDTOOL_EXPORT_DEPENDS}"

RDEPENDS:${PN} += "${ROS_EXEC_DEPENDS}"

# matches with: https://github.com/ros2-gbp/ublox-release/archive/release/humble/ublox_msgs/2.3.0-2.tar.gz
ROS_BRANCH ?= "branch=release/humble/ublox_msgs"
SRC_URI = "git://github.com/ros2-gbp/ublox-release;${ROS_BRANCH};protocol=https"
SRCREV = "5e72a5bf7f568dbc85a65328e1d7905a3f1c4553"
S = "${WORKDIR}/git"

ROS_BUILD_TYPE = "ament_cmake"

inherit ros_${ROS_BUILD_TYPE}
