# Copyright (c) 2026 Wind River Systems, Inc.

# Same bug and fix as ros2-control-cmake's -Werror=missing-braces removal
# (recipes-bbappends/ros2-control-cmake): rosidl-generated *__traits.hpp
# initializes its static constexpr std::array<std::string_view, N>
# member_names with a single-brace list, which is valid C++ via aggregate-
# initialization brace elision but triggers GCC's pedantic -Wmissing-braces
# on std::array's internal array member. Not a real bug in the generated
# code, just GCC being pedantic; this package sets the flag directly in
# its own CMakeLists.txt (unlike the ros2_control-derived packages, which
# get it from the shared ros2_control.cmake macro), so drop it here too.
do_configure:prepend() {
    sed -i -e '/-Werror=missing-braces/d' \
        ${S}/CMakeLists.txt
}
