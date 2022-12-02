# Generated by superflore -- DO NOT EDIT
#
# Copyright Open Source Robotics Foundation

inherit ros_distro_humble
inherit ros_superflore_generated

DESCRIPTION = "Provides service calls for getting ros meta-information, like list of     topics, services, params, etc."
AUTHOR = "Jihoon Lee <jihoonlee.in@gmail.com>"
ROS_AUTHOR = "Jonathan Mace <jonathan.c.mace@gmail.com>"
HOMEPAGE = "http://ros.org/wiki/rosapi"
SECTION = "devel"
LICENSE = "BSD"
LIC_FILES_CHKSUM = "file://package.xml;beginline=10;endline=10;md5=d566ef916e9dedc494f5f793a6690ba5"

ROS_CN = "rosbridge_suite"
ROS_BPN = "rosapi_msgs"

ROS_BUILD_DEPENDS = " \
    builtin-interfaces \
"

ROS_BUILDTOOL_DEPENDS = " \
    ament-cmake-ros-native \
    rosidl-default-generators-native \
"

ROS_EXPORT_DEPENDS = ""

ROS_BUILDTOOL_EXPORT_DEPENDS = ""

ROS_EXEC_DEPENDS = " \
    builtin-interfaces \
    rcl-interfaces \
    rosidl-default-runtime \
"

# Currently informational only -- see http://www.ros.org/reps/rep-0149.html#dependency-tags.
ROS_TEST_DEPENDS = ""

DEPENDS = "${ROS_BUILD_DEPENDS} ${ROS_BUILDTOOL_DEPENDS}"
# Bitbake doesn't support the "export" concept, so build them as if we needed them to build this package (even though we actually
# don't) so that they're guaranteed to have been staged should this package appear in another's DEPENDS.
DEPENDS += "${ROS_EXPORT_DEPENDS} ${ROS_BUILDTOOL_EXPORT_DEPENDS}"

RDEPENDS:${PN} += "${ROS_EXEC_DEPENDS}"

# matches with: https://github.com/ros2-gbp/rosbridge_suite-release/archive/release/humble/rosapi_msgs/1.3.1-1.tar.gz
ROS_BRANCH ?= "branch=release/humble/rosapi_msgs"
SRC_URI = "git://github.com/ros2-gbp/rosbridge_suite-release;${ROS_BRANCH};protocol=https"
SRCREV = "9126b2288cad31d5157341361c5035c6b328d973"
S = "${WORKDIR}/git"

ROS_BUILD_TYPE = "ament_cmake"

inherit ros_${ROS_BUILD_TYPE}
