---
id: components-extension
title: LoadUp Extension Framework
---

> 来源: `loadup-components/loadup-components-extension/README.md`

# LoadUp Extension Framework

这是 LoadUp 的扩展点（Extension）框架，用于在运行时根据 BizScenario（bizCode/useCase/scenario）动态路由和执行扩展实现。框架基于 Spring Boot 3，支持注解与 SPI 两种注册方式。

主要内容摘要：

- 多维度场景匹配（bizCode / useCase / scenario）
- 扩展点注解 `@Extension` 和标记接口 `IExtensionPoint`
- 执行器 `ExtensionExecutor` 提供 execute/run/executeAll/collect 等常用方法
- 支持优先级、降级策略与动态选择
- 支持两种注册方式：注解式和 SPI Provider

快速开始与示例请参阅原始 README：

- 原始路径: `loadup-components/loadup-components-extension/README.md`

(已迁移到 docs 以方便 Docusaurus 展示；详尽示例代码、API 说明和 FAQ 请在原始 README 中查阅或在 docs 中搜索扩展相关页面。)
