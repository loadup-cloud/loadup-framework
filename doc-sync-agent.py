#!/usr/bin/env python3
"""Sync each Maven module's README and ARCHITECTURE to the Hugo site.

Usage: python3 doc-sync-agent.py [--apply | --check | --build]
Default is a read-only preview. --build applies changes before running Hugo.
"""
from __future__ import annotations

import argparse
import json
import os
import re
import subprocess
import sys
from pathlib import Path
from urllib.parse import quote

ROOT = Path(__file__).resolve().parent
SITE = ROOT / "docs"
CONTENT = SITE / "content" / "docs"
SOURCE_URL = "https://github.com/loadup-cloud/loadup-framework/blob/main/"
DOC_NAMES = ("README.md", "ARCHITECTURE.md")
IGNORED = {".git", ".codegraph", "docs", "target"}
LINK = re.compile(r"(!?\[[^\]]*\]\()([^\s)]+)(\))")


def module_directories() -> list[Path]:
    modules = []
    for pom in ROOT.rglob("pom.xml"):
        relative = pom.relative_to(ROOT)
        if len(relative.parts) < 2 or IGNORED.intersection(relative.parts):
            continue
        module = pom.parent
        missing = [name for name in DOC_NAMES if not (module / name).is_file()]
        if missing:
            raise ValueError(f"{module.relative_to(ROOT)} lacks {', '.join(missing)}")
        modules.append(module)
    return sorted(modules)


def site_path(module: Path, name: str) -> Path:
    parts = module.relative_to(ROOT).parts
    family = parts[0]
    if family == "loadup-components":
        slug = module.name.removeprefix("loadup-components-")
        base = CONTENT / "components" / "_index.md" if len(parts) == 1 else CONTENT / "components" / f"{slug}.md"
    elif family == "loadup-commons":
        base = CONTENT / "commons" / "_index.md" if len(parts) == 1 else CONTENT / "commons" / f"{module.name}.md"
    elif family == "loadup-modules":
        slug = module.name.removeprefix("loadup-modules-")
        base = CONTENT / "modules" / "_index.md" if len(parts) == 1 else CONTENT / "modules" / f"{slug}.md"
    elif family == "loadup-testify":
        base = CONTENT / "testing" / "_index.md" if len(parts) == 1 else CONTENT / "testing" / f"{module.name}.md"
    elif family in ("loadup-application", "loadup-dependencies"):
        base = CONTENT / (family.removeprefix("loadup-") + ".md")
    else:
        raise ValueError(f"No site location for {module.relative_to(ROOT)}")
    if name == "README.md":
        return base
    return base.with_name("architecture.md" if base.stem == "_index" else base.stem + "-architecture.md")


def source_map(modules: list[Path]) -> dict[Path, Path]:
    mapping = {module / name: site_path(module, name) for module in modules for name in DOC_NAMES}
    if len(mapping) != len(set(mapping.values())):
        raise ValueError("Two module documents map to the same Hugo page")
    return mapping


def rewrite_destination(destination: str, source: Path, target: Path, mapping: dict[Path, Path], image: bool) -> str:
    if destination.startswith(("#", "/", "http:", "https:", "mailto:", "data:")):
        return destination
    path_part, marker, fragment = destination.partition("#")
    resolved = (source.parent / path_part).resolve()
    try:
        relative = resolved.relative_to(ROOT)
    except ValueError:
        return destination
    suffix = marker + fragment if marker else ""
    if resolved in mapping:
        # Hugo's default pretty URL treats each Markdown page as a directory.
        # Link to that URL, not to the source .md path that Hugo does not serve.
        page = page_url_directory(mapping[resolved])
        current_page = page_url_directory(target)
        return Path(os.path.relpath(page, current_page)).as_posix().rstrip("/") + "/" + suffix
    if resolved.is_file():
        base = SOURCE_URL.replace("github.com/", "raw.githubusercontent.com/").replace("/blob/", "/") if image else SOURCE_URL
        return base + quote(relative.as_posix()) + suffix
    return destination


def page_url_directory(page: Path) -> Path:
    return page.parent if page.stem == "_index" else page.with_suffix("")


def rewrite_links(body: str, source: Path, target: Path, mapping: dict[Path, Path]) -> str:
    lines = []
    fence = None
    for line in body.splitlines(keepends=True):
        match = re.match(r"^\s*(```|~~~)", line)
        if match:
            fence = None if fence == match.group(1) else match.group(1)
        if fence is None and not match:
            line = LINK.sub(lambda item: item.group(1) + rewrite_destination(
                item.group(2), source, target, mapping, item.group(1).startswith("!")) + item.group(3), line)
        lines.append(line)
    return "".join(lines)


