# Copyright (c) 2019-2021 LG Electronics, Inc.

FILESEXTRAPATHS:prepend := "${THISDIR}/${BPN}:"
SRC_URI += "\
    file://0001-CMakeLists.txt-link-with-dl.patch \
    file://0001-FindLuaGoogle.cmake-explicitly-link-with-dl.patch \
    file://cmake.dont.add.Werror.uninitialized.patch \
    file://0001-Fix-build-with-gtest-1.8.1.patch \
    file://0002-CmakeLists.txt-set-C-version-to-C-14.patch \
    file://0001-FindLuaGoogle.cmake-accept-5.4-lua-as-well.patch \
    file://use-newer-abseil-api.patch \
"

inherit pkgconfig

# This is used only to generate documentation so it should
# be native and needs quite a lot of native python dependencies
ROS_BUILD_DEPENDS:remove = "python3-sphinx"

DEPENDS += "\
    protobuf-native \
"

# This -fuse-ld=gold workaround (for liblua.a linking with undefined
# references to dlsym, dlopen, dlerror, dlclose) no longer works at all on
# this toolchain: binutils here doesn't ship ld.gold, so it hard-fails with
# "collect2: fatal error: cannot find 'ld'" before even getting to test
# whether the original dl-linking issue still applies. The two dl-linking
# patches above (0001-CMakeLists.txt-link-with-dl.patch,
# 0001-FindLuaGoogle.cmake-explicitly-link-with-dl.patch) already address
# the named symbols directly; dropping the gold requirement to use this
# toolchain's actual default linker instead.

# Doesn't need runtime dependency on ceres-solver
ROS_EXEC_DEPENDS:remove = "ceres-solver"
