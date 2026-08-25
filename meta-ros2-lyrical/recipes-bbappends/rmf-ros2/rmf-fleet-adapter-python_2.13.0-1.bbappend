# Copyright (c) 2021 LG Electronics, Inc.

DEPENDS += "\
    ament-cmake-gen-version-h \
    ament-package-native \
    python3-catkin-pkg-native \
"

FILES:${PN} += "${libdir}/python"

# Could NOT find PkgConfig (missing: PKG_CONFIG_EXECUTABLE)
inherit pkgconfig
