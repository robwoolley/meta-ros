# Every ROS recipe, generated or not, must contain "inherit ros_distro_${ROS_DISTRO}".
#
# Copyright (c) 2020 LG Electronics, Inc.

ROS_DISTRO = "lyrical"

inherit ${ROS_DISTRO_TYPE}_distro

# Root-cause fix, must run before every other rosidl_default_generators fix
# below: at least 19 recipes (auto-apms-interfaces, cx-msgs, yasmin-msgs,
# autoware-planning-msgs, dataspeed-can-msgs, rslidar-msg,
# tf2-web-republisher-interfaces, and more -- confirmed via a survey of a
# real build's failure logs) declare rosidl_default_generators as a regular
# build_depend in package.xml instead of a buildtool_depend, a REP-149
# violation that superflore faithfully encodes into the generated recipe:
# ROS_BUILD_DEPENDS gets the bare (target) token, ROS_BUILDTOOL_DEPENDS
# never gets the -native one. Since bitbake's DEPENDS is fixed at parse
# time from these two vars (not re-derived from a live package.xml read at
# build time), no amount of staging individual native tools downstream can
# fix this properly: find_package(rosidl_default_generators) keeps
# resolving against the *target* config instead of native (its own
# ament_cmake_export_dependencies-extras.cmake chain is missing pieces
# other correctly-declared consumers get for free -- e.g. a hard
# "Findrosidl_cmake.cmake" not-found error), and even patching around
# that, the codegen scripts it points at live in the target sysroot
# without their
# corresponding Python modules ever on PYTHONPATH (which only ever points
# at *native* site-packages): "ImportError: cannot import name ...
# (unknown location)" or plain "ModuleNotFoundError", confirmed by directly
# reproducing rosidl_generator_type_description's invocation for
# auto-apms-interfaces outside of bitbake.
#
# Correct it at the actual source of the mistake instead of chasing
# downstream symptoms one tool at a time: move the token from
# ROS_BUILD_DEPENDS to ROS_BUILDTOOL_DEPENDS (native) and from DEPENDS's
# bare form to its native form, exactly matching what package.xml should
# have declared. This makes every recipe hitting this bug behave
# identically to a correctly-declared one (e.g. test-msgs) for every
# purpose -- including all the other fixes below, which is why this must
# be the first anonymous python registered in this file.
python() {
    if d.getVar('PN') == 'rosidl-default-generators':
        return
    build_deps = (d.getVar('ROS_BUILD_DEPENDS') or '').split()
    buildtool_deps = (d.getVar('ROS_BUILDTOOL_DEPENDS') or '').split()
    if 'rosidl-default-generators' not in build_deps or 'rosidl-default-generators-native' in buildtool_deps:
        return
    depends = (d.getVar('DEPENDS') or '').split()
    if 'rosidl-default-generators' in depends:
        depends.remove('rosidl-default-generators')
    if 'rosidl-default-generators-native' not in depends:
        depends.append('rosidl-default-generators-native')
    d.setVar('DEPENDS', ' '.join(depends))
    build_deps.remove('rosidl-default-generators')
    d.setVar('ROS_BUILD_DEPENDS', ' '.join(build_deps))
    d.appendVar('ROS_BUILDTOOL_DEPENDS', ' rosidl-default-generators-native')
}

# Widespread bug in this distro's generated recipes: 236 of the 241 that
# reference rosidl-default-runtime at all only declare it in
# ROS_EXEC_DEPENDS (RDEPENDS), not ROS_BUILD_DEPENDS/ROS_EXPORT_DEPENDS
# (which feed DEPENDS) -- unlike the 3 that get it right (e.g.
# builtin-interfaces' equivalent rosidl-core-runtime). Any package
# exporting a CMake config that transitively requires
# rosidl_default_runtime (which nearly all ament_cmake interface
# packages do, via ament_cmake_export_dependencies-extras.cmake) fails
# at its *consumer's* configure time: "Could not find a package
# configuration file provided by rosidl_default_runtime". See
# ros-distro.inc's earlier note (this must live in a class that every
# recipe actually `inherit`s -- a `require`d .conf/.inc file's
# anonymous python is never registered for per-recipe execution, unlike
# a bbclass's).
python() {
    if d.getVar('PN') == 'rosidl-default-runtime':
        return
    if 'rosidl-default-runtime' in (d.getVar('ROS_EXEC_DEPENDS') or '').split():
        d.appendVar('DEPENDS', ' rosidl-default-runtime')
}

