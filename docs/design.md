# Design: matrix-driven bitbake configuration for meta-ros / oeros

Audience: developers maintaining this repository (`meta-ros`, branch `build`) and `meta-oeros`.
This document describes the system as it exists today and why it is shaped this way. For the
step-by-step history of how it got here — including bugs found and corrected along the way — see
[`kas-to-fragments-migration-spec.md`](kas-to-fragments-migration-spec.md), which is a revision log,
not a reference. For task-oriented instructions ("how do I regenerate X" / "how do I read a failed
CI run"), see [`scripts-and-ci-guide.md`](scripts-and-ci-guide.md). For end-user build instructions,
see [`kas/README.md`](../kas/README.md).

## 1. The problem this solves

meta-ros/oeros can be built three different ways — kas, `bitbake-setup`, or manual
`git clone` + `bitbake-config-build` — and all three must resolve to the *same* bitbake
configuration (same `MACHINE`, `DISTRO`, layers, features, QA policy) for a given
(release, ROS distro, machine) combination. Before this work, that combination space
(3 releases × 6 ROS distros × up to 4 machines) was expressed as ~60 hand-maintained, mutually
independent `kas/oeros-<release>-<ros>-<machine>.yml` files, with no bitbake-setup or manual path
at all. Adding a machine, or fixing a QA policy, meant editing N files by hand and hoping none were
missed or made inconsistent.

The fix has two parts:
1. **One source of truth** (`matrix.yml`) that every generated artifact — kas files, bitbake-setup
   registry JSON, the README's support table — is a pure function of.
2. **[Configuration fragments](https://docs.yoctoproject.org/dev/ref-manual/fragments.html)**
   (`OE_FRAGMENTS`), a mechanism built into OpenEmbedded-Core itself starting with the `wrynose`
   release, as the one place build-wide policy (QA relaxations, commercial license flags, DISTRO
   selection) lives — so kas, bitbake-setup, and the manual path all converge on driving the exact
   same bitbake mechanism instead of three parallel, divergeable ones.

`scarthgap` predates fragment support in OpenEmbedded-Core, so it keeps its original, unreduced kas
file shape and has no bitbake-setup or manual-fragment path. This is a permanent split, not a
transitional one — tracked as `fragment_capable: false` in `matrix.yml`.

## 2. `matrix.yml`: the source of truth

A single YAML file at the repo root. Top-level sections:

- **`releases`**: one entry per Yocto release (`scarthgap`, `wrynose`, `master`). Carries
  `fragment_capable`, the `kas/yocto/<release>.yml` file it maps to, pinned commits (pinned
  releases only — `master` tracks its floating branch directly), and per-release quirks
  (`default_source_overrides`, `extra_layers`, `extra_includes`, `qa_fragment`).
- **`ros_distros`**: one entry per ROS distro (`humble`, `jazzy`, `kilted`, `lyrical`, `rolling`,
  `noetic`), each mapping to a `kas/ros{1,2}/<distro>.yml` file and its `qt_layer`
  (`qt5` or `qt6` — intrinsic to the distro, not a freely-selectable per-cell addon; verified
  exhaustively against the full tree before this was encoded as a fixed mapping).
- **`machines`**: one entry per machine, with a human-readable `description` (used in the
  bitbake-setup picker and, indirectly, the generated docs) and any extra `fragments` a
  fragment-capable cell using that machine should enable beyond `machine/<name>` itself — eg
  Raspberry Pi machines list `oeros/allow-commercial-licenses`.
- **`cells`**: the actual enumeration — one entry per (release, ros_distro), each listing every
  `machines` currently built for that combination plus any `source_overrides` layered on top of the
  release's own defaults. This list is deliberately non-uniform (eg `wrynose`/`kilted` has no
  `raspberrypi4-64` cell) and reflects what's actually in the tree, not a theoretical full matrix.

Everything under `kas/oeros-<release>-<ros-distro>-<machine>.yml`,
`meta-oeros/conf/registry/configurations/oeros-<release>-<ros-distro>.conf.json`, and the table in
`kas/README.md` is derived from this file. **Generated files carry no hand-authored content and
must never be hand-edited** — edit `matrix.yml` and regenerate (see the user guide). This is
enforced by CI (`config-check`, section 5 below).

`kas/oeros-devel.yml` and `kas/oeros-distro.yml`, despite matching the `oeros-*.yml` glob, are
hand-maintained inputs, not generated outputs — `oeros-devel.yml` is a personal development
shortcut re-derivable as a documented one-off (see its own header comment), and `oeros-distro.yml`
is one of the fixed include files described in section 3.

## 3. Where `OE_FRAGMENTS` enablement lives

A fragment-capable cell's generated top-level file (`generate-kas.py`'s output) is reduced to just
`header.includes` and any `repos:` overrides — **it carries no `local_conf_header` of its own**.
Instead, `OE_FRAGMENTS` enablement is distributed across the subfiles that already own the
corresponding concern, each appending to `OE_FRAGMENTS` for exactly what it's responsible for:

