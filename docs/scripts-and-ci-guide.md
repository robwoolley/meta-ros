# User guide: scripts and CI

Audience: anyone regenerating files in this repository, adding a new release/ROS-distro/machine
combination, or reading a CI run. This is task-oriented; for *why* the system is shaped this way,
see [`design.md`](design.md). For instructions on *building* meta-ros/oeros as an end user (not
maintaining this repo), see [`kas/README.md`](../kas/README.md).

All commands below assume your working directory is the repo root (`meta-ros-oe-fragments`,
branch `build`).

## Prerequisites

```
python3 -m venv venv
source venv/bin/activate
pip3 install pyyaml            # scripts/generate-kas.py, generate-matrix-table.py, check-readme-table.py
pip3 install kas bitbake-setup # only needed for generate-bitbake-setup.py and the Tier 2 scripts below
```

`generate-kas.py`, `generate-matrix-table.py`, and `check-readme-table.py` need only `pyyaml` — no
network access, no build tools. `generate-bitbake-setup.py` and the Tier 2 validation scripts
(`extract-config-vars.py`, `validate-equivalence.py`) need real network access and, for
`extract-config-vars.py`, a full Yocto layer checkout — expect these to take minutes, not seconds.

## Regenerating files after editing `matrix.yml`

**Never hand-edit a generated file** (any `kas/oeros-<release>-<ros>-<machine>.yml`, any
`meta-oeros/conf/registry/configurations/*.conf.json`, or the table inside `kas/README.md`) —
CI will catch it and it'll be overwritten by the next regeneration anyway. Edit `matrix.yml`, then:

```
# kas files
python3 scripts/generate-kas.py                    # writes kas/oeros-*.yml
python3 scripts/generate-kas.py --check             # verify only, no writes; exit 1 on drift

# bitbake-setup registry JSON (needs kas installed, network access, and a scratch work dir)
python3 scripts/generate-bitbake-setup.py \
    --out-dir /path/to/meta-oeros/conf/registry/configurations \
    --work-dir /tmp/gen-work
# then commit/push the result inside the meta-oeros checkout itself -- this repo only generates
# the kas/ side; meta-oeros's registry directory is a separate repository (both wrynose and master
# branches need regenerating and publishing separately if the change affects both).

# README table
python3 scripts/generate-matrix-table.py            # prints the table -- paste it in by hand
                                                      # between the <!-- Generated... --> markers
                                                      # in kas/README.md
python3 scripts/check-readme-table.py                # verify the paste actually happened
```

A typical workflow for, say, adding a new machine to an existing (release, ROS-distro) group:
1. Add the machine to `matrix.yml`'s `machines:` section if it isn't already listed (with a
   `description` and any needed `fragments:`).
2. Add it to the relevant `cells:` entry's `machines:` list.
3. Run `python3 scripts/generate-kas.py` and review the diff — a new
   `kas/oeros-<release>-<ros>-<machine>.yml` should appear; nothing else should change.
4. Run `python3 scripts/generate-matrix-table.py` and paste the new output into
   `kas/README.md`; confirm with `check-readme-table.py`.
5. If the (release, ros_distro) group is `fragment_capable`, regenerate and republish the
   `meta-oeros` registry JSON for that group (previous step) — its `oe-fragments-one-of.machine`
   picker needs the new option.
6. Commit. `config-check` in CI will fail loudly if any regeneration step was missed.

For a new *release* or *ROS distro* rather than just a machine, the same idea applies but touches
more of `matrix.yml`'s `releases:`/`ros_distros:` sections — read [`design.md`](design.md) sections
2–3 first, since those sections encode real constraints (eg `qt_layer` being fixed per ROS distro,
not freely choosable per cell) that aren't obvious from the schema alone.

## Running the Tier 2 (real equivalence) check locally

This is exactly what the CI `smoke-equivalence` job does — useful for debugging a failure without
waiting for a scheduled run, or for checking a cell other than the one CI covers.

