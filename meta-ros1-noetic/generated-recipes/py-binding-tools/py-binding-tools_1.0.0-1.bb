# Generated by superflore -- DO NOT EDIT
#
# Copyright Open Source Robotics Foundation

inherit ros_distro_noetic
inherit ros_superflore_generated

DESCRIPTION = "Python binding tools for C++"
AUTHOR = "Robert Haschke <rhaschke@techfak.uni-bielefeld.de>"
ROS_AUTHOR = "Robert Haschke <rhaschke@techfak.uni-bielefeld.de>"
HOMEPAGE = "http://wiki.ros.org/py_binding_tools"
SECTION = "devel"
LICENSE = "BSD"
LIC_FILES_CHKSUM = "file://package.xml;beginline=10;endline=10;md5=d566ef916e9dedc494f5f793a6690ba5"

ROS_CN = "py_binding_tools"
ROS_BPN = "py_binding_tools"

ROS_BUILD_DEPENDS = " \
    geometry-msgs \
    pybind11-catkin \
    roscpp \
"

ROS_BUILDTOOL_DEPENDS = " \
    catkin-native \
"

ROS_EXPORT_DEPENDS = " \
    geometry-msgs \
    roscpp \
"

ROS_BUILDTOOL_EXPORT_DEPENDS = ""

ROS_EXEC_DEPENDS = " \
    geometry-msgs \
    roscpp \
"

# Currently informational only -- see http://www.ros.org/reps/rep-0149.html#dependency-tags.
ROS_TEST_DEPENDS = ""

DEPENDS = "${ROS_BUILD_DEPENDS} ${ROS_BUILDTOOL_DEPENDS}"
# Bitbake doesn't support the "export" concept, so build them as if we needed them to build this package (even though we actually
# don't) so that they're guaranteed to have been staged should this package appear in another's DEPENDS.
DEPENDS += "${ROS_EXPORT_DEPENDS} ${ROS_BUILDTOOL_EXPORT_DEPENDS}"

RDEPENDS:${PN} += "${ROS_EXEC_DEPENDS}"

# matches with: https://github.com/ros-gbp/py_binding_tools-release/archive/release/noetic/py_binding_tools/1.0.0-1.tar.gz
ROS_BRANCH ?= "branch=release/noetic/py_binding_tools"
SRC_URI = "git://github.com/ros-gbp/py_binding_tools-release;${ROS_BRANCH};protocol=https"
SRCREV = "df413c6b127c11cdfe2d0f27f897276fb301749d"
S = "${WORKDIR}/git"

ROS_BUILD_TYPE = "catkin"

inherit ros_${ROS_BUILD_TYPE}
