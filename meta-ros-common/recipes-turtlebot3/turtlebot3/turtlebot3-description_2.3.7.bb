# Copyright (c) 2026 Wind River Systems, Inc.
#
# Hand-authored: github.com/ROBOTIS-GIT/turtlebot3 has never had a bloom
# release for any ROS 2 distro (rosdistro tracks it only as doc/source, on
# the "main" branch), so there is no per-package "-release" mirror repo for
# superflore to generate from the way every other ROS package in this tree
# is. Mirrors the ROS_CN/ROS_BPN/ROS_BUILD_DEPENDS conventions superflore
# uses elsewhere for consistency, and still inherits ros_distro_lyrical +
# ros_component (both of which generated recipes get transitively via
# ros_superflore_generated, which doesn't apply here since this isn't
# superflore-generated).
#
# This is one of 8 packages in the same turtlebot3 monorepo; each gets its
# own recipe here, all pointing at the same SRC_URI/SRCREV with S set to
# its own subdirectory.
DESCRIPTION = "3D models of the TurtleBot3 for simulation and visualization"
HOMEPAGE = "https://github.com/ROBOTIS-GIT/turtlebot3"
SECTION = "devel"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://package.xml;beginline=10;endline=10;md5=3dce4ba60d7e51ec64f3c3dc18672dd3"

ROS_CN = "turtlebot3"
ROS_BPN = "turtlebot3_description"

SRC_URI = "git://github.com/ROBOTIS-GIT/turtlebot3.git;branch=main;protocol=https"
SRCREV = "fc817ce3073af1d6032397c64504134882af5e9a"

S = "${UNPACKDIR}/${BP}/turtlebot3_description"

ROS_BUILD_DEPENDS = " \
    urdf \
    xacro \
"

ROS_BUILDTOOL_DEPENDS = " \
    ament-cmake-native \
"

ROS_EXPORT_DEPENDS = ""

ROS_BUILDTOOL_EXPORT_DEPENDS = ""

ROS_EXEC_DEPENDS = " \
    urdf \
    xacro \
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
