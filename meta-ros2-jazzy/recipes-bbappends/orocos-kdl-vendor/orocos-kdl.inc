# Copyright (c) 2022 Wind River Systems, Inc.

HOMEPAGE = "https://orocos.org/kdl.html"

SRC_URI = " \
    git://github.com/ros2-gbp/orocos_kdl_vendor-release;name=release;${ROS_BRANCH};protocol=https \
    git://github.com/orocos/orocos_kinematics_dynamics.git;protocol=https;name=orocos-kdl;destsuffix=git/orocos-kdl;branch=master \
    "

SRCREV_release = "adca6201616af928533d70faea6042fe0239e898"
SRCREV_orocos-kdl = "ce4bcb65a050615b6d7f21bc60fbb2656515791b"
SRCREV_FORMAT = "release_orocos-kdl"
