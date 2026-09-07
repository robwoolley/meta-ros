# Copyright (c) 2026 Wind River Systems, Inc.
#
# Hand-authored: see turtlebot3-yolo-object-detection_1.3.3.bb for the full
# rationale (no bloom release for this repo/distro combination exists to
# generate from). One of 7 packages in the turtlebot3_applications monorepo.
DESCRIPTION = "Application examples for TurtleBot3 (parking, following, panorama, ArUco, YOLO)"
HOMEPAGE = "https://github.com/ROBOTIS-GIT/turtlebot3_applications"
SECTION = "devel"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://package.xml;beginline=10;endline=10;md5=3dce4ba60d7e51ec64f3c3dc18672dd3"

ROS_CN = "turtlebot3_applications"
ROS_BPN = "turtlebot3_applications"

SRC_URI = "git://github.com/ROBOTIS-GIT/turtlebot3_applications.git;branch=main;protocol=https"
SRCREV = "2f0310775daded07bed9be87a6602e0e6e1bae1a"

S = "${UNPACKDIR}/${BP}/turtlebot3_applications"

ROS_BUILD_DEPENDS = ""

ROS_BUILDTOOL_DEPENDS = " \
    ament-cmake-native \
"

ROS_EXPORT_DEPENDS = ""

ROS_BUILDTOOL_EXPORT_DEPENDS = ""

# Upstream's own package.xml also exec_depends on turtlebot3_follower,
# deliberately dropped here: it needs nav2-msgs, which has no recipe in
# this distro at all (see turtlebot3-follower_1.3.3.bb's note -- same Nav2
# generation gap as turtlebot3-navigation2).
#
# turtlebot3_panorama is also dropped: beyond the Boost::system and tf2
# header-rename issues already fixed directly in its own recipe this same
# session, panorama.cpp calls rcpputils::fs::create_directories()/::path()
# (rcpputils' pre-C++17 std::filesystem shim, apparently removed now that
# C++17 filesystem is universally available) and constructs a cv::Mat
# directly from a sensor_msgs::msg::CompressedImage's data field in a way
# that no longer matches this layer set's OpenCV/cv_bridge API. Each is a
# real source-level migration, not a dependency/config fix, so this stays a
# documented gap rather than being chased further here.
ROS_EXEC_DEPENDS = " \
    turtlebot3-aruco-tracker \
    turtlebot3-automatic-parking \
    turtlebot3-automatic-parking-vision \
    turtlebot3-yolo-object-detection \
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
