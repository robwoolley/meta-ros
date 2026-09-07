# Copyright (c) 2026 Wind River Systems, Inc.

# This recipe's own CMakeLists.txt shells out to "cargo build --release"
# directly as a custom build step (it isn't a normal bitbake `inherit cargo`
# recipe), and the superflore-generated ROS_BUILD_DEPENDS only lists "cargo"
# (the target/cross variant, staged into recipe-sysroot). That never puts a
# host-executable cargo binary on PATH, so do_compile fails with
# "/bin/sh: 1: cargo: not found". Add cargo-native, which stages the actual
# host toolchain into recipe-sysroot-native/usr/bin.
DEPENDS += "cargo-native"