# Related bug, harmless-but-no-longer-sufficient bitbake-level half of the
# fix. Any recipe whose CMakeLists.txt calls ament_target_dependencies()
# fails to configure with "Unknown CMake command
# \"ament_target_dependencies\"". ament-cmake-native's own DEPENDS only
# stages ament-cmake-target-dependencies-native into the *native* sysroot,
# never the target-arch ament-cmake-target-dependencies package into a
# cross-compiling consumer's sysroot -- so stage it here too, keyed off
# whichever of the three ament-cmake-family buildtools (ament_cmake,
# ament_cmake_auto, ament_cmake_ros; REP-0140) the recipe declared in
# ROS_BUILDTOOL_DEPENDS.
#
# Turns out this alone doesn't fix the error: ament_target_dependencies()
# has been removed from ament_cmake_target_dependencies outright in
# lyrical, not merely gated behind find_package()'ing it (confirmed by
# grepping the entire sysroot for its definition -- nothing, not even
# inside that package itself; see
# https://github.com/ament/ament_cmake/pull/572). Left this staging fix in
# place anyway since it's harmless and doesn't hurt. The actual fix is a
# compatible reimplementation of the macro, injected globally via
# CMAKE_PROJECT_INCLUDE -- see ament_target_dependencies_compat.cmake next
# to this bbclass.
python() {
    if d.getVar('PN') == 'ament-cmake-target-dependencies':
        return
    buildtool_deps = (d.getVar('ROS_BUILDTOOL_DEPENDS') or '').split()
    ament_cmake_family = {'ament-cmake-native', 'ament-cmake-auto-native', 'ament-cmake-ros-native'}
    if ament_cmake_family & set(buildtool_deps):
        d.appendVar('DEPENDS', ' ament-cmake-target-dependencies')
}

EXTRA_OECMAKE:append = " -DCMAKE_PROJECT_INCLUDE=${ROS2_LYRICAL_LAYER_DIR}/classes/ament_target_dependencies_compat.cmake"

# generate-parameter-library's package.xml declares generate_parameter_library
# as a regular (non-buildtool) dependency, even though its CMake macro
# generate_parameter_library() runs generate_parameter_library_cpp -- a
# code generator that must execute on the build host -- via find_program()
# at configure time. bitbake's "export" mechanism doesn't cascade past one
# hop (see the recipe's own comment on ROS_BUILDTOOL_EXPORT_DEPENDS), so
# generate-parameter-library-native being built is not enough: consumers
# never get the native tool staged into their own sysroot, and
# find_program() fails with "generate_parameter_library_cpp_BIN must not
# be empty". The actual generate_parameter_library_cpp binary is shipped
# by the sibling generate-parameter-library-py-native package (confirmed
# via tmp/sysroots-components), not generate-parameter-library-native
# itself, which only provides the CMake config.
python() {
    if d.getVar('PN') in ('generate-parameter-library', 'generate-parameter-library-py'):
        return
    if 'generate-parameter-library' in (d.getVar('DEPENDS') or '').split():
        d.appendVar('DEPENDS', ' generate-parameter-library-py-native')
}

