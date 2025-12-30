# License Header 更新脚本使用指南

## 📋 概述

`update-license-headers.sh` 是一个用于批量更新 LoadUp Framework 项目中所有 Maven 模块的 license 文件头的脚本。

## 🚀 快速开始

### 基本使用

```bash
# 更新所有模块的license头
./update-license-headers.sh

# 检查所有模块的license头（不修改文件）
./update-license-headers.sh --check

# 详细输出模式
./update-license-headers.sh -v
```

## 📖 使用方法

### 命令格式

```bash
./update-license-headers.sh [选项]
```

### 可用选项

| 选项            | 长选项                 | 说明                   |
|---------------|---------------------|----------------------|
| `-h`          | `--help`            | 显示帮助信息               |
| `-m <module>` | `--module <module>` | 仅更新指定模块              |
| `-d`          | `--dry-run`         | 干运行模式，不实际修改文件        |
| `-v`          | `--verbose`         | 详细输出模式               |
| `-c`          | `--check`           | 检查模式，仅检查license是否缺失  |
| `-f`          | `--format`          | 格式化模式，更新所有文件的license |
|               | `--skip-tests`      | 跳过test目录             |

## 💡 使用示例

### 1. 更新所有模块

```bash
./update-license-headers.sh
```

**输出示例:**

```
==========================================
LoadUp Framework - License Header Update
==========================================

[INFO] 运行模式: 标准更新模式
[INFO] 找到 13 个模块

[INFO] 处理模块: .
[SUCCESS] ✓ loadup-framework-parent 处理完成
[INFO] 处理模块: bom
[SUCCESS] ✓ bom 处理完成
...

==========================================
License更新统计
==========================================
总模块数:     13
成功处理:     13
处理失败:     0
跳过模块:     0
==========================================
所有模块处理成功！
```

### 2. 仅更新特定模块

```bash
# 更新commons目录下的所有模块
./update-license-headers.sh -m commons

# 更新特定的scheduler模块
./update-license-headers.sh -m components/loadup-components-scheduler
```

### 3. 检查模式（不修改文件）

```bash
# 检查所有模块的license头
./update-license-headers.sh --check

# 详细检查特定模块
./update-license-headers.sh --check -m bom -v
```

### 4. 干运行模式（预览操作）

```bash
# 查看将要执行的操作，但不实际修改
./update-license-headers.sh -d

# 结合详细模式使用
./update-license-headers.sh -d -v
```

### 5. 跳过测试文件

```bash
# 仅更新源代码文件，跳过测试文件
./update-license-headers.sh --skip-tests
```

### 6. 组合使用

```bash
# 详细模式 + 格式化 + 跳过测试
./update-license-headers.sh -v -f --skip-tests

# 干运行 + 特定模块 + 详细输出
./update-license-headers.sh -d -m components -v
```

## 📊 输出说明

### 日志级别

- **[INFO]** - 信息消息（蓝色）
- **[SUCCESS]** - 成功消息（绿色）
- **[WARNING]** - 警告消息（黄色）
- **[ERROR]** - 错误消息（红色）
- **[VERBOSE]** - 详细消息（青色，仅在 `-v` 模式下显示）

### 统计报告

脚本执行完成后会显示统计报告：

```
==========================================
License更新统计
==========================================
总模块数:     13
成功处理:     12
处理失败:     1
跳过模块:     0
==========================================
```

## 🔧 前提条件

### 1. Maven 环境

确保已安装 Maven 并在 PATH 中：

```bash
mvn -version
```

### 2. License Plugin 配置

在父 `pom.xml` 中配置 `org.codehaus.mojo:license-maven-plugin`：

