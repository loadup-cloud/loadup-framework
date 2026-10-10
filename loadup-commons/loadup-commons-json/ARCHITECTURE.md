# Common JSON Architecture

## 职责

将项目数据投影为安全的 Map/List/标量，再由独立 Jackson 3 JsonMapper 编码。投影直接读取字段，避免 bean getter、对象自定义 toString 递归及资源访问。继承字段统一处理，静态字段和合成字段跳过。

## 边界

日志诊断与 WebMVC 的真实响应序列化分离，避免因 Web ObjectMapper、授权上下文或脱敏开关改变日志披露策略。commons-dto 和 commons-util 均依赖此轻量模块，消除 DTO → util → DTO 的循环。

## 有限输出

最大深度 6，每个容器最多 32 项，字符串最多 256 字符，总节点预算 128。超限和循环使用 JSON 字符串标记；未知外部对象只显示类型，不调用其 getter/toString。密码、密钥、令牌、验证码、WRITE_ONLY、DiagnosticHidden 和 Masked 字段先处理保护规则，再进入投影。

## 扩展约束

不接受业务注册任意序列化器或 HTTP ObjectMapper。确需新增字段类型时应补充受限投影和隐私测试；货币等具有文本契约的值对象保留自己的格式化方法。

`DiagnosticHidden` 只参与诊断投影，供经过授权的明文响应与 KMS 密文等使用；嵌套在响应 envelope 中仍会隐藏，避免依赖对象自身 toString 的保护被反射绕过。
