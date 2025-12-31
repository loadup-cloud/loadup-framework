# 代码格式化指南

本项目使用 Spotless Maven Plugin 自动格式化代码，确保代码风格一致。

> **📌 配置位置**: Spotless 插件配置在 `loadup-dependencies` parent POM 中，所有子项目自动继承。

## 快速开始

### 1. 安装 Git Hooks（推荐）

```bash
./install-git-hooks.sh
```

安装后，每次 `git push` 前会自动检查代码格式。如果格式不符合规范，push 会被阻止。

### 2. 手动格式化代码

```bash
# 格式化所有代码
./spotless.sh apply

# 仅检查格式
./spotless.sh check

# 格式化特定模块
./spotless.sh apply -pl commons/loadup-commons-api
```

## 格式化规则

### Java 代码

- **风格**: Google Java Style Guide
- **格式化器**: Google Java Format (v1.19.2)
- **缩进**: 2 个空格
- **行长**: 100 字符
- **Import 顺序**: java → javax → jakarta → org → com → others
- **自动移除**: 未使用的 imports
- **行尾**: 删除尾随空格
- **文件结尾**: 添加换行符

### POM 文件

- **缩进**: 4 个空格
- **排序**: 自动排序依赖
- **行尾**: 删除尾随空格

### Markdown 文件

- **格式化器**: Flexmark
- **行尾**: 删除尾随空格

## IDE 配置

### IntelliJ IDEA

1. 安装插件：File → Settings → Plugins → 搜索 "google-java-format"
2. 启用：File → Settings → google-java-format Settings → 勾选 "Enable"
3. Import 顺序：File → Settings → Editor → Code Style → Java → Imports
   - 设置顺序：java, javax, jakarta, org, com, all other imports

### VS Code

安装 "Language Support for Java" 扩展，并配置 settings.json：

```json
{
  "java.format.settings.url": "https://raw.githubusercontent.com/google/styleguide/gh-pages/eclipse-java-google-style.xml",
  "java.format.settings.profile": "GoogleStyle"
}
```

## 常见问题

**Q: 为什么不能用 `mvn spotless:apply`？**
A: Maven 无法识别短前缀。请使用 `./spotless.sh apply` 或完整命令：

```bash
mvn com.diffplug.spotless:spotless-maven-plugin:3.1.0:apply
```

**Q: 如何跳过格式检查？**
A:

- Git push: `git push --no-verify`
- Maven 构建: `mvn verify -Dspotless.check.skip=true`

**Q: 代码片段禁用格式化？**
A:

```java
// @formatter:off
// 这里的代码不会被格式化
// @formatter:on
```

## 最佳实践

1. 提交前运行 `./spotless.sh apply`
2. 团队成员都安装 Git hooks
3. IDE 配置相同的代码风格

## 更多信息

- [Spotless Maven Plugin](https://github.com/diffplug/spotless/tree/main/plugin-maven)
- [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
