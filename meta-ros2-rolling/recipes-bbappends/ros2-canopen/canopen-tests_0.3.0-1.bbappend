# Copyright (c) 2025 Wind River Systems, Inc.

inherit python3native

DEPENDS += "lely-core-libraries-native lely-core-native"

# cogen is located in /opt/ros/<DISTRO>/bin
do_compile:prepend:class-target() {
    export PATH=${STAGING_DIR_NATIVE}${ros_bindir}:$PATH
}
