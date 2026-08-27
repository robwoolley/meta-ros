# Copyright (c) 2021 LG Electronics, Inc.

DEPENDS += "\
    libtinyxml2 \
"

FILESEXTRAPATHS:prepend := "${THISDIR}/${BPN}:"
SRC_URI += "file://0001-CMakeLists-search-for-tinyxml2-instead-of-tinyxml.patch"

inherit pkgconfig

# QA Issue: menge_vendorExport.cmake in menge-vendor-dev contains reference to TMPDIR [buildpaths]
do_install:append() {
    sed -i -e "s#${RECIPE_SYSROOT}##g" \
        ${D}${ros_datadir}/menge_vendor/cmake/menge_vendorExport.cmake
}
