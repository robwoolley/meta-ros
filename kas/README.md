# Building meta-ros / oeros

`meta-oeros` (https://github.com/robwoolley/meta-oeros) is the reference distro for building ROS
on top of Yocto/OpenEmbedded. There are three ways to build it, covered below: **kas** (simplest),
**bitbake-setup** (bitbake's own native setup tool, no extra dependency beyond bitbake itself), and
a fully **manual** git-clone-and-configure path. All three produce the same bitbake configuration
for a given release/ROS-distro/machine combination -- they are different front ends to the same
underlying repos and [configuration fragments](https://docs.yoctoproject.org/dev/ref-manual/fragments.html),
not different builds.

## Supported combinations

`scarthgap` is Yocto's current LTS release and is fully supported, using the plain kas path below
(it predates configuration fragment support in OpenEmbedded-Core, so it is kas-only -- no
bitbake-setup or manual-fragment path exists for it). `wrynose` is the current stable release and
`master` tracks the in-development next release; both support all three paths below.

<!-- Generated from matrix.yml by scripts/generate-matrix-table.py -- do not hand-edit this table. -->
| Release | ROS distro | Machines | Fragment-capable |
|---|---|---|---|
| `scarthgap` | `humble` | `qemux86-64`, `raspberrypi0-2w-64`, `raspberrypi4-64`, `raspberrypi5` | no |
| `scarthgap` | `jazzy` | `qemux86-64`, `raspberrypi0-2w-64`, `raspberrypi4-64`, `raspberrypi5` | no |
| `scarthgap` | `kilted` | `qemux86-64`, `raspberrypi0-2w-64`, `raspberrypi4-64`, `raspberrypi5` | no |
| `scarthgap` | `lyrical` | `qemux86-64`, `raspberrypi0-2w-64`, `raspberrypi4-64`, `raspberrypi5` | no |
| `scarthgap` | `rolling` | `qemux86-64`, `raspberrypi0-2w-64`, `raspberrypi4-64`, `raspberrypi5` | no |
| `wrynose` | `humble` | `qemux86-64`, `raspberrypi4-64`, `raspberrypi5` | yes |
| `wrynose` | `jazzy` | `qemux86-64`, `raspberrypi4-64`, `raspberrypi5` | yes |
| `wrynose` | `kilted` | `qemux86-64`, `raspberrypi5` | yes |
| `wrynose` | `lyrical` | `qemux86-64`, `raspberrypi5` | yes |
| `wrynose` | `rolling` | `qemux86-64`, `raspberrypi4-64`, `raspberrypi5` | yes |
| `wrynose` | `noetic` | `raspberrypi5` | yes |
| `master` | `humble` | `qemux86-64`, `raspberrypi4-64`, `raspberrypi5` | yes |
| `master` | `jazzy` | `qemux86-64`, `raspberrypi4-64`, `raspberrypi5` | yes |
| `master` | `kilted` | `qemux86-64`, `raspberrypi5` | yes |
| `master` | `lyrical` | `qemux86-64`, `raspberrypi5` | yes |
| `master` | `rolling` | `qemux86-64`, `raspberrypi4-64`, `raspberrypi5` | yes |
<!-- End generated table -->

This table is regenerated from `matrix.yml`, the single source of truth for the whole matrix, with:
```
python3 scripts/generate-matrix-table.py
```

## Option 1: kas

Install kas in a virtual environment:
```
python3 -m venv venv
source venv/bin/activate
pip3 install kas
```

Clone the `build` branch of meta-ros, which holds this `kas/` tree:
```
git clone -b build https://github.com/ros/meta-ros
```

Pick a row from the table above and build it, eg wrynose + jazzy + raspberrypi5:
```
mkdir $PROJECT_DIR
export KAS_WORK_DIR=$PROJECT_DIR
kas build meta-ros/kas/oeros-wrynose-jazzy-raspberrypi5.yml
```

This produces an image under `$PROJECT_DIR/build/tmp-glibc/deploy/images/<machine>/`, eg
`ros-image-core-jazzy-raspberrypi5.rootfs.wic.bz2`.

For `wrynose`/`master` cells, everything beyond repos/machine/ROS-distro selection --
`DISTRO=oeros`, the QA relaxations, the commercial license flags on Raspberry Pi machines, and so
on -- is expressed as [configuration fragments](https://docs.yoctoproject.org/dev/ref-manual/fragments.html)
enabled via `OE_FRAGMENTS`, not `local_conf_header` text; run
`kas dump --skip repos_checkout --skip repos_apply_patches meta-ros/kas/oeros-wrynose-jazzy-raspberrypi5.yml`
to see the resolved value. `scarthgap` cells still use `local_conf_header` directly, since scarthgap's
OpenEmbedded-Core predates fragment support.

## Option 2: bitbake-setup

`bitbake-setup` ships with bitbake >= 2.16 (Yocto whinlatter and later). Install it the same way
as kas, or use the copy bundled with your OpenEmbedded-Core/bitbake checkout if you already have one:
```
python3 -m venv venv
source venv/bin/activate
pip3 install bitbake-setup
```

`meta-oeros`'s `conf/registry/configurations/` directory publishes one configuration file per
fragment-capable `(release, ROS-distro)` pair -- eg `oeros-wrynose-jazzy.conf.json` covers every
machine listed for `wrynose`/`jazzy` above, letting you pick the machine interactively (or
non-interactively, see below).

Point `bitbake-setup` at `meta-oeros`'s registry so `list`/`init` can find configurations by name,
instead of a local file path (this only needs doing once):
```
bitbake-setup settings set --global default registry \
    "git://github.com/robwoolley/meta-oeros;protocol=https;branch=wrynose;rev=wrynose"
bitbake-setup list
```

Then initialize a build (this will prompt you to choose a machine):
```
bitbake-setup init oeros-wrynose-jazzy
```

If you'd rather not register the registry, `init` also accepts a path or URL to one specific
configuration file directly, eg after cloning `meta-oeros`:
```
bitbake-setup init meta-oeros/conf/registry/configurations/oeros-wrynose-jazzy.conf.json \
    oeros-wrynose-jazzy machine/qemux86-64
```
(the second argument selects the one configuration inside the file, the third pre-selects the
machine choice non-interactively; add `--non-interactive` to fail instead of prompting for
anything still unresolved.)

Either way, `bitbake-setup` prints the path to the resulting build directory and its
`init-build-env` script when it finishes.

## Option 3: manual git clone + bitbake-config-build

For full manual control, clone the layers a configuration needs yourself and drive `bitbake`
directly. `meta-oeros/conf/registry/configurations/oeros-<release>-<ros-distro>.conf.json` is the
authoritative list of repos (`sources`), layers (`bb-layers`), and fragments (`oe-fragments`,
`oe-fragments-one-of`) for a given cell -- read it rather than hand-copying a layer list here,
since it is generated from the same `matrix.yml` the kas and bitbake-setup paths are and won't
drift from them.

The general pattern, using wrynose + jazzy + qemux86-64 as a worked example (see that JSON file
for the exact repo URLs/branches and the full layer list):
```
# Clone every repo listed under "sources" in the JSON file, eg:
git clone -b wrynose https://github.com/openembedded/openembedded-core.git
git clone -b wrynose https://github.com/openembedded/bitbake.git
git clone -b wrynose https://github.com/openembedded/meta-openembedded.git
git clone -b wrynose https://github.com/ros/meta-ros.git
git clone -b wrynose https://github.com/robwoolley/meta-oeros.git
# ...and so on for every other entry in "sources".

source openembedded-core/oe-init-build-env build

# Add every path listed under "bb-layers" in the JSON file, eg:
bitbake-layers add-layer ../meta-openembedded/meta-oe
bitbake-layers add-layer ../meta-ros/meta-ros-common
bitbake-layers add-layer ../meta-ros/meta-ros2
bitbake-layers add-layer ../meta-ros/meta-ros2-jazzy
bitbake-layers add-layer ../meta-oeros
# ...and so on for every other entry in "bb-layers".

# Enable every fragment listed under "oe-fragments" in the JSON file, plus one choice from
# "oe-fragments-one-of":
bitbake-config-build enable-fragment distro/oeros
bitbake-config-build enable-fragment oeros/common
bitbake-config-build enable-fragment oeros/qa/allow-license-exists
bitbake-config-build enable-fragment machine/qemux86-64

bitbake ros-image-core
```

Run `bitbake-config-build list-fragments` at any point to see every fragment available from the
layers currently in `bblayers.conf`, with its description.

## CI

GitHub Actions (`.github/workflows/config-check.yml`) is the primary CI system; GitLab CI
(`.gitlab-ci.yml`) is also supported. Both run a `config-check` job on every push/PR to `build` that
verifies the generated `kas/oeros-*.yml` files and this README's table haven't drifted from
`matrix.yml` (`scripts/generate-kas.py --check` and `scripts/check-readme-table.py`) -- kept
separate from the actual (manual, per-combination) build so it runs automatically and cheaply.
GitLab CI also has a manual `build-job` that runs a real `kas build`.

**Running this in your own fork**: `config-check` needs no secrets, tokens, or org-specific
configuration on either system, so it works unmodified in a personal fork. Two things to be aware
of:
- GitHub disables Actions on forks by default -- enable them once under the fork's **Actions** tab,
  or trigger `config-check` manually via its `workflow_dispatch` trigger.
- GitLab's `build-job` pulls its build image via the `CROPS_IMAGE` CI/CD variable, which defaults
  to `oeros`'s own container registry path. If that path isn't reachable from your fork's CI (eg no
  access to the upstream registry), override `CROPS_IMAGE` with your own pullable image under
  **Settings > CI/CD > Variables**, rather than editing `.gitlab-ci.yml`. `config-check` doesn't use
  this image and is unaffected either way.

## Writing the image

If using [Balena Etcher](https://etcher.balena.io/), you may provide it with the `.wic.bz2` file
directly.

If using `dd`, you must first decompress the bzip2 file.

If using bmaptool, you may follow the instructions here:
https://docs.yoctoproject.org/dev/dev-manual/bmaptool.html

Since we have already produced the wic file you may use the following example with the last 2
arguments replaced with the path to the wic file and to the SD card you wish to write:
```
oe-run-native bmaptool-native bmaptool copy <build-directory>/tmp/deploy/images/<machine>/<image>.wic </dev/sdX>
```
