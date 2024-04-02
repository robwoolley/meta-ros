# Generated by superflore -- DO NOT EDIT
#
# Copyright Open Source Robotics Foundation

inherit ros_distro_rolling
inherit ros_superflore_generated

DESCRIPTION = "Packages for interfacing ROS2 with OpenCV, a library of programming functions for real time computer vision."
AUTHOR = "Kenji Brameld <kenjibrameld@gmail.com>"
ROS_AUTHOR = "Patrick Mihelich"
HOMEPAGE = "http://www.ros.org/wiki/vision_opencv"
SECTION = "devel"
# Original license in package.xml, joined with "&" when multiple license tags were used:
#         "Apache License 2.0 & BSD"
LICENSE = "Apache-2.0 & BSD"
LIC_FILES_CHKSUM = "file://package.xml;beginline=8;endline=8;md5=12c26a18c7f493fdc7e8a93b16b7c04f"

ROS_CN = "vision_opencv"
ROS_BPN = "vision_opencv"

ROS_BUILD_DEPENDS = ""

ROS_BUILDTOOL_DEPENDS = " \
    ament-cmake-native \
"

ROS_EXPORT_DEPENDS = ""

ROS_BUILDTOOL_EXPORT_DEPENDS = ""

ROS_EXEC_DEPENDS = " \
    cv-bridge \
    image-geometry \
"

# Currently informational only -- see http://www.ros.org/reps/rep-0149.html#dependency-tags.
ROS_TEST_DEPENDS = ""

DEPENDS = "${ROS_BUILD_DEPENDS} ${ROS_BUILDTOOL_DEPENDS}"
# Bitbake doesn't support the "export" concept, so build them as if we needed them to build this package (even though we actually
# don't) so that they're guaranteed to have been staged should this package appear in another's DEPENDS.
DEPENDS += "${ROS_EXPORT_DEPENDS} ${ROS_BUILDTOOL_EXPORT_DEPENDS}"

RDEPENDS:${PN} += "${ROS_EXEC_DEPENDS}"

# matches with: https://github.com/ros2-gbp/vision_opencv-release/archive/release/rolling/vision_opencv/3.4.0-3.tar.gz
ROS_BRANCH ?= "branch=release/rolling/vision_opencv"
SRC_URI = "git://github.com/ros2-gbp/vision_opencv-release;${ROS_BRANCH};protocol=https"
SRCREV = "2f4644d378c52354c9cca530b347640329a66283"
S = "${WORKDIR}/git"

ROS_BUILD_TYPE = "ament_cmake"

inherit ros_${ROS_BUILD_TYPE}
