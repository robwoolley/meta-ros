# Copyright (c) 2024 Wind River Systems, Inc.

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=2a461be67a1edf991251f85f3aadd1d0"

SRC_URI = "git://github.com/gazebosim/gz-gui.git;protocol=https;branch=gz-gui10"

SRCREV = "982993998efd8b5337578d8cd927d32818364532"

inherit cmake

# This resolves the following error:
#   To use a cross-compiled Qt, please set the QT_HOST_PATH cache variable to
#   the location of your host Qt installation.
inherit ${@bb.utils.contains('BBFILE_COLLECTIONS', 'qt6-layer', 'qt6-cmake', '', d)}

# EngineToQtInterface.cc's software-fallback texture-copy path (used when
# the Ogre render target can't be shared directly with the Qt Quick scene
# graph) passes GL_UNSIGNED_INT_8_8_8_8_REV to glTexSubImage2D via Qt's
# QOpenGLExtraFunctions. That constant is a desktop-GL-only enum (from the
# GL_EXT_bgra/OpenGL 1.2 extension); it isn't part of the GLES headers Qt6
# exposes when built for a GLES-only target (this MACHINE's GPU driver
# stack), so the compile fails with "was not declared in this scope".
# The enum's value (0x8367) is fixed by the Khronos OpenGL registry, so
# defining it locally when missing is safe and matches the standard
# portability idiom other GL/GLES-portable codebases use.
do_configure:prepend() {
    if ! grep -q '#define GL_UNSIGNED_INT_8_8_8_8_REV' ${S}/src/plugins/minimal_scene/EngineToQtInterface.cc; then
        sed -i \
            -e '/^#include <QOpenGLExtraFunctions>$/a\
\
#ifndef GL_UNSIGNED_INT_8_8_8_8_REV\
#define GL_UNSIGNED_INT_8_8_8_8_REV 0x8367\
#endif' \
            ${S}/src/plugins/minimal_scene/EngineToQtInterface.cc
    fi
}

DEPENDS = " \
    cppcheck-native \
    doxygen-native \
    gz-cmake \
    gz-common \
    gz-math \
    gz-msgs \
    gz-plugin \
    gz-rendering \
    gz-tools \
    gz-transport \
    gz-utils \
    protobuf \
    protobuf-native \
    libtinyxml2 \
    qtbase \
    qtdeclarative \
    qtdeclarative-native \
"
DEPENDS:append:class-target = "xserver-xorg"

EXTRA_OECMAKE += " \
    -DPROTOBUF_PROTOC_EXECUTABLE=${STAGING_BINDIR_NATIVE}/protoc \
"

FILES:${PN} += " \
    ${libdir}/gz-gui-10/plugins/* \
    ${libdir}/ruby/gz/* \
    ${datadir}/gz/* \
"

BBCLASSEXTEND = "native nativesdk"
