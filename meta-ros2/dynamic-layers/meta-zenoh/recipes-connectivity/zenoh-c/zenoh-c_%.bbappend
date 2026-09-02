LICENSE = "Apache-2.0 | EPL-2.0"

PACKAGECONFIG += "shared-memory unstable-api"

# Build workarounds; carried forward from the kas-based build without re-verifying they are
# still needed on every release this recipe is built for. Applies to both the target and
# -native BBCLASSEXTEND variants, since this is already scoped to zenoh-c by virtue of being
# its bbappend.
INSANE_SKIP += "buildpaths"
DEBUG_PREFIX_MAP:remove = "-fcanon-prefix-map"
