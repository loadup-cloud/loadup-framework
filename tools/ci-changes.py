#!/usr/bin/env python3
"""Decide whether a workflow needs the backend reactor; unknown changes run it."""
import argparse
import json
import os
from pathlib import Path
import subprocess


def requires_backend(paths):
    for path in paths:
        if Path(path).name in {'README.md', 'ARCHITECTURE.md', 'AGENTS.md'}:
            continue
        if path in {'ROADMAP.md', 'LICENSE', 'NOTICE', 'doc-sync-agent.py', 'tools/test_doc_sync.py'}:
            continue
        return True
    return False


def backend_changed(event_name, event, root):
    if event_name == 'pull_request':
        base = event['pull_request']['base']['sha']
        head = event['pull_request']['head']['sha']
        revisions = [f'{base}...{head}']
    elif event_name == 'push':
        base, head = event['before'], event['after']
        if base == '0' * 40 or head == '0' * 40:
            return True
        revisions = [base, head]
    else:
        # Manual runs and unknown events always verify the backend.
        return True
    try:
        result = subprocess.run(
            ['git', 'diff', '--no-renames', '--name-only', '-z', *revisions, '--'],
            cwd=root, check=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE,
        )
    except subprocess.CalledProcessError:
        # Force-push history can be unavailable even after a full checkout.
        print('Comparison history unavailable; running backend checks conservatively.')
        return True
    return requires_backend(os.fsdecode(path) for path in result.stdout.split(b'\0') if path)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--event-name', default=os.environ.get('GITHUB_EVENT_NAME'))
    parser.add_argument('--event-path', default=os.environ.get('GITHUB_EVENT_PATH'))
    args = parser.parse_args()
    if not args.event_name or not args.event_path:
        parser.error('Provide --event-name and --event-path, or their GitHub environment variables')
    event = json.loads(Path(args.event_path).read_text())
    changed = backend_changed(args.event_name, event, Path(__file__).resolve().parents[1])
    value = str(changed).lower()
    print(f'Backend changes: {value}')
    if output := os.environ.get('GITHUB_OUTPUT'):
        with open(output, 'a') as stream:
            stream.write(f'backend={value}\n')
    return 0


if __name__ == '__main__':
    raise SystemExit(main())
