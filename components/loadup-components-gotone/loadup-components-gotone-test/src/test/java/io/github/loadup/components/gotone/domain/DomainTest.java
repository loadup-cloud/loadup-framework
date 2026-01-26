package io.github.loadup.components.gotone.domain;

/*-
 * #%L
 * loadup-components-gotone-test
 * %%
 * Copyright (C) 2026 LoadUp Cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

/**
 * Domain 对象测试
 */
class DomainTest {

    @Test
    void testBusinessCodeDomain() {
        // Given
        BusinessCode businessCode = new BusinessCode();

        // When
        businessCode.setId("1");
        businessCode.setBusinessCode("ORDER_CONFIRM");
        businessCode.setBusinessName("订单确认");
        businessCode.setDescription("订单确认通知");
        businessCode.setEnabled(true);
        businessCode.setCreatedAt(LocalDateTime.now());
        businessCode.setUpdatedAt(LocalDateTime.now());

        // Then
        assertThat(businessCode.getId()).isEqualTo("1");
        assertThat(businessCode.getBusinessCode()).isEqualTo("ORDER_CONFIRM");
        assertThat(businessCode.getBusinessName()).isEqualTo("订单确认");
        assertThat(businessCode.getDescription()).isEqualTo("订单确认通知");
        assertThat(businessCode.getEnabled()).isTrue();
        assertThat(businessCode.getCreatedAt()).isNotNull();
        assertThat(businessCode.getUpdatedAt()).isNotNull();
    }

    @Test
    void testChannelMappingDomain() {
        // Given
        ChannelMapping mapping = new ChannelMapping();

        // When
        mapping.setId("1");
        mapping.setBusinessCode("ORDER_CONFIRM");
        mapping.setChannel("SMS");
        mapping.setTemplateCode("ORDER_CONFIRM_SMS");
        mapping.setProviderList(Arrays.asList("aliyun", "tencent"));
        mapping.setPriority(10);
        mapping.setEnabled(true);

        // Then
        assertThat(mapping.getId()).isEqualTo("1");
        assertThat(mapping.getBusinessCode()).isEqualTo("ORDER_CONFIRM");
        assertThat(mapping.getChannel()).isEqualTo("SMS");
        assertThat(mapping.getTemplateCode()).isEqualTo("ORDER_CONFIRM_SMS");
        assertThat(mapping.getProviderList()).containsExactly("aliyun", "tencent");
        assertThat(mapping.getPriority()).isEqualTo(10);
        assertThat(mapping.getEnabled()).isTrue();
    }

    @Test
    void testNotificationTemplateDomain() {
        // Given
        NotificationTemplate template = new NotificationTemplate();

        // When
        template.setId("1");
        template.setTemplateCode("ORDER_CONFIRM_SMS");
        template.setTemplateName("订单确认短信");
        template.setChannel("SMS");
        template.setContent("您的订单${orderId}已确认");
        template.setTitleTemplate("订单通知");
        template.setTemplateType("SMS");
        template.setEnabled(true);

        // Then
        assertThat(template.getId()).isEqualTo("1");
        assertThat(template.getTemplateCode()).isEqualTo("ORDER_CONFIRM_SMS");
        assertThat(template.getTemplateName()).isEqualTo("订单确认短信");
        assertThat(template.getChannel()).isEqualTo("SMS");
        assertThat(template.getContent()).contains("订单");
        assertThat(template.getTitleTemplate()).isEqualTo("订单通知");
        assertThat(template.getTemplateType()).isEqualTo("SMS");
        assertThat(template.getEnabled()).isTrue();
    }

