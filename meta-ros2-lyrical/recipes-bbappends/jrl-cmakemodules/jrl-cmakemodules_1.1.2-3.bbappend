# Copyright (c) 2026 Wind River Systems, Inc.

# QA Issue: git-archive-all.sh requires /bin/bash, but no providers found
# in RDEPENDS:jrl-cmakemodules [file-rdeps]
RDEPENDS:${PN} += "bash"
