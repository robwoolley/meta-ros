# Copyright (c) 2022 Acceleration Robotics, S.L.

# ERROR: rosidl-parser-3.1.3-1-r0 do_package: QA Issue: rosidl-parser: Files/directories were installed but not shipped in any package
FILES:${PN} = " \
    ${STAGING_DIR_NATIVE}${libdir}/python3.9/site-packages/rosidl_parser-3.1.3-py3.9.egg-info \
    ${STAGING_DIR_NATIVE}${libdir}/python3.9/site-packages/rosidl_parser \
"