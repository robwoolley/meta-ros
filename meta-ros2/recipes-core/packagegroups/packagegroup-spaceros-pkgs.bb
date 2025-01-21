DESCRIPTION = "SpaceROS package group"
LICENSE = "MIT"

inherit packagegroup
inherit ros_distro_${ROS_DISTRO}

PACKAGES = "${PN}"

RDEPENDS:${PN} = " \
    action-msgs \
    ament-cmake \
    ament-lint \
    builtin-interfaces \
    class-loader \
    composition-interfaces \
    console-bridge-vendor \
    cyclonedds \
    domain-coordinator \
    eigen3-cmake-module \
    geometry-msgs \
    gmock-vendor \
    google-benchmark-vendor \
    gtest-vendor \
    iceoryx-binding-c \
    iceoryx-hoofs \
    iceoryx-posh \
    launch \
    launch-ros \
    rcl \
    rcl-action \
    rcl-interfaces \
    rcl-lifecycle \
    rcl-logging-interface \
    rcl-logging-spdlog \
    rcl-yaml-param-parser \
    rclcpp \
    rclcpp-action \
    rclcpp-components \
    rclcpp-lifecycle \
    rclpy \
    rmw \
    rmw-cyclonedds-cpp \
    rmw-dds-common \
    rmw-implementation \
    rmw-implementation-cmake \
    robot-state-publisher \
    ros2action \
    ros2cli \
    ros2cli-test-interfaces \
    ros2component \
    ros2doctor \
    ros2interface \
    ros2lifecycle \
    ros2lifecycle-test-fixtures \
    ros2multicast \
    ros2node \
    ros2param \
    ros2pkg \
    ros2run \
    ros2service \
    ros2topic \
    ros-environment \
    ros-testing \
    rosgraph-msgs \
    rosidl-cli \
    sensor-msgs \
    statistics-msgs \
    std-msgs \
    test-interface-files \
    test-msgs \
    tf2 \
    tf2-msgs \
    tf2-ros \
    tracetools \
    unique-identifier-msgs \
    urdf \
    urdf-parser-plugin \
    urdfdom \
    urdfdom-headers \
"