# The rosidl-adapter-native fix in ros-distro.inc
# (DEPENDS:append:pn-rosidl-cmake) stages the tool into rosidl-cmake's
# *own* recipe-sysroot-native correctly (verified: its own do_configure's
# "Direct dependencies" now lists rosidl-adapter-native), but that alone
# doesn't reach a package like autoware-planning-msgs three hops away
# (autoware-planning-msgs -> rosidl-default-generators -> rosidl-cmake).
# Unlike target-to-target sysroot staging, a target recipe's *native*
# sysroot only gets what's in that recipe's own DEPENDS -- a dependency's
# own native tool requirements don't cascade into a grandparent consumer's
# recipe-sysroot-native. So every package that transitively needs
# rosidl_generate_interfaces() -- i.e. every one that declares
# rosidl_default_generators as its buildtool per REP-149, which superflore
# always maps to the token rosidl-default-generators-native in
# ROS_BUILDTOOL_DEPENDS -- needs rosidl-adapter-native added directly to
# its own DEPENDS.
#
# IMPORTANT: this must check ROS_BUILDTOOL_DEPENDS for the *-native*
# token, not DEPENDS for the bare (target) token. Originally checked
# 'rosidl-default-generators' in DEPENDS, matching the rosidl-default-runtime
# fix's style above -- but that bare token only shows up in DEPENDS for
# packages whose ROS_EXPORT_DEPENDS also happens to list it (e.g.
# autoware-planning-msgs, which is why validation against that recipe
# looked correct). A plain leaf package like test-msgs has *only*
# rosidl-default-generators-native in its DEPENDS (via
# ROS_BUILDTOOL_DEPENDS), never the bare token -- confirmed directly via
# bitbake-getvar, and confirmed this was silently under-triggering by
# rebuilding test-msgs after this exact fix and a cleansstate and seeing
# rosidl_adapter still absent from its sysroot. (The mis-declared-as-
# build_depend packages noted at the top of this file -- auto-apms-interfaces,
# cx-msgs, yasmin-msgs, and more -- are normalized to carry the native
# token in ROS_BUILDTOOL_DEPENDS before this runs, so checking only that
# var here is sufficient.)
python() {
    if d.getVar('PN') in ('rosidl-default-generators', 'rosidl-adapter'):
        return
    if 'rosidl-default-generators-native' in (d.getVar('ROS_BUILDTOOL_DEPENDS') or '').split():
        d.appendVar('DEPENDS', ' rosidl-adapter-native')
}

# Same shape of bug again, same fix. rosidl_generator_py_generate_interfaces.cmake
# (from rosidl-generator-py) does
# find_package(Python3 REQUIRED COMPONENTS Interpreter Development NumPy),
# and fails with "Could NOT find Python3 (missing: Python3_NumPy_INCLUDE_DIRS
# NumPy)" for every consumer with rosidl-default-generators in DEPENDS.
#
# Tried pointing this at target Python first (python3-numpy + inherit
# python3targetconfig), since Development+NumPy for a package that builds
# a real C extension module sounds like it should target the actual
# target Python -- but it can't work here: cmake.bbclass hardcodes
# -DPython3_EXECUTABLE:PATH=${PYTHON}, and PYTHON is set unconditionally
# to the *native* interpreter by python3native.bbclass (which
# ros_ament_cmake.bbclass already inherits, and which python3targetconfig
# itself also inherits without overriding PYTHON -- it only rewrites
# PATH/PYTHONPATH via shell prepends, which a bitbake-time ${PYTHON}
# string expansion never sees). Since CMake's FindPython3 NumPy component
# check works by literally executing the hinted interpreter to `import
# numpy`, and that interpreter is unconditionally native, only
# python3-numpy-native can ever satisfy it here -- confirmed no actual
# numpy files (just an unrelated same-named file from setuptools) exist
# anywhere under any consumer's recipe-sysroot-native.
#
# Gates on ROS_BUILDTOOL_DEPENDS's rosidl-default-generators-native token,
# not DEPENDS's bare token -- see the rosidl-adapter-native fix above for
# why the bare-token check silently missed plain leaf packages like
# test-msgs (and why the mis-declared-as-build_depend packages don't need
# a separate check here either, now that they're normalized up top).
python() {
    if d.getVar('PN') in ('rosidl-default-generators', 'rosidl-generator-py'):
        return
    if 'rosidl-default-generators-native' in (d.getVar('ROS_BUILDTOOL_DEPENDS') or '').split():
        d.appendVar('DEPENDS', ' python3-numpy-native')
}

