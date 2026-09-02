# Copyright (c) 2026 Wind River Systems, Inc.

# CMake Error: format not a string literal and no format arguments
# [-Werror=format-security] -- passing a runtime string (e.what()) as the
# format argument itself, rather than as a %s argument, is a genuine
# format-string footgun (if the message ever contained a literal '%' it
# would be misinterpreted as a format specifier), not a false positive.
# Standard fix: pass an explicit "%s" format string.
do_configure:prepend() {
    sed -i \
        -e 's/RCLCPP_ERROR(logger, e.what());/RCLCPP_ERROR(logger, "%s", e.what());/g' \
        ${S}/src/laser_publisher.cpp
}
