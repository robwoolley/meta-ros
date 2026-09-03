# Copyright (c) 2026 Wind River Systems, Inc.

# error: 'join' is not a member of 'fmt' -- the source uses fmt::join()
# (line uses fmt::format("{}", fmt::join(...))) but only includes
# <fmt/core.h> and <fmt/format.h>; fmt::join is declared in the separate
# <fmt/ranges.h> header. Confirmed this fmt version does provide it there
# (fmt/ranges.h defines join_view and a matching formatter). Two sibling
# tools from the same rosidlcpp-release monorepo
# (rosidlcpp-generator-type-description, rosidlcpp-generator-core) already
# include fmt/ranges.h correctly; this and 3 other tools built from
# different branches of the same repo are missing it.
do_configure:prepend() {
    # Guarded: do_configure:prepend() re-runs on every do_configure
    # invocation, not just after a fresh do_unpack/do_patch, and the
    # anchor line is left unmodified by the insertion, so an unguarded
    # re-run against an already-patched ${S} would insert a duplicate
    # #include.
    if ! grep -q '#include <fmt/ranges.h>' ${S}/src/rosidlcpp_generator_py/rosidlcpp_generator_py.cpp; then
        sed -i -e '/^#include <fmt\/format.h>$/a #include <fmt/ranges.h>' \
            ${S}/src/rosidlcpp_generator_py/rosidlcpp_generator_py.cpp
    fi
}
