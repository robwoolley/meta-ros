# Copyright (c) 2026 Wind River Systems, Inc.

# QA Issue: non -dev/-dbg/nativesdk- package live555-vendor contains
# symlink .so 'lib*.so' [dev-so] -- this vendor package intentionally
# ships only unversioned .so symlinks (no separate .so.N), same situation
# already handled for moveit-core.
inherit ros_insane_dev_so
