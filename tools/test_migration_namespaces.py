"""Keep framework migrations opt-in by module and component templates outside Flyway."""

from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]


class MigrationNamespacesTest(unittest.TestCase):
    def test_business_migrations_have_their_own_module_namespace(self):
        sources = list(ROOT.glob("loadup-modules/**/src/main/resources/db/migration/**/*.sql"))
        self.assertTrue(sources)
        for source in sources:
            family = source.relative_to(ROOT).parts[1].removeprefix("loadup-modules-")
            with self.subTest(source=source):
                self.assertEqual(("db", "migration", "framework", family), source.parent.parts[-4:])

    def test_components_do_not_publish_implicitly_executed_migrations(self):
        self.assertEqual([], list(ROOT.glob("loadup-components/**/src/main/resources/db/migration/**/*.sql")))


if __name__ == "__main__":
    unittest.main()
