#!/usr/bin/env python3
# Copyright (c) 2026 Wind River Systems, Inc.
#
# Generate meta-oeros/conf/registry/configurations/oeros-<release>-<ros-distro>.conf.json from
# matrix.yml, for fragment_capable releases only (Phase 4,
# docs/kas-to-fragments-migration-spec.md section 5).
#
# Design: rather than re-deriving each repo's resolved layers (enabled/disabled subdirs) from
# matrix.yml independently -- kas's own merge rules for this turned out to have real subtleties
# (eg one included file disabling a layer another one doesn't, where the "last file wins" rule
# that holds for scalars like distro:/machine: does NOT hold -- verified live, not assumed) --
# this generator runs `kas dump` against the *already-generated* kas/oeros-*.yml file for each
# (release, ros_distro, machine) combination and treats its resolved repos: block as ground truth.
# For a (release, ros_distro) group spanning several machines, the sources/bb-layers emitted are
# the union across every machine in the group, since bitbake-setup presents machine choice as a
# single-file oe-fragments-one-of picker (matching the existing oeros-master-rolling.conf.json
# precedent) rather than kas's one-file-per-machine layout.
#
# Usage:
#   scripts/generate-bitbake-setup.py [--matrix PATH] [--kas-dir DIR] [--out-dir DIR]
#       [--kas-bin PATH] [--work-dir DIR]

import argparse
import json
import os
import subprocess
from collections import OrderedDict
from pathlib import Path

import yaml

# kas's own repos: keys are short, ad hoc names (eg "ros", "clang", "raspberrypi"). The existing
# oeros-master-rolling.conf.json precedent already published meta-prefixed source names for most
# of these (matching the actual upstream repo names); map kas's names onto that convention so the
# whole registry stays internally consistent instead of mixing "ros" here and "meta-ros" there.
# Anything not listed keeps its kas name unchanged (openembedded-core, bitbake,
# meta-openembedded, meta-yocto already match).
SOURCE_NAME_MAP = {
    "clang": "meta-clang",
    "clang-revival": "meta-clang-revival",
    "realsense": "meta-intel-realsense",
    "ros": "meta-ros",
    "virtualization": "meta-virtualization",
    "zenoh": "meta-zenoh",
    "raspberrypi": "meta-raspberrypi",
    "python-ai": "meta-python-ai",
    "oeros": "meta-oeros",
    "qt5": "meta-qt5",
    "qt6": "meta-qt6",
}


def kas_dump(kas_bin, work_dir, kas_file):
    result = subprocess.run(
        [kas_bin, "dump", "--skip", "repos_checkout", "--skip", "repos_apply_patches", "--sort", str(kas_file)],
        capture_output=True, text=True,
        env={"KAS_WORK_DIR": str(work_dir), **os.environ},
    )
    if result.returncode != 0:
        raise SystemExit(f"kas dump failed for {kas_file}:\n{result.stdout}\n{result.stderr}")
    return yaml.safe_load(result.stdout)


def repo_source_entry(repo):
    branch = repo.get("branch") or "master"
    rev = repo.get("commit") or branch
    return {"git-remote": {"uri": repo["url"], "branch": branch, "rev": rev}}


def repo_bb_layers(source_name, repo):
    layers = repo.get("layers")
    if not layers:
        return [source_name]
    return [f"{source_name}/{sub}" for sub, state in layers.items() if state != "disabled"]


def collect_group(kas_bin, work_dir, kas_dir, release, ros_distro, machines):
    sources = OrderedDict()
    bb_layers = []
    seen_layers = set()
    for machine in machines:
        kas_file = kas_dir / f"oeros-{release}-{ros_distro}-{machine}.yml"
        dumped = kas_dump(kas_bin, work_dir, kas_file)
        for repo_name, repo in dumped.get("repos", {}).items():
            source_name = SOURCE_NAME_MAP.get(repo_name, repo_name)
            layers = repo_bb_layers(source_name, repo)
            # A repo whose every layers: entry is "disabled" (eg clang-revival) contributes
            # nothing to bb-layers; don't clone it for bitbake-setup users at all, unlike kas
            # which fetches it regardless since its repos: mechanism has no way to skip that.
            if repo.get("layers") and not layers:
                continue
            sources[source_name] = repo_source_entry(repo)
            for layer in layers:
                if layer not in seen_layers:
                    seen_layers.add(layer)
                    bb_layers.append(layer)
    return sources, bb_layers


