# Copyright (c) 2026 Wind River Systems, Inc.
# Recipe scaffolded with recipetool create, then finalized by hand: pinned
# to the v2.15.6 release tag (recipetool's git fetcher only accepts branch
# names, not tags, so the initial autorev-based scaffold needed the tag's
# commit hash filled in manually), dropped the "Unknown"-licensed
# glew-1.11.0 copy bundled under samples/thirdparty (only used by OpenVR's
# own sample programs, which this recipe never builds -- see below), and
# added a real SUMMARY/HOMEPAGE.
#
# Needed so ogre-next's own optional find_package(OpenVR) succeeds
# ("-- Could not locate OpenVR"); it's currently only consumed by
# ogre-next's Tutorial_OpenVR sample, which this distro already disables
# via OGRE_BUILD_SAMPLES2:BOOL=FALSE, so this closes the detection gap
# without changing what ogre-next itself ships.
SUMMARY = "Valve's OpenVR SDK for VR headset/controller access"
HOMEPAGE = "https://github.com/ValveSoftware/openvr"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=3f0ba432004f9cf11d8186503a8bd3d7"

SRC_URI = "git://github.com/ValveSoftware/openvr.git;protocol=https;branch=master"
SRCREV = "41bc3825fd35b04047610c86fee26fb33b017b29"

PV = "2.15.6"

inherit cmake

# Only src/ (the openvr_api library itself) is built; samples/ is never
# added as a subdirectory by the top-level CMakeLists.txt at all, so there
# is nothing to explicitly disable here.
EXTRA_OECMAKE = " \
    -DBUILD_SHARED=ON \
"

# Upstream's own CMake install rule names the pkgconfig file "openvr.pc"
# (lowercase), but ogre-next's CMake/Packages/FindOpenVR.cmake looks it up via
# pkg_check_modules(OpenVR_PKGC OpenVR) -- pkg-config resolves module names to
# an exact "<Name>.pc" filename, so on this case-sensitive filesystem that
# search for "OpenVR.pc" never matches "openvr.pc" even though the file is
# staged correctly with the right PKG_CONFIG_LIBDIR. Add a same-case alias
# symlink so both spellings resolve.
do_install:append() {
    ln -sf openvr.pc ${D}${datadir}/pkgconfig/OpenVR.pc
}

FILES:${PN}-dev += "${datadir}/pkgconfig"

# Upstream ships libopenvr_api.so unversioned (no SONAME/symlink chain), so it
# would otherwise be swept into -dev by the default FILES_SOLIBSDEV glob and
# fail do_package_qa's dev-elf check ("-dev package contains non-symlink
# .so"). Same fix already used in this tree for fmilibrary, another
# unversioned-.so upstream.
FILES_SOLIBSDEV = ""
FILES:${PN} += " \
    ${libdir}/lib*${SOLIBSDEV} \
"
