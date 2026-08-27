# Copyright (c) 2022-2023 Wind River Systems, Inc.

ROS_BUILD_DEPENDS += "eigen3-cmake-module"

ROS_BUILDTOOL_DEPENDS += "\
    rosidl-default-generators-native \
"

# Doesn't need runtime dependency on nlohmann-json
ROS_EXEC_DEPENDS:remove = "nlohmann-json"

inherit pkgconfig

# QA Issue: export_rmf_traffic_ros2Export.cmake in rmf-traffic-ros2-dev contains reference to TMPDIR [buildpaths]
do_install:append() {
    sed -i -e "s#${RECIPE_SYSROOT}##g" \
        ${D}${ros_datadir}/rmf_traffic_ros2/cmake/export_rmf_traffic_ros2Export.cmake
}
