# Generated by superflore -- DO NOT EDIT
#
# Copyright Open Source Robotics Foundation

inherit ros_distro_rolling
inherit ros_superflore_generated

DESCRIPTION = "CMake macros and utilities to include Vitis platform into the ROS 2 build system (ament) and its development flows."
AUTHOR = "Víctor Mayoral Vilches <victor@accelerationrobotics.com>"
ROS_AUTHOR = "Víctor Mayoral Vilches <victor@accelerationrobotics.com>"
HOMEPAGE = "https://wiki.ros.org"
SECTION = "devel"
# Original license in package.xml, joined with "&" when multiple license tags were used:
#         "Apache License 2.0"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://package.xml;beginline=11;endline=11;md5=12c26a18c7f493fdc7e8a93b16b7c04f"

ROS_CN = "ament_vitis"
ROS_BPN = "ament_vitis"

ROS_BUILD_DEPENDS = " \
    ament-acceleration \
    ament-cmake-ros \
"

ROS_BUILDTOOL_DEPENDS = " \
    ament-cmake-core-native \
"

ROS_EXPORT_DEPENDS = " \
    ament-acceleration \
    ament-cmake-ros \
"

ROS_BUILDTOOL_EXPORT_DEPENDS = " \
    ament-cmake-core-native \
"

ROS_EXEC_DEPENDS = " \
    ament-acceleration \
    ament-cmake-ros \
"

# Currently informational only -- see http://www.ros.org/reps/rep-0149.html#dependency-tags.
ROS_TEST_DEPENDS = ""

DEPENDS = "${ROS_BUILD_DEPENDS} ${ROS_BUILDTOOL_DEPENDS}"
# Bitbake doesn't support the "export" concept, so build them as if we needed them to build this package (even though we actually
# don't) so that they're guaranteed to have been staged should this package appear in another's DEPENDS.
DEPENDS += "${ROS_EXPORT_DEPENDS} ${ROS_BUILDTOOL_EXPORT_DEPENDS}"

RDEPENDS:${PN} += "${ROS_EXEC_DEPENDS}"

# matches with: https://github.com/ros2-gbp/ament_vitis-release/archive/release/rolling/ament_vitis/0.10.1-3.tar.gz
ROS_BRANCH ?= "branch=release/rolling/ament_vitis"
SRC_URI = "git://github.com/ros2-gbp/ament_vitis-release;${ROS_BRANCH};protocol=https"
SRCREV = "bb01eabd9f1f2de7f54f2178c7e8a4ed55ada096"
S = "${WORKDIR}/git"

ROS_BUILD_TYPE = "ament_cmake"

inherit ros_${ROS_BUILD_TYPE}
