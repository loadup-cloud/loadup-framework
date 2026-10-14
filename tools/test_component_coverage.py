"""Regression tests for cross-module measurement and independent coverage gates."""
import contextlib
import importlib.util
import io
import json
import subprocess
import sys
import tempfile
import unittest
from pathlib import Path
from unittest.mock import patch

spec = importlib.util.spec_from_file_location('coverage_tool', Path(__file__).with_name('component-coverage.py'))
coverage_tool = importlib.util.module_from_spec(spec)
spec.loader.exec_module(coverage_tool)


class ComponentCoverageTest(unittest.TestCase):
    def setUp(self):
        self.directory = tempfile.TemporaryDirectory()
        self.addCleanup(self.directory.cleanup)
        self.root = Path(self.directory.name)
        self.cli = self.root / 'jacoco.jar'
        self.cli.touch()
        self.module('high', compiled=True)
        self.module('low', compiled=True)
        test_module = self.module('shared-test', compiled=False, source=False)
        execution = test_module / 'target/jacoco-unit.exec'
        execution.parent.mkdir(parents=True)
        execution.touch()
        self.execution = execution
        self.warning = False

    def module(self, name, compiled, source=True):
        module = self.root / 'loadup-components/data' / name
        module.mkdir(parents=True)
        (module / 'pom.xml').write_text(f'<project xmlns="http://maven.apache.org/POM/4.0.0"><artifactId>{name}</artifactId></project>')
        if source:
            java = module / 'src/main/java/example/Service.java'
            java.parent.mkdir(parents=True)
            java.touch()
        if compiled:
            classes = module / 'target/classes/example'
            classes.mkdir(parents=True)
            (classes / 'Service.class').touch()
            (classes / 'Service$Nested.class').touch()
            (classes / 'GeneratedMapper.class').touch()
        return module

    def run_cli(self, command, **kwargs):
        if 'report' in command:
            staging = Path(command[command.index('--classfiles') + 1])
            self.assertEqual(sorted(p.name for p in staging.rglob('*.class')), ['Service$Nested.class', 'Service.class'])
            report = Path(command[command.index('--xml') + 1])
            branch_missed = 2 if report.parent.name == 'low' else 0
            report.write_text(f'<report><counter type="LINE" missed="0" covered="100"/><counter type="BRANCH" missed="{branch_missed}" covered="8"/></report>')
        return subprocess.CompletedProcess(command, 0, '[WARN] Classes do not match execution data' if self.warning else '', '')

    def run_tool(self, *arguments):
        argv = ['component-coverage.py', '--root', str(self.root), '--jacoco-cli', str(self.cli), *arguments]
        with patch.object(sys, 'argv', argv), patch.object(coverage_tool.subprocess, 'run', side_effect=self.run_cli), contextlib.redirect_stdout(io.StringIO()), contextlib.redirect_stderr(io.StringIO()):
            return coverage_tool.main()

    def test_sibling_execution_data_and_generated_exclusion_do_not_hide_low_branch_coverage(self):
        self.assertEqual(self.run_tool(), 1)
        summary = json.loads((self.root / 'loadup-components/target/coverage/summary.json').read_text())
        self.assertEqual(summary['execution_files'], 1)
        self.assertEqual(len(summary['modules']), 2)
        self.assertEqual([entry['passed'] for entry in summary['modules']], [True, False])

    def test_report_only_is_explicitly_diagnostic(self):
        self.assertEqual(self.run_tool('--report-only'), 0)

    def test_missing_classes_fail_even_if_the_other_module_passes(self):
        classes = self.root / 'loadup-components/data/low/target/classes'
        import shutil
        shutil.rmtree(classes)
        self.assertEqual(self.run_tool(), 1)

    def test_stale_execution_data_is_rejected(self):
        self.warning = True
        with self.assertRaisesRegex(RuntimeError, 'Stale or inconsistent'):
            self.run_tool()

    def test_partially_compiled_module_cannot_pass(self):
        (self.root / 'loadup-components/data/high/src/main/java/example/Missing.java').touch()
        self.assertEqual(self.run_tool(), 1)

    def test_no_execution_data_is_not_success(self):
        self.execution.unlink()
        with self.assertRaises(SystemExit) as result:
            self.run_tool()
        self.assertEqual(result.exception.code, 2)


if __name__ == '__main__':
    unittest.main()
