#!/usr/bin/env python3
# Copyright (c) 2026 Wind River Systems, Inc.
#
# Generate kas/oeros-<release>-<ros-distro>-<machine>.yml from matrix.yml.
#
# Phase 2 (docs/kas-to-fragments-migration-spec.md section 5): reproduces today's kas/ tree
# byte-for-byte for every release, including wrynose and master, which still carry their
# pre-fragment shape at this point in the migration. Phase 3 will change what this script emits
# for fragment_capable releases to the reduced OE_FRAGMENTS-based shape; that is not implemented
# here yet.
#
# Usage:
#   scripts/generate-kas.py [--check] [--matrix PATH] [--out-dir DIR]
#
# --check: don't write files; instead diff the generated content against what's on disk under
#   --out-dir (default: kas/) and exit non-zero if anything differs. This is the drift-detection
#   check referenced in the spec (section 3.3) and is meant to run in CI.

import argparse
import difflib
import sys
from pathlib import Path

import yaml

HEADER_VERSION = 14


def render_cell(release, release_data, ros_distro, ros_data, machine, source_overrides):
    includes = [release_data["yocto_file"], ros_data["file"], f"kas/machine/{machine}.yml", "kas/common.yml"]
    includes.append(f"kas/layer/{ros_data['qt_layer']}.yml")
    includes.extend(release_data.get("extra_includes", []))
    if release_data.get("fragment_capable"):
        # meta-ros no longer provides its own DISTRO on fragment-capable branches (spec section 1.9);
        # this must come after ros_data["file"] in the list, since kas resolves same-key conflicts
        # between included files in favor of whichever is listed last (verified empirically).
        includes.append("kas/oeros-distro.yml")

    lines = ["header:", f"  version: {HEADER_VERSION}", "  includes:"]
    lines.extend(f"    - {inc}" for inc in includes)

    if source_overrides:
        lines.append("")
        lines.append("repos:")
        for repo_name in sorted(source_overrides.keys()):
            override = source_overrides[repo_name]
            lines.append(f"  {repo_name}:")
            if "branch" in override:
                lines.append(f'    branch: "{override["branch"]}"')
            if "layers" in override:
                lines.append("    layers:")
                for layer_name, layer_state in override["layers"].items():
                    lines.append(f"      {layer_name}: {layer_state}")

    return "\n".join(lines) + "\n"


def merged_overrides(release_data, cell_overrides):
    merged = {k: dict(v) for k, v in release_data.get("default_source_overrides", {}).items()}
    for k, v in (cell_overrides or {}).items():
        merged.setdefault(k, {}).update(v)
    return merged


def generate_all(matrix):
    releases = matrix["releases"]
    ros_distros = matrix["ros_distros"]
    out = {}
    for cell in matrix["cells"]:
        release = cell["release"]
        ros_distro = cell["ros_distro"]
        release_data = releases[release]
        ros_data = ros_distros[ros_distro]
        overrides = merged_overrides(release_data, cell.get("source_overrides"))
        for machine in cell["machines"]:
            filename = f"kas/oeros-{release}-{ros_distro}-{machine}.yml"
            out[filename] = render_cell(release, release_data, ros_distro, ros_data, machine, overrides)
    return out


def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--matrix", default="matrix.yml", type=Path)
    ap.add_argument("--out-dir", default=".", type=Path,
                     help="Directory generated kas/oeros-*.yml paths are relative to (default: repo root)")
    ap.add_argument("--check", action="store_true", help="Diff against disk instead of writing; exit 1 on drift")
    args = ap.parse_args()

    with open(args.matrix) as f:
        matrix = yaml.safe_load(f)

    generated = generate_all(matrix)

    if args.check:
        drift = False
        for filename, content in sorted(generated.items()):
            path = args.out_dir / filename
            if not path.exists():
                print(f"MISSING: {filename}")
                drift = True
                continue
            current = path.read_text()
            if current != content:
                print(f"DRIFT: {filename}")
                diff = difflib.unified_diff(
                    current.splitlines(keepends=True), content.splitlines(keepends=True),
                    fromfile=f"disk/{filename}", tofile=f"generated/{filename}",
                )
                sys.stdout.writelines(diff)
                drift = True
        # Files on disk that the matrix no longer accounts for (eg the double-dot typo file).
        # oeros-devel.yml and oeros-distro.yml are hand-maintained inputs, like yocto/*.yml and
        # ros1/ros2/*.yml, not generated matrix cells, despite matching the oeros-*.yml glob.
        hand_maintained = {"oeros-devel.yml", "oeros-distro.yml"}
        for path in sorted((args.out_dir / "kas").glob("oeros-*.yml")):
            rel = str(path.relative_to(args.out_dir))
            if rel not in generated and path.name not in hand_maintained:
                print(f"UNEXPECTED (not in matrix.yml, not hand-maintained): {rel}")
                drift = True
        sys.exit(1 if drift else 0)
    else:
        for filename, content in generated.items():
            path = args.out_dir / filename
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_text(content)
        print(f"Wrote {len(generated)} files under {args.out_dir}")


if __name__ == "__main__":
    main()
