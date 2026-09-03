# Copyright (c) 2026 Wind River Systems, Inc.

# Same bug as autoware-adapi-version-msgs (see that bbappend's comment for
# the full explanation): package.xml has a .srv file but never depends on
# service_msgs, so rosidl_generate_interfaces.cmake fatals at configure time
# with "you must add a depend tag for 'service_msgs' in your package.xml".
# Genuine upstream package.xml bug; fix by adding the missing tag next to
# the existing ROS_VERSION==2-conditioned rosidl_default_generators depend.
do_configure:prepend() {
    sed -i -e '/rosidl_default_generators<\/build_depend>/a\  <depend>service_msgs</depend>' \
        ${S}/package.xml
}
