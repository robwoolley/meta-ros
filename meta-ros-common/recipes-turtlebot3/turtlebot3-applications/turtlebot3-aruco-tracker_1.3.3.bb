# Copyright (c) 2026 Wind River Systems, Inc.
#
# Hand-authored: see turtlebot3-yolo-object-detection_1.3.3.bb for the full
# rationale (no bloom release for this repo/distro combination exists to
# generate from). One of 7 packages in the turtlebot3_applications monorepo.
DESCRIPTION = "ArUco marker tracking example for TurtleBot3"
HOMEPAGE = "https://github.com/ROBOTIS-GIT/turtlebot3_applications"
SECTION = "devel"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://package.xml;beginline=10;endline=10;md5=3dce4ba60d7e51ec64f3c3dc18672dd3"

ROS_CN = "turtlebot3_applications"
ROS_BPN = "turtlebot3_aruco_tracker"

SRC_URI = "git://github.com/ROBOTIS-GIT/turtlebot3_applications.git;branch=main;protocol=https"
SRCREV = "2f0310775daded07bed9be87a6602e0e6e1bae1a"

S = "${UNPACKDIR}/${BP}/turtlebot3_aruco_tracker"

ROS_BUILD_DEPENDS = " \
    cv-bridge \
    geometry-msgs \
    rclpy \
    sensor-msgs \
    tf2-ros \
"

ROS_BUILDTOOL_DEPENDS = ""

ROS_EXPORT_DEPENDS = ""

ROS_BUILDTOOL_EXPORT_DEPENDS = ""

ROS_EXEC_DEPENDS = " \
    cv-bridge \
    geometry-msgs \
    rclpy \
    sensor-msgs \
    tf2-ros \
"

ROS_TEST_DEPENDS = ""

DEPENDS = "${ROS_BUILD_DEPENDS} ${ROS_BUILDTOOL_DEPENDS}"
# Bitbake doesn't support the "export" concept, so build them as if we needed them to build this package (even though we actually
# don't) so that they're guaranteed to have been staged should this package appear in another's DEPENDS.
DEPENDS += "${ROS_EXPORT_DEPENDS} ${ROS_BUILDTOOL_EXPORT_DEPENDS}"

RDEPENDS:${PN} += "${ROS_EXEC_DEPENDS}"

ROS_BUILD_TYPE = "ament_python"

inherit ros_distro_lyrical
inherit ros_component
inherit ros_${ROS_BUILD_TYPE}
