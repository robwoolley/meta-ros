# Optional OE DISTRO settings for a ROS_DISTRO-aware distro, common to both ROS 1 and ROS 2 distros.
# Not required by anything in meta-ros itself: require this from your own conf/distro/<DISTRO>.conf
# to pick up ROS_DISTRO-derived DISTRO_CODENAME/DISTRO_VERSION and a default network manager for ROS
# images. All assignments are weak so they don't override values your distro.conf already set.
#
# Copyright (c) 2019-2021 LG Electronics, Inc.

DISTRO_NAME ?= "${ROS_OE_DISTRO_NAME} with OpenEmbedded Linux"
DISTRO_CODENAME ?= "${ROS_DISTRO}"

ROS_DISTRO_VERSION_APPEND ??= ""
DISTRO_VERSION ?= "${ROS_DISTRO_METADATA_VERSION}${ROS_DISTRO_VERSION_APPEND}"

# Without this, networking has to be started manually.
ROS_CONNECTION_MANAGER ??= "connman"
IMAGE_INSTALL:append = " \
    ${ROS_CONNECTION_MANAGER} \
"
