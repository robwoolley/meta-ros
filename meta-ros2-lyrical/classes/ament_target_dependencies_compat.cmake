# Copyright (c) 2026 Wind River Systems, Inc.
#
# In lyrical (and rolling), the ament_cmake metapackage no longer hard-depends
# on ament_cmake_target_dependencies -- it's now only a build_export_depend,
# so plain find_package(ament_cmake REQUIRED) no longer transitively loads
# the ament_target_dependencies() macro the way it used to in older distros.
# Upstream's own guidance for packages that still call
# ament_target_dependencies() is to find_package() this directly (see
# https://github.com/ament/ament_cmake/pull/572), but most of the ROS
# ecosystem's CMakeLists.txt files haven't been updated for this yet.
# Injected via CMAKE_PROJECT_INCLUDE (see ros_distro_lyrical.bbclass) so
# every ament_cmake ROS package gets it without needing 25+ individual
# upstream patches. Harmless no-op for packages that don't need it.
find_package(ament_cmake_target_dependencies QUIET)
