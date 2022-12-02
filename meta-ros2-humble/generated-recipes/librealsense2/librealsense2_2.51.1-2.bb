# Generated by superflore -- DO NOT EDIT
#
# Copyright Open Source Robotics Foundation

inherit ros_distro_humble
inherit ros_superflore_generated

DESCRIPTION = "Library for controlling and capturing data from the Intel(R) RealSense(TM) D400 devices."
AUTHOR = "LibRS ROS Team <librs.ros@intel.com>"
ROS_AUTHOR = "LibRS ROS Team <librs.ros@intel.com>"
HOMEPAGE = "https://github.com/IntelRealSense/librealsense/"
SECTION = "devel"
# Original license in package.xml, joined with "&" when multiple license tags were used:
#         "Apache License, Version 2.0"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://package.xml;beginline=21;endline=21;md5=e8978a5103d23266fc6f8ec03dc9eb16"

ROS_CN = "librealsense2"
ROS_BPN = "librealsense2"

ROS_BUILD_DEPENDS = " \
    libusb1 \
    openssl \
    pkgconfig \
    udev \
"

ROS_BUILDTOOL_DEPENDS = " \
    ament-cmake-native \
"

ROS_EXPORT_DEPENDS = ""

ROS_BUILDTOOL_EXPORT_DEPENDS = ""

ROS_EXEC_DEPENDS = ""

# Currently informational only -- see http://www.ros.org/reps/rep-0149.html#dependency-tags.
ROS_TEST_DEPENDS = ""

DEPENDS = "${ROS_BUILD_DEPENDS} ${ROS_BUILDTOOL_DEPENDS}"
# Bitbake doesn't support the "export" concept, so build them as if we needed them to build this package (even though we actually
# don't) so that they're guaranteed to have been staged should this package appear in another's DEPENDS.
DEPENDS += "${ROS_EXPORT_DEPENDS} ${ROS_BUILDTOOL_EXPORT_DEPENDS}"

RDEPENDS:${PN} += "${ROS_EXEC_DEPENDS}"

# matches with: https://github.com/IntelRealSense/librealsense2-release/archive/release/humble/librealsense2/2.51.1-2.tar.gz
ROS_BRANCH ?= "branch=release/humble/librealsense2"
SRC_URI = "git://github.com/IntelRealSense/librealsense2-release;${ROS_BRANCH};protocol=https"
SRCREV = "725d3e212ac7870c8ef9c1ce54cfda156f3e5fee"
S = "${WORKDIR}/git"

ROS_BUILD_TYPE = "ament_cmake"

inherit ros_${ROS_BUILD_TYPE}