def build_config(release, release_data, ros_distro, ros_data, machines, machines_meta):
    fragments = ["distro/oeros", "oeros/common"]
    if release_data.get("qa_fragment"):
        fragments.append(release_data["qa_fragment"])
    # oeros/allow-commercial-licenses is machine-conditional in the kas output (only Raspberry Pi
    # machines need it), but oe-fragments-one-of has no concept of "also enable this fragment when
    # this option is picked" -- bitbake-setup's schema only supports a flat list of mutually
    # exclusive named options per picker group. Rather than omit the fragment (which would silently
    # under-license a Raspberry Pi build picked through this path) or split into per-machine JSON
    # files (losing the picker), it is enabled unconditionally here: accepting a broader set of
    # allowed license flags than a qemu build strictly needs is harmless (spec section 2.1's
    # existing fragment already frames it this way -- "allow", not "require").
    if any("allow-commercial-licenses" in f for m in machines for f in machines_meta.get(m, {}).get("fragments", [])):
        fragments.append("oeros/allow-commercial-licenses")

    machine_options = [
        {
            "name": f"machine/{m}",
            "description": machines_meta.get(m, {}).get("description", m),
        }
        for m in machines
    ]

    return {
        "description": f"OpenEmbedded ROS - {release.capitalize()} {ros_distro.capitalize()}",
        "sources": None,  # filled in by caller, kept here only to fix key order
        "bitbake-setup": {
            "configurations": [
                {
                    "name": f"oeros-{release}-{ros_distro}",
                    "description": f"OpenEmbedded ROS {release.capitalize()} {ros_distro.capitalize()}",
                    "setup-dir-name": f"oeros-{release}-{ros_distro}",
                    "bb-layers": None,  # filled in by caller
                    "bb-env-passthrough-additions": ["DL_DIR", "SSTATE_DIR"],
                    "oe-fragments": fragments,
                    "oe-fragments-one-of": {
                        "machine": {
                            "description": "Target machines",
                            "options": machine_options,
                        }
                    },
                }
            ]
        },
        "version": "1.0",
    }


def load_schema_validator(schema_dir):
    # bitbake-setup.schema.json's "sources" property is a $ref into layers.schema.json in the
    # same directory, so both need to be loadable relative to each other, not just the top-level
    # file. jsonschema's referencing.Registry resolves that from the file:// base URI.
    import jsonschema
    from referencing import Registry, Resource

    def load(name):
        return json.loads((schema_dir / name).read_text())

    root_uri = f"{schema_dir}/bitbake-setup.schema.json"
    schema = load("bitbake-setup.schema.json")
    schema["$id"] = root_uri  # neither file sets its own $id; without one, "layers.schema.json"
    # in the $ref has no base URI to resolve relative to and lookup fails (verified live).

    resources = [
        (root_uri, Resource.from_contents(schema)),
        (f"{schema_dir}/layers.schema.json", Resource.from_contents(load("layers.schema.json"))),
    ]
    registry = Registry().with_resources(resources)
    validator_cls = jsonschema.validators.validator_for(schema)
    return validator_cls(schema, registry=registry)


def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--matrix", default="matrix.yml", type=Path)
    ap.add_argument("--kas-dir", default="kas", type=Path, help="Directory of already-generated kas/oeros-*.yml files")
    ap.add_argument("--out-dir", default="meta-oeros-registry", type=Path)
    ap.add_argument("--kas-bin", default="kas")
    ap.add_argument("--work-dir", required=True, type=Path)
    ap.add_argument("--schema-dir", type=Path,
                     help="Directory containing bitbake-setup.schema.json and layers.schema.json "
                          "(from the bitbake git repo's setup-schema/ directory) to validate output against")
    args = ap.parse_args()

    validator = load_schema_validator(args.schema_dir) if args.schema_dir else None

    with open(args.matrix) as f:
        matrix = yaml.safe_load(f)

    releases = matrix["releases"]
    ros_distros = matrix["ros_distros"]
    machines_meta = matrix["machines"]

    args.out_dir.mkdir(parents=True, exist_ok=True)
    args.work_dir.mkdir(parents=True, exist_ok=True)

    written = []
    for cell in matrix["cells"]:
        release = cell["release"]
        release_data = releases[release]
        if not release_data.get("fragment_capable"):
            continue
        ros_distro = cell["ros_distro"]
        ros_data = ros_distros[ros_distro]
        machines = cell["machines"]

        sources, bb_layers = collect_group(args.kas_bin, args.work_dir, args.kas_dir, release, ros_distro, machines)
        config = build_config(release, release_data, ros_distro, ros_data, machines, machines_meta)
        config["sources"] = sources  # replaces the None placeholder, keeping key order intact
        config["bitbake-setup"]["configurations"][0]["bb-layers"] = bb_layers

        if validator is not None:
            errors = sorted(validator.iter_errors(config), key=lambda e: e.path)
            if errors:
                for e in errors:
                    print(f"SCHEMA ERROR in oeros-{release}-{ros_distro}: {'/'.join(str(p) for p in e.path)}: {e.message}")
                raise SystemExit(1)

        out_path = args.out_dir / f"oeros-{release}-{ros_distro}.conf.json"
        out_path.write_text(json.dumps(config, indent=4) + "\n")
        written.append(out_path)
        print(f"Wrote {out_path}" + (" (schema-valid)" if validator is not None else ""))

    print(f"\n{len(written)} configuration(s) written under {args.out_dir}")


if __name__ == "__main__":
    main()