| File | Contributes |
|---|---|
| `kas/oeros-distro.yml` | `distro/oeros oeros/common` — DISTRO selection and the always-on baseline. Included only by fragment-capable cells. |
| `kas/yocto/wrynose.yml`, `kas/yocto/master.yml` | That release's QA-relax fragment (`oeros/qa/allow-license-exists` / `oeros/qa/allow-license-format`) |
| `kas/machine/<name>.yml` | `machine/<name>`, plus any machine-specific fragment (eg `oeros/allow-commercial-licenses` on Raspberry Pi machines) |

This mirrors the pre-existing kas convention where each subfile already owns its own concern (a
machine file sets `machine:`, a release file pins repos) — fragment enablement was added *alongside*
that existing content, in the same file, rather than synthesized into one new top-level blob. Two
consequences worth knowing:

- **Machine files are shared with `scarthgap`.** Their fragment-enabling `local_conf_header` entry
  is additive, never replacing existing content, and is inert on `scarthgap` — verified live that
  `scarthgap`'s oe-core has zero references to `OE_FRAGMENTS`/`addfragments` anywhere in its source,
  so setting it there is simply unused text, not a conflict.
- **Release files (`kas/yocto/wrynose.yml`/`master.yml`) are never shared with `scarthgap`**, so
  their old QA-relax `local_conf_header` content (`ERROR_QA:remove = "..."`) was fully replaced,
  not added to, when the fragment was introduced.

### The one rule that matters most here

**`local_conf_header` is merged as one flat dict across every included kas file, keyed by the label
under `local_conf_header:` — not per-file, per-key.** Two different included files that happen to
use the same key do not both apply: whichever is processed last silently wins, with no warning from
kas. This was found the hard way (three of four `OE_FRAGMENTS` contributions were silently dropped
when every file used the generic key `"fragments"`). **Every file in `kas/` that contributes an
`OE_FRAGMENTS += "..."` line must use a key name unique across the entire tree** — the convention
used throughout is `<what-it-is>-fragments` (`oeros-distro-fragments`, `wrynose-fragments`,
`qemux86-64-fragments`, `raspberrypi5-fragments`, ...). If you add a new fragment-contributing file,
follow this convention and grep the tree first to confirm the name isn't already taken.

The same class of bug applies to CI job-level variables that shadow global pipeline variables — see
section 5.

## 4. Fragment design rules

These rules govern any new fragment added to `meta-oeros` or `meta-ros-common`, not just the ones
already migrated.

### 4.1 Naming

A fragment's identity is `<BBFILE_COLLECTIONS-name-of-its-layer>/<path-under-conf/fragments,
without .conf>` — eg `meta-oeros/conf/fragments/qa/allow-license-exists.conf` is addressed as
`oeros/qa/allow-license-exists`, because `meta-oeros`'s `BBFILE_COLLECTIONS` is `oeros`. Directory
structure under `conf/fragments/` is purely organizational but becomes part of the public name —
treat renaming a fragment's path as a breaking change, the same as any other public API.

### 4.2 Fragments vs. bbappends

**A `local_conf_header`/config line carrying a `:pn-<recipe>` (or `:pn-<recipe>-native`) override is
recipe-specific and belongs in a `.bbappend` for that recipe, not in a config fragment**, provided
the consuming layer has (or can get) the necessary `dynamic-layers`/`BBFILES_DYNAMIC` plumbing for
the recipe's providing layer. A fragment is for build-wide/distro policy with no single recipe as
its natural home. This was a real correction made during migration: a zenoh workaround
(`INSANE_SKIP:pn-zenoh-c`, `DEBUG_PREFIX_MAP:remove:pn-zenoh-c*`) was initially ported as a fragment,
then moved into `meta-ros2`'s existing
`dynamic-layers/meta-zenoh/recipes-connectivity/zenoh-c/zenoh-c_%.bbappend` once review caught that
the `:pn-` syntax was itself the tell. A bbappend is already scoped to its recipe, so the override
suffix can (and should) be dropped entirely once moved — an unscoped assignment inside a bbappend
already covers both the target recipe and any `-native` `BBCLASSEXTEND` variant.

### 4.3 Granularity

A fragment should correspond to exactly one concern — historically, one entry in what used to be
the alphabetically-sorted `local_conf_header` dict. Don't bundle unrelated `PACKAGECONFIG` tweaks
for different optional layers into one fragment/`require`d `.inc` file the way
`meta-oeros/conf/distro/include/packageconfig.inc` originally did; each belongs with (or
conditioned on) the layer it's actually for.

### 4.4 Required metadata

Every fragment must carry, at minimum:
```
BB_CONF_FRAGMENT_SUMMARY = "One line, imperative, <=72 chars"
BB_CONF_FRAGMENT_DESCRIPTION = "Full sentences, wrapped with \\ line continuations. State what \
  it does AND why/when to use it."
