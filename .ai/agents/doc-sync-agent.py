#!/usr/bin/env python3
"""
LoadUp Doc Sync Agent
用途：分析 git 变更，将代码变化同步到 mkdocs/docs/ 文档，并可选发布文档站。

用法：
    python3 .ai/agents/doc-sync-agent.py              # 分析变更，生成同步报告
    python3 .ai/agents/doc-sync-agent.py --apply      # 分析并自动更新文档
    python3 .ai/agents/doc-sync-agent.py --build      # 分析 + 更新 + mkdocs build 验证
    python3 .ai/agents/doc-sync-agent.py --publish    # 分析 + 更新 + mkdocs gh-deploy 发布
    python3 .ai/agents/doc-sync-agent.py --head       # 分析 HEAD~1 变更（默认分析 staged/working tree）

依赖：Python 3.8+（仅标准库），mkdocs（pip install mkdocs mkdocs-material）
"""

import argparse
import re
import subprocess
import sys
import textwrap
from datetime import datetime
from pathlib import Path

# ─── 常量 ───────────────────────────────────────────────────────────────────

ROOT = Path(
    subprocess.check_output(["git", "rev-parse", "--show-toplevel"], text=True).strip()
)
DOCS_DIR = ROOT / "mkdocs" / "docs"
MKDOCS_YML = ROOT / "mkdocs" / "mkdocs.yml"

# 模块名到文档文件的映射
MODULE_DOC_MAP = {
    "loadup-modules-config": DOCS_DIR / "modules" / "config.md",
    "loadup-modules-upms": DOCS_DIR / "modules" / "upms.md",
    "loadup-modules-log": DOCS_DIR / "modules" / "log.md",
    "loadup-gateway": DOCS_DIR / "gateway.md",
    "loadup-testify": DOCS_DIR / "testify.md",
    "loadup-components-database": DOCS_DIR / "components" / "database.md",
    "loadup-components-cache": DOCS_DIR / "components" / "cache.md",
}

# ─── Git 工具 ────────────────────────────────────────────────────────────────

def get_diff(force_head: bool = False) -> tuple[str, str]:
    """返回 (diff内容, diff来源描述)"""
    if force_head:
        diff = _run_git(["diff", "HEAD~1", "HEAD"])
        return diff, "HEAD~1..HEAD"

    staged = _run_git(["diff", "--cached"])
    if staged.strip():
        return staged, "staged"

    working = _run_git(["diff"])
    if working.strip():
        return working, "working tree"

    return "", "none"


def _run_git(args: list[str]) -> str:
    try:
        return subprocess.check_output(
            ["git"] + args, text=True, stderr=subprocess.DEVNULL, cwd=ROOT
        )
    except subprocess.CalledProcessError:
        return ""


def get_changed_files(diff: str) -> list[str]:
    """从 diff 中提取所有变更文件路径"""
    return re.findall(r"^(?:\+\+\+|---) (?:b/|a/)(.+)$", diff, re.MULTILINE)


# ─── 变更分析 ────────────────────────────────────────────────────────────────

class ChangeAnalysis:
    """分析 diff，提取各类变更信息"""

    def __init__(self, diff: str, files: list[str]):
        self.diff = diff
        self.files = files
        self.new_service_methods: list[dict] = []
        self.new_do_classes: list[dict] = []
        self.schema_changes: list[str] = []
        self.module_changes: dict[str, list[str]] = {}
        self._analyze()

    def _analyze(self):
        # 检测 schema.sql 变更
        self.schema_changes = [f for f in self.files if f.endswith("schema.sql")]

        # 检测新增 @Service public 方法
        for block in re.split(r"^diff --git", self.diff, flags=re.MULTILINE):
            file_match = re.search(r"b/(.+\.java)", block)
            if not file_match:
                continue
            filepath = file_match.group(1)

            # 只检查 app/service 层
            if "app/service" not in filepath and "app\\service" not in filepath:
                continue
            module = self._extract_module(filepath)

            # 找新增的 public 方法签名
            for line in block.splitlines():
                if not line.startswith("+"):
                    continue
                m = re.search(
                    r"^\+\s+public\s+(?!class|interface|enum|@)(\w[\w<>,\s]*)\s+(\w+)\s*\(", line
                )
                if m:
                    self.new_service_methods.append(
                        {"method": m.group(2), "return": m.group(1).strip(), "file": filepath, "module": module}
                    )

        # 检测新增 @Table DO 类
        for block in re.split(r"^diff --git", self.diff, flags=re.MULTILINE):
            file_match = re.search(r"b/(.+DO\.java)", block)
            if not file_match:
                continue
            filepath = file_match.group(1)
            if "infrastructure" not in filepath:
                continue

            added_lines = "\n".join(l[1:] for l in block.splitlines() if l.startswith("+"))
            table_match = re.search(r'@Table\s*\(\s*["\'](\w+)["\']', added_lines)
            class_match = re.search(r"class\s+(\w+DO)\s+extends\s+BaseDO", added_lines)
            if table_match and class_match:
                self.new_do_classes.append(
                    {
                        "class": class_match.group(1),
                        "table": table_match.group(1),
                        "file": filepath,
                        "module": self._extract_module(filepath),
                    }
                )

        # 模块变更归类
        for f in self.files:
            module = self._extract_module(f)
            if module:
                self.module_changes.setdefault(module, []).append(f)

    def _extract_module(self, filepath: str) -> str:
        for key in MODULE_DOC_MAP:
            if key in filepath:
                return key
        return ""


