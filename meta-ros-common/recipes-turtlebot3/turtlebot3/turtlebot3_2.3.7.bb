# Copyright (c) 2026 Wind River Systems, Inc.
#
# Hand-authored: see turtlebot3-description_2.3.7.bb for the full rationale
# (no bloom release for this repo/distro combination exists to generate
# from). One of 8 packages in the same turtlebot3 monorepo.
#
# Upstream's own package.xml also exec_depends on turtlebot3_navigation2,
# deliberately dropped here: it needs nav2-bringup, which has a real
# upstream release for lyrical but was never generated as a recipe in this
# project at all (confirmed: exists for humble/jazzy/kilted, absent for
# lyrical). Backfilling the whole Nav2 stack is a separate, much larger
# undertaking than turtlebot3 itself.
#
# turtlebot3_cartographer is also dropped: it only needs cartographer-ros
# (already fixed and working), but cartographer-ros itself needs
# cartographer, which fails to compile against this layer set's Abseil --
# cartographer/mapping/internal/imu_based_pose_extrapolator.cc (and 2
# sibling files) call LOG_IF_EVERY_N via glog/logging.h, which no longer
# defines it (absl/log/log.h does, under the same name, but isn't included
# here). This is on top of two other real Abseil/GCC-15 compatibility
# issues already fixed directly in cartographer's own bbappend this same
# session (a now-nonexistent -fuse-ld=gold linker requirement, and a
# missing-return warning in ceres-solver's bundled miniglog header) --
# cartographer has its own accumulating, non-trivial compatibility gap with
# this distro's very new toolchain, deserving separate follow-up rather
# than being chased further here.
#
# This metapackage covers the other 6 packages only.
DESCRIPTION = "ROS 2 packages for TurtleBot3"
HOMEPAGE = "https://github.com/ROBOTIS-GIT/turtlebot3"
SECTION = "devel"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://package.xml;beginline=10;endline=10;md5=3dce4ba60d7e51ec64f3c3dc18672dd3"

ROS_CN = "turtlebot3"
ROS_BPN = "turtlebot3"

SRC_URI = "git://github.com/ROBOTIS-GIT/turtlebot3.git;branch=main;protocol=https"
SRCREV = "fc817ce3073af1d6032397c64504134882af5e9a"

S = "${UNPACKDIR}/${BP}/turtlebot3"

ROS_BUILD_DEPENDS = ""

ROS_BUILDTOOL_DEPENDS = " \
    ament-cmake-native \
"

ROS_EXPORT_DEPENDS = ""

ROS_BUILDTOOL_EXPORT_DEPENDS = ""

ROS_EXEC_DEPENDS = " \
    turtlebot3-bringup \
    turtlebot3-description \
    turtlebot3-example \
    turtlebot3-node \
    turtlebot3-teleop \
"

ROS_TEST_DEPENDS = ""

DEPENDS = "${ROS_BUILD_DEPENDS} ${ROS_BUILDTOOL_DEPENDS}"
# Bitbake doesn't support the "export" concept, so build them as if we needed them to build this package (even though we actually
# don't) so that they're guaranteed to have been staged should this package appear in another's DEPENDS.
DEPENDS += "${ROS_EXPORT_DEPENDS} ${ROS_BUILDTOOL_EXPORT_DEPENDS}"

RDEPENDS:${PN} += "${ROS_EXEC_DEPENDS}"

ROS_BUILD_TYPE = "ament_cmake"

inherit ros_distro_lyrical
inherit ros_component
inherit ros_${ROS_BUILD_TYPE}
