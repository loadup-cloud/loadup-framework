#!/usr/bin/env bash
# =============================================================================
# LoadUp Review Agent
# 用途：提交前对 git diff 进行 LoadUp 规范检查，调用 Claude CLI 输出违规报告
# 用法：
#   bash .ai/agents/review-agent.sh              # 检查暂存区（staged），无则检查工作区
#   bash .ai/agents/review-agent.sh --save       # 同上，并保存报告到 .ai/reports/
#   bash .ai/agents/review-agent.sh --head       # 强制检查 HEAD~1 的变更
# 依赖：git, claude CLI（https://docs.anthropic.com/claude/docs/claude-cli）
# =============================================================================

set -euo pipefail

# ─── 配置 ──────────────────────────────────────────────────────────────────
SAVE_REPORT=false
FORCE_HEAD=false
REPORT_DIR="$(git rev-parse --show-toplevel)/.ai/reports"

for arg in "$@"; do
  case "$arg" in
    --save)  SAVE_REPORT=true ;;
    --head)  FORCE_HEAD=true ;;
  esac
done

# ─── 检查 claude CLI ────────────────────────────────────────────────────────
if ! command -v claude &>/dev/null; then
  echo "❌ 未找到 claude CLI。"
  echo "   安装方式：pip install anthropic-cli 或参阅 https://docs.anthropic.com/claude/docs/claude-cli"
  exit 1
fi

# ─── 收集 git diff ──────────────────────────────────────────────────────────
if [[ "$FORCE_HEAD" == "true" ]]; then
  DIFF=$(git diff HEAD~1 HEAD -- '*.java' '*.xml' '*.sql' '*.yml' '*.yaml' '*.csv' 2>/dev/null || true)
  DIFF_SOURCE="HEAD~1..HEAD"
else
  DIFF=$(git diff --cached -- '*.java' '*.xml' '*.sql' '*.yml' '*.yaml' '*.csv' 2>/dev/null || true)
  DIFF_SOURCE="staged"
  if [[ -z "$DIFF" ]]; then
    DIFF=$(git diff -- '*.java' '*.xml' '*.sql' '*.yml' '*.yaml' '*.csv' 2>/dev/null || true)
    DIFF_SOURCE="working tree"
  fi
fi

if [[ -z "$DIFF" ]]; then
  echo "✅ 没有检测到变更，无需审查。"
  exit 0
fi

echo "🔍 正在审查 [${DIFF_SOURCE}] 的变更（$(echo "$DIFF" | wc -l | tr -d ' ') 行 diff）..."

# ─── 构造 Prompt ────────────────────────────────────────────────────────────
PROMPT=$(cat <<'PROMPT_EOF'
你是 LoadUp 项目的代码审查专家。请严格对照以下规范检查提交的 diff，输出违规报告。

## 检查清单（发现违规时输出，否则跳过该条目）

### 架构规范
- [ ] 是否创建了 @RestController / @Controller（应使用 Gateway bean:// 路由）
- [ ] domain 层是否包含 @Table / @Service / Spring 注解（domain 必须零框架注解）
- [ ] DO 是否在 domain 层（DO 必须在 infrastructure.dataobject 包）
- [ ] modules 之间是否存在横向依赖

### 代码规范
- [ ] @Autowired 字段注入（应使用 @RequiredArgsConstructor + final）
- [ ] DO 是否重复声明了 id/createdAt/updatedAt/tenantId/deleted 字段（BaseDO 已有）
- [ ] DO 是否未继承 BaseDO
- [ ] Mapper 是否写了额外 SQL 方法（Mapper 只继承 BaseMapper<XxxDO>）
- [ ] 是否用字符串拼接 SQL（应使用 QueryWrapper）
- [ ] 对象转换是否手写 setter 链（应使用 MapStruct）
- [ ] 是否在日志中打印密码/Token/敏感字段

### pom 规范
- [ ] 子模块 <parent> 是否指向模块自身 pom（应统一指向根 loadup-parent）
- [ ] 子模块是否手写 <version> 引用同项目模块（应由 BOM 统一管理）
- [ ] 新建模块是否未在 loadup-dependencies 中声明

### 数据库规范
- [ ] 表是否缺少 5 个标准字段（id/tenant_id/created_at/updated_at/deleted）
- [ ] 主键是否用了 BIGINT AUTO_INCREMENT（应用 VARCHAR(64) + UUID）
- [ ] 是否用了 BOOLEAN/BOOL 类型（应用 TINYINT）
- [ ] 测试 schema.sql 是否与生产 schema 字段不一致

### 安全规范
- [ ] 是否存在 SQL 注入风险（字符串拼接 SQL）
- [ ] 密码字段是否缺少 @JsonIgnore

### 其他
- [ ] Java 文件头是否包含 License 注释块（应由 CI license-maven-plugin 插入）

## 输出格式

请以如下格式输出：

### 🔴 高优先级违规
[列出必须修复的问题，每条包含：违规文件行号 + 具体说明 + 修正建议]

### 🟡 中优先级违规
[列出建议修复的问题]

### ✅ 通过的检查项
[仅列出已明确检查且通过的关键规范]

### 总结
[1-2 句话总结整体代码质量]

---
以下是本次 diff：

PROMPT_EOF
)

FULL_PROMPT="${PROMPT}
\`\`\`diff
${DIFF}
\`\`\`"

# ─── 调用 Claude CLI ────────────────────────────────────────────────────────
RESULT=$(echo "$FULL_PROMPT" | claude -p - 2>&1)
EXIT_CODE=$?

echo ""
echo "═══════════════════════════════════════════════════════════════"
echo "  LoadUp Code Review Report"
echo "═══════════════════════════════════════════════════════════════"
echo ""
echo "$RESULT"

# ─── 可选：保存报告 ──────────────────────────────────────────────────────────
if [[ "$SAVE_REPORT" == "true" ]]; then
  mkdir -p "$REPORT_DIR"
  TIMESTAMP=$(date +"%Y%m%d-%H%M%S")
  REPORT_FILE="${REPORT_DIR}/review-${TIMESTAMP}.md"
  {
    echo "# LoadUp Code Review — ${TIMESTAMP}"
    echo ""
    echo "**Diff source**: ${DIFF_SOURCE}"
    echo ""
    echo "$RESULT"
  } > "$REPORT_FILE"
  echo ""
  echo "📄 报告已保存至：${REPORT_FILE}"
fi

exit $EXIT_CODE
