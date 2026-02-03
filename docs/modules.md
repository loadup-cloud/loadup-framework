# loadup-modules 概览

`loadup-modules` 是业务模块集合，按业务域组织具体实现。

主要示例：
- `loadup-modules-upms`（系统/权限管理）：包含用户、角色、菜单、部门、授权相关逻辑
- 其它业务模块请在 `loadup-modules` 下按功能创建子模块

开发建议：
- 所有业务逻辑放在 modules 下，遵循模块依赖规则（commons -> components -> modules -> application）
- Controller/Service/Mapper/Entity 命名规范参见 Copilot 指令文档（docs/copilot-instructions.md）

查阅 `loadup-modules/loadup-modules-upms/README.md` 获取详细说明。
