# Copyright (c) 2023 Wind River Systems, Inc.

# CMake Error at CMakeLists.txt:37 (qt5_wrap_cpp):
#   Unknown CMake command "qt5_wrap_cpp".
inherit ${@bb.utils.contains_any('ROS_WORLD_SKIP_GROUPS', ['qt5', 'pyqt5'], '', 'cmake_qt5', d)}

ROS_BUILDTOOL_DEPENDS += " \
    rosidl-default-generators-native \
"

# error: extra ';' [-Werror=pedantic]
CXXFLAGS += "-Wno-error=pedantic"
# error: unused parameter 'foobar' [-Werror=unused-parameter]
CXXFLAGS += "-Wno-error=unused-parameter"
