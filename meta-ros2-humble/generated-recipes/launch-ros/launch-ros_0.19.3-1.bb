# Generated by superflore -- DO NOT EDIT
#
# Copyright Open Source Robotics Foundation

inherit ros_distro_humble
inherit ros_superflore_generated

DESCRIPTION = "ROS specific extensions to the launch tool."
AUTHOR = "Aditya Pande <aditya.pande@osrfoundation.org>"
ROS_AUTHOR = "William Woodall <william@osrfoundation.org>"
HOMEPAGE = "https://wiki.ros.org"
SECTION = "devel"
# Original license in package.xml, joined with "&" when multiple license tags were used:
#         "Apache License 2.0"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://package.xml;beginline=12;endline=12;md5=12c26a18c7f493fdc7e8a93b16b7c04f"

ROS_CN = "launch_ros"
ROS_BPN = "launch_ros"

ROS_BUILD_DEPENDS = " \
    ament-index-python \
    composition-interfaces \
    launch \
    lifecycle-msgs \
    osrf-pycommon \
    python3-importlib-metadata \
    python3-pyyaml \
    rclpy \
"

ROS_BUILDTOOL_DEPENDS = ""

ROS_EXPORT_DEPENDS = " \
    ament-index-python \
    composition-interfaces \
    launch \
    lifecycle-msgs \
    osrf-pycommon \
    python3-importlib-metadata \
    python3-pyyaml \
    rclpy \
"

ROS_BUILDTOOL_EXPORT_DEPENDS = ""

ROS_EXEC_DEPENDS = " \
    ament-index-python \
    composition-interfaces \
    launch \
    lifecycle-msgs \
    osrf-pycommon \
    python3-importlib-metadata \
    python3-pyyaml \
    rclpy \
"

# Currently informational only -- see http://www.ros.org/reps/rep-0149.html#dependency-tags.
ROS_TEST_DEPENDS = " \
    ament-copyright \
    ament-flake8 \
    ament-pep257 \
    python3-pytest \
"

DEPENDS = "${ROS_BUILD_DEPENDS} ${ROS_BUILDTOOL_DEPENDS}"
# Bitbake doesn't support the "export" concept, so build them as if we needed them to build this package (even though we actually
# don't) so that they're guaranteed to have been staged should this package appear in another's DEPENDS.
DEPENDS += "${ROS_EXPORT_DEPENDS} ${ROS_BUILDTOOL_EXPORT_DEPENDS}"

RDEPENDS:${PN} += "${ROS_EXEC_DEPENDS}"

# matches with: https://github.com/ros2-gbp/launch_ros-release/archive/release/humble/launch_ros/0.19.3-1.tar.gz
ROS_BRANCH ?= "branch=release/humble/launch_ros"
SRC_URI = "git://github.com/ros2-gbp/launch_ros-release;${ROS_BRANCH};protocol=https"
SRCREV = "b5e05aa9bbe6adc5b3e4b24e57c125914b7dce42"
S = "${WORKDIR}/git"

ROS_BUILD_TYPE = "ament_python"

inherit ros_${ROS_BUILD_TYPE}
