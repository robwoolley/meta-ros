# Copyright (c) 2026 Wind River Systems, Inc.

# QA Issue: export_dynamixel_workbench_toolboxExport.cmake in dynamixel-workbench-toolbox-dev contains reference to TMPDIR [buildpaths]
do_install:append() {
    sed -i -e "s#${RECIPE_SYSROOT}##g" \
        ${D}${ros_datadir}/dynamixel_workbench_toolbox/cmake/export_dynamixel_workbench_toolboxExport.cmake
}
