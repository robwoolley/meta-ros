# Generated by superflore -- DO NOT EDIT
#
# Copyright Open Source Robotics Foundation

inherit ros_distro_rolling
inherit ros_superflore_generated

DESCRIPTION = "A composable container for Adaptive ROS 2 Node computations.     Allows building Nodes that can select between FPGA, CPU or     GPU, at run-time. Stateless by default, can be made stateful     to meet use-case specific needs. Refer to examples in README.      Technically, provides A ROS 2 Node subclass programmed as a     &quot;Component&quot; and including its own single threaded executor     to build adaptive computations. Adaptive ROS 2 Nodes are able to     perform computations in the CPU, the FPGA or the GPU, adaptively.     Adaptive behavior is controlled through the &quot;adaptive&quot; ROS 2     parameter."
AUTHOR = "Víctor Mayoral Vilches <victor@accelerationrobotics.com>"
ROS_AUTHOR = "Víctor Mayoral Vilches <victor@accelerationrobotics.com>"
HOMEPAGE = "https://wiki.ros.org"
SECTION = "devel"
# Original license in package.xml, joined with "&" when multiple license tags were used:
#         "Apache License 2.0"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://package.xml;beginline=22;endline=22;md5=12c26a18c7f493fdc7e8a93b16b7c04f"

ROS_CN = "adaptive_component"
ROS_BPN = "adaptive_component"

ROS_BUILD_DEPENDS = " \
    rclcpp \
    rclcpp-components \
"

ROS_BUILDTOOL_DEPENDS = " \
    ament-cmake-native \
"

ROS_EXPORT_DEPENDS = " \
    rclcpp \
    rclcpp-components \
"

ROS_BUILDTOOL_EXPORT_DEPENDS = ""

ROS_EXEC_DEPENDS = " \
    rclcpp \
    rclcpp-components \
"

# Currently informational only -- see http://www.ros.org/reps/rep-0149.html#dependency-tags.
ROS_TEST_DEPENDS = " \
    ament-lint-auto \
    ament-lint-common \
"

DEPENDS = "${ROS_BUILD_DEPENDS} ${ROS_BUILDTOOL_DEPENDS}"
# Bitbake doesn't support the "export" concept, so build them as if we needed them to build this package (even though we actually
# don't) so that they're guaranteed to have been staged should this package appear in another's DEPENDS.
DEPENDS += "${ROS_EXPORT_DEPENDS} ${ROS_BUILDTOOL_EXPORT_DEPENDS}"

RDEPENDS:${PN} += "${ROS_EXEC_DEPENDS}"

# matches with: https://github.com/ros2-gbp/adaptive_component-release/archive/release/rolling/adaptive_component/0.2.1-3.tar.gz
ROS_BRANCH ?= "branch=release/rolling/adaptive_component"
SRC_URI = "git://github.com/ros2-gbp/adaptive_component-release;${ROS_BRANCH};protocol=https"
SRCREV = "fc21f98c0e3dc6854addeabe2545b230a697abda"
S = "${WORKDIR}/git"

ROS_BUILD_TYPE = "ament_cmake"

inherit ros_${ROS_BUILD_TYPE}