# ─── 报告生成 ────────────────────────────────────────────────────────────────

def build_report(analysis: ChangeAnalysis, diff_source: str) -> str:
    lines = [
        f"# LoadUp Doc Sync Report — {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}",
        f"**Diff source**: {diff_source}",
        "",
    ]

    # Schema 变更检查
    if analysis.schema_changes:
        lines += [
            "## ⚠️ Schema 变更检测",
            "",
        ]
        for schema_file in analysis.schema_changes:
            module = next(
                (k for k in MODULE_DOC_MAP if k in schema_file), None
            )
            if module:
                test_schema = ROOT / "modules" / module / (module + "-test") / "src/test/resources/schema.sql"
                status = "✅ 存在" if test_schema.exists() else "❌ 不存在"
                lines.append(
                    f"- `{schema_file}` 已变更 → 测试 schema `{test_schema.relative_to(ROOT)}` {status}，请同步更新"
                )
            else:
                lines.append(f"- `{schema_file}` 已变更，请检查对应测试 schema 是否同步")
        lines.append("")

    # 新增 Service 方法
    if analysis.new_service_methods:
        lines += [
            "## 🆕 新增 Service 公共方法",
            "",
            "以下方法可能需要在 Gateway 路由配置中添加对应路由：",
            "",
        ]
        for m in analysis.new_service_methods:
            bean_name = _to_bean_name(Path(m["file"]).stem)
            csv_example = f"/api/v1/TODO,POST,bean://{bean_name}:{m['method']},default,,,true,"
            lines += [
                f"### `{m['method']}` in `{Path(m['file']).name}`",
                "",
                f"- 模块：`{m['module']}`",
                f"- Target：`bean://{bean_name}:{m['method']}`",
                f"- CSV 行示例（FILE 存储）：",
                f"  ```",
                f"  {csv_example}",
                f"  ```",
                f"- SQL 示例（DATABASE 存储）：",
                f"  ```sql",
                f"  INSERT INTO gateway_routes (route_id, route_name, path, method, target, security_code, enabled)",
                f"  VALUES ('{bean_name}-{m['method']}', '{m['method']}', '/api/v1/TODO',",
                f"          'POST', 'bean://{bean_name}:{m['method']}', 'default', 1);",
                f"  ```",
                "",
            ]

    # 新增 DO 类（建议 Flyway migration）
    if analysis.new_do_classes:
        lines += [
            "## 🗄️ 新增 DO 类（请创建 Flyway migration）",
            "",
        ]
        for do_cls in analysis.new_do_classes:
            lines += [
                f"- `{do_cls['class']}` — 表 `{do_cls['table']}`",
                f"  建议 migration 文件：`V{_next_migration_version(do_cls['module'])}__{_to_snake(do_cls['table'])}_create.sql`",
                "",
            ]

    # 文档同步建议
    if analysis.module_changes:
        lines += [
            "## 📚 文档同步建议",
            "",
        ]
        for module, changed_files in analysis.module_changes.items():
            doc_path = MODULE_DOC_MAP.get(module)
            if doc_path:
                status = "✅" if doc_path.exists() else "❌ 文档不存在"
                lines += [
                    f"- 模块 `{module}` 有 {len(changed_files)} 个文件变更 → 请更新 `{doc_path.relative_to(ROOT)}` {status}",
                ]
        lines.append("")

    if not any([analysis.schema_changes, analysis.new_service_methods,
                analysis.new_do_classes, analysis.module_changes]):
        lines.append("✅ 未检测到需要同步的文档变更。")

    return "\n".join(lines)


# ─── 文档更新 ────────────────────────────────────────────────────────────────

