# Generated by superflore -- DO NOT EDIT
#
# Copyright Open Source Robotics Foundation

inherit ros_distro_humble
inherit ros_superflore_generated

DESCRIPTION = "Scripts and launch files for starting basic Leo Rover functionalities."
AUTHOR = "Fictionlab <support@fictionlab.pl>"
ROS_AUTHOR = "Błażej Sowa <blazej@fictionlab.pl>"
HOMEPAGE = "http://wiki.ros.org/leo_bringup"
SECTION = "devel"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://package.xml;beginline=12;endline=12;md5=58e54c03ca7f821dd3967e2a2cd1596e"

ROS_CN = "leo_robot"
ROS_BPN = "leo_bringup"

ROS_BUILD_DEPENDS = ""

ROS_BUILDTOOL_DEPENDS = " \
    ament-cmake-native \
"

ROS_EXPORT_DEPENDS = ""

ROS_BUILDTOOL_EXPORT_DEPENDS = ""

ROS_EXEC_DEPENDS = " \
    geometry-msgs \
    image-proc \
    leo-description \
    leo-fw \
    robot-state-publisher \
    rosapi \
    rosbridge-server \
    sensor-msgs \
    v4l2-camera \
    xacro \
"

# Currently informational only -- see http://www.ros.org/reps/rep-0149.html#dependency-tags.
ROS_TEST_DEPENDS = ""

DEPENDS = "${ROS_BUILD_DEPENDS} ${ROS_BUILDTOOL_DEPENDS}"
# Bitbake doesn't support the "export" concept, so build them as if we needed them to build this package (even though we actually
# don't) so that they're guaranteed to have been staged should this package appear in another's DEPENDS.
DEPENDS += "${ROS_EXPORT_DEPENDS} ${ROS_BUILDTOOL_EXPORT_DEPENDS}"

RDEPENDS:${PN} += "${ROS_EXEC_DEPENDS}"

# matches with: https://github.com/ros2-gbp/leo_robot-release/archive/release/humble/leo_bringup/1.2.0-1.tar.gz
ROS_BRANCH ?= "branch=release/humble/leo_bringup"
SRC_URI = "git://github.com/ros2-gbp/leo_robot-release;${ROS_BRANCH};protocol=https"
SRCREV = "c12dee3bcd0d99ce4a29c29ca39c50177af6141d"
S = "${WORKDIR}/git"

ROS_BUILD_TYPE = "ament_cmake"

inherit ros_${ROS_BUILD_TYPE}
