# Copyright (c) 2023 Wind River Systems, Inc.

inherit python3native

ROS_BUILDTOOL_DEPENDS = " \
    ament-copyright-native \
    ament-lint-cmake-native \
    ament-package-native \
    ament-xmllint-native \
"

ROS_BUILD_DEPENDS = " \
    ament-cmake-lint-cmake \
    ament-cmake-copyright \
    ament-cmake-core \
    ament-cmake-test \
    ament-cmake-xmllint \
"

# Remove ignition-cmake2 which is not needed at runtime
ROS_EXEC_DEPENDS:remove = "ignition-cmake2"
