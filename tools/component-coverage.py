#!/usr/bin/env python3
"""Generate honest cross-module JaCoCo reports and gate component coverage."""
import argparse
import json
import shutil
import subprocess
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

NS = {'m': 'http://maven.apache.org/POM/4.0.0'}

def ratio(counter):
    if counter is None:
        return 1.0
    missed, covered = int(counter.get('missed')), int(counter.get('covered'))
    return covered / (missed + covered) if missed + covered else 1.0

def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--jacoco-cli', type=Path)
    parser.add_argument('--root', type=Path, default=Path(__file__).resolve().parents[1])
    parser.add_argument('--minimum', type=float, default=.90)
    parser.add_argument('--report-only', action='store_true')
    parser.add_argument('--list-modules', action='store_true', help='Print the Maven selection, including sibling test modules')
    args = parser.parse_args()
    root = args.root.resolve()
    component_root = root / 'loadup-components'
    poms = [p for p in sorted(component_root.rglob('pom.xml')) if 'target' not in p.parts]
    modules = [(p.parent, ET.parse(p).getroot()) for p in poms]
    modules = [(p, model) for p, model in modules if model.findtext('m:packaging', 'jar', NS) != 'pom']
    if args.list_modules:
        print(','.join(str(p.relative_to(root)) for p, _ in modules))
        return 0
    if not 0 <= args.minimum <= 1:
        parser.error('--minimum must be between 0 and 1')
    if args.jacoco_cli is None or not args.jacoco_cli.is_file():
        parser.error('JaCoCo CLI jar does not exist')
    output = component_root / 'target' / 'coverage'
    output.mkdir(parents=True, exist_ok=True)
    executions = sorted(set(component_root.rglob('target/jacoco-unit.exec')) | set(component_root.rglob('target/jacoco-it.exec')))
    if not executions:
        parser.error('No component execution data. Run clean verify before reporting.')
    cli = ['java', '-jar', str(args.jacoco_cli.resolve())]
    merged = output / 'components.exec'
    subprocess.run(cli + ['merge', *map(str, executions), '--destfile', str(merged)], check=True, stdout=subprocess.DEVNULL)
    entries = []
    for module, model in modules:
        sources = module / 'src/main/java'
        java_files = list(sources.rglob('*.java')) if sources.exists() else []
        if not java_files:
            continue
        artifact = model.findtext('m:artifactId', namespaces=NS)
        compiled = module / 'target/classes'
        folder = output / artifact
        staging = folder / 'handwritten-classes'
        if staging.exists():
            shutil.rmtree(staging)
        staging.mkdir(parents=True)
        # Generated persistence/MapStruct sources live outside src/main/java.
        # No handwritten DTO, configuration, adapter or exception is excluded.
        source_paths = {f.relative_to(sources).with_suffix('') for f in java_files}
        represented = set()
        copied = 0
        for bytecode in compiled.rglob('*.class') if compiled.exists() else []:
            relative = bytecode.relative_to(compiled)
            outer = relative.parent / relative.stem.split('$', 1)[0]
            if outer not in source_paths:
                continue
            destination = staging / relative
            destination.parent.mkdir(parents=True, exist_ok=True)
            shutil.copy2(bytecode, destination)
            represented.add(outer)
            copied += 1
        entry = {'artifact': artifact, 'component': str(module.relative_to(component_root)).split('/')[1],
                 'module': str(module.relative_to(root)), 'classes': copied}
        missing = source_paths - represented
        if not copied or missing:
            entry.update(status='MISSING_CLASSES', lines=0.0, branches=0.0)
            entry['missing_sources'] = sorted(str(p) for p in missing)
        else:
            report = folder / 'jacoco.xml'
            result = subprocess.run(cli + ['report', str(merged), '--classfiles', str(staging), '--sourcefiles', str(sources),
                                 '--xml', str(report), '--html', str(folder / 'html')], check=True, capture_output=True, text=True)
            diagnostics = result.stdout + result.stderr
            if '[WARN]' in diagnostics:
                # A class mismatch can otherwise look like a valid low/partial report.
                raise RuntimeError(f'Stale or inconsistent coverage data for {artifact}: {diagnostics}')
            tree = ET.parse(report).getroot()
            entry.update(status='MEASURED', lines=ratio(tree.find("counter[@type='LINE']")),
                         branches=ratio(tree.find("counter[@type='BRANCH']")))
            entry['counters'] = {c.get('type'): {'missed': int(c.get('missed')), 'covered': int(c.get('covered'))}
                                 for c in tree.findall('counter')}
        entry['passed'] = entry['status'] == 'MEASURED' and entry['lines'] >= args.minimum and entry['branches'] >= args.minimum
        entries.append(entry)
        print(f"{'PASS' if entry['passed'] else 'FAIL'} {artifact}: lines={entry['lines']:.2%}, branches={entry['branches']:.2%} ({entry['status']})")
    summary = {'minimum': args.minimum, 'execution_files': len(executions), 'modules': entries,
               'passed': bool(entries) and all(e['passed'] for e in entries)}
    (output / 'summary.json').write_text(json.dumps(summary, ensure_ascii=False, indent=2) + '\n')
    rows = ['# Component coverage', '', '| Artifact | Lines | Branches | Result |', '|---|---:|---:|---|']
    rows += [f"| {e['artifact']} | {e['lines']:.2%} | {e['branches']:.2%} | {'PASS' if e['passed'] else e['status'] if e['status'] != 'MEASURED' else f'BELOW {args.minimum:.0%}'} |" for e in entries]
    (output / 'summary.md').write_text('\n'.join(rows) + '\n')
    print(f"Report: {output / 'summary.md'}; passed {sum(e['passed'] for e in entries)}/{len(entries)} jars")
    return 0 if args.report_only or summary['passed'] else 1

if __name__ == '__main__':
    sys.exit(main())
