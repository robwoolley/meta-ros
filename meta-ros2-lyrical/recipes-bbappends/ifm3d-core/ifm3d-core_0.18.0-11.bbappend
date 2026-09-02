# Copyright (c) 2020 LG Electronics, Inc.

FILESEXTRAPATHS:prepend := "${THISDIR}/${BPN}:"
SRC_URI += "file://0001-CMakeLists.txt-enable-C-language-to-fix-try_compile-.patch \
    file://0002-CMakeLists.txt-drop-s.patch \
"

# CMake Error at CMakeLists.txt:75 (add_subdirectory):
#   add_subdirectory given source \"/usr/src/gtest\" which is not an existing
#   directory.
EXTRA_OECMAKE += "-DBUILD_TESTS=OFF"

inherit ros_insane_dev_so

# Boost 1.90 removed the compiled boost_system stub library and its CMake
# package entirely -- it's been header-only since 1.69, and upstream
# projects generally just drop the component. See
# https://github.com/gnss-sdr/gnss-sdr/issues/972.
do_configure:prepend() {
    sed -i \
        -e 's/find_package(Boost REQUIRED COMPONENTS system)/find_package(Boost REQUIRED)/' \
        -e '/Boost::system/d' \
        ${S}/modules/pcicclient/src/libifm3d_pcicclient/CMakeLists.txt \
        ${S}/modules/framegrabber/src/libifm3d_framegrabber/CMakeLists.txt
}
