# Copyright (c) 2024 Wind River Systems, Inc.
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=2a461be67a1edf991251f85f3aadd1d0 \
                    file://tpe/lib/src/aabb_tree/LICENSE;md5=fd0ac4e17e55ad320e9429c05b5c23c7"

SRC_URI = "git://github.com/gazebosim/gz-physics.git;protocol=https;branch=gz-physics9"

SRCREV = "df50665d7a6eee58ef08eb6534685c7ae609c637"


# urdfdom is needed directly (not just transitively via gz-dartsim-vendor's
# own dartsim recipe, which already depends on it): DARTConfig.cmake's
# dart_utils-urdfComponent.cmake calls find_package(urdfdom QUIET CONFIG)
# itself when gz-physics requests DART's "utils-urdf" component, so
# urdfdom-config.cmake has to be staged in gz-physics's own sysroot too --
# same "grandparent doesn't inherit a dependency's own staging" shape as
# elsewhere in this tree. Without it: "Cannot retrieve dart-utils-urdf
# because the dependency urdfdom could not be found" and the whole dartsim
# physics engine plugin gets silently skipped.
DEPENDS += " \
    gz-cmake \
    gz-common \
    gz-dartsim-vendor \
    gz-math \
    gz-plugin \
    gz-utils \
    protobuf \
    sdformat \
    libeigen \
    bullet \
    cppcheck-native \
    google-benchmark-vendor \
    urdfdom \
"

EXTRA_OECMAKE += " -DBUILD_TESTING=OFF"

inherit cmake

FILES:${PN} += " \
    ${libdir}/gz-physics-9/engine-plugins/lib*${SOLIBS} \
"

FILES:${PN}-dev += " \
    ${libdir}/gz-physics-9/engine-plugins/lib*${SOLIBSDEV} \
"
