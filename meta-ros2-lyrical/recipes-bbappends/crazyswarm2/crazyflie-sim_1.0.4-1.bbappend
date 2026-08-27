# Copyright (c) 2026 Wind River Systems, Inc.

# QA Issue: crazyflie_server shebang points at the full native python3 path
# (>128 bytes); rewrite to the standard short form. Same fix as xacro.
do_install:append() {
    sed -i 's@^#!/.*python3@#!/usr/bin/env python3@g' ${D}${ros_libdir}/crazyflie_sim/crazyflie_server
}
