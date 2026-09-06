# Copyright (c) 2026 Wind River Systems, Inc.

# This recipe installs the RenderDoc in-application API header flatly at
# ${includedir}/renderdoc_app.h, but consumers of it (e.g. ogre-next's own
# CMake/Packages/FindRenderDoc.cmake: find_path(RenderDoc_INCLUDE_DIR NAMES
# renderdoc/renderdoc_app.h ...)) expect the Debian/Ubuntu packaging
# convention of nesting it under a renderdoc/ subdirectory instead --
# "-- Could NOT find RenderDoc (missing: RenderDoc_INCLUDE_DIR)" despite
# renderdoc being correctly staged and the header genuinely present.
# Symlink it into the expected location alongside the original rather than
# moving it, since other consumers may expect the flat path.
do_install:append() {
    install -d ${D}${includedir}/renderdoc
    ln -sf ../renderdoc_app.h ${D}${includedir}/renderdoc/renderdoc_app.h
}

FILES:${PN}-dev += "${includedir}/renderdoc"