```
# kas side: real checkout + bitbake-getvar extraction
python3 scripts/extract-config-vars.py \
    kas/oeros-wrynose-rolling-qemux86-64.yml \
    --work-dir /tmp/kas-side \
    --out /tmp/kas-snapshot.json

# bitbake-setup side: clone the registry, init, then extract from the resulting build dir
git clone --branch wrynose --depth 1 https://github.com/robwoolley/meta-oeros.git /tmp/meta-oeros-registry
bitbake-setup --setting default top-dir-prefix /tmp/bbsetup-side \
    init /tmp/meta-oeros-registry/conf/registry/configurations/oeros-wrynose-rolling.conf.json \
    oeros-wrynose-rolling machine/qemux86-64 --non-interactive
python3 scripts/extract-config-vars.py \
    kas/oeros-wrynose-rolling-qemux86-64.yml \
    --work-dir /tmp/bbsetup-side/bitbake-builds/oeros-wrynose-rolling \
    --skip-checkout \
    --out /tmp/bbsetup-snapshot.json

# compare
python3 scripts/validate-equivalence.py \
    /tmp/kas-snapshot.json /tmp/bbsetup-snapshot.json \
    --cell wrynose-rolling-qemux86-64 \
    --ignore-vars TEMPLATECONF,OE_FRAGMENTS,BBLAYERS
```

`--ignore-vars TEMPLATECONF,OE_FRAGMENTS,BBLAYERS` excludes three *permanent, understood*
differences (see [`design.md`](design.md) section 7) — don't add more variables to this list to
silence a failure without first confirming in `validate-equivalence.py`'s report that the
difference really is one of these already-documented cases and not a new regression.

`extract-config-vars.py --work-dir` is reused across runs when possible (a shared scratch
directory), so repos already fetched for one cell aren't re-cloned for the next. **Never point
`--work-dir` at a symlink into a real, actively-developed repo checkout** — `kas checkout`/`kas`'s
own tooling runs real `git checkout` inside whatever exists at a repo's path, and will happily move
that repo's branch pointer backward to a pinned commit if it turns out to be your actual working
copy.

## Reading a CI run

### `config-check` (every push/PR, both GitHub and GitLab)

Runs `generate-kas.py --check` and `check-readme-table.py`. Output on failure:
- `DRIFT: <file>` with a unified diff — `matrix.yml` and the committed generated file disagree.
  Fix: regenerate (see above) and commit the result.
- `MISSING: <file>` — `matrix.yml` describes a cell whose generated file was never committed.
- `UNEXPECTED (not in matrix.yml, not hand-maintained): <file>` — a `kas/oeros-*.yml` file exists
  that neither corresponds to a current `matrix.yml` cell nor is one of the two hand-maintained
  exceptions (`oeros-devel.yml`, `oeros-distro.yml`). Fix: either the file should be deleted (a
  removed cell) or `matrix.yml` is missing the cell that should produce it.
- `<file> table does not match ...` from `check-readme-table.py` — the table pasted into
  `kas/README.md` is stale relative to `matrix.yml`. Fix: re-run `generate-matrix-table.py` and
  paste its output in between the markers.

This job needs no secrets and no external services beyond `pip install pyyaml` — if it fails, the
fix is always "regenerate locally and commit," never a CI environment problem.

### `smoke-equivalence` (nightly, or manual dispatch)

Runs the real kas-vs-bitbake-setup comparison for `wrynose`/`rolling`/`qemux86-64` described above.
On GitHub, trigger it manually from the **Actions** tab → `Smoke equivalence check` → **Run
workflow**. On GitLab there is no equivalent one-click manual run — the job is gated by
`rules: - if: '$CI_PIPELINE_SOURCE == "schedule"'` specifically to keep it off ordinary push/PR/
`Run pipeline` runs (`CI_PIPELINE_SOURCE` is a predefined GitLab variable, not one you can set from
the manual-pipeline form), so it only ever runs from an actual Pipeline Schedule
(`Settings > CI/CD > Schedules`). Once a schedule exists, that same Schedules page has a **Play**
(▶) button to run it immediately instead of waiting for its next cron time — that's the closest
thing to a manual trigger available on GitLab for this job.

