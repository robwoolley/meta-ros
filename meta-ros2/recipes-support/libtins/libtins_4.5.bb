SUMMARY = "packet crafting and sniffing library"
DESCRIPTION = ""
DESCRIPTION = "High-level, multiplatform C++ network packet sniffing and crafting library."
HOMEPAGE = "http://libtins.github.io/"
SECTION = "examples"
DEPENDS = "libpcap openssl"
LICENSE = "BSD-2-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=dcaaaf1a01e7f9ceb200d383a0d4320c"

SRC_URI = "git://github.com/mfontanini/libtins;branch=master;protocol=https"
SRCREV = "142b6f62cb5eab87a99b3a21e546c5ef5bca5439"


inherit cmake

EXTRA_OECMAKE += "-DBUILD_SHARED_LIBS=ON"

# QA Issue: libtinsConfig.cmake / libtinsTargets.cmake contain references to TMPDIR [buildpaths]
# LIBTINS_INCLUDE_DIRS separately hardcodes the source tree's own include/
# path (${S}), not just ${RECIPE_SYSROOT}.
do_install:append() {
    sed -i -e "s#${RECIPE_SYSROOT}##g" \
        -e "s#${S}/include#${includedir}#g" \
        ${D}${libdir}/cmake/libtins/libtinsConfig.cmake \
        ${D}${libdir}/cmake/libtins/libtinsTargets.cmake
}
