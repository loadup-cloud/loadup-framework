# Async Result Architecture

## 职责与依赖

Mode A：API 提供 AsyncResultTemplate、AsyncDispatch、AsyncReplyKey 与单后端 SPI AsyncResultProvider；Local 使用
Caffeine，Redis 使用 Redisson。API 不依赖任何 binder，不引入 MQ。test 模块通过真实 Kafka/MySQL/Redis 验证组合路径，传输依赖仅为
test scope。所有子模块直接继承根 parent，版本通过 BOM 管理。

## 状态与并发

本地 Pending 包含类型化 CompletableFuture、调用上下文、开始时间、超时任务和读取互斥标记。独立 Semaphore
分别控制等待数量和注册/发送回调执行数量，回调许可在其退出时才归还；注册、执行入口与关闭通过短临界区协调。先放入本地表，再异步注册后端，成功后才调用发送器。发送回调和结果读取使用虚拟线程，定时线程仅调度，不直接执行
Redis I/O 或用户 continuation。超时队列移除取消任务，减少高吞吐下已完成请求的滞留。

共享记录状态为 pending / success / failure。等待状态为 waiting / completed / timed-out / cancelled / closed；Future
的原子完成决定本次等待的终态。等待退出移除本地 Pending、取消超时任务并归还许可，不删除后端记录。正在执行的发送可能越过等待截止时间，组件不承诺取消远端副作用。

## Local 后端

Caffeine 按注册时 TTL
过期，每条记录原地保存终态，完成操作在记录的短临界区内判断首次、重复或冲突。容量许可只在过期/清理时释放，保留期内快速完成的记录也计入容量。通知仅作为提示；没有监听者也保存结果。单进程重启即丢失该后端记录。

## Redis 后端

每个结果是一个 Redis Hash，包含 owner 与可选 result。键按 namespace、tenantId、operation、businessId 分段 Base64URL
编码，correlationId 为随机 UUID；不使用原始标识拼接 Redis Cluster hash tag。一次脚本只访问一个结果键。

注册脚本使用 EXISTS + HSET + PEXPIRE，拒绝复用已有键。完成脚本先判断键存在与结果是否已写，首次完成执行 HSET，再 PUBLISH 到
owner 的实例通道。保留期从注册开始，不因重复或完成延长。Redis ACL 需允许使用的 Hash、过期、Lua、发布/订阅命令；脚本错误并不提供对外部副作用的回滚保证。

owner 是每次 Template 启动生成的随机 UUID，不复用 Pod 名称。订阅在接受请求前建立，消息只携带关联键。Pub/Sub
不承担结果持久性；定期读取已保存结果作为断线或通知丢失的兜底，同一 Pending 最多一个读取任务。查询短暂失败继续等待并计数，到等待上限返回超时。

Redis 故障转移或丢失数据可能导致等待失败；Redis 的实际持久化/复制策略由部署方配置。数据库业务状态、消费幂等和结果消息重发负责恢复业务事实。首版不声称跨故障恰好一次。

## 结果协议与安全

请求键包含可信租户、操作协议版本、业务号及单次 correlationId，业务号不承担等待唯一性。JSON 成功载荷不含类名；失败只传枚举稳定
code，业务显式识别远端码。诊断 toString 隐藏 payload。Redis 固定协议使用受控 JsonMapper；业务 DTO 的序列化使用应用
ObjectMapper。编码相同才判断为相同结果，业务端应使用固定 DTO/schema，避免同义 JSON 的字段顺序不同产生冲突。

## 事务与生命周期

入口检测实际活动事务并拒绝；此检查不能替代业务对挂起事务、数据库锁与回调事务的正确设计。回调可调用独立事务
Service，提交后才开始可见的 Outbox 投递。成功回传必须在业务提交后，可靠回传可使用消费者自己的 Outbox。

关闭先禁止新请求，移除订阅并使等待失败，再停止自有调度器与虚拟线程执行器；应用持有的
RedissonClient、MeterRegistry、ObjectMapper 不关闭。超时/取消不会删除共享结果，亦不撤销远端任务。服务恢复通过业务号查询数据库，而不是恢复已断开的原
HTTP 连接。

## 验收

通过公开 Template/SPI 验证提前回调、乱序/并发、容量恢复、超时后晚到、取消/中断、重复/冲突、类型及大小拒绝、事务入口保护、后端故障和上下文恢复。真实环境测试覆盖
Redis 跨实例、过期、Kafka 双向消息和 MySQL Outbox 提交后完成。部署前还需按目标环境执行负载与 K8s 故障演练。
