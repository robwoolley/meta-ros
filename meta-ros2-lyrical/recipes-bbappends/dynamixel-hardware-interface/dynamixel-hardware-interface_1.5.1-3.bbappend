# Copyright (c) 2026 Wind River Systems, Inc.

# QA Issue: create_udev_rules requires /bin/bash, but no providers found
# in RDEPENDS:dynamixel-hardware-interface [file-rdeps]
RDEPENDS:${PN} += "bash"

# QA Issue: export_dynamixel_hardware_interfaceExport.cmake in
# dynamixel-hardware-interface-dev contains reference to TMPDIR [buildpaths]
do_install:append() {
    sed -i -e "s#${RECIPE_SYSROOT}##g" \
        ${D}${ros_datadir}/dynamixel_hardware_interface/cmake/export_dynamixel_hardware_interfaceExport.cmake
}