    @Test
    void testNotificationRecordDomain() {
        // Given
        NotificationRecord record = new NotificationRecord();
        LocalDateTime now = LocalDateTime.now();

        // When
        record.setId("1");
        record.setTraceId("trace-001");
        record.setBusinessCode("ORDER_CONFIRM");
        record.setBizId("biz-001");
        record.setMessageId("msg-001");
        record.setChannel("SMS");
        record.setReceivers(Arrays.asList("13800138000"));
        record.setTemplateCode("ORDER_CONFIRM_SMS");
        record.setTitle("订单确认");
        record.setContent("您的订单已确认");
        record.setProvider("aliyun");
        record.setStatus("SUCCESS");
        record.setRetryCount(0);
        record.setPriority(10);
        record.setErrorMessage(null);
        record.setSendTime(now);

        // Then
        assertThat(record.getId()).isEqualTo("1");
        assertThat(record.getTraceId()).isEqualTo("trace-001");
        assertThat(record.getBusinessCode()).isEqualTo("ORDER_CONFIRM");
        assertThat(record.getBizId()).isEqualTo("biz-001");
        assertThat(record.getMessageId()).isEqualTo("msg-001");
        assertThat(record.getChannel()).isEqualTo("SMS");
        assertThat(record.getReceivers()).containsExactly("13800138000");
        assertThat(record.getTemplateCode()).isEqualTo("ORDER_CONFIRM_SMS");
        assertThat(record.getTitle()).isEqualTo("订单确认");
        assertThat(record.getContent()).isEqualTo("您的订单已确认");
        assertThat(record.getProvider()).isEqualTo("aliyun");
        assertThat(record.getStatus()).isEqualTo("SUCCESS");
        assertThat(record.getRetryCount()).isEqualTo(0);
        assertThat(record.getPriority()).isEqualTo(10);
        assertThat(record.getErrorMessage()).isNull();
        assertThat(record.getSendTime()).isEqualTo(now);
    }

    @Test
    void testDomainObjectsInheritBaseDomain() {
        // Given
        BusinessCode businessCode = new BusinessCode();
        ChannelMapping channelMapping = new ChannelMapping();
        NotificationTemplate template = new NotificationTemplate();
        NotificationRecord record = new NotificationRecord();

        // When
        LocalDateTime now = LocalDateTime.now();
        businessCode.setCreatedAt(now);
        channelMapping.setCreatedAt(now);
        template.setCreatedAt(now);
        record.setCreatedAt(now);

        // Then - 验证都继承了 BaseDomain
        assertThat(businessCode.getCreatedAt()).isEqualTo(now);
        assertThat(channelMapping.getCreatedAt()).isEqualTo(now);
        assertThat(template.getCreatedAt()).isEqualTo(now);
        assertThat(record.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void testChannelMappingWithEmptyProviderList() {
        // Given
        ChannelMapping mapping = new ChannelMapping();

        // When
        mapping.setProviderList(Arrays.asList());

        // Then
        assertThat(mapping.getProviderList()).isEmpty();
    }

    @Test
    void testNotificationRecordWithMultipleReceivers() {
        // Given
        NotificationRecord record = new NotificationRecord();

        // When
        record.setReceivers(Arrays.asList("user1@example.com", "user2@example.com", "user3@example.com"));

        // Then
        assertThat(record.getReceivers()).hasSize(3);
        assertThat(record.getReceivers()).contains("user1@example.com", "user2@example.com", "user3@example.com");
    }

    @Test
    void testBusinessCodeToString() {
        // Given
        BusinessCode businessCode = new BusinessCode();
        businessCode.setBusinessCode("TEST");

        // When
        String result = businessCode.toString();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).contains("TEST");
    }

    @Test
    void testDomainObjectsAreSerializable() {
        // Given
        BusinessCode businessCode = new BusinessCode();
        ChannelMapping channelMapping = new ChannelMapping();
        NotificationTemplate template = new NotificationTemplate();
        NotificationRecord record = new NotificationRecord();

        // Then - 验证都实现了 Serializable（通过 BaseDomain）
        assertThat(businessCode).isInstanceOf(java.io.Serializable.class);
        assertThat(channelMapping).isInstanceOf(java.io.Serializable.class);
        assertThat(template).isInstanceOf(java.io.Serializable.class);
        assertThat(record).isInstanceOf(java.io.Serializable.class);
    }
}

