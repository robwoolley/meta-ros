# Generated by superflore -- DO NOT EDIT
#
# Copyright Open Source Robotics Foundation

inherit ros_distro_galactic
inherit ros_superflore_generated

DESCRIPTION = "A package containing scripts for demos"
AUTHOR = "Yadunund <yadunund@openrobotics.org>"
ROS_AUTHOR = "Grey <grey@openrobotics.org>"
HOMEPAGE = "https://wiki.ros.org"
SECTION = "devel"
# Original license in package.xml, joined with "&" when multiple license tags were used:
#         "Apache Licence 2.0"
LICENSE = "Apache-Licence-2.0"
LIC_FILES_CHKSUM = "file://package.xml;beginline=10;endline=10;md5=1b1c76d8de4ab08fc409e5ecfd544e9d"

ROS_CN = "rmf_demos"
ROS_BPN = "rmf_demos_tasks"

ROS_BUILD_DEPENDS = " \
    rmf-dispenser-msgs \
    rmf-fleet-msgs \
    rmf-lift-msgs \
    rmf-task-msgs \
"

ROS_BUILDTOOL_DEPENDS = ""

ROS_EXPORT_DEPENDS = " \
    rmf-dispenser-msgs \
    rmf-fleet-msgs \
    rmf-lift-msgs \
    rmf-task-msgs \
"

ROS_BUILDTOOL_EXPORT_DEPENDS = ""

ROS_EXEC_DEPENDS = " \
    rmf-dispenser-msgs \
    rmf-fleet-msgs \
    rmf-lift-msgs \
    rmf-task-msgs \
"

# Currently informational only -- see http://www.ros.org/reps/rep-0149.html#dependency-tags.
ROS_TEST_DEPENDS = ""

DEPENDS = "${ROS_BUILD_DEPENDS} ${ROS_BUILDTOOL_DEPENDS}"
# Bitbake doesn't support the "export" concept, so build them as if we needed them to build this package (even though we actually
# don't) so that they're guaranteed to have been staged should this package appear in another's DEPENDS.
DEPENDS += "${ROS_EXPORT_DEPENDS} ${ROS_BUILDTOOL_EXPORT_DEPENDS}"

RDEPENDS:${PN} += "${ROS_EXEC_DEPENDS}"

# matches with: https://github.com/ros2-gbp/rmf_demos-release/archive/release/galactic/rmf_demos_tasks/1.3.0-1.tar.gz
ROS_BRANCH ?= "branch=release/galactic/rmf_demos_tasks"
SRC_URI = "git://github.com/ros2-gbp/rmf_demos-release;${ROS_BRANCH};protocol=https"
SRCREV = "6a4936ec4fa57733e33ccbe904e00d259c468b73"
S = "${WORKDIR}/git"

ROS_BUILD_TYPE = "ament_python"

inherit ros_${ROS_BUILD_TYPE}
