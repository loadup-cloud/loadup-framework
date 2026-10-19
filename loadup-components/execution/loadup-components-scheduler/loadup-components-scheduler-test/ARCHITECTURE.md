# PowerJob 真实环境测试设计

JUnit 测试 JVM 承载 Boot 4 / Java 25 Worker；发布的 Server fat jar 以独立 Java 子进程运行；临时 MySQL 由 Testcontainers 管理。管理 API 完成管理员登录、namespace/app 初始化，原生 Client 完成任务管理。所有业务执行发生在真实 Worker，不 mock 调度结果或数据库。

该套件保留 PoC 的原生测试生命周期，以确保先创建数据库和 Server、再启动 Worker；常规业务数据库用例使用 Testify / @EnableTestContainers。生产组件是唯一被测集成代码。HTTP 可访问之后还查询 Worker 在线与过载状态，避免将 Server 启动误当成任务可执行。

单机实例恢复不证明 k8s、多 Server 或 Worker 崩溃后的业务恢复，不证明业务 exactly-once。原生逻辑删除与任务实例身份分别断言。
