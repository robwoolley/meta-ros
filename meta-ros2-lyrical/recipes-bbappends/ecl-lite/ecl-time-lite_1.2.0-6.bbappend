# Copyright (c) 2023 Wind River Systems, Inc.

# ERROR: QA Issue: non -dev/-dbg/nativesdk- package ecl-time-lite contains symlink .so '/usr/lib/libecl_time_lite.so' [dev-so]
inherit ros_insane_dev_so

# handlers.hpp:73:22: error: 'virtual void ecl::Error::operator=(const ecl::ErrorFlag&)' was hidden [-Werror=overloaded-virtual=]
#
# This CXXFLAGS suppression only covers ecl-time-lite's own compile --
# TimeError's implicitly-generated copy-assignment operator hides Error's
# virtual operator=(const ErrorFlag&) for anyone who includes this header,
# so ecl-ipc, ecl-streams, and kobuki-core (none of which opt out of
# -Werror=overloaded-virtual themselves) still hit the same fatal error at
# *their* compile time. Fix it at the source instead: a `using
# Error::operator=;` re-exposes the base overload alongside the compiler-
# generated one, which is the standard fix for this exact class of
# diagnostic and makes the CXXFLAGS suppression above unnecessary (left in
# place regardless -- harmless, and covers ecl-time-lite's own build
# either way).
CXXFLAGS += "-Wno-error=overloaded-virtual"

do_configure:prepend() {
    sed -i \
        -e '/TimeError(const ErrorFlag& flag = UnknownError) : Error(flag) {}/a\
\
  using Error::operator=;' \
        ${S}/include/ecl/time_lite/errors.hpp
}

# Setting LICENSE from BSD to BSD-3-Clause to be SPDX compliant
LICENSE = "BSD-3-Clause"