```
**Both are hard-required** — `bitbake-config-build`/`bitbake-setup` fatal at fragment-discovery time
if either is missing, or if a multi-line `BB_CONF_FRAGMENT_DESCRIPTION` is missing its trailing
line-continuation backslash. This is not a per-fragment failure: a malformed fragment anywhere in a
layer breaks `list-fragments`/`enable-fragment` discovery for the **entire layer**. This has
happened for real in this tree (a missing trailing `\` in `allow-commercial-licenses.conf`) — if
`list-fragments` suddenly reports nothing from a layer that should have fragments, check every
`.conf` under that layer's `conf/fragments/` for this before looking anywhere else.

### 4.5 Conditional `inherit` for optionally-present layers

Fragments run through bitbake's `ConfHandler`, not `BBHandler` — the `inherit` **statement** is
only valid in `.bb`/`.bbclass`/`.bbappend` files and fails to parse in a fragment (confirmed live:
a bare `inherit foo` in a `.conf`-parsed fragment produces a hard `bb.parse.ParseError`). Use the
`INHERIT` **variable** instead: `INHERIT += "foo"`.

If the class being inherited comes from a layer that might not be present in the build (eg an
optional companion layer like `meta-tasklogger`), an unconditional `INHERIT += "tasklogger"` will
hard-crash the parse the moment the fragment is enabled without that layer. Use the existing
meta-ros precedent (`bb.utils.contains` against `BBFILE_COLLECTIONS`, the same pattern
`meta-ros-common` already uses for `qt5-layer`):
```
INHERIT += "${@bb.utils.contains('BBFILE_COLLECTIONS', 'tasklogger', 'tasklogger', '', d)}"
```
This resolves to `""` (a no-op) when the layer isn't present, and to the class name when it is —
verified both directions live.

### 4.6 Weak vs. hard assignment and precedence

`local_conf_header` (parsed as part of `local.conf`) parses *before* `addfragments`, which parses
before `machine.conf`/`distro.conf`. Moving a setting from `local_conf_header` into a fragment is
safe under every assignment form actually in use in this tree:

| Form | Effect of moving from local.conf to a fragment |
|---|---|
| `VAR = "x"` / `VAR ?= "x"` | Safe — a fragment's hard `=` still wins over anything set earlier; `?=` only takes effect if nothing already set it, unchanged semantics. |
| `VAR += "x"` / `VAR:append` | Safe and order-independent for the final value (only `bitbake-getvar`'s variable-history *attribution* moves, which is a presentation detail). |
| `VAR:remove = "x"` | Safe — resolved lazily at finalize time regardless of which file contributed it. |
| Built-in `machine/<name>`, `distro/<name>` fragments | Require any pre-existing assignment to be weak (`??=`) or absent, or bitbake fatals with "already got an assignment." kas already emits weak `MACHINE ??=`/`DISTRO ??=`, so this is safe for kas-authored configs specifically. |

## 5. Generators

Three scripts under `scripts/`, each a pure function of `matrix.yml` (no network access, no
side effects beyond writing files) except where noted:

- **`generate-kas.py`**: emits every `kas/oeros-<release>-<ros>-<machine>.yml`. For
  `fragment_capable` releases, output is `header.includes` (the fixed chain: release file, ROS
  distro file, machine file, `kas/common.yml`, the ROS distro's `qt_layer` file, any
  `extra_includes`, and `kas/oeros-distro.yml` last — order matters, see section 6) plus any
  `repos:` overrides; no `local_conf_header` of its own (section 3). For `scarthgap`, output is
  unchanged in shape from the original hand-maintained files. `--check` diffs generated content
  against what's on disk and exits non-zero on drift (section 7); it also flags any
  `kas/oeros-*.yml` file that matches neither a generated cell nor the two hand-maintained
  exceptions (`oeros-devel.yml`, `oeros-distro.yml`).
- **`generate-bitbake-setup.py`**: emits
  `meta-oeros/conf/registry/configurations/oeros-<release>-<ros-distro>.conf.json` for every
  fragment-capable (release, ros_distro) group. Rather than re-deriving each repo's resolved
  `layers:` state independently from `matrix.yml` — kas's own merge rules for this have real
  subtlety (a `disabled` layer state from *any* included file can win regardless of include order,
  not simply "last file wins" the way scalar keys like `distro:` do) — this generator runs
  `kas dump --skip repos_checkout --skip repos_apply_patches` against the *already-generated*
  `kas/oeros-*.yml` file for each (release, ros_distro, machine) and treats its resolved `repos:`
  block as ground truth, unioning across every machine in the group (`oe-fragments-one-of`
  presents machine choice as a single-file picker, unlike kas's one-file-per-machine layout).
  This is the one generator that needs network access (`kas dump` resolves floating-branch repos'
  current commits) and a `kas` binary on `PATH`. Optionally validates output against
  `bitbake-setup.schema.json`/`layers.schema.json` (from the `bitbake` git repo's `setup-schema/`
  directory — **not** shipped in the `bitbake-setup` pip package) if `--schema-dir` is given.
- **`generate-matrix-table.py`**: emits the "supported combinations" Markdown table embedded in
  `kas/README.md` between `<!-- Generated from matrix.yml -->`/`<!-- End generated table -->`
  markers.

A fourth script, **`check-readme-table.py`**, isn't a generator — it re-runs
`generate-matrix-table.py` and diffs the result against what's currently embedded in
`kas/README.md`, exiting non-zero on drift. It exists because the table itself isn't regenerated
in place automatically; a human runs `generate-matrix-table.py` and pastes its output in, and this
script is what would catch a forgotten paste.

## 6. Why fragment/repo ordering in `includes:` matters

`meta-ros` no longer defines its own `DISTRO` on fragment-capable branches (`ros1.conf`/`ros2.conf`
were removed from `meta-ros1`/`meta-ros2` — `meta-oeros` is the reference distro going forward).
`kas/oeros-distro.yml`, which sets `distro: "oeros"`, must therefore be listed **after** the ROS
distro's own file in `header.includes` — kas resolves same-key conflicts between included files in
favor of whichever is listed last, verified empirically, not assumed from kas's own documentation.
`generate-kas.py` encodes this by appending `kas/oeros-distro.yml` to the includes list last,
unconditionally, for every fragment-capable cell.

## 7. Validation framework

Two tiers, deliberately different in cost:

- **Tier 1 — drift detection** (`generate-kas.py --check`, `check-readme-table.py`): cheap,
  offline, structural. Confirms generated files still match what `matrix.yml` says they should be.
  Catches "someone hand-edited a generated file" and "matrix.yml changed but nobody regenerated."
  Runs on every push/PR (`config-check`, section 8).
- **Tier 2 — real equivalence** (`extract-config-vars.py` + `validate-equivalence.py`): expensive,
  network-dependent, semantic. Performs a real `kas checkout` (or reuses an existing one via
  `--skip-checkout`) and runs `bitbake-getvar --value` over a curated set of config-level variables
  (`MACHINE`, `DISTRO`, `DISTRO_FEATURES*`, `IMAGE_FEATURES`, `INHERIT*`, `BBLAYERS`,
  `OE_FRAGMENTS`, `LICENSE_FLAGS_ACCEPTED`, `TEMPLATECONF`, `WARN_QA`/`ERROR_QA`, and a handful of
  build-host tuning vars), producing a JSON snapshot. `validate-equivalence.py` diffs two snapshots
  (eg a kas-produced one against a `bitbake-setup`-produced one for the nominally same cell) and
  reports pass/fail. Two comparison modes exist beyond exact string equality, both found necessary
  against real output rather than assumed up front:
  - **Set-semantic** (`DISTRO_FEATURES`, `IMAGE_FEATURES`, `INHERIT*`, `LICENSE_FLAGS_ACCEPTED`,
    `WARN_QA`, `ERROR_QA`): compared as whitespace-tokenized sets, since bitbake itself only ever
    checks membership in these, never exact text or count — a value legitimately appearing twice
    (a fragment and a still-included `local_conf_header` both contributing the same setting) is not
    a real difference.
  - **Path-suffix** (`BBLAYERS`): entries are absolute paths that never share a checkout root
    between two different tools/work-dirs, so exact comparison can never pass even when the layer
    set is identical — compared by each path's trailing 1–2 components instead.
  - `--ignore-vars` documents *permanent, understood* differences excluded from the verdict (not
    silently — they're still listed in the report) rather than making the tool unable to ever pass.
    Three are known and used by the CI smoke job (section 8): `TEMPLATECONF` (kas-only, no
    bitbake-setup equivalent), `OE_FRAGMENTS` and `BBLAYERS` (bitbake-setup's one-JSON-file-per-group
    design means `oe-fragments-one-of` can't condition a fragment on which machine gets picked, and
    `bb-layers` is the union across every machine in the group — both a confirmed strict superset
    of any single kas cell's equivalent, not a bug).

Because all three entry paths (kas, bitbake-setup, manual `bitbake-config-build enable-fragment`)
write into the *same* `OE_FRAGMENTS` variable via the *same* `addfragments` statement in
`bitbake.conf`, their convergence is a structural property of the mechanism, not something that
needs re-proving per fragment. What Tier 2 actually needs to catch is a **generator** bug — kas's
generator and bitbake-setup's generator selecting a different fragment/layer set for what's meant
to be the same cell — not a bitbake-mechanism question.

## 8. CI architecture

Two GitHub Actions workflows (`.github/workflows/`) and matching GitLab CI jobs
(`.gitlab-ci.yml`), split by cost:

- **`config-check`**: Tier 1 only. No network beyond checkout, no external binaries beyond
  `pyyaml`. Runs on every push/PR.
- **`smoke-equivalence`**: Tier 2, for one fixed representative cell
  (`wrynose`/`rolling`/`qemux86-64`). Clones the real layer set and runs `bitbake-getvar`, which
  costs real minutes, so it's nightly-only (`schedule`/cron) with manual dispatch available, not
  wired into every PR. GitLab has no automatic pipeline schedules — the job is gated behind
  `$CI_PIPELINE_SOURCE == "schedule"` and stays dormant until a Pipeline Schedule is created in
  project settings. Deliberately covers one cell, not the full matrix — a full-matrix nightly tier
  is future work, not yet built.

Kept as **separate jobs from the actual build** (GitLab's manual `build-job`, which runs a real
`kas build`) rather than folded in, since neither validates a build output and both need to run
automatically/cheaply where a real build cannot.

**Fork-friendliness** was an explicit design goal, not an afterthought: `config-check` and
`smoke-equivalence` need no secrets or org-specific configuration on either CI system, by
construction — they only ever clone public repos already named in `matrix.yml`/the registry JSON.
`build-job`'s container image is the one org-specific piece (`$CI_REGISTRY/oeros/aws-runner/crops-container`,
which resolves against `oeros`'s own container registry regardless of which namespace forked the
repo, since `$CI_REGISTRY` is GitLab-instance-scoped, not project-scoped); it's exposed as a
`CROPS_IMAGE` CI/CD variable with that path as the default specifically so a fork can override it
under project settings without touching `.gitlab-ci.yml`.

## 9. Traps for future maintainers

A condensed list of things that look correct but silently aren't, each found and fixed once during
this migration — check these first if something "should" work and doesn't:

1. **Duplicate `local_conf_header` keys across kas files silently drop content** (section 3). If a
   new fragment-contributing kas file's `OE_FRAGMENTS` line isn't showing up in `kas dump` output,
   check for a key-name collision before anything else.
2. **`inherit` (statement) vs `INHERIT` (variable)** (section 4.5) — a fragment is `ConfHandler`-parsed;
   only `INHERIT +=` works there.
3. **A malformed `BB_CONF_FRAGMENT_DESCRIPTION` (missing trailing `\` on a wrapped line) breaks
   fragment discovery for the entire layer**, not just the offending file (section 4.4).
4. **A `:pn-<recipe>` override in a fragment is a signal it belongs in a bbappend instead**
   (section 4.2).
5. **`bitbake-getvar -q` does not mean "bare value, no history"** — it only silences server
   logging. The flag that actually strips the `VAR=`/history-comment wrapping is `--value`.
6. **Never symlink a real, actively-developed repo checkout into a tool's managed working
   directory** (a kas `KAS_WORK_DIR`, a bitbake-setup top-dir). `kas checkout` runs real `git
   checkout` inside whatever exists at a repo's configured path — if that path is a symlink to a
   real repo, kas can and will force-move that repo's branch pointer backward to a pinned commit.
   Use a disposable copy, or a fresh clone, for anything a script will drive `git` inside.
7. **CI job-level `variables:` with the same name as a global pipeline variable silently shadow
   it**, the identical class of bug as trap 1 — eg `.gitlab-ci.yml`'s `smoke-equivalence` job
   deliberately uses `SMOKE_ROS_DISTRO`/`SMOKE_MACHINE` rather than `ROS_DISTRO`/`MACHINE`, since
   the latter two are already global pipeline-input variables with their own `options:` picker
   for `build-job`.
