# Copyright (c) 2026 Wind River Systems, Inc.

# QA Issue: all 13 installed example scripts have a shebang pointing at the
# full native python3 path (>128 bytes); rewrite to the standard short form.
# Same fix already used for xacro.
do_install:append() {
    for script in nice_hover set_param simple_mapper_multiranger arming \
        infinite_flight multi_trajectory goto_unicast cmd_full_state swap \
        figure8 group_mask vel_mux hello_world; do
        sed -i 's@^#!/.*python3@#!/usr/bin/env python3@g' ${D}${ros_libdir}/crazyflie_examples/${script}
    done
}
