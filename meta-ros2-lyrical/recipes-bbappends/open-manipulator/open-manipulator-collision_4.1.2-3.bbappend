# Copyright (c) 2026 Wind River Systems, Inc.

# QA Issue: ament_cmake_export_include_directories-extras.cmake in
# open-manipulator-collision-dev contains reference to TMPDIR [buildpaths]
do_install:append() {
    sed -i -e "s#${RECIPE_SYSROOT}##g" \
        ${D}${ros_datadir}/open_manipulator_collision/cmake/ament_cmake_export_include_directories-extras.cmake
}
