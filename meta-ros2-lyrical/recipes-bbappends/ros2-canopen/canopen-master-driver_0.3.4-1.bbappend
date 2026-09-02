# Copyright (c) 2026 Wind River Systems, Inc.

# CMake Error: format not a string literal and no format arguments
# [-Werror=format-security] -- passing a runtime string (e.what()) as the
# format argument itself, rather than as a %s argument, is a genuine
# format-string footgun (if the message ever contained a literal '%' it
# would be misinterpreted as a format specifier), not a false positive.
# Standard fix: pass an explicit "%s" format string.
do_configure:prepend() {
    sed -i \
        -e 's/RCLCPP_ERROR(this->node_->get_logger(), e.what());/RCLCPP_ERROR(this->node_->get_logger(), "%s", e.what());/g' \
        ${S}/include/canopen_master_driver/node_interfaces/node_canopen_basic_master_impl.hpp
}
