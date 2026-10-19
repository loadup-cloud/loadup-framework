"""Regression tests for reactor discovery and generated documentation ownership."""
import contextlib
import importlib.util
import io
import sys
import tempfile
import unittest
from pathlib import Path
from unittest.mock import patch
from xml.sax.saxutils import escape

spec = importlib.util.spec_from_file_location('doc_sync', Path(__file__).resolve().parents[1] / 'doc-sync-agent.py')
sync = importlib.util.module_from_spec(spec)
with patch.object(sys, "dont_write_bytecode", True):
    spec.loader.exec_module(sync)


class DocSyncTest(unittest.TestCase):
    def setUp(self):
        self.temporary = tempfile.TemporaryDirectory()
        self.addCleanup(self.temporary.cleanup)
        self.root = Path(self.temporary.name).resolve()
        self.site = self.root / 'docs'
        self.content = self.site / 'content/docs'
        self.content.mkdir(parents=True)
        (self.site / 'hugo.toml').write_text('title = "fixture"\n')
        self.patcher = patch.multiple(sync, ROOT=self.root, SITE=self.site, CONTENT=self.content)
        self.patcher.start()
        self.addCleanup(self.patcher.stop)
        self.pom('.', ['loadup-components'])
        self.pom('loadup-components', ['data/loadup-components-cache'])
        self.pom('loadup-components/data/loadup-components-cache', ['cache-api'])
        self.pom('loadup-components/data/loadup-components-cache/cache-api')

    def pom(self, name, children=()):
        directory = self.root / name
        directory.mkdir(parents=True, exist_ok=True)
        modules = ''.join(f'<module>{escape(child)}</module>' for child in children)
        (directory / 'pom.xml').write_text(
            f'<project xmlns="http://maven.apache.org/POM/4.0.0"><modules>{modules}</modules></project>')
        for filename in sync.DOC_NAMES:
            (directory / filename).write_text(f'# {directory.name}\n\nSource {filename}.\n')
        return directory

    def execute(self, *options):
        with patch.object(sys, 'argv', ['doc-sync-agent.py', *options]), contextlib.redirect_stdout(io.StringIO()):
            return sync.main()

    def test_sync_supports_an_independent_site_repository(self):
        with tempfile.TemporaryDirectory() as directory:
            site = Path(directory).resolve()
            (site / 'hugo.toml').write_text('title = "separate repository"\n')
            (site / 'content').mkdir()
            homepage = site / 'content/_index.md'
            homepage.write_text('# Independently maintained homepage\n')
            self.assertEqual(self.execute('--site', str(site), '--apply'), 0)
            self.assertEqual(self.execute('--site', str(site), '--check'), 0)
            self.assertTrue((site / 'content/docs/components/cache.md').is_file())
            self.assertEqual(homepage.read_text(), '# Independently maintained homepage\n')

    def test_discovers_only_declared_modules_at_current_category_paths(self):
        ghost = self.pom('loadup-components/loadup-components-cache')
        self.pom('loadup-components/platform/abandoned-experiment')
        modules = sync.module_directories()
        self.assertNotIn(ghost, modules)
        self.assertEqual([str(p.relative_to(self.root)) for p in modules], [
            'loadup-components', 'loadup-components/data/loadup-components-cache',
            'loadup-components/data/loadup-components-cache/cache-api'])
        mapping = sync.source_map(modules)
        source = self.root / 'loadup-components/data/loadup-components-cache/README.md'
        self.assertEqual(mapping[source], self.content / 'components/cache.md')

    def test_missing_declared_pom_is_reported_instead_of_silently_skipped(self):
        self.pom('loadup-components', ['data/missing-component'])
        with self.assertRaisesRegex(ValueError, 'missing-component'):
            sync.module_directories()

    def test_repeated_module_declarations_are_rejected(self):
        self.pom('loadup-components', ['data/loadup-components-cache', 'data/loadup-components-cache'])
        with self.assertRaisesRegex(ValueError, 'Repeated or cyclic'):
            sync.module_directories()

    def test_malformed_pom_is_reported_before_sync(self):
        (self.root / 'loadup-components/pom.xml').write_text('<project>')
        with self.assertRaisesRegex(ValueError, 'Invalid Maven POM'):
            sync.module_directories()

    def test_module_outside_repository_is_rejected(self):
        self.pom('loadup-components', ['../../outside-repository'])
        with self.assertRaisesRegex(ValueError, 'outside'):
            sync.module_directories()

    def test_missing_module_document_is_still_a_failure(self):
        (self.root / 'loadup-components/data/loadup-components-cache/ARCHITECTURE.md').unlink()
        with self.assertRaisesRegex(ValueError, 'ARCHITECTURE.md'):
            sync.module_directories()

    def test_sync_never_writes_to_source_tree_or_handwritten_landing_pages(self):
        homepage = self.site / 'content/_index.md'
        landing = self.content / '_index.md'
        homepage.write_text('# Handwritten homepage\n')
        landing.write_text('# Handwritten docs landing\n')
        ghost = self.root / 'loadup-components/loadup-components-cache/cache.iml'
        ghost.parent.mkdir(parents=True)
        ghost.write_text('<module />')
        source_files = {p: p.read_bytes() for p in self.root.rglob('*') if p.is_file() and self.site not in p.parents}
        self.assertEqual(self.execute('--apply'), 0)
        self.assertEqual(self.execute('--check'), 0)
        self.assertEqual(homepage.read_text(), '# Handwritten homepage\n')
        self.assertEqual(landing.read_text(), '# Handwritten docs landing\n')
        self.assertEqual(source_files, {p: p.read_bytes() for p in self.root.rglob('*')
                                        if p.is_file() and self.site not in p.parents})
        generated = (self.content / 'components/cache.md').read_text()
        self.assertIn('loadup-components/data/loadup-components-cache/README.md', generated)
        self.assertNotIn('loadup-components/loadup-components-cache/README.md', generated)

    def test_obsolete_generated_page_removes_its_empty_parents_only(self):
        obsolete = self.content / 'components/old-cache/readme.md'
        obsolete.parent.mkdir(parents=True)
        obsolete.write_text(sync.GENERATED_MARKER + ' old -->\n')
        manual = self.content / 'components/manual/notes.md'
        manual.parent.mkdir(parents=True)
        manual.write_text('# Kept\n')
        self.assertEqual(self.execute('--check'), 1)
        self.assertTrue(obsolete.exists())
        self.assertEqual(self.execute('--apply'), 0)
        self.assertFalse(obsolete.parent.exists())
        self.assertTrue(manual.is_file())
        self.assertTrue(self.content.is_dir())


if __name__ == '__main__':
    unittest.main()
