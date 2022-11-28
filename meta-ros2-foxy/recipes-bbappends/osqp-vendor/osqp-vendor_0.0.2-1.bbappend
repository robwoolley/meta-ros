# Copyright (c) 2020 LG Electronics, Inc.

# Instead of fetching
# https://github.com/oxfordcontrol/osqp.git from tag v0.6.1.dev0
# during do_compile
FILESEXTRAPATHS:prepend := "${THISDIR}/${BPN}:"
SRC_URI += "file://0002-CMakeLists.txt-fetch-osqp-with-bitbake-fetcher.patch \
    git://github.com/oxfordcontrol/osqp.git;protocol=https;name=osqp;destsuffix=git/osqp-upstream;branch=master \
    git://github.com/oxfordcontrol/qdldl.git;protocol=https;name=qdldl;destsuffix=git/osqp-upstream/lin_sys/direct/qdldl/qdldl_sources;branch=master \
"
SRCREV_osqp = "c536acf012d071eed3529e0147afdd419348362a"
SRCREV_qdldl = "12d56ee6b9494efee1da1e1dc6e7ac9c226f9ccf"
