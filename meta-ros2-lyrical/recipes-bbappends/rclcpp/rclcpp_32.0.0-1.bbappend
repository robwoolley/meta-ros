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
    # Each sed block below is guarded by a grep -q check for its own
    # end-state, making it safe to run more than once against the same
    # already-patched ${S} -- do_configure:prepend() re-runs on every
    # do_configure invocation, not just after a fresh do_unpack/do_patch,
    # and these substitutions are substring-based (e.g. "mask.include_subscription"
    # matches inside the already-fixed "inner_mask.include_subscription"),
    # so an unguarded second run silently double-applies them
    # ("inner_inner_mask", "locked_locked_subscription_topic_stats" --
    # discovered when a later do_configure re-run on a still-populated
    # ${S} corrupted a prior successful build's source tree, breaking
    # compilation with "was not declared in this scope").
    if ! grep -q 'error_msg_' ${S}/include/rclcpp/exceptions/exceptions.hpp; then
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
    fi

    # wait_set_template.hpp:143/221: the lambdas passed to
    # sync_add_subscription/sync_remove_subscription redeclare a "mask"
    # parameter that shadows add_subscription/remove_subscription's own
    # "mask" parameter -- every sibling method here (add_timer, add_client,
    # add_service, add_guard_condition, add_waitable, etc.) already renames
    # its lambda's parameter with an "inner_" prefix to avoid exactly this;
    # only the subscription pair's *second* lambda parameter (mask) and
    # add_waitable's second lambda parameter (associated_entity) were missed
    # upstream. Same fix, same convention already used throughout this file.
    if ! grep -q 'inner_mask' ${S}/include/rclcpp/wait_set_template.hpp; then
        sed -i \
            -e 's/const rclcpp::SubscriptionWaitSetMask \& mask)/const rclcpp::SubscriptionWaitSetMask \& inner_mask)/' \
            -e 's/mask\.include_subscription/inner_mask.include_subscription/g' \
            -e 's/mask\.include_events/inner_mask.include_events/g' \
            -e 's/mask\.include_intra_process_waitable/inner_mask.include_intra_process_waitable/g' \
            -e 's/std::shared_ptr<void> \&\& associated_entity)/std::shared_ptr<void> \&\& inner_associated_entity)/' \
            -e 's/std::move(associated_entity));/std::move(inner_associated_entity));/' \
            ${S}/include/rclcpp/wait_set_template.hpp
    fi

    # create_subscription.hpp:101: sub_call_back's lambda declares a local
    # "subscription_topic_stats" that shadows the enclosing function's local
    # of the same name (the classic weak_ptr::lock()-into-same-name pattern).
    # Renamed the lambda-local copy; the enclosing scope's original is
    # untouched.
    if ! grep -q 'locked_subscription_topic_stats' ${S}/include/rclcpp/create_subscription.hpp; then
        sed -i \
            -e 's/auto subscription_topic_stats = weak_subscription_topic_stats.lock();/auto locked_subscription_topic_stats = weak_subscription_topic_stats.lock();/' \
            -e 's/if (subscription_topic_stats) {/if (locked_subscription_topic_stats) {/' \
            -e 's/subscription_topic_stats->publish_message_and_reset_measurements();/locked_subscription_topic_stats->publish_message_and_reset_measurements();/' \
            ${S}/include/rclcpp/create_subscription.hpp
    fi
}
