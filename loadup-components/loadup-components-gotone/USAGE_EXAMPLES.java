package io.github.loadup.components.gotone.example;

import io.github.loadup.components.gotone.api.NotificationService;
import io.github.loadup.components.gotone.model.NotificationRequest;
import io.github.loadup.components.gotone.model.NotificationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Gotone 组件使用示例
 *
 * <p>本示例展示了 ServiceCode 驱动架构的各种使用场景
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GotoneUsageExample {

    private final NotificationService notificationService;

    /**
     * 示例1：基本使用 - 自动多渠道发送
     */
    public void example1_basicUsage() {
        NotificationRequest request = NotificationRequest.builder()
            .serviceCode("ORDER_CREATED")  // 业务代码
            .receivers(List.of(
                "user@example.com",        // 自动识别为 EMAIL
                "13800138000"              // 自动识别为 SMS
            ))
            .templateParams(Map.of(
                "userName", "张三",
                "orderNo", "20260209001",
                "amount", "99.00"
            ))
            .build();

        NotificationResponse response = notificationService.send(request);

        log.info("发送结果: success={}, traceId={}, channels={}",
            response.getSuccess(),
            response.getTraceId(),
            response.getChannelResults().size());
    }

    /**
     * 示例2：幂等性保证
     */
    public void example2_idempotence() {
        String orderId = "12345";

        NotificationRequest request = NotificationRequest.builder()
            .serviceCode("ORDER_CREATED")
            .receivers(List.of("user@example.com"))
            .templateParams(Map.of("orderNo", orderId))
            .requestId("ORDER_" + orderId)  // 幂等性：同一个 requestId 只发送一次
            .build();

        // 第一次调用：正常发送
        NotificationResponse response1 = notificationService.send(request);
        log.info("第一次发送: {}", response1.getSuccess());

        // 第二次调用：自动跳过（幂等性）
        NotificationResponse response2 = notificationService.send(request);
        log.info("第二次发送（幂等性跳过）: {}", response2.getErrorMessage());
    }

    /**
     * 示例3：指定渠道发送
     */
    public void example3_specifyChannels() {
        NotificationRequest request = NotificationRequest.builder()
            .serviceCode("ORDER_CREATED")
            .receivers(List.of("user@example.com", "13800138000"))
            .templateParams(Map.of("orderNo", "12345"))
            .channels(List.of("EMAIL"))  // 只发送 EMAIL，忽略 SMS
            .build();

        NotificationResponse response = notificationService.send(request);
        log.info("仅发送 EMAIL: {}", response.getChannelResults());
    }

    /**
     * 示例4：异步发送
     */
    public void example4_asyncSend() {
        NotificationRequest request = NotificationRequest.builder()
            .serviceCode("ORDER_CREATED")
            .receivers(List.of("user@example.com"))
            .templateParams(Map.of("orderNo", "12345"))
            .async(true)  // 异步发送
            .build();

        // 立即返回，不阻塞
        notificationService.sendAsync(request);
        log.info("异步发送请求已提交");
    }

    /**
     * 示例5：批量发送
     */
    public void example5_batchSend() {
        List<String> receivers = List.of(
            "user1@example.com",
            "user2@example.com",
            "13800138000",
            "13800138001"
        );

        NotificationRequest request = NotificationRequest.builder()
            .serviceCode("PROMOTION_ACTIVITY")  // 促销活动通知
            .receivers(receivers)
            .templateParams(Map.of(
                "activityName", "双十一大促",
                "discount", "5折"
            ))
            .build();

        NotificationResponse response = notificationService.send(request);
        log.info("批量发送: 总数={}, 成功={}",
            response.getTotalReceivers(),
            response.getChannelResults().stream()
                .mapToInt(NotificationResponse.ChannelSendResult::getSuccessCount)
                .sum());
    }

    /**
     * 示例6：动态配置使用场景
     *
     * <p>场景：运营需要临时禁用邮件渠道（例如邮件服务器故障）
     */
    public void example6_dynamicConfiguration() {
        // 在数据库中执行：
        // UPDATE gotone_service_channel
        // SET enabled = FALSE
        // WHERE service_code = 'ORDER_CREATED' AND channel = 'EMAIL';

        NotificationRequest request = NotificationRequest.builder()
            .serviceCode("ORDER_CREATED")
            .receivers(List.of("user@example.com", "13800138000"))
            .templateParams(Map.of("orderNo", "12345"))
            .build();

        // 系统自动跳过已禁用的 EMAIL 渠道，只发送 SMS
        NotificationResponse response = notificationService.send(request);
        log.info("动态禁用 EMAIL 后，只发送了: {}",
            response.getChannelResults().stream()
                .map(NotificationResponse.ChannelSendResult::getChannel)
                .toList());
    }

    /**
     * 示例7：验证码发送（同步+高优先级）
     */
    public void example7_verificationCode() {
        NotificationRequest request = NotificationRequest.builder()
            .serviceCode("VERIFICATION_CODE")  // 验证码服务
            .receivers(List.of("13800138000"))
            .templateParams(Map.of(
                "code", "123456",
                "minutes", "5"
            ))
            .async(false)  // 同步发送（验证码需要立即发送）
            .build();

        NotificationResponse response = notificationService.send(request);
        log.info("验证码发送: {}", response.getSuccess());
    }

    /**
     * 示例8：错误处理
     */
    public void example8_errorHandling() {
        try {
            NotificationRequest request = NotificationRequest.builder()
                .serviceCode("NON_EXISTENT_SERVICE")  // 不存在的服务
                .receivers(List.of("user@example.com"))
                .templateParams(Map.of())
                .build();

            notificationService.send(request);
        } catch (Exception e) {
            log.error("发送失败: {}", e.getMessage());
            // 处理异常：记录日志、告警、重试等
        }
    }

    /**
     * 示例9：多语言支持
     */
    public void example9_multiLanguage() {
        // 配置多个模板，根据语言选择不同的 serviceCode
        String serviceCode = getUserLanguage().equals("en")
            ? "ORDER_CREATED_EN"
            : "ORDER_CREATED_ZH";

        NotificationRequest request = NotificationRequest.builder()
            .serviceCode(serviceCode)
            .receivers(List.of("user@example.com"))
            .templateParams(Map.of("orderNo", "12345"))
            .build();

        notificationService.send(request);
    }

    /**
     * 示例10：集成到业务流程
     */
    @Service
    @RequiredArgsConstructor
    public static class OrderService {

        private final NotificationService notificationService;

        public void createOrder(Order order) {
            // 1. 创建订单
            saveOrder(order);

            // 2. 发送通知（自动多渠道）
            notificationService.send(NotificationRequest.builder()
                .serviceCode("ORDER_CREATED")
                .receivers(List.of(order.getUserEmail(), order.getUserPhone()))
                .templateParams(Map.of(
                    "userName", order.getUserName(),
                    "orderNo", order.getOrderNo(),
                    "amount", order.getAmount()
                ))
                .requestId("ORDER_" + order.getId())
                .build());
        }

        private void saveOrder(Order order) {
            // 保存订单逻辑
        }
    }

    // ========== 辅助类 ==========

    private String getUserLanguage() {
        return "zh";  // 简化示例
    }

    public static class Order {
        private String id;
        private String orderNo;
        private String userName;
        private String userEmail;
        private String userPhone;
        private String amount;

        public String getId() { return id; }
        public String getOrderNo() { return orderNo; }
        public String getUserName() { return userName; }
        public String getUserEmail() { return userEmail; }
        public String getUserPhone() { return userPhone; }
        public String getAmount() { return amount; }
    }
}