On failure, download the job's artifacts (`report.json`, `kas-snapshot.json`, `bbsetup-snapshot.json`):
- `report.json`'s `variable_diffs` list is the actual failure — each entry names the variable and
  both tools' resolved value. `ignored_diffs` lists the three expected differences even on a pass,
  for visibility.
- A new, unexpected entry in `variable_diffs` (anything beyond the three `--ignore-vars` names)
  means kas's generator and bitbake-setup's generator have genuinely diverged for this cell —
  treat it as a real bug, most likely in `generate-bitbake-setup.py`'s `collect_group()`/
  `build_config()` or in a recent `matrix.yml` edit that only one generator picked up correctly.

This job takes real minutes (full layer checkout) and real network — it is not expected to be fast,
and a timeout under an hour would be unusual enough to investigate rather than just re-run.

### `build-job` (GitLab only, manual)

Runs an actual `kas build`. Triggered manually from GitLab's pipeline UI, with
`OE_RELEASE_SERIES`/`ROS_DISTRO`/`MACHINE` picked from dropdowns (kept in sync with `matrix.yml` by
hand today — if you add a release/ROS-distro/machine to `matrix.yml`, add it to this job's
`variables:` `options:` lists in `.gitlab-ci.yml` too; `config-check` does not check this for you).
`META_ROS_GIT`/`META_ROS_BRANCH` and `VIRTUAL_ENV_PKGS` allow overriding the `meta-ros` source or
adding a local `meta-virtualenv` checkout for a one-off build without editing any kas file.

## Running this in your own fork

Covered in full in [`kas/README.md`](../kas/README.md#ci), briefly: neither `config-check` nor
`smoke-equivalence` needs secrets or org-specific configuration, so both work unmodified in a
personal fork. GitHub disables Actions (and, separately, scheduled workflows specifically after 60
days of repo inactivity) on forks until you enable them once under the **Actions** tab; GitLab has
no automatic schedules at all, so `smoke-equivalence` there needs a Pipeline Schedule created under
**Settings > CI/CD > Schedules** in your fork before it ever runs. `build-job`'s container image
can be overridden via the `CROPS_IMAGE` CI/CD variable if the canonical `oeros` registry path isn't
reachable from your fork's CI.

## Troubleshooting

- **`bitbake-config-build list-fragments` (or `bitbake-setup init`) reports fewer fragments than
  expected, or none at all, from a layer that should have some.** Check every `.conf` under that
  layer's `conf/fragments/` for a missing `BB_CONF_FRAGMENT_SUMMARY`/`BB_CONF_FRAGMENT_DESCRIPTION`,
  or a multi-line `BB_CONF_FRAGMENT_DESCRIPTION` missing its trailing `\` — one malformed fragment
  breaks discovery for the whole layer, not just itself (design.md section 4.4).
- **A `kas dump` for a fragment-capable cell shows an `OE_FRAGMENTS` line missing that you know is
  in one of the included files.** Check for a `local_conf_header` key-name collision across the
  included kas files (design.md section 3) — kas merges these as one flat dict and silently drops
  whichever is processed first.
- **`bitbake-getvar` output looks like it still has a `VAR=` prefix or history comments when a
  script expected a bare value.** Use `--value`, not `-q` — `-q` only silences server logging, it
  doesn't change the output format.
- **A local repo checkout's branch mysteriously moved backward after running a script from this
  repo.** You likely pointed `--work-dir`/`KAS_WORK_DIR` at a symlink into that real checkout
  instead of a disposable directory — see the warning in the Tier 2 section above.