def render(source: Path, target: Path, mapping: dict[Path, Path]) -> str:
    body = source.read_text(encoding="utf-8").lstrip("\ufeff")
    if body.startswith("---\n"):
        _, _, body = body.partition("\n---\n")
    match = re.search(r"^# (.+)$", body, re.MULTILINE)
    title = match.group(1).strip() if match else source.parent.name
    if source.name == "ARCHITECTURE.md" and not re.search(r"架构|architecture", title, re.IGNORECASE):
        title += " 架构"
    relative = source.relative_to(ROOT).as_posix()
    frontmatter = f"---\ntitle: {json.dumps(title, ensure_ascii=False)}\n---\n\n"
    marker = f"<!-- Generated by doc-sync-agent.py; source: {relative} -->\n\n"
    return frontmatter + marker + rewrite_links(body.rstrip() + "\n", source, target, mapping)


def obsolete_placeholders(mapping: dict[Path, Path]) -> list[Path]:
    original = CONTENT / "original"
    obsolete = [path for path in original.glob("*.md")
                if "(Full contents from repository)" in path.read_text(encoding="utf-8")]
    old_modules_index = CONTENT / "modules" / "index.md"
    if old_modules_index.is_file() and "已迁移到 `docs/modules/`" in old_modules_index.read_text(encoding="utf-8"):
        obsolete.append(old_modules_index)
    expected = set(mapping.values()) | {CONTENT / "_index.md"}
    obsolete.extend(path for path in CONTENT.rglob("*.md") if path not in expected
                    and "Generated by doc-sync-agent.py" in path.read_text(encoding="utf-8")[:300])
    return sorted(obsolete)


def render_index() -> str:
    return """---
title: "LoadUp Framework"
---

<!-- Generated by doc-sync-agent.py; source: Maven module documentation -->

# LoadUp Framework

LoadUp 是供 Spring Boot 应用按需引入的框架/SDK。使用 `loadup-dependencies` BOM 管理版本，
再选择需要的技术组件或通用业务模块。`loadup-application` 用于本地集成验证。

## 文档入口

| 范围 | 接入文档 | 设计文档 |
|---|---|---|
| BOM 与版本 | [Dependencies](dependencies/) | [设计](dependencies-architecture/) |
| 通用基础 | [Commons](commons/) | [设计](commons/architecture/) |
| 技术组件 | [Components](components/) | [设计](components/architecture/) |
| 业务模块 | [Modules](modules/) | [设计](modules/architecture/) |
| 测试框架 | [Testify](testing/) | [设计](testing/architecture/) |
| 本地验证应用 | [Application](application/) | [设计](application-architecture/) |

每个模块的 README 是开发接入手册；对应的 ARCHITECTURE 说明职责边界、依赖、装配和扩展点。
模块目录页提供下层模块链接。
"""


def pending(mapping: dict[Path, Path]) -> tuple[list[tuple[Path, str]], list[Path]]:
    updates = []
    for source, target in mapping.items():
        content = render(source, target, mapping)
        if not target.exists() or target.read_text(encoding="utf-8") != content:
            updates.append((target, content))
    index = CONTENT / "_index.md"
    content = render_index()
    if not index.exists() or index.read_text(encoding="utf-8") != content:
        updates.append((index, content))
    return updates, obsolete_placeholders(mapping)


def main() -> int:
    parser = argparse.ArgumentParser(description="Sync Maven module docs to Hugo/LotusDocs")
    mode = parser.add_mutually_exclusive_group()
    mode.add_argument("--apply", action="store_true", help="write generated pages")
    mode.add_argument("--check", action="store_true", help="fail if generated pages are stale")
    mode.add_argument("--build", action="store_true", help="apply and run hugo --gc --minify")
    args = parser.parse_args()
    if not (SITE / "hugo.toml").is_file():
        parser.error(f"Hugo site not found: {SITE}")
    try:
        modules = module_directories()
        mapping = source_map(modules)
        updates, obsolete = pending(mapping)
    except (OSError, ValueError) as error:
        print(f"Documentation sync failed: {error}", file=sys.stderr)
        return 1
    print(f"{len(modules)} modules; {len(mapping)} source documents; {len(updates)} updates, {len(obsolete)} obsolete placeholders")
    for path, _ in updates:
        print(f"  update {path.relative_to(ROOT)}")
    for path in obsolete:
        print(f"  remove {path.relative_to(ROOT)}")
    if args.check:
        return int(bool(updates or obsolete))
    if args.apply or args.build:
        for path, content in updates:
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_text(content, encoding="utf-8")
        for path in obsolete:
            path.unlink()
    if args.build:
        try:
            result = subprocess.run(["hugo", "--gc", "--minify"], cwd=SITE, check=False)
        except FileNotFoundError:
            print("hugo executable not found", file=sys.stderr)
            return 1
        return result.returncode
    return 0


if __name__ == "__main__":
    sys.exit(main())
