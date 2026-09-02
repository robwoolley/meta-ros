# Copyright (c) 2026 Wind River Systems, Inc.

# ros2_control.cmake's set_compiler_options() macro (shared by every
# ros2_controllers package -- gpio-controllers, joint-state-broadcaster,
# mecanum-drive-controller, etc.) adds -Werror=missing-braces
# unconditionally. Every rosidl-generated *__traits.hpp initializes its
# static constexpr std::array<std::string_view, N> member_names with a
# single-brace list (e.g. {"sec", "nanosec"}), which is valid C++ via
# aggregate-initialization brace elision but triggers GCC's pedantic
# -Wmissing-braces on std::array's internal array member. Not a real bug
# in the generated code, just GCC being pedantic about a well-defined
# initialization; drop it from the shared macro's flag list rather than
# patching every message package's generated headers (there's no
# per-message-type source to patch -- they're all freshly generated).
do_configure:prepend() {
    sed -i -e '/-Werror=missing-braces/d' \
        ${S}/cmake/ros2_control.cmake
}
