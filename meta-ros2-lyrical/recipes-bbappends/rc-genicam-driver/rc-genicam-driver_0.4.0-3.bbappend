# Copyright (c) 2021 LG Electronics, Inc.

# depends on rc-genicam-api, use the same restriction
COMPATIBLE_MACHINE = "(^$)"
COMPATIBLE_MACHINE:x86-64 = "(.*)"
COMPATIBLE_MACHINE:x86 = "(.*)"
COMPATIBLE_MACHINE:aarch64 = "(.*)"
COMPATIBLE_MACHINE:armv7a = "${@bb.utils.contains('TUNE_FEATURES', 'callconvention-hard', '(^$)', '(.*)', d)}"
COMPATIBLE_MACHINE:armv7ve = "${@bb.utils.contains('TUNE_FEATURES', 'callconvention-hard', '(^$)', '(.*)', d)}"

# RC_PROJECT_VERSION is set in cmake/project_version.cmake by git describe checking for 'v?([0-9.]+).*' tag
# but in roboception-gbp/rc_genicam_api-release repo there are no suitable tags:
# rc-genicam-driver/0.1.3-1-r0/git$ git describe --all
# tags/release/rolling/rc_genicam_driver/0.1.3-1
# so it ends 0.0.0 triggering an warning:
# -- PACKAGE_XML_VERSION: 0.1.3
# CMake Warning at cmake/project_version.cmake:137 (message):
#   Version from package.xml (0.1.3) doesn't match RC_PROJECT_VERSION (0.0.0)
RC_PROJECT_VERSION = "${@'${PV}'.split('-')[0]}"

EXTRA_OECMAKE += "-DRC_PROJECT_VERSION=${RC_PROJECT_VERSION}"

# CMake Error: format not a string literal and no format arguments
# [-Werror=format-security] -- passing a runtime string (ex.what()) as the
# format argument itself, rather than as a %s argument, is a genuine
# format-string footgun (if the message ever contained a literal '%' it
# would be misinterpreted as a format specifier), not a false positive.
# Standard fix: pass an explicit "%s" format string.
do_configure:prepend() {
    sed -i \
        -e 's/RCLCPP_WARN(this->get_logger(), ex.what());/RCLCPP_WARN(this->get_logger(), "%s", ex.what());/g' \
        -e 's/RCLCPP_ERROR(this->get_logger(), ex.what());/RCLCPP_ERROR(this->get_logger(), "%s", ex.what());/g' \
        -e 's/RCLCPP_FATAL(this->get_logger(), ex.what());/RCLCPP_FATAL(this->get_logger(), "%s", ex.what());/g' \
        ${S}/src/genicam_driver.cpp
}
