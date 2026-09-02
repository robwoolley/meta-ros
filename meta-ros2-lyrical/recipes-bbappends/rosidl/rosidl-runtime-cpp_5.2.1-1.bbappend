ROS_BUILD_DEPENDS += "ament-cmake-ros-core"

# traits.hpp's value_to_yaml(const std::u16string&) uses
# std::wstring_convert<std::codecvt_utf8_utf16<...>>, deprecated by the
# C++17 standard itself (not a project-specific warning) -- fails any
# consumer built with -Werror=deprecated-declarations or a blanket
# -Werror. Upstream hasn't fixed this yet (as of this writing their own
# discussion only mentions an MSVC-specific silencing macro, see
# https://docs.ros.org/en/humble/p/rosidl_runtime_cpp/generated/program_listing_file_include_rosidl_runtime_cpp_traits.hpp.html),
# so fix it directly: the wstring_convert is only ever reached in the
# ASCII branch (guarded by `!(character & 0xff80)`, i.e. code points
# 0x00-0x7F), where UTF-8 encoding is trivially the identity byte and
# to_bytes() does nothing a plain char cast doesn't already do -- and the
# surrounding `std::hex` was already a no-op here since it was streaming
# a std::string, not an integer. Same observable output, no deprecated
# API.
do_configure:prepend() {
    sed -i \
        -e '/^#include <codecvt>$/d' \
        -e '/std::wstring_convert<std::codecvt_utf8_utf16<char16_t>, char16_t> convert;/d' \
        -e 's/std::string character_as_string = convert.to_bytes(character);/out << static_cast<char>(character);/' \
        -e '/out << std::hex << character_as_string.c_str();/d' \
        ${S}/include/rosidl_runtime_cpp/traits.hpp
}
