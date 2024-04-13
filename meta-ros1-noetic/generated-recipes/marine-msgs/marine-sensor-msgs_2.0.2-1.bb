# Generated by superflore -- DO NOT EDIT
#
# Copyright Open Source Robotics Foundation

inherit ros_distro_noetic
inherit ros_superflore_generated

DESCRIPTION = "The marine_sensor_msgs package, meant to contain messages for common   underwater sensors (e.g., conductivity, turbidity, dissolved oxygen)"
AUTHOR = "Laura Lindzey <lindzey@uw.edu>"
ROS_AUTHOR = "Aaron Marburg <amarburg@uw.edu>"
HOMEPAGE = "https://wiki.ros.org"
SECTION = "devel"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://package.xml;beginline=12;endline=12;md5=4633480cdd27d7906aaf3ef4b72014b2"

ROS_CN = "marine_msgs"
ROS_BPN = "marine_sensor_msgs"

ROS_BUILD_DEPENDS = " \
    message-generation \
    rosbag-migration-rule \
    std-msgs \
"

ROS_BUILDTOOL_DEPENDS = " \
    catkin-native \
"

ROS_EXPORT_DEPENDS = " \
    rosbag-migration-rule \
    std-msgs \
"

ROS_BUILDTOOL_EXPORT_DEPENDS = ""

ROS_EXEC_DEPENDS = " \
    message-runtime \
    rosbag-migration-rule \
    std-msgs \
"

# Currently informational only -- see http://www.ros.org/reps/rep-0149.html#dependency-tags.
ROS_TEST_DEPENDS = ""

DEPENDS = "${ROS_BUILD_DEPENDS} ${ROS_BUILDTOOL_DEPENDS}"
# Bitbake doesn't support the "export" concept, so build them as if we needed them to build this package (even though we actually
# don't) so that they're guaranteed to have been staged should this package appear in another's DEPENDS.
DEPENDS += "${ROS_EXPORT_DEPENDS} ${ROS_BUILDTOOL_EXPORT_DEPENDS}"

RDEPENDS:${PN} += "${ROS_EXEC_DEPENDS}"

# matches with: https://github.com/CCOMJHC/marine_msgs-release/archive/release/noetic/marine_sensor_msgs/2.0.2-1.tar.gz
ROS_BRANCH ?= "branch=release/noetic/marine_sensor_msgs"
SRC_URI = "git://github.com/CCOMJHC/marine_msgs-release;${ROS_BRANCH};protocol=https"
SRCREV = "e2ce21b5040560a9841efb91a4a5640b52d157a4"
S = "${WORKDIR}/git"

ROS_BUILD_TYPE = "catkin"

inherit ros_${ROS_BUILD_TYPE}
