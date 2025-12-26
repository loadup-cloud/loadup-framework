# 🚀 快速开始
## 什么变了？
✅ **已完成**：
- 合并了 `captcha-core` 和 `captcha-spring-boot-starter` 子模块
- 优化了默认字符集，移除了易混淆字符（0, O, 1, I, L, i, l, o）
- 新增了自定义字符集功能
## 立即使用
### 1. 默认使用（推荐）
什么都不用改，直接使用即可享受优化后的字符集：
```java
import com.github.loadup.components.captcha.SpecCaptcha;
SpecCaptcha captcha = new SpecCaptcha(130, 48, 4);
String code = captcha.text();  // 自动使用优化后的字符集
```
### 2. 自定义字符集（可选）
#### 方式1: 配置文件
```yaml
captcha:
  custom-characters: 23456789ABCDEFGH  # 仅使用这些字符
```
#### 方式2: 编程方式
```java
import com.github.loadup.components.captcha.base.Randoms;
Randoms.setCustomAlpha("23456789ABCDEFGH");
```
## 新字符集说明
### 已移除的混淆字符
- ❌ `0` `O` - 数字零和字母O长得太像
- ❌ `1` `I` `L` `l` `i` - 数字一和各种I、L长得太像
- ❌ `o` - 小写o和数字0长得太像
### 当前默认字符
✅ **数字**: 2 3 4 5 6 7 8 9
✅ **大写**: A B C D E F G H J K M N P Q R S T U V W X Y Z
✅ **小写**: a b c d e f g h j k m n p q r s t u v w x y z
## 更多文档
- 📖 [完整示例](EXAMPLES.md) - 7个实用场景
- 📋 [迁移指南](MIGRATION_NOTES.md) - 详细的迁移说明
- 🔧 [清理指南](CLEANUP_GUIDE.md) - 如何清理旧模块
- 📊 [重构总结](REFACTORING_SUMMARY.md) - 完整的改动说明
## 验证安装
运行验证脚本：
```bash
./verify.sh
```
## 问题排查
### Q: 如何验证字符集已优化？
```java
char[] chars = Randoms.getCurrentAlpha();
System.out.println("当前字符集: " + new String(chars));
// 输出应该不包含 0, O, 1, I, L, i, l, o
```
### Q: 想要只用数字怎么办？
```yaml
captcha:
  custom-characters: 23456789
```
### Q: 原来的代码需要改吗？
不需要！所有 API 保持向后兼容。
## 开始使用
代码已准备就绪，Maven 编译成功！🎉
现在就可以：
1. 运行现有的验证码代码（自动享受优化）
2. 或者添加自定义字符集配置（可选）
3. 查看 EXAMPLES.md 了解更多用法
祝使用愉快！✨
