# Copyright (c) 2024 Wind River Systems, Inc.

inherit pkgconfig

# do_package QA Issue: _lttngpy_pybind11.cpython-314-x86_64-linux-gnu.so was
# already stripped -- pybind11's own CMake build strips its extension module
# at link time, conflicting with OE's separate strip/dbg-split step. Same
# fix already used for other pybind11-based bindings in this tree (e.g.
# rosbag2-py, rclpy, moveit-py).
INSANE_SKIP:${PN} += "already-stripped"
