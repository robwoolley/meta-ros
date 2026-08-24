# Copyright (c) 2026 Wind River Systems, Inc.

# sound-play depends on festival, which requires porting festival and its
# speech-tools dependency from meta-ros1 (not reasonable effort for this
# one optional TTS backend); drop it from the "install everything available"
# audio-common aggregator rather than skip the whole package.
RDEPENDS:${PN}:remove = "sound-play"
