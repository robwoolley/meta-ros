# Copyright (c) 2026 Wind River Systems, Inc.

# QA Issue: _image_transport.cpython-314-x86_64-linux-gnu.so was already
# stripped, this will prevent future debugging! [already-stripped]
# Same situation as mrpt-random's pybind11 extension module.
INSANE_SKIP:${PN} += "already-stripped"
