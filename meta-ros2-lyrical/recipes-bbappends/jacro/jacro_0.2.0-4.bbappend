# Copyright (c) 2026 Wind River Systems, Inc.

# QA Issue: both installed scripts have a shebang pointing at the full
# native python3 path (>128 bytes); rewrite to the standard short form.
# Same fix as xacro.
do_install:append() {
    sed -i 's@^#!/.*python3@#!/usr/bin/env python3@g' ${D}${ros_libdir}/jacro/jacro
    sed -i 's@^#!/.*python3@#!/usr/bin/env python3@g' ${D}${ros_bindir}/jacro
}
