# Generated by superflore -- DO NOT EDIT
#
# Copyright Open Source Robotics Foundation

inherit ros_distro_noetic
inherit ros_superflore_generated

DESCRIPTION = "OMPL is a free sampling-based motion planning library."
AUTHOR = "Mark Moll <mmoll@rice.edu>"
ROS_AUTHOR = "Kavraki Lab"
HOMEPAGE = "https://ompl.kavrakilab.org"
SECTION = "devel"
LICENSE = "BSD"
LIC_FILES_CHKSUM = "file://package.xml;beginline=7;endline=7;md5=d566ef916e9dedc494f5f793a6690ba5"

ROS_CN = "ompl"
ROS_BPN = "ompl"

ROS_BUILD_DEPENDS = " \
    boost \
    cmake \
    libeigen \
    libflann \
    ode \
    pkgconfig \
"

ROS_BUILDTOOL_DEPENDS = " \
    cmake-native \
"

ROS_EXPORT_DEPENDS = ""

ROS_BUILDTOOL_EXPORT_DEPENDS = ""

ROS_EXEC_DEPENDS = " \
    boost \
    libeigen \
    libflann \
    ode \
"

# Currently informational only -- see http://www.ros.org/reps/rep-0149.html#dependency-tags.
ROS_TEST_DEPENDS = ""

DEPENDS = "${ROS_BUILD_DEPENDS} ${ROS_BUILDTOOL_DEPENDS}"
# Bitbake doesn't support the "export" concept, so build them as if we needed them to build this package (even though we actually
# don't) so that they're guaranteed to have been staged should this package appear in another's DEPENDS.
DEPENDS += "${ROS_EXPORT_DEPENDS} ${ROS_BUILDTOOL_EXPORT_DEPENDS}"

RDEPENDS:${PN} += "${ROS_EXEC_DEPENDS}"

# matches with: https://github.com/ros-gbp/ompl-release/archive/release/noetic/ompl/1.6.0-1.tar.gz
ROS_BRANCH ?= "branch=release/noetic/ompl"
SRC_URI = "git://github.com/ros-gbp/ompl-release;${ROS_BRANCH};protocol=https"
SRCREV = "24760fbfdfc0daf75fb9943081cc14c618668c40"
S = "${WORKDIR}/git"

ROS_BUILD_TYPE = "cmake"

inherit ros_${ROS_BUILD_TYPE}
