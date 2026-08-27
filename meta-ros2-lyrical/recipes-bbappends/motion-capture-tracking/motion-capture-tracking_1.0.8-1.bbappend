# Copyright (c) 2026 Wind River Systems, Inc.

# QA Issue: motion_capture_tracking_node requires liblibrigidbodytracker.so
# and liblibmotioncapture.so, but no providers found [file-rdeps]. Upstream
# names these internal, EXCLUDE_FROM_ALL vendored CMake targets
# "librigidbodytracker"/"libmotioncapture" (with "lib" baked into the target
# name itself), so CMake's default naming convention doubles the prefix to
# liblibrigidbodytracker.so/liblibmotioncapture.so. They're built and
# RPATH'd as part of this same recipe; the automatic shlib provider scanner
# just doesn't recognize the doubled-lib pattern.
INSANE_SKIP:${PN} += "file-rdeps"
