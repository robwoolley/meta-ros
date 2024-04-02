# Generated by superflore -- DO NOT EDIT
#
# Copyright Open Source Robotics Foundation

inherit ros_distro_rolling
inherit ros_superflore_generated

DESCRIPTION = "UBLOX UBX Interfaces"
AUTHOR = "Nick Hortovanyi <nick@aussierobots.com.au>"
HOMEPAGE = "https://github.com/aussierobots/ublox_dgnss"
SECTION = "devel"
# Original license in package.xml, joined with "&" when multiple license tags were used:
#         "Apache License, Version 2.0"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://package.xml;beginline=8;endline=8;md5=e8978a5103d23266fc6f8ec03dc9eb16"

ROS_CN = "ublox_dgnss"
ROS_BPN = "ublox_ubx_interfaces"

ROS_BUILD_DEPENDS = " \
    rosidl-default-generators \
"

ROS_BUILDTOOL_DEPENDS = " \
    ament-cmake-native \
"

ROS_EXPORT_DEPENDS = " \
    rosidl-default-generators \
"

ROS_BUILDTOOL_EXPORT_DEPENDS = ""

ROS_EXEC_DEPENDS = " \
    rosidl-default-generators \
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

# matches with: https://github.com/ros2-gbp/ublox_dgnss-release/archive/release/rolling/ublox_ubx_interfaces/0.5.2-2.tar.gz
ROS_BRANCH ?= "branch=release/rolling/ublox_ubx_interfaces"
SRC_URI = "git://github.com/ros2-gbp/ublox_dgnss-release;${ROS_BRANCH};protocol=https"
SRCREV = "edbd2c9446b1d22de89cc04ffdce72be11895ee0"
S = "${WORKDIR}/git"

ROS_BUILD_TYPE = "ament_cmake"

inherit ros_${ROS_BUILD_TYPE}
