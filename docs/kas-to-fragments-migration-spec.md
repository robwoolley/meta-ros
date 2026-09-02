# Migrating meta-ros kas configurations to native bitbake configuration fragments

Status: draft specification, revision 4 (2026-09-02) — Phase 0 and Phase 1 of § 5 are now implemented and
committed (locally) against `meta-ros-oe-fragments`, `meta-oeros`, and `meta-ros`; this document is being kept
in sync with that work as it proceeds, not just revised in response to review comments.
Scope: `meta-ros` `build` branch `kas/` tree (this repository), `meta-oeros`, `meta-ros-common` and the ROS
sub-layers in `meta-ros`
Author context: produced empirically against real checkouts; every mechanism described below was exercised
against the actual tools and the actual repository content. See the Appendix for exact versions, commits and
commands.

**Revision 2 changes** (maintainer decisions made after revision 1 was reviewed):
- **Whinlatter is end-of-life and dropped from the target set.** Only **wrynose** and **master** are migration
  targets now; whinlatter's 16 top-level files and `kas/yocto/whinlatter.yml` are recommended for removal (§ 1.2,
  § 1.7, § 5 Phase 0). This also **removes** the whinlatter/wrynose renamed-variable fork that was the biggest
  wrinkle in revision 1 — one fragment spelling now covers the entire target set (§ 0.2).
- **Both Phase-0 bugs now have exact, verified fixes**, not just a diagnosis (§ 0.2).
- **The `ros1`/`ros2` vs `oeros` distro question (revision 1 § 0.7/§ 6 item 1) is now resolved**: meta-ros is
  being made distro-agnostic, `DISTRO=ros1`/`DISTRO=ros2` are being retired, and `meta-oeros` becomes the sole
  distro. See new § 0.7 and § 1.9.

**Revision 3 changes** (two corrections from maintainer review of revision 2):
- **§ 0.6 and § 6 item 5 retracted.** `meta-ros` is branched per Yocto release (standard OE-layer convention —
  `wrynose`/`wrynose-next`, `whinlatter`/`whinlatter-next`, `master`/`master-next` for the not-yet-released
  "blacksail" series, etc.); `LAYERSERIES_COMPAT_* = "wrynose"` on the single checkout revision 1/2 read from
  was correct for that branch, not a tree-wide inconsistency. Verified live across four branches (§ Appendix
  item 11). There was never a bug here, and the "update LAYERSERIES_COMPAT" step is removed from Phase 0c.
- **§ 1.9's change list re-scoped and reworded — no layer is deleted.** Revision 2's phrasing could be misread
  as proposing to remove `meta-ros1`/`meta-ros2`/`meta-ros1-noetic`/every `meta-ros2-<distro>` as layers. That
  was never the proposal: the change is exactly two three-line files (`ros1.conf`, `ros2.conf`), which contain
  only a `DISTRO_NAME` and a `require` — no recipe, class, or the `BBFILE_COLLECTIONS`/`BBFILE_PRIORITY`
  machinery that keeps same-named per-ROS-distro generated recipes from colliding. § 1.9 now states this
  explicitly up front, and the change must be applied per-branch (`wrynose` and `master`, both confirmed to
  have identical `ros1.conf`/`ros2.conf` content today), not once.

**Revision 4 changes** (Phase 0 and Phase 1 implemented; one design correction caught during implementation):
- **Phase 0 (§ 5) is done**: the `meta-oeros` `DISTRO_FEATURES_DEFAULT` fix, whinlatter removal, and the
  `meta-ros` distro-agnostic change are all committed on the relevant branches (`wrynose`/`master` where
  applicable), each verified live before committing.
- **Most of Phase 1 is done**: eleven fragments landed in `meta-oeros`/`meta-ros-common`, each verified with
  real `bitbake-config-build list-fragments`/`enable-fragment` runs. Caught and fixed along the way: the
  already-shipped `allow-commercial-licenses.conf` fragment had a malformed multi-line `BB_CONF_FRAGMENT_DESCRIPTION`
  that broke fragment discovery for the entire `meta-oeros` layer (already fixed on `wrynose` by the maintainer,
  but missing from a stale local `master` checkout — fast-forwarded non-destructively rather than reset).
- **§ 2.2 correction, caught by maintainer review**: the zenoh workaround was initially implemented as a
  `meta-ros-common` fragment, mirroring its kas `local_conf_header` shape too literally. `:pn-zenoh-c` overrides
  are recipe-specific, not distro policy, and `meta-ros2` already has the right home for this
  (`dynamic-layers/meta-zenoh/.../zenoh-c_%.bbappend`) — moved there instead, verified against the real
  `zenoh-c` recipe. This generalizes into a rule applied retroactively: any `local_conf_header` line carrying a
  `:pn-<recipe>` override is a bbappend candidate, not a fragment candidate. Applied immediately to the
  `python3`/tk case (no new plumbing needed, done); `clang`/`qtbase-native` remain deferred, now for this reason
  in addition to the original behavior-change concern (§ 2.2, § 2.3).

## 0. Summary of what was verified, up front

Because several sections below revise assumptions stated in the originating brief, the corrected facts are
collected here first:

1. **Fragment support in OE-Core starts one release earlier than assumed, is only complete two releases
   later — and, with whinlatter now dropped as EOL, none of that gap matters for the actual target set.**
   The base fragment mechanism (`meta/lib/bbconfigbuild/configfragments.py`, `OE_FRAGMENTS`, `addfragments`)
   first appears in **walnascar** (Yocto 5.2). **Built-in fragments** (`machine/<name>`, `distro/<name>`)
   require bitbake commit `3b9d7bea`, which landed in bitbake **2.16** (whinlatter, Yocto 5.3), and the
   **`bitbake-setup` tool itself** (`bin/bitbake-setup`) likewise first ships in bitbake 2.16. **Current
   target set, per maintainer decision: wrynose and master only** (whinlatter is end-of-life upstream and is
   being removed, not just deprioritized — § 1.2, § 1.7, § 5 Phase 0; walnascar was never in scope, this
   repository ships no walnascar configuration). Both wrynose and master have full fragment support, built-in
   fragments, and bitbake-setup, so — unlike revision 1 of this document — there is now exactly **one**
   fragment-capable release tier to design for, not three with an internal maturity split.
2. **Two variables used today by this repository's kas files are hard parse errors on wrynose and master —
   both now have exact, verified fixes.** `DISTRO_FEATURES_DEFAULT` (used in `meta-oeros/conf/distro/oeros.conf`)
   and `DISTRO_FEATURES_BACKFILL_CONSIDERED` (used in `kas/systemd.yml`) were renamed via `BB_RENAMED_VARIABLES`
   starting on the wrynose branch, and referencing either now aborts parsing with `bb.BBHandledException`. This
   was reproduced live, and — new in this revision — **both fixes were verified live too** (§ Appendix items 9
   and 10):
   - **`meta-oeros/conf/distro/oeros.conf` line 15.** `OEROS_DEFAULT_DISTRO_FEATURES` is not defined anywhere
     in `meta-oeros` — the line `DISTRO_FEATURES ?= "${DISTRO_FEATURES_DEFAULT} ${OEROS_DEFAULT_DISTRO_FEATURES}"`
     was already dead weight (a copy-paste of poky.conf's pattern that resolved to nothing, since neither
     variable had real content in this layer even before the rename) — confirmed by grepping the whole
     `meta-oeros` tree for `OEROS_DEFAULT_DISTRO_FEATURES` and finding only this one reference, its own
     definition. **Fix: delete the line outright.** oe-core's `bitbake.conf` already unconditionally does
     `DISTRO_FEATURES:append = " ${@oe.utils.filter_default_features('DISTRO_FEATURES', d)}"`
     (= `DISTRO_FEATURES_DEFAULTS` minus `DISTRO_FEATURES_OPTED_OUT`), so nothing needs to replace it unless
     `oeros` wants real default-feature-backfill behavior it didn't have before, in which case add
     `DISTRO_FEATURES_DEFAULTS += "..."` as its own, separate line — verified live: with the dead line removed
     and a `DISTRO_FEATURES_DEFAULTS += "sysvinit pulseaudio"` / `DISTRO_FEATURES_OPTED_OUT += "sysvinit"` pair
     added instead, `bitbake-getvar DISTRO_FEATURES` on wrynose/2.18 parsed cleanly and correctly excluded
     `sysvinit` while keeping `pulseaudio`.
   - **`kas/systemd.yml`**: `DISTRO_FEATURES_BACKFILL_CONSIDERED += "sysvinit"` → **`DISTRO_FEATURES_OPTED_OUT
     += "sysvinit"`**. Verified live in the same test as above — identical opt-out effect, no other semantic
     change.
   - `kas/systemd.yml` is not referenced by any of the 63 current top-level matrix files (see finding 3 below),
     so this bug has never been triggered in CI; it would have been triggered the moment `kas/systemd.yml` was
     used directly or converted to a fragment and enabled on wrynose/master.
   - Revision 1 flagged a per-release content fork here (whinlatter needed the old spelling, wrynose/master the
     new one, and neither spelling worked on both). **With whinlatter dropped, this fork is no longer needed —
     `DISTRO_FEATURES_OPTED_OUT` is the only spelling the fragment ever has to carry.**
