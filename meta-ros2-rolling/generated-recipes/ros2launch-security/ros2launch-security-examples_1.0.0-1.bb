# Generated by superflore -- DO NOT EDIT
#
# Copyright Open Source Robotics Foundation

inherit ros_distro_rolling
inherit ros_superflore_generated

DESCRIPTION = "Examples of how to use the ros2launch_security extension."
AUTHOR = "Ted Kern <ted.kern@canonical.com>"
HOMEPAGE = "https://wiki.ros.org"
SECTION = "devel"
# Original license in package.xml, joined with "&" when multiple license tags were used:
#         "Apache License 2.0"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://package.xml;beginline=8;endline=8;md5=12c26a18c7f493fdc7e8a93b16b7c04f"

ROS_CN = "ros2launch_security"
ROS_BPN = "ros2launch_security_examples"

ROS_BUILD_DEPENDS = " \
    ament-nodl \
    example-interfaces \
    rclcpp \
    rclcpp-components \
    rclpy \
    ros2launch-security \
    sensor-msgs \
"

ROS_BUILDTOOL_DEPENDS = " \
    ament-cmake-native \
"

ROS_EXPORT_DEPENDS = " \
    ament-nodl \
    example-interfaces \
    rclcpp \
    rclcpp-components \
    rclpy \
    ros2launch-security \
    sensor-msgs \
"

ROS_BUILDTOOL_EXPORT_DEPENDS = ""

ROS_EXEC_DEPENDS = " \
    ament-nodl \
    example-interfaces \
    rclcpp \
    rclcpp-components \
    rclpy \
    ros2launch-security \
    sensor-msgs \
"

# Currently informational only -- see http://www.ros.org/reps/rep-0149.html#dependency-tags.
ROS_TEST_DEPENDS = " \
    ament-lint-auto \
    ament-lint-common \
    launch-testing \
    launch-testing-ament-cmake \
    launch-testing-ros \
    nodl-python \
    nodl-to-policy \
    sros2 \
"

DEPENDS = "${ROS_BUILD_DEPENDS} ${ROS_BUILDTOOL_DEPENDS}"
# Bitbake doesn't support the "export" concept, so build them as if we needed them to build this package (even though we actually
# don't) so that they're guaranteed to have been staged should this package appear in another's DEPENDS.
DEPENDS += "${ROS_EXPORT_DEPENDS} ${ROS_BUILDTOOL_EXPORT_DEPENDS}"

RDEPENDS:${PN} += "${ROS_EXEC_DEPENDS}"

# matches with: https://github.com/ros2-gbp/ros2launch_security-release/archive/release/rolling/ros2launch_security_examples/1.0.0-1.tar.gz
ROS_BRANCH ?= "branch=release/rolling/ros2launch_security_examples"
SRC_URI = "git://github.com/ros2-gbp/ros2launch_security-release;${ROS_BRANCH};protocol=https"
SRCREV = "32405ad3e6aecd669f26e47f241b731b3dd5de59"
S = "${WORKDIR}/git"

ROS_BUILD_TYPE = "ament_cmake"

inherit ros_${ROS_BUILD_TYPE}
