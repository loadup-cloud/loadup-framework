# Testify Core 架构

## 责任与依赖

Core 仅集中测试内部 JSON 解析/绑定，不提供案例执行器、测试生命周期或服务容器。运行依赖是 Jackson 3，由 BOM 管理版本；不依赖
Spring，也不依赖 data-engine/assert-engine/starter。

## 内部结构

JsonUtil 持有一个静态 JsonMapper。日期关闭 WRITE_DATES_AS_TIMESTAMPS，反序列化关闭
FAIL_ON_UNKNOWN_PROPERTIES。各引擎共享这套测试转换规则，但 CaseFiles 为文件加载单独持有 YAML/JSON Mapper。

## 设计取舍

共享工具避免每个 matcher 配置一份 Mapper，也让无 Spring 的引擎可调用。代价是无法通过替换 Boot ObjectMapper 改变它的行为；测试转换不是
HTTP 序列化真实性证明。

equals 比较 JSON 字符串，字段顺序差异可能影响结果。需要 JSON 结构比较时使用 JsonMatcher；需要精确协议验证时读取真实响应。

## 失败与扩展

readTree/toJson 包装解析异常；其他绑定错误由 Jackson 负责。不存在可自动发现的 Mapper 配置
SPI。变更这层时需验证数字、日期、未知字段和各断言引擎的转换语义。现有示例/单测在独立 testify-test 模块，见其 README。本轮未执行测试。
