# Copyright (c) 2026 Wind River Systems, Inc.

# Boost 1.90 removed the compiled boost_system stub library and its CMake
# package entirely -- it's been header-only since 1.69, and upstream
# projects generally just drop the component. See
# https://github.com/gnss-sdr/gnss-sdr/issues/972.
#
# Also: boost::asio::io_service was deprecated in Boost 1.66 in favor of
# io_context, and the compatibility alias was removed outright in this
# layer set's Boost 1.90 ("'io_service' is not a member of 'boost::asio'").
# io_context is a drop-in rename for every usage in this small package
# (plain construction, pass-by-reference into basic_serial_port), so a
# straight sed across every affected source/header file is sufficient --
# confirmed via a real build.
do_configure:prepend() {
    if ! grep -q "io_context" ${S}/src/hlds_laser_publisher.cpp; then
        sed -i -e 's/boost::asio::io_service/boost::asio::io_context/g' \
            ${S}/src/hlds_laser_publisher.cpp \
            ${S}/src/hlds_laser_segment_publisher.cpp \
            ${S}/include/hls_lfcd_lds_driver/lfcd_laser.hpp \
            ${S}/include/hls_lfcd_lds_driver/hlds_laser_segment_publisher.hpp \
            ${S}/applications/lds_driver/lds_driver.cpp \
            ${S}/applications/lds_driver/lds_driver.hpp \
            ${S}/applications/lds_polar_graph/lds_polar_graph.hpp
    fi

    sed -i \
        -e 's/find_package(Boost REQUIRED system)/find_package(Boost REQUIRED)/' \
        ${S}/CMakeLists.txt
}
