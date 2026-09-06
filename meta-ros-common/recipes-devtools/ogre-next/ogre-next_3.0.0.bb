# Copyright (c) 2023-2025 Wind River Systems, Inc.

LICENSE = "MIT & BSL-1.0 & CC-BY-3.0 & LGPL-2.1-or-later"
LIC_FILES_CHKSUM = "file://CMake/Templates/DemoLicense.rtf;md5=2711c49576d18cf781ec81aad76f40d6 \
                    file://COPYING;md5=65d1ee510d57bbd05663424f2ff8d660 \
                    file://Samples/Media/materials/textures/Cubemaps/License.txt;md5=81b3db517e68c27c535791b2276d5ffd \
                    file://Tools/Common/setup/License.rtf;md5=e1311ad52d6fe736b3819ce831a2a595 \
                    file://Tools/MilkshapeExport/setup/License.rtf;md5=e1311ad52d6fe736b3819ce831a2a595 \
                    file://Tools/XSIExport/setup/License.rtf;md5=e1311ad52d6fe736b3819ce831a2a595 \
                    file://Tools/MaterialEditor/wxscintilla_1.69.2/src/scintilla/License.txt;md5=d680acd8db69807fdfb587a342690eac\
"

SRC_URI = "git://github.com/OGRECave/ogre-next.git;protocol=https;branch=master \
           file://ogremathlibneon-type-fix.patch \
           file://0001-Fixed-compile-error-3.0.0.patch \
           file://0002-Use_OGRE_NEXT_prefix_for_libraries.patch \
           file://build-error-with-gcc15.patch \
           file://ogremathlibneon-add-header.patch \
           file://fix-ogremathlibneon-include-order.patch"


SRCREV = "75643c3997f5b6d2aa1d7bd8400b9be6736d9908"

inherit cmake features_check pkgconfig

DEPENDS = " \
    cppunit \
    doxygen-native \
    freeimage \
    freetype \
    glslang \
    libsdl2 \
    libtinyxml2 \
    libx11 \
    libxaw \
    libxcb \
    libxrandr \
    openvr \
    rapidjson \
    renderdoc \
    mesa \
    poco \
    shaderc \
    tbb \
    vulkan-headers \
    zlib \
    zziplib \
    ${@bb.utils.contains('DISTRO_FEATURES', 'opengl', 'virtual/libgl libglu', '', d)} \
"

EXTRA_OECMAKE += " \
    -DOGRE_USE_NEW_PROJECT_NAME=ON \
    -DOGRE_FULL_RPATH:BOOL=FALSE \
    -DOGRE_BUILD_RENDERSYSTEM_GL3PLUS:BOOL=TRUE \
    -DOGRE_BUILD_RENDERSYSTEM_GLES2:BOOL=FALSE \
    -DOGRE_BUILD_RENDERSYSTEM_VULKAN:BOOL=TRUE \
    -DOGRE_BUILD_COMPONENT_HLMS:BOOL=TRUE \
    -DOGRE_BUILD_COMPONENT_PLANAR_REFLECTIONS:BOOL=TRUE \
    -DOGRE_INSTALL_DOCS:BOOL=TRUE \
    -DOGRE_BUILD_SAMPLES2:BOOL=FALSE \
    -DOGRE_INSTALL_SAMPLES:BOOL=FALSE \
    -DOGRE_VULKAN_WINDOW_NULL:BOOL=TRUE \
    -DOGRE_CONFIG_UNIX_NO_X11:BOOL=FALSE \
    -DOGRE_GLSUPPORT_USE_EGL_HEADLESS:BOOL=TRUE \
    -DOGRE_GLSUPPORT_USE_GLX:BOOL=TRUE \
    -DOGRE_CONFIG_RENDERDOC_INTEGRATION:BOOL=FALSE \
"

# OGRE_CONFIG_RENDERDOC_INTEGRATION defaults to TRUE once RenderDoc_FOUND, but
# this ogre-next release's integration code is incompatible with the
# renderdoc_app.h shipped by our renderdoc recipe (1.38): OgreRenderSystem.h
# forward-declares "struct RENDERDOC_API_1_4_1;", while this header version
# instead aliases that name via "typedef RENDERDOC_API_1_6_0
# RENDERDOC_API_1_4_1;" -- a genuine struct-tag vs. typedef-name conflict, not
# an OE packaging issue. RenderDoc itself is still found and satisfies the
# optional-dependency check; only the broken runtime capture hook (unneeded
# for headless simulation) is disabled.

do_configure:append() {
    # Remove the old copy of glxext.h to use the system one that defines PFNGLXSWAPINTERVALMESAPROC
    #    https://gitlab.freedesktop.org/mesa/mesa/-/commit/cc93f08f1e3e84f09cb2bb587d6de702dc836478
    #
    # These resolves a build error when using GL3Plus:
    #   git/RenderSystems/GL3Plus/src/windowing/GLX/OgreGLXWindow.cpp:720:9: error: 'PFNGLXSWAPINTERVALMESAPROC'
    #       was not declared in this scope; did you mean 'PFNGLXSWAPINTERVALEXTPROC'?
    #   git/RenderSystems/GL3Plus/src/windowing/GLX/OgreGLXWindow.cpp:721:9: error: '_glXSwapInterval
    rm -f ${S}/RenderSystems/GL3Plus/include/GL/glxext.h
}

FILES:${PN}-dev += "${libdir}/OGRE-Next/cmake ${libdir}/OGRE-Next/*${SOLIBSDEV}"
FILES:${PN} += "${datadir}/OGRE-Next ${libdir}/OGRE-Next "

REQUIRED_DISTRO_FEATURES = "x11"
