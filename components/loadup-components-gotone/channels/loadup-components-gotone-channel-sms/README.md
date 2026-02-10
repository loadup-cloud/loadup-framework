# Gotone SMS 模块

## 概述

`loadup-components-gotone-binder-sms` 是 Gotone 通知组件的短信发送模块，支持多个主流短信服务提供商。

## 支持的提供商

- ✅ **阿里云短信** (Aliyun SMS)
- ✅ **腾讯云短信** (Tencent Cloud SMS)
- ✅ **华为云短信** (Huawei Cloud SMS)
- ✅ **云片短信** (Yunpian SMS)

## 快速开始

### 1. 添加依赖

```xml

<dependency>
    <groupId>io.github.loadup-cloud</groupId>
    <artifactId>loadup-components-gotone-binder-sms</artifactId>
    <version>${loadup.version}</version>
</dependency>
```

### 2. 配置提供商

```yaml
loadup:
  gotone:
    sms:
      # 阿里云短信
      aliyun:
        access-key-id: ${ALIYUN_ACCESS_KEY_ID}
        access-key-secret: ${ALIYUN_ACCESS_KEY_SECRET}
        sign-name: 你的签名
        region-id: cn-hangzhou

      # 腾讯云短信
      tencent:
        secret-id: ${TENCENT_SECRET_ID}
        secret-key: ${TENCENT_SECRET_KEY}
        sdk-app-id: 1400XXXXXX
        sign-name: 你的签名

      # 华为云短信
      huawei:
        app-key: ${HUAWEI_APP_KEY}
        app-secret: ${HUAWEI_APP_SECRET}
        sender: 106XXXXXXXX
        signature: 你的签名

      # 云片短信
      yunpian:
        api-key: ${YUNPIAN_API_KEY}
        sign-name: 【你的签名】
```

### 3. 使用示例

```java

@Autowired
private GotoneNotificationService notificationService;

public void sendSms() {
    NotificationRequest request = NotificationRequest.builder()
            .businessCode("VERIFICATION_CODE")
            .address("13800138000")
            .params(Map.of(
                    "code", "123456",
                    "minutes", "5"
            ))
            .build();

    NotificationResult result = notificationService.send(request);
}
```

## 提供商详细配置

### 阿里云短信

#### 1. 获取密钥

1. 登录阿里云控制台
2. 访问 AccessKey 管理页面
3. 创建 AccessKey（保存好 AccessKeyId 和 AccessKeySecret）

#### 2. 创建签名和模板

1. 进入短信服务控制台
2. 创建签名（如：【公司名】）
3. 创建模板（如：验证码${code}，${minutes}分钟内有效）

#### 3. 配置

```yaml
loadup:
  gotone:
    sms:
      aliyun:
        access-key-id: LTAI5t...
        access-key-secret: xxx...
        sign-name: 公司名
        region-id: cn-hangzhou  # 可选，默认 cn-hangzhou
        endpoint: dysmsapi.aliyuncs.com  # 可选
```

#### 4. 模板配置

```sql
INSERT INTO gotone_notification_template
    (template_code, template_name, channel, content, template_type)
VALUES ('VERIFICATION_CODE_SMS',
        '验证码短信',
        'SMS',
        'SMS_123456789', # 阿里云模板 CODE
        'ALIYUN');
```

### 腾讯云短信

#### 1. 获取密钥

1. 登录腾讯云控制台
2. 访问访问管理 > API 密钥管理
3. 创建密钥（SecretId 和 SecretKey）

#### 2. 配置

```yaml
loadup:
  gotone:
    sms:
      tencent:
        secret-id: AKIDxxxxx
        secret-key: xxxxx
        sdk-app-id: 1400123456
        sign-name: 公司名
        region: ap-guangzhou  # 可选
```

### 华为云短信

#### 1. 配置

```yaml
loadup:
  gotone:
    sms:
      huawei:
        app-key: xxxxx
        app-secret: xxxxx
        sender: 106XXXXXXXX
        signature: 【公司名】
        url: https://smsapi.cn-north-4.myhuaweicloud.com:443  # 可选
```

### 云片短信

#### 1. 配置

```yaml
loadup:
  gotone:
    sms:
      yunpian:
        api-key: xxxxx
        sign-name: 【公司名】
```

## 多提供商降级

配置多个提供商实现自动降级：

```sql
INSERT INTO gotone_channel_mapping
    (business_code, channel, template_code, provider_list, priority)
VALUES ('VERIFICATION_CODE',
        'SMS',
        'VERIFICATION_CODE_SMS',
        '["aliyun", "tencent", "huawei"]', -- 按优先级排列
        10);
```

