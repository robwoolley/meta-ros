# Copyright (c) 2026 Wind River Systems, Inc.

# Same bug and fix as ffw-joystick-controller (see that bbappend's
# comment for the full explanation): -Werror=missing-braces set directly
# in this package's own CMakeLists.txt trips on rosidl-generated
# *__traits.hpp's pedantically-flagged, but valid, single-brace
# std::array initialization.
do_configure:prepend() {
    sed -i -e '/-Werror=missing-braces/d' \
        ${S}/CMakeLists.txt
}
