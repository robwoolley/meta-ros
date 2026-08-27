# Copyright (c) 2026 Wind River Systems, Inc.

# QA Issue: _codec.cpython-314-x86_64-linux-gnu.so and
# _point_cloud_transport.cpython-314-x86_64-linux-gnu.so were already
# stripped, this will prevent future debugging! [already-stripped]
# Same situation as mrpt-random's pybind11 extension modules.
INSANE_SKIP:${PN} += "already-stripped"