```xml

<build>
    <plugins>
        <plugin>
            <groupId>org.codehaus.mojo</groupId>
            <artifactId>license-maven-plugin</artifactId>
            <version>2.4.0</version>
            <configuration>
                <licenseName>mit</licenseName>
                <licenseResolver>${project.baseUri}/src/license</licenseResolver>
                <organizationName>LoadUp Framework</organizationName>
                <inceptionYear>2025</inceptionYear>
                <projectName>LoadUp Framework</projectName>
                <includes>
                    <include>**/*.java</include>
                </includes>
                <excludes>
                    <exclude>**/target/**</exclude>
                    <exclude>**/test/**</exclude>
                </excludes>
                <roots>
                    <root>src/main/java</root>
                    <root>src/test/java</root>
                </roots>
            </configuration>
            <executions>
                <execution>
                    <id>update-file-headers</id>
                    <goals>
                        <goal>update-file-header</goal>
                    </goals>
                    <phase>process-sources</phase>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

**可用的 Maven 目标：**

- `mvn license:update-file-header` - 更新源文件的license头
- `mvn license:check-file-header` - 检查license头是否存在
- `mvn license:remove-file-header` - 移除所有license头

### 3. License 模板文件

`org.codehaus.mojo:license-maven-plugin` 使用内置的 license 模板。

如果需要自定义，可以在 `src/license` 目录下创建自定义模板文件。

## 🏗️ 项目结构

脚本会自动识别以下模块结构：

```
loadup-framework/
├── update-license-headers.sh  ← 脚本位置
├── pom.xml                     ← 父pom.xml
├── bom/
│   └── pom.xml
├── commons/
│   ├── loadup-commons-api/
│   ├── loadup-commons-dto/
│   ├── loadup-commons-lang/
│   └── loadup-commons-util/
└── components/
    ├── loadup-components-cache/
    ├── loadup-components-scheduler/
    └── ...
```

## 🐛 故障排查

### 问题1: Maven 未找到

**错误:**

```
[ERROR] Maven未安装或不在PATH中
```

**解决:**

```bash
# macOS
brew install maven

# 或检查PATH
export PATH="/usr/local/bin:$PATH"
```

### 问题2: 未在项目根目录

**错误:**

```
[ERROR] 未找到pom.xml文件，请确保在项目根目录下运行脚本
```

**解决:**

```bash
cd /path/to/loadup-framework
./update-license-headers.sh
```

### 问题3: 模块不存在

**错误:**

```
[ERROR] 模块不存在: commons/invalid-module
```

**解决:**
检查模块路径是否正确，使用 `-h` 查看帮助。

### 问题4: 某些模块失败

如果某些模块处理失败：

1. 使用 `-v` 查看详细错误信息
2. 检查该模块的 `pom.xml` 配置
3. 手动进入模块目录执行 `mvn license:update-file-header`

## 📝 最佳实践

### 1. 在提交前检查

```bash
# 提交前检查license头
./update-license-headers.sh --check

# 如果有缺失，自动更新
./update-license-headers.sh
```

### 2. CI/CD 集成

在 CI 流程中添加检查：

```yaml
# .github/workflows/ci.yml
- name: Check License Headers
  run: ./update-license-headers.sh --check
```

### 3. 定期更新

建议定期运行以保持一致性：

```bash
# 每周或每次发布前
./update-license-headers.sh -v
```

### 4. 新模块添加后

添加新模块后立即更新：

```bash
# 仅更新新模块
./update-license-headers.sh -m components/new-module
```

## 🔄 工作流程

```
┌─────────────────────────────────────────────┐
│  1. 解析命令行参数                            │
└─────────────────┬───────────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────────┐
│  2. 检查环境（Maven、项目根目录）              │
└─────────────────┬───────────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────────┐
│  3. 读取pom.xml，获取所有模块列表              │
└─────────────────┬───────────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────────┐
│  4. 遍历每个模块                              │
│     ├─ 检查pom.xml是否存在                    │
│     ├─ 切换到模块目录                         │
│     ├─ 执行mvn license:update-file-header/check          │
│     └─ 记录结果                               │
└─────────────────┬───────────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────────┐
│  5. 生成统计报告                              │
└─────────────────────────────────────────────┘
```

## 📚 相关资源

- [License Maven Plugin 文档](https://mycila.carbou.me/license-maven-plugin/)
- [Maven 官方文档](https://maven.apache.org/)
- [LoadUp Framework 项目](https://github.com/loadup-cloud/loadup-framework)

## 🤝 贡献

如需改进此脚本，请：

1. Fork 项目
2. 创建特性分支
3. 提交 Pull Request

## 📄 许可证

GNU General Public License v3.0 (GPL-3.0) - 详见 [LICENSE](LICENSE) 文件

---

**最后更新:** 2025-12-30  
**版本:** 1.0.0

