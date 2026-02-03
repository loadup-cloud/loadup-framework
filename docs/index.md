# LoadUp 文档索引

此目录汇总了仓库内面向开发者与 AI 的关键文档，便于后续使用静态站点生成工具（如 MkDocs、Docusaurus、GitHub Pages）快速构建文档网站。

## 文档列表

- [项目概览与上下文](./ai-project-context.md) — 为 AI 助手提供的项目上下文、快速开始、模块说明与开发约定。
- [GitHub Copilot 指令](./copilot-instructions.md) — 用于指导 Copilot 在本项目中生成代码的详细指令与模板（中文）。
- [Cursor AI 规则](./cursor-rules.md) — 指导 Cursor AI 为本项目生成或修改代码时应遵守的规则（中文）。
- [根 README](./README.md) — 项目顶层 README，包含更详尽的组件与使用示例。

## 如何使用

推荐：使用 MkDocs 或 Docusaurus 将 `docs/` 目录内容发布为网站。

简单示例（MkDocs）：

1. 安装 mkdocs：

```bash
pip install mkdocs mkdocs-material
```

2. 在仓库根目录创建 `mkdocs.yml` 并配置 `docs_dir: docs`。
3. 本地预览：

```bash
mkdocs serve
```

## 贡献说明

如果你更新了文档，请确保：

- 文档放在 `docs/` 目录下
- 提交前运行拼写检查与格式化工具（如有）
- 若文档涉及依赖或版本请勿修改 `loadup-dependencies`，变更需在 PR 中说明理由并经过审查

---

文档生成完毕。
