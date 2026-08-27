# Copyright (c) 2026 Wind River Systems, Inc.

# QA Issue: export_phidgets_apiExport.cmake in phidgets-api-dev contains reference to TMPDIR [buildpaths]
do_install:append() {
    sed -i -e "s#${RECIPE_SYSROOT}##g" \
        ${D}${ros_datadir}/phidgets_api/cmake/export_phidgets_apiExport.cmake
}
