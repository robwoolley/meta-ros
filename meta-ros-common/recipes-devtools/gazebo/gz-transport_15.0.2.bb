# Copyright (c) 2024 Wind River Systems, Inc.
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=2a461be67a1edf991251f85f3aadd1d0"

SRC_URI = "git://github.com/gazebosim/gz-transport.git;protocol=https;branch=gz-transport15"

SRCREV = "05cd874ac9a1b33a7777a74bc8a2960f3429f267"

inherit cmake pkgconfig python3targetconfig

# zenoh-c/zenoh-cpp enable gz-transport's optional Zenoh backend
# (gz_find_package(zenohc)/gz_find_package(zenohcxx) in its CMakeLists.txt,
# CONFIG-mode find_package under those exact names) alongside the always-on
# ZeroMQ one. Same recipes and unversioned PN already used for this exact
# purpose by meta-ros2-lyrical's zenoh-cpp-vendor bbappend (rmw_zenoh's own
# vendor package) -- version is resolved via meta-zenoh's own
# PREFERRED_VERSION_zenoh-c/zenoh-cpp (currently 1.10.0), so it isn't pinned
# here.
DEPENDS = " \
    gz-cmake \
    gz-msgs \
    gz-utils \
    sqlite3 \
    util-linux-libuuid \
    zeromq \
    cppzmq \
    zenoh-c \
    zenoh-cpp \
    doxygen-native \
    graphviz-native \
    protobuf \
    protobuf-native \
    cppcheck-native \
    python3-pytest-native \
"

RDEPENDS:${PN} += "ruby"

EXTRA_OECMAKE += " \
    -DPROTOBUF_PROTOC_EXECUTABLE=${STAGING_BINDIR_NATIVE}/protoc \
"

FILES:${PN} += " \
  ${libdir}/ruby/gz \
  ${libdir}/python/gz \
  ${datadir}/gz \
"

BBCLASSEXTEND = "native nativesdk"
