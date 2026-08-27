# Copyright (c) 2026 Wind River Systems, Inc.

# QA Issues: replay_test's shebang points at the full native python3 path,
# which is simultaneously flagged as [shebang-size] (>128 bytes),
# [buildpaths] (embeds TMPDIR), and [file-rdeps] (the automatic RDEPENDS
# scanner doesn't resolve a raw recipe-sysroot-native path to a real
# package). Rewriting to the standard short form fixes all three at once,
# same fix as xacro.
do_install:append() {
    sed -i 's@^#!/.*python3@#!/usr/bin/env python3@g' ${D}${ros_libdir}/replay_testing/replay_test
}
