# Copyright (c) 2026 Wind River Systems, Inc.
#
# Hand-authored: see turtlebot3-description_2.3.7.bb for the full rationale
# (no bloom release for this repo/distro combination exists to generate
# from). One of 8 packages in the same turtlebot3 monorepo.
DESCRIPTION = "TurtleBot3 driver node that include diff drive controller, odometry and tf node"
HOMEPAGE = "https://github.com/ROBOTIS-GIT/turtlebot3"
SECTION = "devel"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://package.xml;beginline=10;endline=10;md5=3dce4ba60d7e51ec64f3c3dc18672dd3"

ROS_CN = "turtlebot3"
ROS_BPN = "turtlebot3_node"

SRC_URI = "git://github.com/ROBOTIS-GIT/turtlebot3.git;branch=main;protocol=https"
SRCREV = "fc817ce3073af1d6032397c64504134882af5e9a"

S = "${UNPACKDIR}/${BP}/turtlebot3_node"

ROS_BUILD_DEPENDS = " \
    dynamixel-sdk \
    geometry-msgs \
    message-filters \
    nav-msgs \
    rclcpp \
    rcutils \
    sensor-msgs \
    std-msgs \
    std-srvs \
    tf2 \
    tf2-ros \
    turtlebot3-msgs \
"

ROS_BUILDTOOL_DEPENDS = " \
    ament-cmake-native \
"

ROS_EXPORT_DEPENDS = ""

ROS_BUILDTOOL_EXPORT_DEPENDS = ""

ROS_EXEC_DEPENDS = " \
    dynamixel-sdk \
    geometry-msgs \
    message-filters \
    nav-msgs \
    rclcpp \
    rcutils \
    sensor-msgs \
    std-msgs \
    std-srvs \
    tf2 \
    tf2-ros \
    turtlebot3-msgs \
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

# message_filters 7.4.1 and tf2 0.45.7 (this layer set's versions) both
# renamed their headers from .h to .hpp; upstream turtlebot3_node's
# odometry.hpp (written against older releases of both) still includes the
# old .h names, which no longer exist at all ("message_filters/subscriber.h:
# No such file or directory", then "tf2/LinearMath/Quaternion.h: No such
# file or directory").
do_configure:prepend() {
    if ! grep -q "message_filters/subscriber.hpp" ${S}/include/turtlebot3_node/odometry.hpp; then
        sed -i \
            -e 's#message_filters/subscriber\.h#message_filters/subscriber.hpp#' \
            -e 's#message_filters/sync_policies/approximate_time\.h#message_filters/sync_policies/approximate_time.hpp#' \
            -e 's#message_filters/synchronizer\.h#message_filters/synchronizer.hpp#' \
            -e 's#tf2/LinearMath/Quaternion\.h#tf2/LinearMath/Quaternion.hpp#' \
            ${S}/include/turtlebot3_node/odometry.hpp
    fi

    # message_filters::Subscriber's 2-arg (node, topic) constructor was
    # removed; message_filters 7.4.1 requires an explicit rclcpp::QoS as a
    # 3rd argument. A "qos" variable already holding the exact QoS these
    # two subscribers should use (rclcpp::QoS(rclcpp::KeepLast(10))) is
    # already defined a few lines above both call sites in odometry.cpp --
    # just pass it through instead of introducing a new one.
    if ! grep -q '"joint_states", qos)' ${S}/src/odometry.cpp; then
        sed -i \
            -e 's#"joint_states");#"joint_states", qos);#' \
            -e 's#"imu");#"imu", qos);#' \
            ${S}/src/odometry.cpp
    fi

    # ${PROJECT_NAME}_lib is built SHARED (ament_cmake's default) and the
    # turtlebot3_ros executable links against it, but CMakeLists.txt never
    # installs the library itself -- only the executable
    # (install(TARGETS ${EXECUTABLE_NAME} ...)). This is a genuine upstream
    # packaging bug: it likely only ever "worked" via the build tree's own
    # rpath surviving in-place on a dev machine, which doesn't exist once
    # actually packaged and installed elsewhere. Confirmed via a real
    # build's QA failure: "turtlebot3_ros ... requires
    # libturtlebot3_node_lib.so ..., but no providers found". Add the
    # missing install rule, matching the standard ament_cmake layout (lib/
    # for the .so, not lib/${PROJECT_NAME}/ like the executable).
    if ! grep -q "TARGETS \${PROJECT_NAME}_lib" ${S}/CMakeLists.txt; then
        sed -i \
            -e '/^install(TARGETS \${EXECUTABLE_NAME}$/i\
install(TARGETS ${PROJECT_NAME}_lib\
  LIBRARY DESTINATION lib\
  ARCHIVE DESTINATION lib\
  RUNTIME DESTINATION bin\
)\
' \
            ${S}/CMakeLists.txt
    fi
}
