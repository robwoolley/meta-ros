# Generated by superflore -- DO NOT EDIT
#
# Copyright Open Source Robotics Foundation

inherit ros_distro_jazzy
inherit ros_superflore_generated

DESCRIPTION = "SLAM and navigation packages for Raspberry Pi Mouse V3"
AUTHOR = "RT Corporation <shop@rt-net.jp>"
ROS_AUTHOR = "Shuhei Kozasa <kozasa@rt-net.jp>"
HOMEPAGE = "https://github.com/rt-net/raspimouse_slam_navigation_ros2"
SECTION = "devel"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://package.xml;beginline=9;endline=9;md5=5bb539404d0b40bbfb80f7ad10fa0c2b"

ROS_CN = "raspimouse_slam_navigation_ros2"
ROS_BPN = "raspimouse_slam_navigation"

ROS_BUILD_DEPENDS = ""

ROS_BUILDTOOL_DEPENDS = " \
    ament-cmake-native \
"

ROS_EXPORT_DEPENDS = ""

ROS_BUILDTOOL_EXPORT_DEPENDS = ""

ROS_EXEC_DEPENDS = " \
    raspimouse-navigation \
    raspimouse-slam \
"

# Currently informational only -- see http://www.ros.org/reps/rep-0149.html#dependency-tags.
ROS_TEST_DEPENDS = ""

DEPENDS = "${ROS_BUILD_DEPENDS} ${ROS_BUILDTOOL_DEPENDS}"
# Bitbake doesn't support the "export" concept, so build them as if we needed them to build this package (even though we actually
# don't) so that they're guaranteed to have been staged should this package appear in another's DEPENDS.
DEPENDS += "${ROS_EXPORT_DEPENDS} ${ROS_BUILDTOOL_EXPORT_DEPENDS}"

RDEPENDS:${PN} += "${ROS_EXEC_DEPENDS}"

# matches with: https://github.com/ros2-gbp/raspimouse_slam_navigation_ros2-release/archive/release/jazzy/raspimouse_slam_navigation/3.0.0-1.tar.gz
ROS_BRANCH ?= "branch=release/jazzy/raspimouse_slam_navigation"
SRC_URI = "git://github.com/ros2-gbp/raspimouse_slam_navigation_ros2-release;${ROS_BRANCH};protocol=https"
SRCREV = "acc1c15fa47b95abd9f0a987650fd6825065eea2"
S = "${WORKDIR}/git"

ROS_BUILD_TYPE = "ament_cmake"

inherit ros_${ROS_BUILD_TYPE}