发送逻辑：

1. 优先使用阿里云
2. 阿里云失败则使用腾讯云
3. 腾讯云失败则使用华为云

## 常见问题

### 1. 签名不匹配

**症状**: 发送失败，提示签名不匹配

**解决方案**:

- 检查签名是否已审核通过
- 确认配置的签名名称与控制台一致
- 注意签名格式（有些需要【】，有些不需要）

### 2. 模板参数错误

**症状**: InvalidTemplateParam

**解决方案**:

- 检查模板参数名称是否匹配
- 确认参数值格式是否正确
- 查看模板文档的参数要求

### 3. 发送频率限制

**症状**: 发送失败，提示超过频率限制

**解决方案**:

```yaml
loadup:
  gotone:
    sms:
      rate-limit:
        enabled: true
        per-phone: 5      # 每个号码每分钟最多 5 条
        per-ip: 100       # 每个 IP 每分钟最多 100 条
```

### 4. 余额不足

**症状**: InsufficientBalance

**解决方案**:

- 登录服务商控制台充值
- 配置余额告警

## 测试

### 单元测试

```bash
mvn test -pl loadup-components-gotone-binder-sms
```

### 测试覆盖率

- AliyunSmsProvider: 10/10 ✅
- TencentSmsProvider: 7/7 ✅
- HuaweiSmsProvider: 6/6 ✅
- YunpianSmsProvider: 7/7 ✅
- Integration Tests: 14/14 ✅

### Mock 测试

```java

@Test
public void testSendSms() {
    SendRequest request = SendRequest.builder()
            .receivers(List.of("13800138000"))
            .title("验证码")
            .content("123456")
            .templateParams(Map.of("code", "123456"))
            .build();

    SendResult result = smsProvider.send(request);

    assertThat(result.isSuccess()).isTrue();
}
```

## 性能优化

### 1. 批量发送

```java
// 一次发送给多个号码
NotificationRequest request = NotificationRequest.builder()
                .businessCode("PROMOTION_SMS")
                .address("13800138000,13900139000,13700137000")  // 逗号分隔
                .params(params)
                .build();
```

### 2. 异步发送

```java

@Async
public CompletableFuture<NotificationResult> sendSmsAsync(NotificationRequest request) {
    return CompletableFuture.completedFuture(notificationService.send(request));
}
```

### 3. 连接池

提供商SDK自动使用HTTP连接池。

## 监控指标

```java
// 发送成功率
gotone.sms.send.success.rate

// 各提供商使用率
gotone.sms.provider.

usage {provider = "aliyun"}
gotone.sms.provider.

usage {provider = "tencent"}

// 失败原因统计
gotone.sms.failure.

reason {reason = "invalid_phone"}
```

## 最佳实践

1. **使用多提供商**: 配置 2-3 个提供商备份
2. **限制发送频率**: 防止恶意刷短信
3. **验证手机号**: 发送前验证号码格式
4. **记录发送日志**: 便于排查问题
5. **监控余额**: 及时充值
6. **合规性**: 遵守反垃圾短信规定
7. **敏感词过滤**: 避免触发运营商过滤

## 费用说明

各提供商计费方式（仅供参考，以官方为准）：

| 提供商 |    价格    |    备注    |
|-----|----------|----------|
| 阿里云 | 0.045元/条 | 预付费或后付费  |
| 腾讯云 | 0.045元/条 | 预付费套餐更优惠 |
| 华为云 | 0.042元/条 | 短信包更优惠   |
| 云片  | 0.05元/条  | 充值赠送     |

## 依赖

```xml

<dependencies>
    <dependency>
        <groupId>io.github.loadup-cloud</groupId>
        <artifactId>loadup-components-gotone-api</artifactId>
    </dependency>

    <!-- 阿里云SDK -->
    <dependency>
        <groupId>com.aliyun</groupId>
        <artifactId>dysmsapi20170525</artifactId>
    </dependency>

    <!-- 腾讯云SDK -->
    <dependency>
        <groupId>com.tencentcloudapi</groupId>
        <artifactId>tencentcloud-sdk-java-sms</artifactId>
    </dependency>

    <!-- 华为云SDK -->
    <dependency>
        <groupId>com.huaweicloud.sdk</groupId>
        <artifactId>huaweicloud-sdk-msgsms</artifactId>
    </dependency>
</dependencies>
```

## 相关文档

- [主文档](../README.md)
- [API 模块](../loadup-components-gotone-api/README.md)
- [扩展指南](../PROVIDER_EXTENSION_GUIDE.md)

## 许可证

GPL-3.0 License
