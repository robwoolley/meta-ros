# Copyright (c) 2022-2023 Wind River Systems, Inc.

LICENSE = "BSD-3-Clause"

FILESEXTRAPATHS:prepend := "${THISDIR}/${BPN}:"
SRC_URI += "file://add-cstdint.patch"

ROS_BUILD_DEPENDS += " \
    service-msgs \
"

ROS_BUILDTOOL_DEPENDS = " \
    ament-cmake-ros-native \
    python3-numpy-native \
"
