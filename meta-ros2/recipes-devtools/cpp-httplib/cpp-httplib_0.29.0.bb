LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=1321bdf796c67e3a8ab8e352dd81474b"

SRC_URI = "git://github.com/yhirose/cpp-httplib.git;protocol=https;branch=master"

# tag: v0.29.0
SRCREV = "5304464a53f793f8e3e4777ea5941d60cf8c91e3"

inherit cmake

EXTRA_OECMAKE = "-DHTTPLIB_TEST=OFF -DHTTPLIB_COMPILE=OFF"

# Header-only library — create empty base package for runtime
# dependency satisfaction
ALLOW_EMPTY:${PN} = "1"

BBCLASSEXTEND = "native nativesdk"
