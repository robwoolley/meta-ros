# Copyright (c) 2026 Wind River Systems, Inc.

# QA Issue: export_phidgets_apiExport.cmake in phidgets-api-dev contains reference to TMPDIR [buildpaths]
#
# libphidget22-extras.cmake computes libphidget22's real install location
# (sysroot's /usr/include, /usr/lib -- outside the /opt/ros/lyrical prefix)
# via a "list(APPEND ... "${libphidget22_DIR}/../../../../../../usr/...")"
# relative-escape trick from its own ROS-prefixed cmake dir. That's fine
# when re-evaluated live, but phidgets_api's own install(EXPORT) bakes the
# *expanded* value in as a plain string, so after the sed above strips the
# ${RECIPE_SYSROOT} prefix it leaves a bare "/opt/ros/lyrical/share/
# libphidget22/cmake/../../.../usr/include"-style path with nothing to
# anchor it back into a consumer's own sysroot -- CMake errors with
# "includes non-existent path" for any of the 12 phidgets-* sensor
# packages that link phidgets_api. Re-anchor both entries (include dir and
# link library) to CMake's own ${CMAKE_SYSROOT}, which OE's toolchain file
# sets and which gets re-evaluated fresh in each consumer's own configure.
do_install:append() {
    sed -i \
        -e "s#${RECIPE_SYSROOT}##g" \
        -e "s#/opt/ros/lyrical/share/libphidget22/cmake/\.\./\.\./\.\./\.\./\.\./\.\./usr#\${CMAKE_SYSROOT}/usr#g" \
        ${D}${ros_datadir}/phidgets_api/cmake/export_phidgets_apiExport.cmake
}
