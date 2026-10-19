"""Regression coverage for backend CI selection, including renames and history."""
import contextlib
import importlib.util
import io
import json
import os
import sys
from pathlib import Path
import subprocess
import tempfile
import unittest
from unittest.mock import patch

spec = importlib.util.spec_from_file_location('ci_changes', Path(__file__).with_name('ci-changes.py'))
changes = importlib.util.module_from_spec(spec)
spec.loader.exec_module(changes)


class BackendChangesTest(unittest.TestCase):
    def test_documentation_only_skips_backend(self):
        self.assertFalse(changes.requires_backend([
            'README.md', 'ROADMAP.md', 'loadup-components/demo/ARCHITECTURE.md',
            'doc-sync-agent.py', 'tools/test_doc_sync.py',
        ]))
        self.assertFalse(changes.requires_backend([]))

    def test_runtime_markdown_unknown_and_mixed_changes_run_backend(self):
        for path in ['pom.xml', 'loadup-dependencies/pom.xml', 'config/quality/checkstyle.xml',
                     '.github/workflows/ci.yml', '.github/actions/setup-build/action.yml',
                     'tools/component-coverage.py', 'quality-check.sh',
                     'loadup-application/src/main/resources/template.md', 'frontend/package.json',
                     'docs/hugo.toml', 'unknown.txt']:
            with self.subTest(path=path):
                self.assertTrue(changes.requires_backend(['README.md', path]))

    def test_manual_and_new_branch_runs_verify_backend(self):
        self.assertTrue(changes.backend_changed('workflow_dispatch', {}, Path('.')))
        self.assertTrue(changes.backend_changed('push', {'before': '0' * 40, 'after': 'a' * 40}, Path('.')))

    def test_missing_history_runs_backend(self):
        with patch.object(changes.subprocess, 'run', side_effect=subprocess.CalledProcessError(128, 'git')):
            self.assertTrue(changes.backend_changed('push', {'before': 'a', 'after': 'b'}, Path('.')))

    def test_cli_appends_boolean_workflow_output(self):
        with tempfile.TemporaryDirectory() as directory:
            event = Path(directory) / 'event.json'
            event.write_text(json.dumps({}))
            output = Path(directory) / 'output'
            output.write_text('existing=value\n')
            environment = {'GITHUB_EVENT_NAME': 'workflow_dispatch',
                           'GITHUB_EVENT_PATH': str(event), 'GITHUB_OUTPUT': str(output)}
            with patch.dict(os.environ, environment), patch.object(sys, 'argv', ['ci-changes.py']), contextlib.redirect_stdout(io.StringIO()):
                self.assertEqual(changes.main(), 0)
            self.assertEqual(output.read_text(), 'existing=value\nbackend=true\n')

    def test_git_comparison_detects_removed_backend_file_and_pr_merge_base(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)

            def git(*args):
                return subprocess.run(['git', *args], cwd=root, check=True, capture_output=True, text=True).stdout.strip()

            def commit(message):
                git('add', '.')
                git('commit', '-qm', message)
                return git('rev-parse', 'HEAD')

            git('init', '-q')
            git('config', 'user.name', 'CI fixture')
            git('config', 'user.email', 'fixture@example.invalid')
            (root / 'backend.java').write_text('fixture')
            base = commit('backend')
            (root / 'guide').mkdir()
            (root / 'guide/README.md').write_text('fixture')
            documentation = commit('documentation')
            self.assertFalse(changes.backend_changed('push', {'before': base, 'after': documentation}, root))
            pr = {'pull_request': {'base': {'sha': base}, 'head': {'sha': documentation}}}
            self.assertFalse(changes.backend_changed('pull_request', pr, root))
            git('checkout', '-qb', 'target', base)
            (root / 'backend.java').write_text('target branch change')
            target = commit('target advances')
            pr['pull_request']['base']['sha'] = target
            self.assertFalse(changes.backend_changed('pull_request', pr, root))
            git('checkout', '-q', documentation)
            # Renaming backend code into docs must still trigger the backend checks.
            (root / 'archive').mkdir()
            (root / 'backend.java').rename(root / 'archive/README.md')
            moved = commit('move backend into docs')
            self.assertTrue(changes.backend_changed('push', {'before': documentation, 'after': moved}, root))


if __name__ == '__main__':
    unittest.main()
