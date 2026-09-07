# Copyright (c) 2026 Wind River Systems, Inc.
# Recipe scaffolded with recipetool create against the pycollada PyPI
# package.
#
# Needed so webots-ros2-importer's ROS_UNRESOLVED_DEP-python3-collada
# resolves to a real recipe -- previously left mapped to "" (deliberately
# dropped) since no python3-collada recipe existed, forcing the
# webots-python-modules skip group. The other originally-cited gap
# (urdf2webots-pip) is no longer needed at all: webots_ros2's current
# 2025.0.1 release dropped that dependency from webots-ros2-importer's
# package.xml.
SUMMARY = "python library for reading and writing collada documents"
HOMEPAGE = "http://pycollada.readthedocs.org/"
LICENSE = "BSD"
LIC_FILES_CHKSUM = "file://COPYING;md5=23ee71032e9f461d31ec438d4e417e23"

SRC_URI[sha256sum] = "c34d6dcf0fe2eba5896f71c96d37a1c0fe1a61f08440fa0cfcec3dc2895d3302"

inherit pypi python_setuptools_build_meta

PYPI_PACKAGE = "pycollada"

RDEPENDS:${PN} += " \
    python3-dateutil \
    python3-numpy \
"
