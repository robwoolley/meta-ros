# Copyright (c) 2026 Wind River Systems, Inc.

# package.xml declares srv/InterfaceVersion.srv (via rosidl_generate_interfaces())
# but never depends on service_msgs, which every ROS2 service definition
# implicitly needs. This is a separate, fatal *configure-time* bug from the
# already-fixed link-time "file in wrong format" symptom that the
# action-msgs/service-msgs/unique-identifier-msgs DEPENDS-staging fix in
# ros_distro_lyrical.bbclass addresses (that fix stages the target-arch
# package into the sysroot for linking; it doesn't make CMakeLists.txt call
# find_package(service_msgs) in the first place). rosidl_generate_interfaces.cmake
# enforces this directly at configure time: "Unable to generate service
# interface for 'srv/InterfaceVersion.srv' ... you must add a depend tag for
# 'service_msgs' in your package.xml" -- genuine upstream package.xml bug
# (confirmed: no service_msgs depend at all), fixed the same way any other
# missing dependency tag would be.
do_configure:prepend() {
    sed -i -e '/rosidl_default_generators<\/build_depend>/a\  <depend>service_msgs</depend>' \
        ${S}/package.xml
}
