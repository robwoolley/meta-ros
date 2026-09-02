# Copyright (c) 2021 LG Electronics, Inc.

# Without the target dependencies, ament finds the native packages and then fails to link (error: incompatible target).
ROS_BUILD_DEPENDS += "\
    rosidl-default-runtime \
    rosidl-adapter-native \
    ament-cmake-ros-native \
    python3-numpy-native \
    rosidl-generator-c-native \
    rosidl-generator-cpp-native \
    rosidl-typesupport-fastrtps-c-native \
    rosidl-typesupport-fastrtps-cpp-native \
    rosidl-typesupport-introspection-cpp-native \
    rosidl-typesupport-cpp-native \
    rosidl-generator-py-native \
"

# CMake Error: format not a string literal and no format arguments
# [-Werror=format-security] -- passing a runtime string as the format
# argument itself, rather than as a %s argument, is a genuine
# format-string footgun (if the message ever contained a literal '%' it
# would be misinterpreted as a format specifier), not a false positive.
# Standard fix: pass an explicit "%s" format string.
do_configure:prepend() {
    sed -i \
        -e 's/RCLCPP_DEBUG(logger, (std::string{"Sending: "} + msg).c_str());/RCLCPP_DEBUG(logger, "%s", (std::string{"Sending: "} + msg).c_str());/g' \
        -e 's/RCLCPP_DEBUG(logger, ("Received: " + std::to_string(len)).c_str());/RCLCPP_DEBUG(logger, "%s", ("Received: " + std::to_string(len)).c_str());/g' \
        -e 's/RCLCPP_DEBUG(logger, (std::string{"Received: "} + msg).c_str());/RCLCPP_DEBUG(logger, "%s", (std::string{"Received: "} + msg).c_str());/g' \
        ${S}/src/connection.cpp
    sed -i \
        -e 's/RCLCPP_DEBUG(this->logger, ("Received: " + recv).c_str());/RCLCPP_DEBUG(this->logger, "%s", ("Received: " + recv).c_str());/g' \
        ${S}/src/rcss3d_agent.cpp
}
