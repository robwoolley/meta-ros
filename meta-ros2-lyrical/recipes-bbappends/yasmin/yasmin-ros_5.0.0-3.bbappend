# Copyright (c) 2026 Wind River Systems, Inc.

# CMake Error: format not a string literal and no format arguments
# [-Werror=format-security] -- passing a runtime string (oss.str()) as the
# format argument itself, rather than as a %s argument, is a genuine
# format-string footgun (if the message ever contained a literal '%' it
# would be misinterpreted as a format specifier), not a false positive.
# Standard fix: pass an explicit "%s" format string.
do_configure:prepend() {
    sed -i \
        -e 's/RCLCPP_DEBUG(logger, oss.str().c_str());/RCLCPP_DEBUG(logger, "%s", oss.str().c_str());/g' \
        -e 's/RCLCPP_ERROR(logger, oss.str().c_str());/RCLCPP_ERROR(logger, "%s", oss.str().c_str());/g' \
        -e 's/RCLCPP_INFO(logger, oss.str().c_str());/RCLCPP_INFO(logger, "%s", oss.str().c_str());/g' \
        -e 's/RCLCPP_WARN(logger, oss.str().c_str());/RCLCPP_WARN(logger, "%s", oss.str().c_str());/g' \
        ${S}/src/yasmin_ros/ros_logs.cpp
}
