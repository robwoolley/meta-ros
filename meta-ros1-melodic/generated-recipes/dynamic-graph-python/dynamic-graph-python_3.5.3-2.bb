# Generated by superflore -- DO NOT EDIT
#
# Copyright Open Source Robotics Foundation

inherit ros_distro_melodic
inherit ros_superflore_generated

DESCRIPTION = "Dynamic graph library Python bindings"
AUTHOR = "Olivier Stasse <ostasse@laas.fr>"
ROS_AUTHOR = "Nicolas Mansard"
HOMEPAGE = "http://github.com/stack-of-tasks/dynamic-graph-python"
SECTION = "devel"
LICENSE = "BSD"
LIC_FILES_CHKSUM = "file://package.xml;beginline=9;endline=9;md5=d566ef916e9dedc494f5f793a6690ba5"

ROS_CN = "dynamic-graph-python"
ROS_BPN = "dynamic-graph-python"

ROS_BUILD_DEPENDS = " \
    boost \
    doxygen \
    dynamic-graph \
    git \
    roscpp \
"

ROS_BUILDTOOL_DEPENDS = " \
    cmake-native \
"

ROS_EXPORT_DEPENDS = " \
    boost \
    dynamic-graph \
    roscpp \
"

ROS_BUILDTOOL_EXPORT_DEPENDS = ""

ROS_EXEC_DEPENDS = " \
    boost \
    catkin \
    dynamic-graph \
    roscpp \
"

# Currently informational only -- see http://www.ros.org/reps/rep-0149.html#dependency-tags.
ROS_TEST_DEPENDS = ""

DEPENDS = "${ROS_BUILD_DEPENDS} ${ROS_BUILDTOOL_DEPENDS}"
# Bitbake doesn't support the "export" concept, so build them as if we needed them to build this package (even though we actually
# don't) so that they're guaranteed to have been staged should this package appear in another's DEPENDS.
DEPENDS += "${ROS_EXPORT_DEPENDS} ${ROS_BUILDTOOL_EXPORT_DEPENDS}"

RDEPENDS_${PN} += "${ROS_EXEC_DEPENDS}"

# matches with: https://github.com/stack-of-tasks/dynamic-graph-python-ros-release/archive/release/melodic/dynamic-graph-python/3.5.3-2.tar.gz
ROS_BRANCH ?= "branch=release/melodic/dynamic-graph-python"
SRC_URI = "git://github.com/stack-of-tasks/dynamic-graph-python-ros-release;${ROS_BRANCH};protocol=https"
SRCREV = "5b73c1587fff2b7532fbd1fd6c2e4b748e37dfa4"
S = "${WORKDIR}/git"

ROS_BUILD_TYPE = "cmake"

inherit ros_${ROS_BUILD_TYPE}