3. **The seven cross-cutting files (`common.yml`, `systemd.yml`, `visualization.yml`, `world.yml`,
   `diskmon.yml`, `limit-pressure.yml`, `buildci.yml`, `awsci.yml`) are already designed as optional,
   composable overlays** — none of the 63 `oeros-*.yml` top-level files include any of the latter six via
   `header.includes` (confirmed by grep across the whole tree). They are meant to be appended on the kas command
   line with `:` (kas's documented multi-file syntax), e.g.
   `kas build oeros-wrynose-jazzy-raspberrypi5.yml:kas/systemd.yml`. This was proven to compose correctly with
   `kas dump`. Practically, this means **six of the seven cross-cutting files are already living in exactly the
   shape a configuration fragment wants** — independently selectable, no forced coupling to a specific matrix
   cell. Only `common.yml` is unconditionally included everywhere and represents "always-on" behavior for the
   `oeros` product line.
4. **kas already emits weak defaults for `MACHINE` and `DISTRO`** (`MACHINE ??= "..."`, `DISTRO ??= "..."`,
   `kas/config.py` via `libcmds.py:522-523`), which makes kas-authored `machine:`/`distro:` keys **compatible
   with, and safely overridable by,** built-in `machine/<name>` / `distro/<name>` fragments — the built-in
   fragment mechanism only `bb.fatal`s when the target variable already has a *non-weak* assignment. This was
   proven live: enabling `machine/qemuarm64` cleanly overrode a `MACHINE ??= "qemux86-64"` set by kas-style
   local.conf content.
5. **`kas`'s `local_conf_header` merge order is alphabetical by key, across *all* merged/included files, not
   include order** (`kas/config.py::_get_conf_header`: `for key, value in sorted(...)`). This is a real,
   pre-existing property of kas that the fragment-based scheme should be judged against, not a regression to
   avoid — see § 1.4 (Ordering and precedence).
6. **`meta-ros` is branched per Yocto release — `LAYERSERIES_COMPAT_* = "wrynose"` on the checkout this spec
   initially read was correct for that checkout, not a tree-wide fact, and revision 1 wrongly treated it as an
   inconsistency.** `meta-ros` follows the standard OE-layer convention of one stable branch plus one staging
   branch per release (`scarthgap`/`scarthgap-next`, `whinlatter`/`whinlatter-next`, `wrynose`/`wrynose-next`,
   `master`/`master-next` for the in-development, not-yet-released "blacksail" series, plus older EOL releases).
   Verified live by checking out each branch: `scarthgap` declares `LAYERSERIES_COMPAT_ros-common-layer =
   "scarthgap"`, `wrynose` declares `"wrynose"`, `whinlatter` declares `"whinlatter"`, and `master` declares
   `"wrynose"` — correctly, since blacksail hasn't released yet and a development branch conventionally still
   claims the last shipped series until its own ships. **This is not a gap; retract revision 1's § 6 item 5 and
   the LAYERSERIES_COMPAT-update step it implied.** The real consequence for this migration: `kas` already
   selects the correct `meta-ros` branch per release automatically (`defaults.repos.branch` in each
   `kas/yocto/<release>.yml` applies to every repo that doesn't override its own `branch:`, including the `ros`
   repo — confirmed by re-reading `kas/ros1/noetic.yml`/`kas/ros2/*.yml`, neither of which sets `branch:`), so
   the matrix/generator design (§ 3) must replicate that same per-release branch selection for `meta-ros`
   specifically on the bitbake-setup side too — and the existing `meta-oeros/conf/registry/configurations/oeros-master-rolling.conf.json`
   currently pins `meta-ros` to `"master-next"` rather than the plain `master` branch name kas would use for
   that release, which needs reconciling (§ 6).
7. **`DISTRO` in every currently-shipping configuration is `ros1` or `ros2`, not `oeros` — and this is now a
   resolved decision to change, not an open question.** `meta-ros/conf/distro/ros1.conf` / `ros2.conf`
   (both of which `require conf/distro/include/ros-common.inc`) are the only things that couple meta-ros to
   being its own `DISTRO`. **Everything else in meta-ros's ROS-version-selection machinery is already
   distro-agnostic**, traced directly through source: `ROS1_DISTRO`/`ROS2_DISTRO` (e.g. `"jazzy"`) are set
   per-layer inside each `meta-ros2-<distro>`'s own `conf/ros-distro/include/<distro>/ros-distro.inc`, pulled
   in via `require` from that layer's `conf/layer.conf` — selecting a ROS distro is already just "enable a
   layer," with zero reference to `DISTRO`. The derived `ROS_DISTRO` in `meta-ros-common/conf/ros-distro/ros-distro.conf`
   is likewise unconditionally active whenever `meta-ros-common` is in `BBLAYERS`, independent of `DISTRO`. And
   the `:ros1-distro`/`:ros2-distro` bitbake *override* class used by several recipes (`ros_insane_dev_so.bbclass`,
   the turtlebot3 packagegroups) is added to `DISTROOVERRIDES` by `ros_distro.bbclass`
   (`DISTROOVERRIDES .= ":${ROS_DISTRO_BASELINE_PLATFORM}:${ROS_DISTRO_TYPE}-distro:${ROS_DISTRO}"`), driven by
   `ROS_DISTRO_TYPE` — itself set per-layer (e.g. `meta-spaceros-jazzy` sets `ROS_DISTRO_TYPE = "spaceros"`) —
   **not** by bitbake's `DISTRO` variable at all. **Decision: `ros1.conf`/`ros2.conf` are retired, `meta-ros`
   ships no `conf/distro/*.conf` of its own, and `meta-oeros` becomes the sole `DISTRO`.** See § 1.9 for the
   concrete file-level plan.
8. **Fragment full names are derived from `BBFILE_COLLECTIONS`, not the layer directory name.**
   `meta-ros-common/conf/layer.conf` declares `BBFILE_COLLECTIONS += "ros-common-layer"`, so a fragment at
   `meta-ros-common/conf/fragments/oeros/ros2-jazzy.conf` is addressed as **`ros-common-layer/oeros/ros2-jazzy`**,
   not `meta-ros-common/...`. `meta-oeros/conf/layer.conf` declares `BBFILE_COLLECTIONS += "oeros"`, so its
   fragments are addressed as `oeros/<subdir>/<name>`. This is proven live (§ Appendix) and drives the naming
   convention in § 2.

---

## 1. Inventory and mapping analysis

### 1.1 Classification legend

| Code | Meaning |
|---|---|
| **FRAG-ROS** | Becomes a fragment in `meta-ros-common/conf/fragments/` |
| **FRAG-OEROS** | Becomes a fragment in `meta-oeros/conf/fragments/` |
| **FRAG-BUILTIN** | Uses OE-Core's built-in `machine/<name>` or `distro/<name>` fragment, no new file needed |
| **BB-SETUP** | Expressed only in the bitbake-setup JSON `sources` / `bb-layers` (repo pins, layer enablement) |
| **KAS-ONLY** | Stays in kas form; no clean native equivalent, or only applies to legacy (non-fragment-capable) releases |
| **DROP** | Recommend removing outright, with justification |

### 1.2 `kas/yocto/*.yml` — one per Yocto release

| File | Constructs | Classification | Notes |
|---|---|---|---|
| `kirkstone.yml`, `mickledore.yml`, `styhead.yml`, `walnascar.yml` | repo pins (oe-core/bitbake/meta-openembedded commits+branches), `local_conf_header.distro` (`WARN_QA:remove`/`ERROR_QA:remove = "license-exists"`) | **KAS-ONLY** | Not fragment-capable (kirkstone/mickledore/styhead) or fragment-incomplete and currently unreferenced by any top-level file (walnascar). Confirmed dead code today: no `oeros-*.yml` includes any of these four. Recommend leaving untouched; document as historical/manual-use only, not part of the generated matrix. |
| `scarthgap.yml` | same shape, plus `header.includes: [kas/layer/clang-revival.yml]` | **KAS-ONLY** (legacy, actively used) | Per decision #1, scarthgap keeps its current kas approach unchanged. The `local_conf_header.distro` block (`WARN_QA:remove = "license-exists"`) stays as kas `local_conf_header`; there is no fragment target since scarthgap's oe-core has no `configfragments.py`. |
| `whinlatter.yml` | repo pins, `local_conf_header.distro` (`ERROR_QA:remove = "license-exists"`) | **REMOVE.** Whinlatter is end-of-life upstream (maintainer decision, 2026-09-02) and is no longer a target at all — not fragment migration, not continued kas maintenance. Recommend deleting `kas/yocto/whinlatter.yml` and all 16 `kas/oeros-whinlatter-*.yml` files in Phase 0 (§ 5), the same disposal `kirkstone.yml`/`mickledore.yml`/`styhead.yml`/`walnascar.yml` already have, except those four are kept as inert historical references while whinlatter should actually be deleted since it was, until this decision, a live target with active files. | Removing whinlatter drops the `DISTRO_FEATURES_BACKFILL_CONSIDERED` vs `DISTRO_FEATURES_OPTED_OUT` fork entirely (§ 0.2) — the migration now only ever has to generate one spelling. |
| `wrynose.yml` | repo pins + `meta-yocto` (poky/meta-yocto-bsp) repo, `local_conf_header.distro` (`ERROR_QA:remove = "license-exists"`) | **BB-SETUP** for repo pins. `ERROR_QA:remove = "license-exists"` → **FRAG-OEROS** (`oeros/qa/allow-license-exists.conf`) — a policy-level QA relaxation, not ROS-specific, matching decision #2. The extra `meta-yocto` repo (poky compatibility layer) is **BB-SETUP** (`sources`/`bb-layers` entries). | `ERROR_QA:remove` targets the same variable family whose default is set later in `conf/distro/defaultsetup.conf`/`sanity.conf` (parsed after `addfragments`); a `:remove` override resolves lazily regardless of statement order, so moving it into a fragment changes nothing about its effect. wrynose is the only remaining release file pulling in `meta-yocto`; confirm during matrix design whether this is still required (flagged as an open question, § 6). |
| `master.yml` | repo pins, `local_conf_header.distro` (`ERROR_QA:remove = "license-format"` — **different flag than the release branches**, and commented "Ignore license-format errors temporarily" per the actual git history of this file) | **BB-SETUP** for pins. The QA-relaxation line is **FRAG-OEROS**, but explicitly time-boxed / temporary per the existing commit message (`72ba090516 master.yml: Ignore license-format errors temporarily`); the fragment's `BB_CONF_FRAGMENT_DESCRIPTION` should say so, and the generator/matrix should carry an expiry note so it isn't silently permanent. |

### 1.3 `kas/ros1/*.yml`, `kas/ros2/*.yml` — ROS distro selection

**Superseded by the § 1.9 distro-agnostic decision.** `distro: "ros1"`/`"ros2"` is removed from these six files
entirely, not converted to a `distro/ros1`/`distro/ros2` built-in fragment as revision 1 conditionally proposed
— per § 0.7/§ 1.9, `DISTRO` becomes `oeros` uniformly across the whole matrix, set once (in the fragment
equivalent of `common.yml`, or the built-in `distro/oeros` fragment), not per ROS-distro file. What remains in
each of these six files after removing `distro:`:

| Construct | Classification | Notes |
|---|---|---|
| `repos: ros: layers: [meta-ros-common, meta-ros1/meta-ros2, meta-ros1-noetic/meta-ros2-<distro>]` | **BB-SETUP** — `bb-layers` entries in the registry JSON, one list per ROS distro. Unaffected by the distro-agnostic change; layer selection was already how ROS-version selection worked (§ 0.7). |
| `target: [ros-image-core]` | **BB-SETUP** has no direct equivalent (bitbake-setup's JSON schema has no "default recipe/target" field — verified against `bitbake-setup.schema.json`, which only has `bb-layers`, `oe-fragments*`, `bb-env-passthrough-additions`, `setup-dir-name`). This must be **documented**, not encoded: the generated bitbake-setup `README` (bitbake-setup writes one per setup dir, confirmed in the smoke test) and the manual-path documentation should both state the default target explicitly. **DROP is not correct here** — this is real information users need; it becomes documentation output from the matrix, appended into the `description` field of each bitbake-setup configuration and into the generated docs, not a bitbake mechanism. |

### 1.4 `kas/machine/*.yml`

| File | Constructs | Classification | Notes |
|---|---|---|---|
| `qemux86-64.yml`, `qemuarm64.yml` | `machine:` only, no repos, no `local_conf_header` | **FRAG-BUILTIN** (`machine/qemux86-64`, `machine/qemuarm64` — genuinely built-in, oe-core ships QEMU machine confs; no fragment file needed at all) |
| `raspberrypi0-2w-64.yml`, `raspberrypi4-64.yml`, `raspberrypi5.yml` | `machine:`, `repos.raspberrypi` (meta-raspberrypi), `local_conf_header.raspberrypi` (`LICENSE_FLAGS_ACCEPTED += "commercial synaptics-killswitch"`) | `machine:` → **FRAG-BUILTIN**. The license-flags line → **FRAG-OEROS**, and this exact fragment **already exists**: `meta-oeros/conf/fragments/allow-commercial-licenses.conf`. The repo pin for `meta-raspberrypi` → **BB-SETUP**. | This is the cleanest 1:1 mapping in the whole tree — meta-oeros's existing fragment is verbatim what these three files need, down to the flag string. Migration work here is close to zero; each of the three machine files just needs `machine/raspberrypiN...` + `oeros/allow-commercial-licenses` enabled together. |
| `jetson-agx-xavier-devkit.yml`, `jetson-orin-nano-devkit.yml` | `machine:`, `repos` (meta-tegra, meta-tegra-community, tegra-demo-distro sub-layers, meta-virtualization), `local_conf_header` (`DISTRO_FEATURES:append = " virtualization"`) | `machine:` → **FRAG-BUILTIN**. Repos → **BB-SETUP**. The `DISTRO_FEATURES:append = " virtualization"` line is **identical text** to `kas/layer/virtualization.yml`'s `local_conf_header` — **DROP the duplicate**, keep one **FRAG-OEROS** fragment (`oeros/virtualization`) that both the Jetson machine selection and the standalone `layer/virtualization.yml` add-on enable. Today these two files silently duplicate the same `DISTRO_FEATURES:append`; because `:append` is idempotent-by-accumulation-not-by-value (bitbake does not deduplicate `:append` results), enabling both today would append `" virtualization"` twice into `DISTRO_FEATURES`, which is harmless for `DISTRO_FEATURES` membership checks but is exactly the kind of latent double-inclusion the fragment model's `enable-fragment` command actively prevents (`enable-fragment` is idempotent and warns "already included"). Consolidating removes the risk entirely. | 
| `polarfire-soc-icicle-kit-es.yml` | `machine: "icicle-kit-es"`, `repos.polarfire` (three sub-layers), no `local_conf_header` | `machine:` → **FRAG-BUILTIN**. Repos → **BB-SETUP**. |

### 1.5 `kas/layer/*.yml` — optional add-on layers

| File | Constructs | Classification | Notes |
|---|---|---|---|
| `clang.yml` | repo (meta-clang), `local_conf_header` (`PACKAGECONFIG:append:pn-clang = "  libomp"`) | Repo → **BB-SETUP**. `local_conf_header` → **not a fragment** (§ 2.2): `:pn-clang` is a recipe-specific override, so this belongs in a bbappend for the `clang` recipe. `meta-oeros/conf/distro/include/packageconfig.inc` **already contains this exact line**, unconditionally — stays there for now, since moving it needs `meta-oeros` to gain `dynamic-layers`/`BBFILES_DYNAMIC` plumbing for `meta-clang` that doesn't exist yet (deferred, unlike the python3 case which needed no such plumbing and was fixed directly). |
| `clang-revival.yml` | repo only (meta-clang-revival), no `local_conf_header` | **BB-SETUP** only. |
| `lts-mixins.yml` | repo only (meta-lts-mixins), branch varies per top-level file (`scarthgap/rust`) | **BB-SETUP**, with the branch override expressed as a per-matrix-cell `sources` override in the registry JSON, or (manual path) documented literally as "use the `scarthgap/rust` branch of meta-lts-mixins." No fragment involved — this is pure repo pinning, nothing bitbake-config-level. |
| `python-ai.yml` | repo (meta-python-ai), `local_conf_header` (`FORTRAN:forcevariable`, `RUNTIMETARGET:append:pn-gcc-runtime`, `HOSTTOOLS += "gfortran"`) | Repo → **BB-SETUP**. `local_conf_header` → **FRAG-OEROS** (`oeros/fortran-runtime` or similar) — this is fortran/gcc-runtime enablement, not ROS-specific, so per decision #2 it belongs in `meta-oeros`, not `meta-ros-common`, even though it is currently only pulled in for the `meta-python-ai` layer. (meta-ros-common already ships `conf/ros-distro/include/enable-fortran.inc` — check for overlap before authoring a new fragment; do not duplicate.) |
| `qt5.yml` | repo (meta-qt5), `local_conf_header` (`PACKAGECONFIG:append:pn-qtbase-native = " gui"`) | Repo → **BB-SETUP**. `local_conf_header` → **not a fragment** (§ 2.2), same reasoning as `clang.yml`: recipe-specific override, deferred to a future `meta-oeros/dynamic-layers/meta-qt5/...` bbappend once that plumbing exists. Still **already exists** verbatim, unconditionally, in `meta-oeros/conf/distro/include/packageconfig.inc`. |
| `qt6.yml` | repo (meta-qt6, pinned `branch: "6.11"`), no `local_conf_header` | **BB-SETUP** only. |
| `realsense.yml` | repo (meta-intel-realsense) only | **BB-SETUP** only. Branch override (`wrynose`) seen in every `oeros-master-*.yml` file → per-cell `sources` override, same pattern as `lts-mixins`. |
| `tasklogger.yml` | repo (meta-tasklogger, `branch: "main"`), `local_conf_header` (`INHERIT += "tasklogger"`) | Repo → **BB-SETUP**. `INHERIT += "tasklogger"` → **FRAG-OEROS** (`oeros/tasklogger`). `INHERIT` additions are pure accumulation (space-separated class list); a fragment doing `INHERIT += "tasklogger"` parsed after local.conf and before distro.conf behaves identically to today's kas `local_conf_header` placement, since `INHERIT` is processed by `finalize()`/`bb.parse.BBHandler.inherit()` using the *fully accumulated* value at recipe-parse time, not the value at any specific conf-parse checkpoint — order across conf-file boundaries does not matter for `INHERIT +=`. |
| `virtualization.yml` | repo (meta-virtualization), `local_conf_header` (`DISTRO_FEATURES:append = " virtualization"`) | Repo → **BB-SETUP**. `local_conf_header` → **FRAG-OEROS** `oeros/virtualization`, consolidated with the Jetson machine files per § 1.4. |
| `zenoh.yml` | repo (meta-zenoh), `local_conf_header` (`INSANE_SKIP:pn-zenoh-c += "buildpaths"`, two `DEBUG_PREFIX_MAP:remove:pn-zenoh-c...` lines with comment "Needed for scarthgap") | Repo → **BB-SETUP**, branch/commit overrides per-cell (whinlatter/wrynose pin specific commits; every file overrides `branch: master`, and `oeros-wrynose-noetic-raspberrypi5.yml` additionally sets `meta-zenoh: disabled` — see § 1.6). `local_conf_header` → **not a fragment — landed as a bbappend** (§ 2.2, corrected from an earlier fragment implementation): both lines are `:pn-zenoh-c`/`:pn-zenoh-c-native` recipe-specific overrides, and `meta-ros2` already ships `dynamic-layers/meta-zenoh/recipes-connectivity/zenoh-c/zenoh-c_%.bbappend` (`BBFILES_DYNAMIC`-activated whenever `meta-zenoh` is enabled), the correct, already-existing home for this. Landed there, collapsed to one line each (an unscoped assignment inside a bbappend already covers the `-native` `BBCLASSEXTEND` variant — verified live against the real recipe). `meta-oeros/conf/distro/include/zenoh.inc`'s duplicate copy of these two lines is removed; its `ZENOH_SHARED_MEMORY`/`ZENOH_UNSTABLE_API` lines stay, confirmed against the real `meta-zenoh` source to be genuine distro-level policy (consumed via `PACKAGECONFIG[vardeps]`, weak-defaulted in `meta-zenoh`'s own `layer.conf`), not recipe overrides. This resolves the placement conflict revision 3 flagged in § 6 item 3 by eliminating the duplication rather than choosing a side. The "Needed for scarthgap" comment is dropped rather than carried forward, since scarthgap never becomes fragment-capable and the workaround's applicability to wrynose/master specifically was never independently verified — the bbappend's comment says so explicitly instead of asserting a release it doesn't apply to. |

### 1.6 Cross-cutting files

| File | Constructs | Classification | Notes |
|---|---|---|---|
| `common.yml` | `header.includes` (clang, clang-revival, realsense, python-ai, virtualization, zenoh), `local_conf_header.common` (`DISTRO_FEATURES += "usrmerge"`, `IMAGE_FEATURES += "..."`, `INHERIT += "report-error"`, `INHERIT += "rm_work"`, `INHERIT_DISTRO:remove = "create-spdx"`, `TEMPLATECONF = ""`) | The five includes decompose into their own fragments per § 1.5 (**common.yml stops being a bundling mechanism** — the generator should list these five as always-on fragments in every matrix cell's `oe-fragments`, not as one meta-fragment, so that a user can `enable-fragment`/`disable-fragment` any one of them independently — this is a genuine improvement fragments offer over kas's `header.includes`, which cannot selectively disable one included file). `usrmerge`/`IMAGE_FEATURES`/`INHERIT report-error`/`INHERIT rm_work` → **FRAG-OEROS** (`oeros/common` or split further, see § 2). `INHERIT_DISTRO:remove = "create-spdx"` → **flag for § 6**: `INHERIT_DISTRO` is itself normally set inside `conf/distro/${DISTRO}.conf`, which parses **after** `addfragments` (confirmed in bitbake.conf's include order). A `:remove` override, like the QA ones above, resolves lazily and is unaffected by this ordering — verified this is standard bitbake override semantics, not something fragments change. `TEMPLATECONF = ""` → **KAS-context-only** in one sense (it exists to blank out kas's default templateconf search so kas doesn't try to pull in poky's default `local.conf.sample`), but the underlying need — "don't apply any oe-core `TEMPLATECONF` defaults" — has no meaning at all in the bitbake-setup or manual-git paths, since neither of those tools does kas's `TEMPLATECONF`-driven template lookup in the first place. **Classification: DROP for the fragment-capable/bitbake-setup/manual paths; KAS-ONLY for legacy releases** where kas is still used as-is. |
| `systemd.yml` | `local_conf_header.systemd` (`DISTRO_FEATURES += " pam systemd "`, `DISTRO_FEATURES_BACKFILL_CONSIDERED += "sysvinit"`, `VIRTUAL-RUNTIME_init_manager = "systemd"`) | **FRAG-OEROS**, with `DISTRO_FEATURES_BACKFILL_CONSIDERED` corrected to **`DISTRO_FEATURES_OPTED_OUT`** (§ 0.2) — the fragment content as written today would hard-fail parsing the instant anyone enables it on wrynose/master, since that variable was renamed. This is the single highest-value catch in this inventory — a real bug that has simply never been exercised, because no top-level file references `systemd.yml` today (§ 0.3). With whinlatter dropped (revision 2), there is now exactly one correct spelling to generate, not a per-release fork. |
| `visualization.yml` | `local_conf_header.visualization` (`DISTRO_FEATURES:append = " x11 opengl vulkan polkit"`, `PACKAGECONFIG:append:pn-python3 = "  tk"`, `LICENSE_FLAGS_ACCEPTED += "commercial"`) | `DISTRO_FEATURES:append` → **FRAG-OEROS** (`oeros/distro-features/visualization`, landed). `PACKAGECONFIG:append:pn-python3` → **not part of the fragment** (§ 2.2): moved to `meta-oeros/recipes-devtools/python/python3_%.bbappend` instead, unconditional (python3 is oe-core, always present, so this needed no new dynamic-layers plumbing — landed). Note `LICENSE_FLAGS_ACCEPTED += "commercial"` here is a strict subset of the existing `allow-commercial-licenses.conf` fragment's `"commercial synaptics-killswitch"` — the `visualization` fragment's description directs users to also enable `oeros/allow-commercial-licenses` rather than re-adding `LICENSE_FLAGS_ACCEPTED` a second time. |
| `world.yml` | `local_conf_header.world` (`DISTRO_FEATURES:append = " opencl"`) | **FRAG-OEROS**, trivial one-line fragment. |
| `diskmon.yml` | `local_conf_header.diskmon` (`BB_DISKMON_DIRS`) | **FRAG-OEROS**. Purely a build-host resource-management setting, no ROS/product coupling — a clean, uncontroversial single-purpose fragment. |
| `limit-pressure.yml` | `local_conf_header.bb_limit_pressure` (`BB_NICE_LEVEL`, `BB_PRESSURE_MAX_*`) | **FRAG-OEROS**, same reasoning as diskmon. |
| `buildci.yml` | `local_conf_header.buildci` (`INHERIT += "buildhistory"`, `buildstats`, `buildstats-summary`, `image-buildinfo`) | **FRAG-OEROS**. All four are `INHERIT +=` accumulations — order-independent per § 1.5's `tasklogger` analysis. |
| `awsci.yml` | `repos.tegra-demo-distro` (meta-demo-ci sub-layer), `local_conf_header.awsci` (mirror URLs, `INHERIT += "own-mirrors"`, `INHERIT:append = " mirror_updates"`, `SSTATE_MIRRORS`, `PARALLEL_MAKE`/`BB_NUMBER_THREADS` host-sizing, a commented-out `FETCHCMD_s3` line) | **KAS-ONLY / CI-infrastructure-only — recommend DROP from the fragment/bitbake-setup surface entirely.** This file references S3 bucket names (`s3://oeros-bitbake-cache/...`) and implicitly depends on AWS credentials being present in the CI runner's environment (the commented-out `FETCHCMD_s3` line and `scripts/install_awscli.sh` in this repo both point at that). It is infrastructure secrets/topology, not product configuration — publishing it as a `meta-oeros` fragment would put CI bucket names in a public, general-purpose layer that regular users' `bitbake-config-build list-fragments` output would show. See § 6 for the recommended replacement (CI-runner-local `local.conf` injection, kept entirely outside version-controlled fragment layers). The `tegra-demo-distro`/`meta-demo-ci` repo dependency likewise should not appear in any public bitbake-setup registry JSON — it is CI-specific tooling, not a product layer. |

### 1.7 Top-level matrix files (`kas/oeros-*.yml`, `kas/oeros-devel.yml`)

All 63 `oeros-<release>-<ros-distro>-<machine>.yml` files (20 scarthgap + 16 whinlatter + 14 wrynose + 13
master — `oeros-devel.yml` is a separate, 64th file, handled at the end of this section) share one shape:
`header.includes` of exactly
`[yocto/<release>.yml, ros{1,2}/<distro>.yml, machine/<machine>.yml, common.yml, layer/qt5.yml?]`, plus an
optional trailing `repos:` block. **Per § 1.2, the 16 whinlatter files are recommended for deletion outright**
(not migrated in any form), leaving **47 active top-level files (20 scarthgap + 14 wrynose + 13 master)** as
the real starting point for the matrix — the whinlatter row below is kept only as a record of what existed
before removal. **The remaining shape is unnecessary as a physical file per cell** — in a fragment/bitbake-setup
world, the matrix is expressed once as data (§ 3) and a "matrix cell" becomes a generator-produced JSON
`configurations` entry or a generator-produced *reduced* kas file (repos + fragment list only, no per-cell
hand-editing). The distinct `repos:` override patterns actually observed, cataloged exhaustively by parsing all
63 files programmatically:

| Pattern | Files (count) | Classification |
|---|---|---|
| `realsense: {branch: wrynose}` | all 13 `oeros-master-*.yml` files | **BB-SETUP** per-cell `sources` override |
| `lts-mixins: {branch: scarthgap/rust}` | all 20 `oeros-scarthgap-*.yml` files (i.e. every scarthgap cell also pulls in `layer/lts-mixins.yml`, which is *not* included by any wrynose/master cell) | **BB-SETUP**/**KAS-ONLY** (scarthgap stays kas) |
| ~~`qt5: {branch: master}` + `zenoh: {branch: master, commit: 1a3815e2...}`~~ | ~~12 of 16 `oeros-whinlatter-*.yml` files~~ | **N/A — whinlatter files deleted, § 1.2.** Kept here only so the pattern isn't silently lost from the historical record; do not carry into the generator. |
| `zenoh: {branch: master}` | all 14 `oeros-wrynose-*.yml` files | **BB-SETUP** |
| `zenoh: {branch: master, layers: {meta-zenoh: disabled}}` | `oeros-wrynose-noetic-raspberrypi5.yml` only | **BB-SETUP** — this is the one cell (ROS 1 Noetic) where the zenoh *repo* is still fetched (for pin-tracking consistency, presumably) but its layer is deliberately not enabled, since zenoh is a ROS 2 middleware with no ROS 1 integration. The bitbake-setup equivalent: include the `sources.meta-zenoh` entry but omit `meta-zenoh/meta-zenoh` from `bb-layers` for that one configuration. |
| No `repos:` override at all | remaining top-level files (scarthgap non-lts-mixins-only cells already counted above; some `oeros-wrynose-kilted-*` and `oeros-wrynose-lyrical-*` files add `qt5: {branch: master}` alongside zenoh — treat as a variant of the wrynose pattern above) | — |

`oeros-devel.yml` (`yocto/master.yml` + `ros2/rolling.yml` + `machine/qemux86-64.yml` + `common.yml` +
`layer/qt6.yml`, no repo overrides) is a convenience alias for the maintainer's own inner-loop development
config. **Classification: KAS-ONLY**, explicitly excluded from the generated matrix (it is not a product
release combination, it's a personal/development shortcut) — but it should be re-derivable as a one-off
generator invocation (`generate --release master --ros-distro rolling --machine qemux86-64 --addon qt6`) so it
never drifts from the matrix's underlying repo pins, even though it isn't itself a matrix-file row.

### 1.8 Ordering and precedence — general rule derived from source

Combining the bitbake.conf parse order (`site.conf → auto.conf → toolcfg.conf → local.conf → addfragments →
machine.conf → machine-sdk.conf → distro.conf → defaultsetup.conf → …`, confirmed by reading
`meta/conf/bitbake.conf` directly) with kas's own local.conf-generation behavior (`local_conf_header` text is
written straight into `local.conf`, which parses *before* `addfragments` runs) gives one precedence table that
governs every migration decision above:

| Assignment operator | Behavior when moved from `local_conf_header` (parses before fragments) to a fragment (parses after local.conf, before machine/distro conf) |
|---|---|
| `VAR = "x"` / `VAR ?= "x"` | **Safe, functionally stronger.** A fragment's hard `=` will always win over anything set earlier (local.conf or another fragment listed before it in `OE_FRAGMENTS`); a `?=` in a fragment only takes effect if nothing set it already — identical semantics to today, just evaluated slightly later. |
| `VAR += "x"` / `VAR:append = "x"` | **Safe, order-independent for final value**, *except* that `bitbake-getvar`'s variable-history attribution (and hence the validation framework's per-variable diff, § 4) will show the fragment's contribution landing after local.conf's, which is correct and matches reality — this is a presentation detail, not a correctness one. |
| `VAR:remove = "x"` | **Safe.** `:remove` is resolved lazily at variable-expansion/finalize time regardless of which conf file contributed it, confirmed for the QA and `INHERIT_DISTRO` cases above; moving these from local.conf to a fragment changes nothing about their effect. |
| Built-in `machine/<name>`, `distro/<name>` | **Requires the pre-existing assignment (if any) to be weak (`?=`) or absent**, or bitbake fatals with "already got an assignment." kas already emits weak `MACHINE ??=`/`DISTRO ??=`, so this is safe *for kas-authored configs specifically*; a hand-written `local.conf` with a hard `MACHINE = "..."` would need to be edited first (call out in user-facing migration docs, not a generator concern). |

No construct found anywhere in the tree requires reordering relative to `machine.conf`/`distro.conf`
themselves, since both `local_conf_header` (today) and fragments (proposed) parse strictly before those two —
this was the one ordering question genuinely worth checking and it resolves cleanly in favor of the fragment
approach being a drop-in replacement, *modulo* the two release-specific renamed-variable breaks in § 0.2.

### 1.9 Making meta-ros distro-agnostic and retiring `DISTRO=ros1`/`DISTRO=ros2`

**Finding: meta-ros's ROS-version-selection machinery is already distro-agnostic (§ 0.7); the only real
coupling is two thin files.** `meta-ros1/conf/distro/ros1.conf` and `meta-ros2/conf/distro/ros2.conf` are each
three real lines — a `DISTRO_NAME`, a `require conf/distro/include/ros-common.inc`, and (ros1 only) a commented-out
`SKIP_RECIPE`. Nothing else in either `meta-ros1`, `meta-ros2`, `meta-ros1-noetic`, any `meta-ros2-<distro>`, or
`meta-ros-common` references `DISTRO`. Confirmed by grepping the whole `meta-ros` tree for `DISTRO`-conditional
overrides and finding only `:ros1-distro`/`:ros2-distro` — a *different*, already-distro-agnostic override
namespace (§ 0.7) — so removing `DISTRO=ros1`/`ros2` cannot silently break those overrides.

**What `ros-common.inc` (in `meta-ros-common`, `require`d by the two files being retired) actually sets, and
where each piece should go:**

| Content | Nature | Recommended disposition |
|---|---|---|
| `DISTRO_CODENAME = "${ROS_DISTRO}"`, `DISTRO_VERSION = "${ROS_DISTRO_METADATA_VERSION}${ROS_DISTRO_VERSION_APPEND}"` | ROS-metadata-derived versioning — genuinely ROS-specific | Keep in `meta-ros-common`, but **change the hard `=` to weak `?=`/`:append`** so an opt-in `require` from a consuming distro doesn't silently clobber that distro's own versioning. **Open question for the maintainer** (§ 6): should `oeros`'s `DISTRO_VERSION`/`DISTRO_CODENAME` track `ROS_DISTRO` (rebuilding for a different ROS distro changes the reported OS version/codename), or should `oeros` keep its own independent release identity (it already sets `DISTRO_VERSION = "2026.04"` / `DISTRO_CODENAME = "alpha"` today, unconditionally, with no ROS coupling at all)? This spec does not pick one — it's a product decision, not a technical one. |
| `ROS_CONNECTION_MANAGER ??= "connman"` + `IMAGE_INSTALL:append = " ${ROS_CONNECTION_MANAGER}"` | ROS-specific (network manager default for a ROS-capable image) | Keep in `meta-ros-common`, already weak (`??=`) — no change needed. |
| `INIT_MANAGER = "systemd"` (hard) | **Not ROS-specific — general Yocto distro policy.** ROS itself has no opinion on init system. | **Remove from meta-ros entirely.** This is exactly the content already headed into the `oeros/systemd` fragment (§ 1.6) — meta-ros currently duplicates it as an unconditional hard-set, which is both redundant with, and a silent-override risk against, whatever `oeros`/its fragments decide. |
| `MAINTAINER = "Rob Woolley <...>"` | Per-project/organizational, not a component-layer concern | **Remove.** A reusable layer should never hard-set this; `meta-oeros/conf/distro/oeros.conf` already sets its own. |

**Scope of the change, stated precisely to avoid the ambiguity revision 2 originally had:** this touches
**exactly two files**, three lines of real content each, and nothing else. **No layer is deleted, merged, or
restructured.** `meta-ros1`, `meta-ros2`, `meta-ros1-noetic`, every `meta-ros2-<distro>`, and `meta-ros-common`
keep every recipe, class, `dynamic-layers/` entry, and — critically — the `BBFILE_COLLECTIONS`/`BBFILE_PRIORITY`
declarations in each layer's own `conf/layer.conf` that keep a same-named recipe generated for two different
ROS distros from colliding. That separation lives entirely in `conf/layer.conf` and the `ROS1_DISTRO`/
`ROS2_DISTRO`/`ROS_DISTRO_TYPE`-driven override chain (§ 0.7) — neither file being deleted here touches either
mechanism. **Each of the following must be applied once per active `meta-ros` branch** (`wrynose` and `master`
per § 0.1 — the two files' content is confirmed identical on both, § Appendix), the same way any other
per-release content change in this migration works, not as a one-time change on a single branch:

**Concrete change list:**

1. Delete `meta-ros1/conf/distro/ros1.conf` and `meta-ros2/conf/distro/ros2.conf` — their entire content, in
   full, is:
   ```
   DISTRO_NAME = "Robot Operating System (ROS)"          # or "...2 (ROS 2)" for ros2.conf
   require conf/distro/include/ros-common.inc
   ```
   Nothing else is in either file. `meta-ros1`, `meta-ros2`, `meta-ros1-noetic`, every `meta-ros2-<distro>`, and
   `meta-ros-common` **remain fully intact as layers** — they simply stop being *distros themselves*, which is
   the whole point: a distro-agnostic component layer doesn't ship a `conf/distro/*.conf`, a distro
   implementation (`meta-oeros`) does.
2. Trim `meta-ros-common/conf/distro/include/ros-common.inc` per the table above (weaken the two versioning
   assignments, delete `INIT_MANAGER` and `MAINTAINER`). The file stays in `meta-ros-common` as an **optional**
   include — anyone's distro.conf may `require` it, meta-ros no longer forces it via its own distro.conf.
3. `meta-oeros/conf/distro/oeros.conf` becomes the **only** place `DISTRO` is defined across the whole matrix.
   Optionally `require conf/distro/include/ros-common.inc` for the trimmed ROS-versioning defaults (maintainer
   decision above); either way, add `ros-common-layer` to `LAYERDEPENDS_oeros` (currently `"core
   openembedded-layer meta-python virtualization-layer"`) once `oeros.conf` structurally depends on anything in
   `meta-ros-common`, for correct dependency declaration.
4. `kas/ros1/noetic.yml` and all five `kas/ros2/*.yml` files drop `distro: "ros1"`/`"ros2"` entirely (§ 1.3) —
   they become pure layer-selection + `target:` files. `distro: "oeros"` moves to the one place shared by every
   matrix cell (the fragment equivalent of `common.yml`, or the built-in `distro/oeros` fragment on
   fragment-capable releases — proven mechanism, § 2.5) instead of being repeated six times.

(Revision 2 originally listed a fifth step here, "update `LAYERSERIES_COMPAT_*`" — **retracted**, § 0.6:
`LAYERSERIES_COMPAT` already correctly differs per `meta-ros` branch; there was never anything to update.)

**What this does *not* require touching:** any `:ros1-distro`/`:ros2-distro` override usage
(`ros_insane_dev_so.bbclass`, the turtlebot3 packagegroups, `ros1-image-sdktest.bb`), any recipe, any class, any
`dynamic-layers/` mapping, and none of the per-ROS-distro recipe separation — all confirmed above to be
entirely independent of `DISTRO`, so none of it changes behavior or requires any edit when `DISTRO` becomes
`oeros`.

---

## 2. Fragment design rules

### 2.1 Naming and namespace

A fragment's global identity is **`<BBFILE_COLLECTIONS-name-of-its-layer>/<relative-path-under-conf/fragments,
without-.conf>`** (verified directly: a fragment at `meta-scratch/conf/fragments/oeros/ros2-jazzy.conf` in a
layer whose `BBFILE_COLLECTIONS` is `scratch` is addressed as `scratch/oeros/ros2-jazzy`, and
`bitbake-config-build list-fragments` reported it exactly that way). Concretely:

- `meta-ros-common` (`BBFILE_COLLECTIONS_ros-common-layer`) → fragments read as **`ros-common-layer/...`**
- `meta-oeros` (`BBFILE_COLLECTIONS_oeros`) → fragments read as **`oeros/...`**

Recommended subdirectory scheme under `conf/fragments/` in each layer (directories are purely organizational —
they become part of the fragment name, so pick them once and treat renames as breaking changes, same as any
other public API):

```
meta-oeros/conf/fragments/
  license/            allow-commercial-licenses.conf (existing, unchanged)
  qa/                  allow-license-exists.conf, allow-license-format.conf (temporary)
  distro-features/     systemd.conf, visualization.conf, world.conf, virtualization.conf
  build-host/          diskmon.conf, limit-pressure.conf
  ci/                  buildci.conf   (NOT awsci — see § 1.6/§ 6)
  packageconfig/       (only if split finer than the existing single packageconfig.inc — see § 2.2)

meta-ros-common/conf/fragments/
  zenoh/               zenoh-workarounds.conf   (pending § 1.5/§ 6 placement decision)
  toolchain/           fortran-runtime.conf, clang-libomp.conf
  tasklogger/           tasklogger.conf
```

### 2.2 Fragments vs. bbappends — a correction made during implementation

**Revision 4 correction.** The zenoh workaround (`INSANE_SKIP:pn-zenoh-c`, `DEBUG_PREFIX_MAP:remove:pn-zenoh-c[-native]`)
was originally implemented as a `meta-ros-common/conf/fragments/zenoh/zenoh-workarounds.conf` config fragment,
mechanically carrying forward its kas `local_conf_header` shape. This was wrong, caught in review: the
`:pn-zenoh-c` override syntax is itself the signal that this is **recipe-specific** content, not build-wide
distro policy, and `meta-ros2` already ships `dynamic-layers/meta-zenoh/recipes-connectivity/zenoh-c/zenoh-c_%.bbappend`
— a `BBFILES_DYNAMIC`-activated bbappend that applies automatically whenever `meta-zenoh` is enabled, with no
separate opt-in step. The fix, implemented and verified against the real `zenoh-c` recipe (§ Appendix): delete
the fragment, add the two lines to the existing bbappend instead — and drop the `:pn-zenoh-c`/`:pn-zenoh-c-native`
override suffixes entirely, since a bbappend is already scoped to its recipe and an unscoped assignment inside
it applies to both the target recipe and its `-native` `BBCLASSEXTEND` variant (confirmed live: a single
unscoped `DEBUG_PREFIX_MAP:remove = "-fcanon-prefix-map"` removed the flag from both `zenoh-c` and
`zenoh-c-native`).

**General rule, apply everywhere in § 1, not just to zenoh: a `local_conf_header` line carrying a `:pn-<recipe>`
(or `:pn-<recipe>-native`) override is recipe-specific and belongs in a `.bbappend` for that recipe, not in a
config fragment**, provided the consuming layer has (or can get) the necessary dynamic-layers/`BBFILES_DYNAMIC`
plumbing for the recipe's providing layer. A fragment is for build-wide/distro policy — `DISTRO_FEATURES`,
`INHERIT`, `LICENSE_FLAGS_ACCEPTED`, machine/build-host settings — content with no single recipe as its natural
home. This reclassifies § 2.3's `packageconfig.inc` split: `PACKAGECONFIG:append:pn-clang`,
`:pn-qtbase-native`, and `:pn-python3` should become bbappends (`meta-clang`'s `clang` recipe,
`meta-qt5`'s `qtbase-native`, oe-core's `python3`), not `packageconfig/*.conf` fragments as originally proposed
below. Unlike zenoh, `meta-oeros` does not yet have `dynamic-layers/` `BBFILES_DYNAMIC` entries for `meta-clang`
or `meta-qt5`, so this is genuine additional plumbing work, not just moving three lines — tracked as still
deferred (§ 2.3), now for this reason as well as the behavior-change reason already given there.

### 2.3 Granularity: one concern per fragment

The existing `meta-oeros/conf/distro/include/packageconfig.inc` bakes three unrelated `PACKAGECONFIG:append`
lines (clang/libomp, qt5/gui, python3/tk) into one unconditional `require`. This is the opposite of fragment
granularity: today those three are only relevant when `layer/clang.yml`, `layer/qt5.yml`, and
`visualization.yml` respectively are in play, but `oeros.conf` applies all three to every build regardless.
**Superseded by § 2.2**: these should become bbappends (`meta-oeros/dynamic-layers/meta-clang/...`,
`meta-oeros/dynamic-layers/meta-qt5/...`, and a plain `recipes-devtools/python3/python3_%.bbappend` for the
oe-core-provided `python3`, which needs no dynamic-layers gating since oe-core is always present), not fragments
— revision 1's `packageconfig/clang-libomp.conf` etc. proposal below is kept struck through for the record, not
as the plan. Either way, splitting out of the unconditional `require` remains a behavior change for `meta-oeros`
(today unconditional; under bbappends/fragments, opt-in — a bbappend only "opts in" via whether the providing
layer, eg `meta-clang`, is enabled at all, which is a coarser but still real behavior change from always-on),
which must be called out explicitly to whoever currently consumes `meta-oeros` directly — flagged in the
Migration Plan as a required communication step, not a silent cleanup.

~~**Recommendation:** split into three one-line fragments (`packageconfig/clang-libomp.conf`,
`packageconfig/qt5-gui.conf`, `packageconfig/python3-tk.conf`) so `list-fragments`/`enable-fragment` can
toggle each independently, matching the modularity the kas tree already has.~~ (Superseded by § 2.2.)

Rule of thumb used throughout § 1: **a fragment should correspond to exactly one kas `local_conf_header` key
today** (i.e., one entry in the alphabetically-sorted dict), *except* where two kas keys were shown to be
byte-identical duplicates (§ 1.4's Jetson/virtualization case), which collapse into one fragment.

### 2.4 Required metadata

Every fragment must carry, at minimum, the two variables `bitbake-config-build` hard-requires (verified: their
absence is a `bb.fatal` at discovery time, so a missing-metadata fragment is not silently ignored — it breaks
`list-fragments`/`enable-fragment` for the *entire layer*, not just the one file):

```
BB_CONF_FRAGMENT_SUMMARY = "One line, imperative, <=72 chars"
BB_CONF_FRAGMENT_DESCRIPTION = "One or more lines, full sentences, wrapped with \\ line continuations. \
  State what it does AND why/when to use it — this is what a user reads before enabling it."
```

Plus a copyright header matching the existing `meta-oeros` convention (`# Copyright (c) 2026 Wind River
Systems, Inc.`) or `meta-ros-common`'s existing per-file convention (check the specific file being touched —
`meta-ros-common` files are not uniformly copyrighted the same way `meta-oeros` files are; do not invent a new
header style, match whichever layer the fragment lands in).

### 2.5 Three entry paths must converge — verified

| Entry path | Mechanism | Verified |
|---|---|---|
| kas | `local_conf_header` appending to `OE_FRAGMENTS` directly (kas has no native "enable this fragment" verb; the matrix generator emits a reduced kas file whose `local_conf_header` is exactly `OE_FRAGMENTS += "layer/path/name ..."`) | Confirmed `kas dump` merges and writes `local_conf_header` text into `local.conf` unchanged; `local.conf`'s `OE_FRAGMENTS += "..."` is picked up by the same `addfragments` statement bitbake-setup and manual `enable-fragment` also drive — all three converge on identical `OE_FRAGMENTS` processing in `ast.py`, not three different mechanisms. |
| `bitbake-config-build enable-fragment <name>` (manual git path) | Writes `OE_FRAGMENTS += "<name>"` into `conf/toolcfg.conf` | Live-tested end to end (§ Appendix): `enable-fragment scratch/oeros/ros2-jazzy` correctly wrote `toolcfg.conf` and `bitbake-getvar` showed the fragment's variables in effect. |
| bitbake-setup | `oe-fragments` / `oe-fragments-one-of` in the registry JSON → also writes `OE_FRAGMENTS += "..."` into the generated `conf/toolcfg.conf` | Live-tested end to end (§ Appendix): a bitbake-setup-generated project's `toolcfg.conf` was byte-for-byte the same shape as the manually-generated one, and `bitbake-getvar MACHINE` returned the fragment-selected value with correct varhistory attribution to `toolcfg.conf`. |

Because all three paths write into the *same* `OE_FRAGMENTS` variable via the *same* `addfragments` statement
in `bitbake.conf`, convergence is not something that needs ongoing testing per se — it is a structural property
of the mechanism. What the validation framework (§ 4) actually needs to test is that each path's *generator*
(kas file generator, bitbake-setup JSON generator, manual-path documentation) selects the *same set* of
fragment names for a given matrix cell, which is a matrix/generator-design correctness question, not a
bitbake-mechanism question.

---

## 3. Matrix file and generators

### 3.1 Schema

A single YAML file, `matrix.yml`, is the source of truth. Structure (worked example below is complete and
covers the requested wrynose × jazzy × raspberrypi5 cell with the qt5 add-on and the zenoh branch override):

```yaml
schema_version: 1

releases:
  scarthgap:
    yocto_version: "5.0"
    fragment_capable: false
    bitbake_branch: "2.8"
    oe_core_commit: "ece80784b493c8b7493478fa2ba0dc1d6d80aa79"
    bitbake_commit: "82abbfcdbda949851a03bb2cb2049ea689564ad6"
    meta_openembedded_commit: "b0c2c648a1af89e7a8dd4c2ec841f3bc0ed0ccb9"
    qa_relax_var: "WARN_QA"          # per-release spelling, see §1.2
  # whinlatter removed: end-of-life upstream, no longer a target (§0.1, §1.2)
  wrynose:
    yocto_version: "6.0"
    fragment_capable: true
    builtin_fragments: true
    bitbake_setup_available: true
    bitbake_branch: "2.18"
    oe_core_commit: "06dd66e6220e5ce4ed4b9af4d8231ae5f0a8ce80"
    bitbake_commit: "22021758e66737bcf68dfd2b74adc6a0cb1d42d9"
    meta_openembedded_commit: "100027977216601000cbefc42c2ff6cf667e7b5e"
    extra_layers: ["meta-yocto"]     # poky/meta-yocto-bsp, see §1.2 open question
    qa_relax_var: "ERROR_QA"
  master:
    yocto_version: "devel"
    fragment_capable: true
    builtin_fragments: true
    bitbake_setup_available: true
    bitbake_branch: "master"
    qa_relax_var: "ERROR_QA"
    qa_relax_flag: "license-format"   # differs from release branches (license-exists) — §1.2

# DISTRO is "oeros" uniformly across every cell (§0.7, §1.9) — no per-release or per-ros-distro
# fork needed. distro_features_opted_out_var is likewise a single global constant now that
# whinlatter is dropped: "DISTRO_FEATURES_OPTED_OUT" (§0.2).
distro: oeros
distro_features_opted_out_var: "DISTRO_FEATURES_OPTED_OUT"

ros_distros:
  ros1-noetic:
    family: ros1
    layers: [meta-ros-common, meta-ros1, meta-ros1-noetic]
  ros2-jazzy:
    family: ros2
    layers: [meta-ros-common, meta-ros2, meta-ros2-jazzy]
  # ...humble, kilted, lyrical, rolling identically shaped — none of these carry a distro_var
  # any more; ROS-version selection is pure layer selection (§0.7, §1.9)

machines:
  raspberrypi5:
    layers: [meta-raspberrypi]
    fragments: [license/allow-commercial-licenses]
  qemux86-64: {}
  jetson-agx-xavier-devkit:
    layers: [meta-tegra, meta-tegra-community, tegra-demo-distro]
    fragments: [distro-features/virtualization]

addons:
  qt5:
    layers: [meta-qt5]
    # qtbase-native/gui PACKAGECONFIG stays in meta-oeros/conf/distro/include/packageconfig.inc
    # for now (§2.2/§2.3) -- no fragment or bbappend entry until meta-oeros gains dynamic-layers
    # plumbing for meta-qt5.
  zenoh:
    layers: [meta-zenoh]
    # No fragment: the zenoh-c workarounds live in meta-ros2's own
    # dynamic-layers/meta-zenoh/.../zenoh-c_%.bbappend and apply automatically once meta-zenoh
    # is enabled, so this addon only needs to select the layer (§2.2).

cells:
  - release: wrynose
    ros_distro: ros2-jazzy
    machine: raspberrypi5
    addons: [qt5, zenoh]
    source_overrides:
      zenoh: { branch: master }             # matches oeros-wrynose-jazzy-raspberrypi5.yml today
```

`cells` is the enumeration that today lives implicitly as "one file per combination"; everything else is
looked up. A cell whose `release` is not `fragment_capable` is generated as a full legacy kas file (unchanged
shape); a `fragment_capable` cell is generated as a *reduced* kas file (repos + `OE_FRAGMENTS` only) **and** a
bitbake-setup JSON `configurations` entry **and** a manual-path doc snippet, from the same cell data.

### 3.2 Generators

Two generator scripts, both pure functions of `matrix.yml` (plus, for pin updates, live `git ls-remote` calls
against the `floating`-branch repos, isolated to an explicit `update-pins` subcommand so ordinary generation
runs are fully offline and deterministic):

- `generate-kas.py`: for each cell, emits `kas/oeros-<release>-<ros>-<machine>[-<addon>...].yml`. For
  fragment-capable releases the emitted file's `local_conf_header` is reduced to one key,
  `OE_FRAGMENTS += "..."` (built-in machine/distro fragment names plus the resolved addon/machine fragment
  names), and `repos:` carries only pins + `source_overrides`. For non-fragment-capable releases, the emitted
  file is unchanged in shape from today (kept byte-comparable, see § 3.3).
- `generate-bitbake-setup.py`: for each fragment-capable cell (or, more naturally, one JSON *file per release*
  with one `configurations` entry per cell, matching the existing `oeros-master-rolling.conf.json` precedent
  already in `meta-oeros`), emits the `sources`/`bb-layers`/`oe-fragments`/`oe-fragments-one-of` structure,
  validated against `bitbake/setup-schema/bitbake-setup.schema.json` as a generation-time check (schema
  validation is cheap and catches typos before they reach a user).
- `generate-docs.py` (or a shared template step inside the above two): emits the manual git-clone +
  `oe-init-build-env` + `bitbake-config-build enable-fragment ...` instructions per cell, so the three paths
  are generated from one place rather than hand-maintained in parallel.

### 3.3 Drift detection

A CI check (`check-matrix-drift`) regenerates everything from `matrix.yml` into a scratch directory and diffs
against the committed `kas/` tree and `meta-oeros`/`meta-ros-common` registry JSON files. For legacy releases
the diff should be **byte-identical**, since the file shape is unchanged — this is a strong, cheap regression
test that the migration hasn't altered anything it didn't mean to. For fragment-capable releases the diff is
necessarily non-trivial on first migration (old shape → new reduced shape) but should be byte-identical on
every commit *after* that, and the check should fail the build the moment someone hand-edits a generated file
instead of `matrix.yml`. Recommend a marker comment (`# GENERATED FROM matrix.yml — DO NOT EDIT`) at the top of
every generated file, checked by the same CI step.

---

## 4. Validation framework

### 4.1 Extraction method — chosen and justified empirically

Two candidate build-free extraction methods were tested against a real checkout:

- **`kas dump --skip repos_checkout --skip repos_apply_patches`**: fully resolves the *kas-level* merged
  configuration (repos with commit/branch overrides, `local_conf_header` merged and sorted, `machine`/`distro`/
  `target`) without a full working-tree checkout. **However**, this was observed to still perform lightweight
  network operations (`git ls-remote`/fetch) for any repo not already present in `KAS_WORK_DIR` with a resolved
  commit — repos pinned by explicit `commit:` were served from cache with no network call ("Repository
  openembedded-core already contains ... as commit"), but repos pinned only by floating `branch:` triggered a
  fetch ("Repository clang updated"). **This is not build-free in the strict sense of zero network I/O**, but
  it is checkout-free and task-free, and it is by far the cheapest way to extract kas-level repo/pin/local_conf
  truth. It cannot see actual bitbake variable values (`DISTRO_FEATURES` etc. are opaque text inside
  `local_conf_header` strings at this level) — it validates the *kas config*, not the *bitbake config*.
- **`bitbake-getvar`**: confirmed via `strace`-free direct observation of its source
  (`tinfoil.prepare(quiet=2, config_only=True)`) and live testing that it parses only configuration files
  (`bitbake.conf`, `local.conf`, `layer.conf`, `machine.conf`, `distro.conf`, fragment `.conf` files) and
  **never** parses or executes a recipe or task — confirmed by triggering a real `BB_RENAMED_VARIABLES` parse
  error (§ 0.2) purely from config-file content, and by the tool completing in ~1–2 seconds against a two-layer
  tree with no network access once repos are checked out locally. Critically, its output includes full
  **variable history** — every contributing file, line number, and operation (`set`, `:append`, `:remove`),
  plus the pre-expansion and final value — which is exactly the per-variable, per-source diff format § 4.5's
  report needs, for free.

**Chosen design**: a two-tier extraction pipeline per matrix cell.

1. **Tier 1 (pin/config check, network-light, seconds):** `kas dump --skip repos_checkout --skip
   repos_apply_patches --sort` for the kas-generated project; direct parse of the generated bitbake-setup JSON
   (no tool invocation needed — it's already the ground truth for that side) for the bitbake-setup project.
   Compares: repo URLs, branches, commits, `BBLAYERS`/`bb-layers` membership and order.
2. **Tier 2 (bitbake variable check, requires real repo checkouts, config-only bitbake parse):** actually run
   `kas checkout` (real clone, no build) for the kas side, and `bitbake-setup init --non-interactive` (real
   clone, no build) for the bitbake-setup side, into two independent directories sharing one git object cache
   (§ 4.3). Then run `bitbake-getvar <VAR>` for each variable in the curated set (§ 4.2), against each project's
   `build/` directory, and diff the `<VAR>=<value>` lines. **No `bitbake -e` fallback was needed** —
   `bitbake-getvar` covers every variable in the curated set including list-typed ones (`BBLAYERS`,
   `DISTRO_FEATURES`); `bitbake -e | grep` was considered per the brief's explicit suggestion but rejected as
   strictly worse (slower — parses and prints the *entire* datastore — and its plain-text `grep` extraction is
   more fragile than `bitbake-getvar`'s structured single-variable output, which was designed for exactly this
   use case).

### 4.2 Curated variable set

Derived exhaustively from every `local_conf_header`, `machine:`, `distro:` construct cataloged in § 1, plus the
fixed set named in the brief:

```
MACHINE  DISTRO  DISTRO_FEATURES  DISTRO_FEATURES_DEFAULTS  DISTRO_FEATURES_OPTED_OUT  IMAGE_FEATURES
INHERIT  INHERIT_DISTRO  BBLAYERS  OE_FRAGMENTS
LICENSE_FLAGS_ACCEPTED  TEMPLATECONF  WARN_QA  ERROR_QA
VIRTUAL-RUNTIME_init_manager
PACKAGECONFIG:pn-clang  PACKAGECONFIG:pn-qtbase-native  PACKAGECONFIG:pn-python3
FORTRAN  RUNTIMETARGET:pn-gcc-runtime  HOSTTOOLS
INSANE_SKIP:pn-zenoh-c  DEBUG_PREFIX_MAP:pn-zenoh-c  DEBUG_PREFIX_MAP:pn-zenoh-c-native
BB_DISKMON_DIRS  BB_NICE_LEVEL  BB_PRESSURE_MAX_CPU  BB_PRESSURE_MAX_IO  BB_PRESSURE_MAX_MEMORY
```

(`bitbake-getvar` takes the *base* variable name and reports overrides/appends in its history output, so
`:pn-foo`-suffixed entries above are queried as their base name — e.g. `bitbake-getvar PACKAGECONFIG` — and the
per-recipe override is visible in the returned variable history, not as a separate query.) The list is
generated, not hand-maintained: `generate-validation-varlist.py` walks `matrix.yml`'s resolved fragment set for
a cell and extracts every left-hand-side variable name textually, so it grows automatically as fragments are
added.

### 4.3 Directory layout, caching, network expectations

```
validation/
  cache/
    git-mirror/            # bare mirrors of oe-core, bitbake, meta-openembedded, meta-ros, meta-oeros, ...
                            # populated once with `git clone --mirror`, updated with `git remote update`
  cells/
    <cell-id>/
      kas/
        workdir/            # KAS_WORK_DIR, repos cloned with --reference against cache/git-mirror
      bitbake-setup/
        top-dir/             # bitbake-setup top directory, repos symlinked/cloned with --reference equivalent
                              # (bitbake-setup has no native --reference flag; use git's global
                              # $GIT_ALTERNATE_OBJECT_DIRECTORIES or per-cell `--source-overrides` pointing
                              # at the mirror as a local `git-remote.uri` — verify against a real multi-repo
                              # bitbake-setup run before relying on this for the full-matrix tier)
      report.json
  report.json               # aggregate across all cells
  report.md                 # human summary
```

**Network expectations**: initial mirror population is one clone per unique upstream repo. Enumerated exactly
by parsing every `repos:` key across the whole `kas/` tree programmatically: `openembedded-core`, `bitbake`,
`meta-openembedded`, `meta-yocto`, `ros` (meta-ros, multiple sub-layers, one clone), `clang`, `clang-revival`,
`lts-mixins`, `python-ai`, `qt5`, `qt6`, `realsense`, `tasklogger`, `virtualization`, `zenoh`, `raspberrypi`,
`meta-tegra`, `meta-tegra-community`, `tegra-demo-distro`, `polarfire` — **20 distinct upstream repos total
across the whole matrix, not per cell.** Then every cell's Tier 2 checkout
clones from the *local* mirror (`git clone --reference`) rather than upstream again, since kas supports
`--reference`-style local clone acceleration via its own repo cache reuse when `KAS_WORK_DIR` or a shared
`kas`-managed cache directory is reused across cells (this needs to be a **shared, not per-cell,**
`KAS_WORK_DIR` for the kas side to get this benefit — confirmed by the live test in § Appendix, where the
second `kas dump` invocation against a different cell reused the first cell's already-fetched repos from the
same `KAS_WORK_DIR` with no re-fetch for pinned-commit repos). Tier 1 needs no local mirror at all and can run
against a cold cache; it is the appropriate tier for "did the matrix generator produce something sane" checks
on every PR. Tier 2 needs the mirror and should be reserved for nightly/scheduled full-matrix runs plus an
on-demand single-cell smoke test on PRs that touch fragment content.

### 4.4 Runtime budget and tiers

- **Smoke tier (one cell, on every PR touching `kas/`, `meta-oeros/conf/fragments/`, or `matrix.yml`)**: Tier 1
  for all cells (fast, seconds each, no mirror needed) + Tier 2 for exactly one representative cell (e.g.
  wrynose × jazzy × raspberrypi5, the cell already worked through in this spec). Budget: a few minutes,
  dominated by the one real checkout.
- **Full-matrix tier (scheduled, e.g. nightly)**: Tier 1 for every cell, Tier 2 for every fragment-capable cell
  (legacy/kas-only cells get Tier 1 only, since there is no bitbake-setup counterpart to diff against for them —
  their "validation" is just today's existing kas-build CI, unchanged). Budget dominated by however many
  *distinct* `(release, ros_distro, machine, addon-set)` combinations exist after dedup — **27** fragment-capable
  cells now that whinlatter is dropped (14 wrynose + 13 master, per the file counts in § 1.7; the 20 scarthgap
  cells stay Tier 1 only, per above), each needing one real
  `oe-init-build-env`-equivalent config-only bitbake invocation per side (kas, bitbake-setup) — no recipe
  fetch/parse, so each Tier 2 cell should be low-single-digit seconds of actual bitbake time once repos are
  checked out, with checkout time dominating and amortized by the shared mirror.

### 4.5 Report format

`report.json`, one entry per cell:

```json
{
  "cell": "wrynose-jazzy-raspberrypi5",
  "tier1": {"pass": true, "repo_diffs": []},
  "tier2": {
    "pass": false,
    "variable_diffs": [
      {
        "variable": "DISTRO_FEATURES_OPTED_OUT",
        "kas_value": null,
        "kas_history": [],
        "bitbake_setup_value": "sysvinit",
        "bitbake_setup_history": [{"file": ".../oeros/systemd.conf", "line": 4, "op": "append"}]
      }
    ]
  }
}
```

`report.md` summarizes pass/fail counts per tier and release, and prints the human-readable form of any
`variable_diffs` (reusing `bitbake-getvar`'s own multi-line history formatting, already close to ideal for this
purpose as observed in every live test above).

---

## 5. Migration plan

**Phase 0 — prerequisite fixes and cleanup (blocking, before any fragment work is validated on wrynose/master).**
Four independent, parallelizable items, none of which touch fragments yet:
- **0a.** Fix `meta-oeros/conf/distro/oeros.conf`'s `DISTRO_FEATURES_DEFAULT` reference — delete the dead line,
  add `DISTRO_FEATURES_DEFAULTS`/`DISTRO_FEATURES_OPTED_OUT` only if real default-backfill behavior is wanted
  (§ 0.2, exact diff given there). Acceptance: `bitbake-getvar DISTRO_FEATURES` succeeds against a minimal
  `oeros`-distro build tree on wrynose and master (already verified live for this exact fix, § Appendix).
- **0b.** Delete `kas/yocto/whinlatter.yml` and the 16 `kas/oeros-whinlatter-*.yml` files (§ 1.2, § 1.7) — no
  longer a target, upstream EOL. Acceptance: `grep -rl whinlatter kas/` returns nothing.
- **0c.** Retire `DISTRO=ros1`/`DISTRO=ros2` per § 1.9. **Scope: two files only** — delete
  `meta-ros1/conf/distro/ros1.conf` and `meta-ros2/conf/distro/ros2.conf` (three lines of real content each;
  no recipe, class, or layer boundary is affected — `meta-ros1`/`meta-ros2`/`meta-ros1-noetic`/every
  `meta-ros2-<distro>`/`meta-ros-common` remain exactly as they are otherwise). Trim
  `meta-ros-common/conf/distro/include/ros-common.inc` (weaken versioning assignments, drop
  `INIT_MANAGER`/`MAINTAINER`); decide and implement whether `meta-oeros` `require`s the trimmed include
  (§ 1.9's open versioning question must be settled first); add `ros-common-layer` to `LAYERDEPENDS_oeros`.
  **Apply on both the `wrynose` and `master` branches of `meta-ros`** (§ 0.1's active target set; content
  confirmed identical on both branches today, § Appendix) — this is a per-branch content change like any other
  in this migration, not a one-time edit. Acceptance: a build tree with `DISTRO=oeros` and any one
  `meta-ros2-<distro>` layer enabled parses cleanly with `bitbake-getvar DISTRO`/`ROS_DISTRO` both resolving
  correctly; per § 6 item 9, follow with an actual `ros-image-core` build, not just a config-parse check, before
  treating this as validated.
- **0d.** Fix `kas/systemd.yml`'s `DISTRO_FEATURES_BACKFILL_CONSIDERED` → `DISTRO_FEATURES_OPTED_OUT` (§ 0.2,
  exact diff given there) — folded into Phase 1 authorship of the `systemd` fragment rather than fixed in kas
  form first, since the kas file is being retired anyway once whinlatter is gone and wrynose/master are the
  only targets left to carry it.

**Phase 1 — land fragments with no consumer changes.** Author the `meta-oeros`/`meta-ros-common` fragments
cataloged in § 1 (starting with the already-proven `allow-commercial-licenses` pattern, then the trivial
single-line ones — `world`, `diskmon`, `limit-pressure`, `buildci` — then `systemd` with the corrected
`DISTRO_FEATURES_OPTED_OUT` spelling from Phase 0d — no per-release fork needed now that whinlatter is gone).
Acceptance: `bitbake-config-build list-fragments` shows every new fragment with correct
summary/description; no existing kas file is touched.

**Phase 2 — matrix file + generators, byte-identical legacy output.** Author `matrix.yml` and the generators
(§ 3); run `generate-kas.py` for scarthgap and diff against today's committed files. Acceptance: zero diff (or
every diff explicitly documented as intentional, e.g. fixing the `raspberrypi5..yml` double-dot typo found in
the current tree, `oeros-scarthgap-lyrical-raspberrypi4-64..yml`).

**Phase 3 — switch fragment-capable kas configs to `OE_FRAGMENTS`.** Regenerate wrynose/master matrix files
from `matrix.yml` in their reduced shape, `distro: oeros` uniformly (§ 1.9). Acceptance: the validation
framework's Tier 2 (§ 4) passes for every fragment-capable cell against the *old* (pre-migration) kas files as
the equivalence baseline.

**Phase 4 — publish bitbake-setup configurations.** Extend `meta-oeros/conf/registry/configurations/` (already
started with `oeros-master-rolling.conf.json`) to cover every fragment-capable cell via the generator.
Acceptance: `bitbake-setup init --non-interactive` succeeds for every published configuration in the smoke
tier; Tier 2 validation passes bitbake-setup vs. kas for the same cells.

**Phase 5 — documentation for all three entry paths.** Regenerate `kas/README.md`'s instructions (today it
documents only kirkstone+humble, already stale relative to the current matrix) plus new bitbake-setup and
manual-git-clone instructions, all generated from `matrix.yml` per § 3.2.

**Phase 6 — CI integration.** Wire drift detection (§ 3.3) and the validation framework's smoke tier into every
PR; full-matrix tier nightly.

**What existing kas users must change: nothing**, for scarthgap or any pre-fragment release — those files are
generated in their current shape, unchanged. For wrynose/master users, the top-level filenames stay the same;
only the internal `local_conf_header`/`repos` shape changes (fewer lines, `OE_FRAGMENTS`-based) — a
`kas build oeros-wrynose-jazzy-raspberrypi5.yml` invocation continues to work unmodified. Users who currently
layer on `kas/systemd.yml` etc. via colon-chaining on non-legacy releases should be told explicitly, once, that
these files become fragments and the colon-chaining stops working the moment Phase 3 lands for their release —
though as established in § 0.3/§ 1.6, **no current top-level file does this today**, so the actual blast radius
of that specific change is zero known consumers, confirmed by exhaustive grep. **Whinlatter users must change
everything** — there is no migration path for them, since the release itself is deleted from the repository
(Phase 0b); this should be called out as a breaking change in the same release notes that announce the
migration, not discovered silently.

---

## 6. Risks and open questions

1. **`ros1`/`ros2` vs `oeros` distro (§ 0.7) — resolved as a direction, one sub-question remains open.**
   `DISTRO=ros1`/`DISTRO=ros2` are being retired and `meta-oeros` becomes the sole distro (§ 1.9). What's not
   yet decided: should `oeros`'s `DISTRO_VERSION`/`DISTRO_CODENAME` track `ROS_DISTRO` (via a trimmed,
   opt-in `require` of `meta-ros-common/conf/distro/include/ros-common.inc`), or keep the independent,
   date-based identity `oeros.conf` already has today (`"2026.04"`/`"alpha"`, no ROS coupling)? This is a
   product decision, not a technical one — this spec's § 1.9 change list is written to work either way, gated
   on this one choice.
2. **`DISTRO_FEATURES_DEFAULT`/`DISTRO_FEATURES_DEFAULTS` rename (§ 0.2) — no longer a per-release fork, but
   still a real one-time fact to encode.** With whinlatter dropped, wrynose and master agree on the new
   spelling (`DISTRO_FEATURES_DEFAULTS`/`DISTRO_FEATURES_OPTED_OUT`), so the matrix no longer needs
   per-release variable-name generation for this construct (§ 3.1 simplified accordingly). No other
   renamed-variable collision was found in the current file set (§ 0.2's exhaustive cross-check against
   oe-core's full `BB_RENAMED_VARIABLES` list turned up only these two, both now fixed), but the generator
   should re-run that cross-check on every oe-core pin bump, not just once — a future rename between wrynose
   and master, or introduced on master ahead of the next stable release, would reintroduce exactly this kind
   of latent break.
3. **Resolved in revision 4, differently than proposed here.** This item originally read: "`meta-zenoh`
   fragment placement conflict — decision #2 says ROS-recipe workarounds belong in `meta-ros-common`; the
   existing, already-shipped `meta-oeros/conf/distro/include/zenoh.inc` puts the same content in `meta-oeros`.
   Recommend the maintainer choose one location and delete the other." That framing was itself wrong: the
   workaround content (`INSANE_SKIP:pn-zenoh-c`, `DEBUG_PREFIX_MAP:remove:pn-zenoh-c[-native]`) was never a
   config-fragment-vs-config-fragment placement question — it's recipe-specific override syntax that doesn't
   belong in *either* location. It now lives in `meta-ros2`'s existing `dynamic-layers/meta-zenoh/.../zenoh-c_%.bbappend`
   (§ 2.2, § 1.5), which both locations' original content is superseded by. `meta-oeros/conf/distro/include/zenoh.inc`
   keeps only `ZENOH_SHARED_MEMORY`/`ZENOH_UNSTABLE_API`, verified against the real `meta-zenoh` source to be
   genuine distro-level policy.
4. **CI-only configuration (`awsci.yml`) should not become a public fragment (§ 1.6).** It embeds S3 bucket
   names and depends on ambient AWS credentials. Recommended replacement: keep it as a CI-runner-injected
   `local.conf` snippet (written by the CI pipeline itself, outside any version-controlled layer), or, if the
   maintainer wants it fragment-shaped for consistency, put it in a **separate, non-published** internal layer
   that is never listed in the public `meta-oeros` registry JSON and never appears in `list-fragments` output
   for ordinary users. Either way, it should not be migrated 1:1 into `meta-oeros/conf/fragments/`.
5. **Retracted in revision 3 — was never a real risk for `meta-ros`.** Revision 1/2 flagged
   `LAYERSERIES_COMPAT = "wrynose"` as an inconsistency across meta-ros layers and proposed "fixing" it in
   Phase 0c. Confirmed by checking out multiple `meta-ros` branches directly (§ 0.6, § Appendix item 11) that
   this is the correct, standard, per-branch OE-layer convention — `scarthgap` declares `"scarthgap"`,
   `wrynose` declares `"wrynose"`, `whinlatter` declared `"whinlatter"`, and `master` correctly declares
   `"wrynose"` since the in-development "blacksail" series hasn't shipped. Nothing to update in `meta-ros`.
   **`meta-oeros` is a separate, open question, though**: its `wrynose` and `master` branches are currently the
   *same commit* (`git rev-parse origin/wrynose origin/master` → identical), with a single
   `LAYERSERIES_COMPAT_oeros = "wrynose"` on both. Unlike `meta-ros`, it's not established whether `meta-oeros`
   is intended to branch per release the same way once wrynose and master diverge, or to stay a single line
   with `LAYERSERIES_COMPAT_oeros` updated in place over time. Worth settling before Phase 4 publishes
   bitbake-setup configurations for both releases from what may need to become two different `meta-oeros`
   branches — this spec does not assume an answer.
6. **`target:` on the bitbake-setup and manual paths (§ 1.3).** bitbake-setup's JSON schema has no field for
   "default build target" — it is a `bitbake <target>` argument the user types, not something a `.conf.json`
   config can pre-select. This must live in generated documentation/description text, not in the schema; there
   is no bitbake-setup mechanism to `DROP` this into, and inventing a non-standard schema extension is
   explicitly not recommended (it would diverge from the upstream schema and break `bitbake-setup list
   --write-json` interoperability with other tooling).
7. **The `meta-yocto` (poky compat) dependency on wrynose is unverified as still-necessary (§ 1.2).** Flagged,
   not resolved — someone with a working wrynose build tree should check whether dropping `meta-poky`/
   `meta-yocto-bsp` from the layer list still builds `ros-image-core`, before the generator hard-codes it as a
   universal wrynose extra layer.
8. **One pre-existing tree defect surfaced incidentally, unrelated to the fragment migration itself but worth
   fixing alongside it** since the matrix/generator work will touch this file anyway: the double-dot typo in
   `kas/oeros-scarthgap-lyrical-raspberrypi4-64..yml`. (Revision 1 also flagged an asymmetric qt5/zenoh override
   pattern in the whinlatter rolling cells — moot now that whinlatter is deleted outright, § 1.2.)
9. **New in this revision: the meta-ros distro-agnostic change (§ 1.9) is untested beyond source-level tracing.**
   Every claim in § 1.9 (no `DISTRO`-conditional coupling outside the two files being deleted, the
   `:ros1-distro`/`:ros2-distro` override chain being independent of `DISTRO`) was verified by reading source
   and grepping the tree, **not** by an actual build with `ros1.conf`/`ros2.conf` removed and `DISTRO=oeros`
   substituted. Phase 0c's acceptance criterion (`bitbake-getvar` parsing cleanly) is a necessary but not
   sufficient check — it confirms the *configuration* resolves, not that every recipe that might have
   implicitly assumed `DISTRO=ros1`/`ros2` (e.g. via a `PREFERRED_PROVIDER` or `BBCLASSEXTEND` keyed off
   something other than the checked override namespaces) still behaves correctly. Recommend an actual
   `ros-image-core` build (not just config parse) as part of Phase 0c's acceptance before treating the
   distro-agnostic change as validated, since this spec's "no builds" constraint (§ Decisions) applies to the
   *fragment-equivalence validation framework*, not to ordinary pre-merge testing of a real code change like
   this one.

---

## Appendix: verification environment

**Host**: Debian GNU/Linux bookworm, Linux 6.1.0-52-amd64. Python 3.11.2.

**Tools installed and their exact versions, in an isolated venv at
`~/Projects/meta-ros-oe-fragments/spec-work/venv`:**
- `kas 5.5` (configuration format version 23, earliest compatible version 1), installed via `pip install kas`
- `bitbake-setup 2.19.0`, installed via `pip install bitbake-setup`

**Repositories cloned for verification** (all under
`/tmp/.../scratchpad/verify/`, not part of the deliverable):
- `openembedded/bitbake.git` — branches inspected: `2.8`, `2.10`, `2.12`, `2.16`, `2.18`, `master`.
  Key commits: `fdb611e13b` (bitbake-config-build fragments plugin, oe-core side, 2024-12-13),
  `3b9d7bea` (built-in fragment support, bitbake `ast.py`, landed between 2.12 and 2.16).
- `openembedded/openembedded-core.git` — branches inspected: `scarthgap`, `walnascar`, `whinlatter`,
  `wrynose`, `master`. `HEAD` of `wrynose` at verification time: `06dd66e6220e5ce4ed4b9af4d8231ae5f0a8ce80`
  (matches this repo's `kas/yocto/wrynose.yml` pin, confirming the checkout used was the same commit already
  pinned by this repository).

**Live commands executed and their key observed output** (full transcripts were produced during this session;
summarized here):
1. `kas dump --skip repos_checkout --skip repos_apply_patches --sort kas/oeros-wrynose-jazzy-raspberrypi5.yml`
   — produced the fully merged repos/local_conf_header/machine/distro/target for a real matrix cell, no build.
2. `kas dump ... "kas/oeros-wrynose-jazzy-raspberrypi5.yml:kas/systemd.yml:kas/visualization.yml"` — confirmed
   colon-chained files merge identically to `header.includes`, and `local_conf_header` keys are emitted in
   alphabetical order (`clang, common, distro, python-ai, qt5, raspberrypi, systemd, virtualization,
   visualization, zenoh`), matching `kas/config.py`'s `sorted(...)` call read directly from source.
3. A scratch layer (`meta-scratch`) with a real `conf/fragments/oeros/ros2-jazzy.conf` carrying
   `BB_CONF_FRAGMENT_SUMMARY`/`DESCRIPTION` was built against `openembedded-core` (`wrynose`) +
   `bitbake` (`2.18`): `bitbake-config-build list-fragments` correctly discovered and named it
   `scratch/oeros/ros2-jazzy`; `bitbake-config-build enable-fragment scratch/oeros/ros2-jazzy` wrote
   `OE_FRAGMENTS += "scratch/oeros/ros2-jazzy"` to `conf/toolcfg.conf`; `bitbake-getvar SCRATCH_ROS_DISTRO`
   returned `"jazzy"` with correct file/line attribution; `bitbake-getvar SCRATCH_TEST_APPEND` showed a
   local.conf `set` followed by the fragment's `:append`, combining to `"base from-fragment"`.
4. `bitbake-config-build enable-fragment machine/qemuarm64` against a build tree whose `local.conf` had
   `MACHINE ??= "qemux86-64"` correctly overrode `MACHINE` to `qemuarm64`, confirming built-in fragments beat
   weak defaults without error.
5. Injecting `DISTRO_FEATURES_DEFAULT = "x"` and, separately, `DISTRO_FEATURES_BACKFILL_CONSIDERED += "sysvinit"`
   into `local.conf` on the wrynose/2.18 tree both produced a hard `ERROR:`
   (`Variable ... is obsolete...` / `Variable ... has been renamed to ...`) and `bb.BBHandledException` from
   `bitbake-getvar`; the same two variables were confirmed present and *not* renamed on the whinlatter/2.16
   tree by grepping `BB_RENAMED_VARIABLES[...]` entries directly out of each branch's `meta/conf/bitbake.conf`.
6. A minimal `bitbake-setup` registry JSON (validated by hand against
   `bitbake/setup-schema/bitbake-setup.schema.json` and `layers.schema.json`) was initialized with
   `bitbake-setup init ./smoke.conf.json -L bitbake <local-checkout> -L openembedded-core <local-checkout>
   --non-interactive`, using local-source symlinking (`-L`) to avoid network cloning. The generated
   `conf/toolcfg.conf` contained `OE_FRAGMENTS += "machine/qemux86-64"`; `bitbake-getvar MACHINE` against the
   generated build directory returned `qemux86-64` with variable history correctly attributing it to
   `toolcfg.conf` line 6, identical in shape to the manual `bitbake-config-build enable-fragment` case in item 4.
7. `kas/` in this repository (`meta-ros-oe-fragments`, branch `build`) contains 104 `.yml` files, not the ~78
   estimated going into this work: 8 `yocto/*.yml`, 6 `ros1/ros2/*.yml`, 8 `machine/*.yml`, 10 `layer/*.yml`,
   8 cross-cutting files (`common`, `systemd`, `visualization`, `world`, `diskmon`, `limit-pressure`,
   `buildci`, `awsci`), 63 top-level `oeros-<release>-<ros>-<machine>.yml` matrix files, and `oeros-devel.yml`.
   Every file in the first six categories (40 files) was read in full. All 63 top-level matrix files (plus
   `oeros-devel.yml`) were additionally parsed programmatically (Python + PyYAML) to extract
   every `repos:` override block exhaustively, producing the table in § 1.7.
8. `LAYERSERIES_COMPAT_*` and `BBFILE_COLLECTIONS` were read directly from `conf/layer.conf` in
   `meta-ros-common`, `meta-ros1`, `meta-ros1-noetic`, `meta-ros2`, `meta-ros2-jazzy`, and `meta-oeros`
   (checked out locally at `~/Projects/meta-ros` and `~/Projects/meta-oeros`, branch `wrynose` for the latter).
9. **(Revision 2)** The `meta-oeros/conf/distro/oeros.conf` fix was verified on the live wrynose/2.18 tree from
   item 5: a scratch `conf/distro/scratch.conf` first reproduced the bug with the naive "rename in place" fix
   (`DISTRO_FEATURES ?= "${DISTRO_FEATURES_DEFAULTS}"` referenced directly) and showed double-counting
   (`sysvinit pulseaudio ... sysvinit pulseaudio` in the resolved `DISTRO_FEATURES`), because oe-core's
   `meta/conf/bitbake.conf:899` (`DISTRO_FEATURES:append = " ${@oe.utils.filter_default_features('DISTRO_FEATURES', d)}"`,
   found via `grep -n DISTRO_FEATURES meta/conf/bitbake.conf` and `meta/lib/oe/utils.py`'s
   `filter_default_features`) already performs this fold-in unconditionally. The corrected fix — deleting the
   `DISTRO_FEATURES ?=` line entirely and setting `DISTRO_FEATURES_DEFAULTS`/`DISTRO_FEATURES_OPTED_OUT`
   directly, with no reference between them — was then verified clean: `bitbake-getvar DISTRO_FEATURES`
   returned `sysvinit` correctly excluded and `pulseaudio` correctly included, exactly once each.
10. **(Revision 2)** The meta-ros distro-agnosticism claims in § 1.9 were verified by direct source reading, not
    live execution: `meta-ros1/conf/distro/ros1.conf` and `meta-ros2/conf/distro/ros2.conf` were read in full
    (three real lines each); `meta-ros-common/conf/distro/include/ros-common.inc` and
    `ros-useful-buildcfg-vars.inc` were read in full; `grep -rn "ROS1_DISTRO\|ROS2_DISTRO"` across `~/Projects/meta-ros`
    located their per-layer definitions in each `meta-ros2-<distro>`'s `conf/ros-distro/include/<distro>/ros-distro.inc`;
    `grep -rn ":ros1\b\|:ros2\b\|getVar('DISTRO')"` across all `.conf`/`.inc`/`.bbclass`/`.bb`/`.bbappend` files
    found no literal `DISTRO`-conditional override anywhere, only `:ros1-distro`/`:ros2-distro`, traced to
    `meta-ros-common/classes/ros_distro.bbclass`'s `DISTROOVERRIDES .= ":${ROS_DISTRO_BASELINE_PLATFORM}:${ROS_DISTRO_TYPE}-distro:${ROS_DISTRO}"`,
    whose inputs (`ROS_DISTRO_TYPE`, `ROS_DISTRO_BASELINE_PLATFORM`) were confirmed set per-layer (e.g.
    `meta-spaceros-jazzy/conf/ros-distro/include/jazzy/ros-distro.inc: ROS_DISTRO_TYPE = "spaceros"`), not
    derived from bitbake's `DISTRO` variable anywhere in the chain. **This was not verified with a live build**
    — see § 6 item 9 for the corresponding risk.
11. **(Revision 3, correcting revision 2)** `~/Projects/meta-ros`'s branch structure was enumerated in full
    (`git branch -a`), confirming `meta-ros` follows the standard OE-layer one-stable-plus-one-staging-branch-
    per-release convention (`scarthgap`/`scarthgap-next`, `whinlatter`/`whinlatter-next`, `wrynose`/`wrynose-next`,
    `master`/`master-next`, plus older EOL series back through `dunfell`). `LAYERSERIES_COMPAT_ros-common-layer`
    was read directly from `meta-ros-common/conf/layer.conf` on four branches via `git show <branch>:<path>`
    without checking them out: `scarthgap` → `"scarthgap"`, `wrynose` → `"wrynose"`, `whinlatter` →
    `"whinlatter"`, `master` → `"wrynose"` (correct — the in-development "blacksail" series hasn't released).
    `meta-ros1/conf/distro/ros1.conf` and `meta-ros2/conf/distro/ros2.conf` were confirmed byte-identical in
    content between the `wrynose` and `master` branches the same way. Revision 1/2 had read only the single
    locally-checked-out `master` branch of `meta-ros` and incorrectly treated its `LAYERSERIES_COMPAT` value as
    a tree-wide inconsistency (§ 0.6, § 6 item 5 — both retracted in revision 3).
12. **(Revision 3)** `~/Projects/meta-oeros`'s branches were checked the same way, for contrast:
    `git rev-parse origin/wrynose origin/master` returned the identical commit hash for both
    (`fe51347d5e29dac2c2bd6fe204e8e805016fa2a0` at verification time), and both branches' `conf/layer.conf`
    carry a single `LAYERSERIES_COMPAT_oeros = "wrynose"`. Unlike `meta-ros`, `meta-oeros` does not currently
    demonstrate a per-release-branch convention — its `wrynose` and `master` branches simply haven't diverged
    yet, and it's unestablished whether they're intended to (§ 6 item 5).
13. **(Revision 4)** All eleven Phase 1 fragments were discovered and enabled against real scratch build trees
    with `bitbake-config-build list-fragments`/`enable-fragment`, and their effects confirmed with
    `bitbake-getvar`: `DISTRO_FEATURES` correctly excludes `sysvinit` once `distro-features/systemd` is
    enabled (confirming the `DISTRO_FEATURES_OPTED_OUT` fix), `ERROR_QA` correctly drops `license-exists`,
    `BB_DISKMON_DIRS`/`VIRTUAL-RUNTIME_init_manager` resolve as expected. This surfaced the pre-existing
    `allow-commercial-licenses.conf` parse bug (§ Revision 4 summary above), traced via `xxd`/`cmp`/`git log
    origin/<branch>..<branch>` to a stale local `master` checkout of `meta-oeros`, fixed by cherry-picking the
    already-existing upstream fix (`fe51347`) rather than a destructive reset.
14. **(Revision 4)** The zenoh-c relocation was verified against the real `meta-zenoh` layer (cloned from
    `https://github.com/Jarsop/meta-zenoh.git`), not a scratch stand-in: `grep` across its source confirmed
    `ZENOH_SHARED_MEMORY`/`ZENOH_UNSTABLE_API` are real, consumed variables (`PACKAGECONFIG[vardeps]` in
    `zenoh.inc`/`zenoh-c.inc`/every `zenoh-pico_*.bb`, weak-defaulted in `meta-zenoh/conf/layer.conf`) before
    deciding to keep them in `meta-oeros/conf/distro/include/zenoh.inc`. A scratch recipe with
    `BBCLASSEXTEND = "native"` plus an unscoped `:remove` in its bbappend confirmed live that a bbappend's
    unscoped assignment applies to both the target and `-native` variant (`bitbake-getvar -r scratchpkg` and
    `-r scratchpkg-native` both showed the flag removed), justifying collapsing the original two `:pn-zenoh-c`/
    `:pn-zenoh-c-native` lines to one. The final change was then verified against the actual `zenoh-c` 1.9.0
    recipe (a cargo/rust package) via `bitbake-getvar -r zenoh-c`/`-r zenoh-c-native`, with unrelated
    dependency-missing bbappends in the same layers masked out with `BBMASK` to isolate the check.
15. **(Revision 4)** The `python3`/tk relocation was verified against the real oe-core `python3_3.14.6.bb`
    recipe: `bitbake-getvar -r python3 PACKAGECONFIG` includes `tk`, attributed to the new
    `meta-oeros/recipes-devtools/python/python3_%.bbappend`.

**Documentation consulted**: `https://docs.yoctoproject.org/dev/ref-manual/fragments.html` and
`https://docs.yoctoproject.org/bitbake/dev/bitbake-user-manual/bitbake-user-manual-environment-setup.html`
were fetched, but were found to be materially incomplete relative to actually reading
`meta/lib/bbconfigbuild/configfragments.py`, `meta/conf/bitbake.conf`, `lib/bb/parse/ast.py`, and
`setup-schema/*.schema.json` directly from the bitbake/oe-core source trees — every mechanism described in this
document beyond the top-level narrative was verified against source and/or live execution, not against the
rendered docs pages, and the docs pages are noted in § 0 as a discrepancy where they under-document the actual
mechanism (no directory-layout, `OE_FRAGMENTS` syntax, or `BB_CONF_FRAGMENT_*` explanation was present in the
fetched fragments.html content at all).
