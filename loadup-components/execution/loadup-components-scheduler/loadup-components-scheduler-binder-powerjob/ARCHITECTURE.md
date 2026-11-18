# PowerJob binder 架构

`PowerJobSchedulerProvider` 使用管理 Client 同步任务清单。定义以 PowerJob App、管理标记和任务名识别，使用原生 jobId 更新现有定义。配置缺失的已管理任务通过停用保留历史。SDK 调用失败时应用启动失败。若之前部分定义已成功写入，下次启动从当前定义继续同步。

`loadupSchedulerProcessor` 是 Spring 管理的 `BasicProcessor`。它将原生 jobParams 中的任务名交给统一分派回调，执行异常交给 Worker 记录。默认单实例并发、平台任务和实例重试均为零。应用关闭时解除本地分派绑定。

原生 Worker 由官方 Starter 装配。选中其他 binder 时跳过 Worker 和管理 Client 的自动配置。PowerJob Processor 切面保留原生执行上下文和 Micrometer Observation。
