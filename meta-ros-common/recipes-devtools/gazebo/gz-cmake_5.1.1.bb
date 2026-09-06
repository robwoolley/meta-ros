# Copyright (c) 2024 Wind River Systems, Inc.
SUMMARY = "A set of CMake modules that are used by the C++-based Gazebo projects"
HOMEPAGE = "https://gazebosim.org/libs/cmake/"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=2a461be67a1edf991251f85f3aadd1d0"

SRC_URI = "git://github.com/gazebosim/gz-cmake.git;protocol=https;branch=gz-cmake5 \
           file://Fix-pkgconfig-install-dir.patch"

SRCREV = "b71b3794087cbee28decb7f9223d3c9441685b76"

inherit cmake

# FindGzOGRE.cmake (used by gz-rendering to locate classic OGRE 1.x) looks
# up pkg-config's search paths itself via `pkg-config --variable pc_path
# pkg-config` instead of going through CMake's own sysroot-aware PkgConfig
# module (which every other gz_pkg_check_modules_quiet() caller in this
# same tree correctly uses, and which does find OGRE's other cross-compiled
# dependencies fine). `pc_path` reports pkg-config's compiled-in default
# search path, which is unaffected by the PKG_CONFIG_LIBDIR/
# PKG_CONFIG_SYSROOT_DIR env vars OE sets for cross-compilation -- so on a
# cross build it only ever probes host-side paths, never the actual
# target sysroot's pkgconfig directory where OGRE.pc (and
# OGRE-RTShaderSystem.pc, OGRE-Terrain.pc, etc.) actually live, even though
# ogre_1.12.12.bb stages them there correctly. Silent symptom: gz-rendering
# configures "successfully" but with "-- Looking for GzOGRE - not found",
# silently dropping the ogre1 rendering backend (and, downstream, anything
# gz-sensors builds against it) rather than failing loudly.
#
# Fix by prepending PKG_CONFIG_LIBDIR's own directories to the path list
# this module searches, alongside whatever pkg-config's own default
# reports -- harmless for a native build, where PKG_CONFIG_LIBDIR is
# unset and this is a no-op.
do_configure:prepend() {
    if ! grep -q 'ENV{PKG_CONFIG_LIBDIR}' ${S}/cmake/FindGzOGRE.cmake; then
        sed -i \
            -e '/^  if ("\${PKG_CONFIG_PATH_TMP}" STREQUAL "")$/i\
  if(DEFINED ENV{PKG_CONFIG_LIBDIR})\
    set(PKG_CONFIG_PATH_TMP "$ENV{PKG_CONFIG_LIBDIR}:${PKG_CONFIG_PATH_TMP}")\
  endif()' \
            ${S}/cmake/FindGzOGRE.cmake
    fi
}

FILES:${PN} += "${datadir}/gz/gz-cmake/*"

FILES:${PN}-dev += "\
    pkgconfig/gz-cmake5.pc \
    ${includedir} \
    ${datadir}/cmake/gz-cmake/cmake/ \
    ${datadir}/gz/gz-cmake/ \
"

BBCLASSEXTEND = "native nativesdk"
