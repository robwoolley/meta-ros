# Copyright (c) 2019 LG Electronics, Inc.
# Copyright (c) 2023-2025 Wind River Systems, Inc.

ROS_BUILDTOOL_DEPENDS += "\
    python3-empy-native \
    python3-numpy-native \
    rcutils-native \
"

# ld: .../tmp-glibc/work/riscv64-oe-linux/rclcpp-components/28.1.5-1-r0/recipe-sysroot/opt/ros/jazzy/lib/librclcpp.so: undefined reference to `__atomic_exchange_1'
LDFLAGS:append:riscv64 = " -Wl,--no-as-needed -latomic -Wl,--as-needed"
LDFLAGS:append:riscv32 = " -Wl,--no-as-needed -latomic -Wl,--as-needed"

# exceptions.hpp:71:22: error: declaration of 'invalid_index' shadows a member
# of 'rclcpp::exceptions::InvalidNodeNameError' [-Werror=shadow] (and same for
# 'error_msg', and for the sibling InvalidNamespaceError/InvalidTopicNameError/
# InvalidServiceNameError classes). These four derived-class constructors use
# bare parameter names (error_msg, invalid_index) that collide with
# NameValidationError's own member names of the same name -- meanwhile the
# base class's own constructor already avoids this by using a trailing
# underscore (error_msg_, invalid_index_) for its parameters, per the
# convention used throughout the rest of this header. Fixed in this origin
# header (not suppressed per-consumer) since it's a shared, widely-included
# rclcpp header: affects any consumer built with -Werror=shadow, at least the
# 24-recipe ros2_control_cmake family (its shared macro already sets
# -Werror=shadow) plus others such as osrf-testing-tools-cpp.
do_configure:prepend() {
    sed -i \
        -e 's/InvalidNodeNameError(const char \* node_name, const char \* error_msg, size_t invalid_index)/InvalidNodeNameError(const char * node_name, const char * error_msg_, size_t invalid_index_)/' \
        -e 's/: NameValidationError("node name", node_name, error_msg, invalid_index)/: NameValidationError("node name", node_name, error_msg_, invalid_index_)/' \
        -e 's/InvalidNamespaceError(const char \* namespace_, const char \* error_msg, size_t invalid_index)/InvalidNamespaceError(const char * namespace_, const char * error_msg_, size_t invalid_index_)/' \
        -e 's/: NameValidationError("namespace", namespace_, error_msg, invalid_index)/: NameValidationError("namespace", namespace_, error_msg_, invalid_index_)/' \
        -e 's/InvalidTopicNameError(const char \* namespace_, const char \* error_msg, size_t invalid_index)/InvalidTopicNameError(const char * namespace_, const char * error_msg_, size_t invalid_index_)/' \
        -e 's/: NameValidationError("topic name", namespace_, error_msg, invalid_index)/: NameValidationError("topic name", namespace_, error_msg_, invalid_index_)/' \
        -e 's/InvalidServiceNameError(const char \* namespace_, const char \* error_msg, size_t invalid_index)/InvalidServiceNameError(const char * namespace_, const char * error_msg_, size_t invalid_index_)/' \
        -e 's/: NameValidationError("service name", namespace_, error_msg, invalid_index)/: NameValidationError("service name", namespace_, error_msg_, invalid_index_)/' \
        ${S}/include/rclcpp/exceptions/exceptions.hpp
}
