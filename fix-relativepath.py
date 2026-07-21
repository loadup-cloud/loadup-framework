#!/usr/bin/env python3
"""Remove relativePath lines referencing loadup-dependencies from all POMs."""
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parent

count = 0
for pom in ROOT.rglob("pom.xml"):
    if "target" in str(pom):
        continue
    content = pom.read_text(encoding="utf-8")
    new_content = re.sub(
        r"\s*<relativePath>.*?loadup-dependencies/pom\.xml</relativePath>\s*\n",
        "\n",
        content,
    )
    if new_content != content:
        pom.write_text(new_content, encoding="utf-8")
        count += 1
        print(f"  Fixed: {pom.relative_to(ROOT)}")

print(f"Removed relativePath from {count} POMs")
