✅ **文档完善** - 配置元数据完整

### 测试建议

```bash
# 1. 测试默认行为（全部启用）
mvn test

# 2. 测试全局禁用
mvn test -Dloadup.testcontainers.enabled=false

# 3. 测试单个禁用
# 在 application-test.yml 中配置后测试

# 4. 测试 IDE 自动补全
# 在 IDE 中打开 application.yml 输入 "loadup.test"
```

---

**状态：** ✅ 修复完成  
**日期：** 2026-01-08  
**修改者：** LoadUp Framework Team

---

🎉 **所有问题已修复！现在可以正常使用全局开关和 YAML 自动补全功能了！** 🚀

