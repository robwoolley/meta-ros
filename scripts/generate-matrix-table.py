#!/usr/bin/env python3
# Copyright (c) 2026 Wind River Systems, Inc.
#
# Emit the "supported combinations" Markdown table from matrix.yml, for kas/README.md (Phase 5,
# docs/kas-to-fragments-migration-spec.md section 5). This is the one part of the documentation
# that would silently drift if hand-maintained -- everything else in the README is prose.
#
# Usage: scripts/generate-matrix-table.py [--matrix PATH]

import argparse
from pathlib import Path

import yaml


def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--matrix", default="matrix.yml", type=Path)
    args = ap.parse_args()

    with open(args.matrix) as f:
        matrix = yaml.safe_load(f)

    print("| Release | ROS distro | Machines | Fragment-capable |")
    print("|---|---|---|---|")
    for cell in matrix["cells"]:
        release = cell["release"]
        fragment_capable = matrix["releases"][release].get("fragment_capable", False)
        machines = ", ".join(f"`{m}`" for m in cell["machines"])
        print(f"| `{release}` | `{cell['ros_distro']}` | {machines} | {'yes' if fragment_capable else 'no'} |")


if __name__ == "__main__":
    main()
