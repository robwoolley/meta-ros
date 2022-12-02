# Generated by superflore -- DO NOT EDIT
#
# Copyright Open Source Robotics Foundation

inherit ros_distro_humble
inherit ros_superflore_generated

DESCRIPTION = "octovis is visualization tool for the OctoMap library based on Qt and libQGLViewer. See   http://octomap.github.io for details."
AUTHOR = "Armin Hornung <armin@hornung.io>"
ROS_AUTHOR = "Kai M. Wurm <wurm@informatik.uni-freiburg.de>"
HOMEPAGE = "http://octomap.github.io"
SECTION = "devel"
# Original license in package.xml, joined with "&" when multiple license tags were used:
#         "GPLv2"
LICENSE = "GPL-2.0-only"
LIC_FILES_CHKSUM = "file://package.xml;beginline=11;endline=11;md5=fe8b75cf0aba647401e1038bcd69ee74"

ROS_CN = "octomap"
ROS_BPN = "octovis"

ROS_BUILD_DEPENDS = " \
    octomap \
    qtbase \
"

ROS_BUILDTOOL_DEPENDS = " \
    cmake-native \
"

ROS_EXPORT_DEPENDS = ""

ROS_BUILDTOOL_EXPORT_DEPENDS = ""

ROS_EXEC_DEPENDS = " \
    ${ROS_UNRESOLVED_DEP-libqglviewer2-qt5} \
    octomap \
    qtbase \
"

# Currently informational only -- see http://www.ros.org/reps/rep-0149.html#dependency-tags.
ROS_TEST_DEPENDS = ""

DEPENDS = "${ROS_BUILD_DEPENDS} ${ROS_BUILDTOOL_DEPENDS}"
# Bitbake doesn't support the "export" concept, so build them as if we needed them to build this package (even though we actually
# don't) so that they're guaranteed to have been staged should this package appear in another's DEPENDS.
DEPENDS += "${ROS_EXPORT_DEPENDS} ${ROS_BUILDTOOL_EXPORT_DEPENDS}"

RDEPENDS:${PN} += "${ROS_EXEC_DEPENDS}"

# matches with: https://github.com/ros2-gbp/octomap-release/archive/release/humble/octovis/1.9.8-1.tar.gz
ROS_BRANCH ?= "branch=release/humble/octovis"
SRC_URI = "git://github.com/ros2-gbp/octomap-release;${ROS_BRANCH};protocol=https"
SRCREV = "55c571b77872f5b0747f694dffc5d3449a70adb3"
S = "${WORKDIR}/git"

ROS_BUILD_TYPE = "cmake"

inherit ros_${ROS_BUILD_TYPE}
