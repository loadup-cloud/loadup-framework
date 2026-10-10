# Common JSON Architecture

## 职责

`JsonUtil` 负责真实报文的 Jackson 3 转换和默认日期配置；`MultiDateDeserializer` 负责 Date 的多格式输入。格式常量由 JsonUtil 统一定义，commons-dto 的 CommonConstants 引用这些常量。JSON 不依赖 commons-util 或 commons-dto，空值检查使用 JDK，避免循环依赖。

`ToStringUtils` 将项目数据投影为安全的 Map/List/标量，再由独立 Jackson 3 JsonMapper 编码。投影直接读取字段，避免 bean getter、对象自定义 toString 递归及资源访问。继承字段统一处理，静态字段和合成字段跳过。

## 边界

日志诊断与 WebMVC 的真实响应序列化分离，避免因 Web ObjectMapper、授权上下文或脱敏开关改变日志披露策略。commons-dto 和 commons-util 均单向依赖本模块，JSON 仅向下依赖 commons-log、commons-masking 与 Jackson。

WebMVC 对应用 mapper 使用 JsonUtil.customize，再通过 setObjectMapper 同步给 JsonUtil；诊断 mapper 不参与同步。此次归并保留转换 API 和容错行为，仅调整包、模块与依赖所有权；未额外注册 Guava 模块或改变序列化行为。

## 有限输出

最大深度 6，每个容器最多 32 项，字符串最多 256 字符，总节点预算 128。超限和循环使用 JSON 字符串标记；未知外部对象只显示类型，不调用其 getter/toString。密码、密钥、令牌、验证码、WRITE_ONLY、DiagnosticHidden 和 Masked 字段先处理保护规则，再进入投影。

## 扩展约束

诊断投影不接受业务注册任意序列化器或 HTTP ObjectMapper；报文扩展通过应用 Jackson 配置完成。确需新增字段类型时应补充受限投影和隐私测试；货币等具有文本契约的值对象保留自己的格式化方法。

`DiagnosticHidden` 只参与诊断投影，供经过授权的明文响应与 KMS 密文等使用；嵌套在响应 envelope 中仍会隐藏，避免依赖对象自身 toString 的保护被反射绕过。

## 多格式日期输入

TemporalFormats 在模块内集中维护不可变 DateTimeFormatter，使用 `uuuu` 与 STRICT 完整解析，避免 SimpleDateFormat 只解析前缀、宽松纠正非法日期及共享可变 formatter 的问题。MultiDateDeserializer、MultiLocalDateDeserializer、MultiLocalDateTimeDeserializer 各自维护明确的目标类型语义，统一由 JsonUtil.customize 注册。

Date 无时区输入使用 DeserializationContext 的 mapper 时区；显式时区输入先解析成 Instant，再转换为毫秒精度 Date。JSON 整数固定解释为 Unix 毫秒。Local 类型只接受相应的本地文本，不丢弃时区、不截取日期、不自动补时间。Local 反序列化器继承 Jackson 的 Java Time reader，字段明确指定 JsonFormat 格式时使用 Jackson 原生 contextual reader。

输出规则和其他 Java Time 类型保持不变。MultiTemporalDeserializerTest 验证格式矩阵、闰日和严格解析、时区转换、时间戳边界、null、非法 token、字段格式及固定输出。