def apply_doc_updates(analysis: ChangeAnalysis, dry_run: bool = True) -> list[str]:
    """执行文档更新，返回已更新文件列表"""
    updated = []

    for module, changed_files in analysis.module_changes.items():
        doc_path = MODULE_DOC_MAP.get(module)
        if not doc_path or not doc_path.exists():
            continue

        content = doc_path.read_text(encoding="utf-8")
        timestamp = datetime.now().strftime("%Y-%m-%d")

        # 在文档顶部或底部追加"最后同步"时间戳注释（幂等：只更新已有的）
        sync_marker = "<!-- last-sync:"
        new_marker = f"<!-- last-sync: {timestamp} -->"
        if sync_marker in content:
            content = re.sub(r"<!-- last-sync: .+? -->", new_marker, content)
        else:
            content = new_marker + "\n" + content

        if not dry_run:
            doc_path.write_text(content, encoding="utf-8")
            updated.append(str(doc_path.relative_to(ROOT)))
        else:
            updated.append(f"[dry-run] {doc_path.relative_to(ROOT)}")

    return updated


# ─── mkdocs 操作 ─────────────────────────────────────────────────────────────

def run_mkdocs_build() -> bool:
    """运行 mkdocs build --strict，返回是否成功"""
    print("\n🔨 运行 mkdocs build --strict ...")
    result = subprocess.run(
        ["mkdocs", "build", "--strict"],
        cwd=ROOT / "mkdocs",
        capture_output=False,
    )
    return result.returncode == 0


def run_mkdocs_publish() -> bool:
    """运行 mkdocs gh-deploy 发布文档站"""
    print("\n🚀 运行 mkdocs gh-deploy ...")
    result = subprocess.run(
        ["mkdocs", "gh-deploy", "--force"],
        cwd=ROOT / "mkdocs",
        capture_output=False,
    )
    return result.returncode == 0


# ─── 工具函数 ────────────────────────────────────────────────────────────────

def _to_bean_name(class_name: str) -> str:
    """ConfigItemService → configItemService"""
    return class_name[0].lower() + class_name[1:] if class_name else class_name


def _to_snake(name: str) -> str:
    """ConfigItem → config_item"""
    return re.sub(r"(?<!^)(?=[A-Z])", "_", name).lower()


def _next_migration_version(module: str) -> str:
    """猜测下一个 Flyway 版本号"""
    migration_dir = ROOT / "modules" / module / (module + "-infrastructure") / "src/main/resources/db/migration"
    if not migration_dir.exists():
        return "1"
    existing = sorted(migration_dir.glob("V*.sql"))
    if not existing:
        return "1"
    last = existing[-1].name
    m = re.match(r"V(\d+)__", last)
    return str(int(m.group(1)) + 1) if m else "N"


# ─── 主入口 ──────────────────────────────────────────────────────────────────

def main():
    parser = argparse.ArgumentParser(
        description="LoadUp Doc Sync Agent — 同步代码变更到 mkdocs 文档并可选发布"
    )
    parser.add_argument("--apply", action="store_true", help="自动更新文档文件（写入磁盘）")
    parser.add_argument("--build", action="store_true", help="更新后运行 mkdocs build --strict 验证")
    parser.add_argument("--publish", action="store_true", help="更新后运行 mkdocs gh-deploy 发布")
    parser.add_argument("--head", action="store_true", help="分析 HEAD~1 变更（默认分析 staged/working tree）")
    parser.add_argument("--save", action="store_true", help="将报告保存到 .ai/reports/")
    args = parser.parse_args()

    # 收集 diff
    diff, diff_source = get_diff(force_head=args.head)
    if not diff.strip():
        print("✅ 没有检测到变更，无需同步。")
        return

    changed_files = get_changed_files(diff)
    print(f"🔍 分析 [{diff_source}] 的变更（{len(changed_files)} 个文件）...")

    # 分析变更
    analysis = ChangeAnalysis(diff, changed_files)

    # 生成报告
    report = build_report(analysis, diff_source)
    print("\n" + "═" * 65)
    print(report)
    print("═" * 65)

    # 保存报告
    if args.save:
        report_dir = ROOT / ".ai" / "reports"
        report_dir.mkdir(parents=True, exist_ok=True)
        ts = datetime.now().strftime("%Y%m%d-%H%M%S")
        report_file = report_dir / f"doc-sync-{ts}.md"
        report_file.write_text(report, encoding="utf-8")
        print(f"\n📄 报告已保存至：{report_file.relative_to(ROOT)}")

    # 可选：更新文档
    if args.apply or args.build or args.publish:
        updated = apply_doc_updates(analysis, dry_run=False)
        if updated:
            print("\n📝 已更新文档：")
            for f in updated:
                print(f"   - {f}")
        else:
            print("\n📝 无需更新文档文件。")

    # 可选：mkdocs build 验证
    if args.build or args.publish:
        if not run_mkdocs_build():
            print("\n❌ mkdocs build 失败，请修复后重试。")
            sys.exit(1)
        print("✅ mkdocs build 成功。")

    # 可选：发布
    if args.publish:
        if not run_mkdocs_publish():
            print("\n❌ mkdocs gh-deploy 失败。")
            sys.exit(1)
        print("✅ 文档站已发布。")


if __name__ == "__main__":
    main()
