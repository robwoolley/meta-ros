# Copyright (c) 2026 Wind River Systems, Inc.

# Boost 1.90 removed the compiled boost_system stub library and its CMake
# package entirely -- it's been header-only since 1.69, and upstream
# projects generally just drop the component. See
# https://github.com/gnss-sdr/gnss-sdr/issues/972.
do_configure:prepend() {
    sed -i \
        -e 's/find_package(Boost CONFIG REQUIRED COMPONENTS thread system)/find_package(Boost CONFIG REQUIRED COMPONENTS thread)/' \
        -e 's/Boost::thread Boost::system)/Boost::thread)/' \
        -e 's/-lboost_thread -lboost_system/-lboost_thread/' \
        ${S}/CMakeLists.txt
}
