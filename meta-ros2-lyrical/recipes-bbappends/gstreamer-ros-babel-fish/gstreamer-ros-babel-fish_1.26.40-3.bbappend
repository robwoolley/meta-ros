# Copyright (c) 2026 Wind River Systems, Inc.

# CMake Error: pkg-config tool not found / Could NOT find PkgConfig -- the
# recipe never sets up PKG_CONFIG_EXECUTABLE unless pkgconfig.bbclass is
# inherited (it stages pkg-config-native and points CMake's FindPkgConfig
# at it).
inherit pkgconfig
