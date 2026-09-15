# Copyright (c) 2026 Wind River Systems, Inc.

# rmw_test_fixture_implementation (run_rmw_isolated) is only invoked by the
# ament_add_ros_isolated_{test,gtest,gmock,pytest} CMake macros this package
# ships. Every ROS ament_cmake recipe in this distro is built with
# -DBUILD_TESTING=OFF unconditionally (see EXTRA_OECMAKE:append in
# ros_ament_cmake.bbclass), so those macros are never reached and
# run_rmw_isolated is never executed during any *-native package's build.
#
# Left alone, this RDEPENDS entry gets suffixed -native by native.bbclass for
# every recipe that uses ament-cmake-ros as a buildtool (nearly the whole
# distro), which forces rmw-test-fixture-implementation-native to exist,
# which in turn forces the entire rmw_implementation backend matrix
# (rmw-fastrtps-cpp, rmw-fastrtps-dynamic-cpp, rmw-cyclonedds-cpp,
# rmw-zenoh-cpp, and via it a native Rust/Clang toolchain for
# zenoh-cpp-vendor) plus tracetools/lttng-ust to exist as -native recipes,
# purely for bookkeeping that no native build step actually uses.
ROS_EXEC_DEPENDS:remove:class-native = "rmw-test-fixture-implementation"
