# Copyright (c) 2021 LG Electronics, Inc.

inherit pkgconfig

ROS_BUILD_DEPENDS += "\
    ament-cmake-ros-native \
    python3-numpy-native \
    rosidl-adapter-native \
    rosidl-generator-c-native \
    rosidl-generator-cpp-native \
    rosidl-generator-py-native \
    rosidl-typesupport-cpp-native \
    rosidl-typesupport-fastrtps-c-native \
    rosidl-typesupport-fastrtps-cpp-native \
    rosidl-typesupport-introspection-cpp-native \
"

# CMake Error: format not a string literal and no format arguments
# [-Werror=format-security] -- passing a runtime string (msg.c_str()) as
# the format argument itself, rather than as a %s argument, is a genuine
# format-string footgun (if the message ever contained a literal '%' it
# would be misinterpreted as a format specifier), not a false positive.
# Standard fix: pass an explicit "%s" format string.
do_configure:prepend() {
    sed -i \
        -e 's/RCLCPP_ERROR(logger_, msg.c_str());/RCLCPP_ERROR(logger_, "%s", msg.c_str());/g' \
        ${S}/src/parameters.cpp
}