# Different symptom, same "package.xml under-declares a rosidl-tooling-
# injected implicit dependency" family: rosidl_generate_interfaces()
# silently needs action_msgs (for any .action file: goal ID/status types),
# unique_identifier_msgs (action goal UUIDs), and service_msgs (any .srv
# file, since a recent rosidl version -- see the cras-msgs investigation
# earlier in this project's history) -- none of which package.xml authors
# are expected to declare, confirmed by grepping several affected
# recipes' generated ROS_BUILD_DEPENDS/ROS_EXEC_DEPENDS: completely absent
# in all of them, e.g. test-msgs (has actions) and
# autoware-internal-debug-msgs (has services).
#
# Symptom: CMakeCache.txt shows action_msgs_DIR/service_msgs_DIR/
# unique_identifier_msgs_DIR all resolved to recipe-sysroot-*native*
# (confirmed: none of the three exist as target-arch in the affected
# recipes' own recipe-sysroot at all), so the final target link command
# ends up passing native x86-64 .so files to the aarch64 linker:
# "error adding symbols: file in wrong format". Affected at least 9
# recipes in the last full build.
#
# Can't gate this on file contents (does this package actually have a
# .action/.srv file?) the way the earlier per-recipe fixes in this file
# gate on DEPENDS/ROS_EXEC_DEPENDS tokens -- ${S} isn't populated yet at
# parse time when this anonymous python runs, well before do_unpack. So,
# same as python3-numpy-native and rosidl-adapter-native above: stage
# unconditionally for every rosidl_generate_interfaces() user (anything
# declaring rosidl_default_generators as its buildtool) rather than only
# those that need it -- harmless for the ones that don't.
#
# Gates on ROS_BUILDTOOL_DEPENDS's rosidl-default-generators-native token,
# not DEPENDS's bare token -- see the rosidl-adapter-native fix above for
# why the bare-token check silently missed plain leaf packages like
# test-msgs (confirmed by rebuilding it after this exact fix and a
# cleansstate: action_msgs/service_msgs/unique_identifier_msgs were still
# completely absent from its DEPENDS and its sysroot). Same note as above
# about the mis-declared-as-build_depend packages already being normalized
# up top.
python() {
    if d.getVar('PN') in ('rosidl-default-generators', 'action-msgs', 'service-msgs', 'unique-identifier-msgs'):
        return
    if 'rosidl-default-generators-native' in (d.getVar('ROS_BUILDTOOL_DEPENDS') or '').split():
        d.appendVar('DEPENDS', ' action-msgs service-msgs unique-identifier-msgs')
}

# Unrelated family: ros_ament_cmake.bbclass (meta-ros2/classes, shared by
# every ROS2 distro) never adds ament_cmake itself to DEPENDS -- it only
# sets EXTRA_OECMAKE flags and inherits cmake/python3native, relying on
# package.xml's own buildtool_depend on ament_cmake (or one of its
# variants: ament_cmake_auto, ament_cmake_ros, ...) to stage it. That
# holds for most packages, but breaks two different ways: a narrower
# buildtool variant that doesn't itself pull in plain ament_cmake for
# *this* package's own configure (rosidlcpp-typesupport-c/cpp declare
# only ament_cmake_ros_core), or no buildtool_depend on ament_cmake at
# all despite declaring ament_cmake as the build_type (zenoh-security-tools'
# ROS_BUILDTOOL_DEPENDS is completely empty -- package.xml only has the
# <export><build_type>ament_cmake</build_type></export> tag). Both fail
# hard at configure time: "By not providing Findament_cmake.cmake ...
# CMake did not find one." Since every ament_cmake-build-type package
# fundamentally needs ament_cmake's own macros regardless of what
# package.xml happens to separately declare, stage it unconditionally for
# anything using this build type rather than trying to enumerate every
# buildtool variant that might or might not cover it.
#
# Exclude the whole ament-cmake-* bootstrap family (not just ament-cmake
# itself): ament-cmake's own ROS_BUILD_DEPENDS pulls in ament-cmake-core
# and ament-cmake-export-dependencies, which transitively pull in nearly
# every other ament-cmake-export-* package via ROS_BUILDTOOL_EXPORT_DEPENDS
# -- these sub-packages collectively *are* ament_cmake's own constituent
# macros, still bootstrapping it, so making any of them depend back on
# ament-cmake-native is circular (confirmed via bitbake -n: "Task
# ...ament-cmake_2.8.7-3.bb:do_create_recipe_spdx has circular dependency
# on ...ament-cmake-export-definitions_2.8.7-3.bb:do_create_recipe_spdx").
python() {
    if (d.getVar('PN') or '').startswith('ament-cmake'):
        return
    if d.getVar('ROS_BUILD_TYPE') == 'ament_cmake':
        d.appendVar('DEPENDS', ' ament-cmake-native')
}
