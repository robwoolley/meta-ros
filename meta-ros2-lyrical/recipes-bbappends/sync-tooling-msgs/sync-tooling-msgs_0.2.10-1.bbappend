# Copyright (c) 2026 Wind River Systems, Inc.

# QA Issue: export_sync_tooling_msgsExport.cmake in sync-tooling-msgs-dev contains reference to TMPDIR [buildpaths]
do_install:append() {
    sed -i -e "s#${RECIPE_SYSROOT}##g" \
        ${D}${ros_datadir}/sync_tooling_msgs/cmake/export_sync_tooling_msgsExport.cmake
}
