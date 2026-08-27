# Copyright (c) 2023 Wind River Systems, Inc.

FILESEXTRAPATHS:prepend := "${THISDIR}/${BPN}:"
SRC_URI += "file://add-lanelet2.patch"

LICENSE = "BSD-3-Clause"

EXTRA_OECMAKE += "-DCMAKE_POLICY_VERSION_MINIMUM=3.5"

# QA Issue: mrt_cmake_modules-extras.cmake in mrt-cmake-modules-dev contains reference to TMPDIR [buildpaths]
do_install:append() {
    sed -i -e "s#${RECIPE_SYSROOT}##g" \
        ${D}${ros_datadir}/mrt_cmake_modules/cmake/mrt_cmake_modules-extras.cmake
}
