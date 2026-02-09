//package io.github.loadup.components.gotone.converter;
//
///*-
// * #%L
// * loadup-components-gotone-test
// * %%
// * Copyright (C) 2026 LoadUp Cloud
// * %%
// * This program is free software: you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as
// * published by the Free Software Foundation, either version 3 of the
// * License, or (at your option) any later version.
// *
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Public License for more details.
// *
// * You should have received a copy of the GNU General Public
// * License along with this program.  If not, see
// * <http://www.gnu.org/licenses/gpl-3.0.html>.
// * #L%
// */
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//import io.github.loadup.components.gotone.core.dataobject.NotificationRecordDO;
//import io.github.loadup.components.gotone.domain.ChannelMapping;
//import io.github.loadup.components.gotone.domain.NotificationRecord;
//import java.time.LocalDateTime;
//import java.util.Arrays;
//import java.util.List;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
///** GotoneConverter 测试类 */
//class GotoneConverterTest {
//
//    private io.github.loadup.components.gotone.core.converter.GotoneConverter converter;
//
//    @BeforeEach
//    void setUp() {
//        converter = new GotoneConverterImpl();
//    }
//
//    @Test
//    void testToChannelMapping() {
//        // Given
//        ChannelMappingDO channelMappingDO = new ChannelMappingDO();
//        channelMappingDO.setId("1");
//        channelMappingDO.setBizCode("ORDER_CONFIRM");
//        channelMappingDO.setChannel("SMS");
//
//        // When
//        ChannelMapping result = converter.toChannelMapping(channelMappingDO);
//
//        // Then
//        assertThat(result).isNotNull();
//        assertThat(result.getId()).isEqualTo("1");
//        assertThat(result.getBusinessCode()).isEqualTo("ORDER_CONFIRM");
//        assertThat(result.getChannel()).isEqualTo("SMS");
//        assertThat(result.getTemplateCode()).isEqualTo("ORDER_CONFIRM_SMS");
//        assertThat(result.getProviderList()).containsExactly("aliyun", "tencent");
//        assertThat(result.getPriority()).isEqualTo(10);
//        assertThat(result.getEnabled()).isTrue();
//    }
//
//    @Test
//    void testToChannelMappingWithNullProviderList() {
//        // Given
//        ChannelMappingDO channelMappingDO = new ChannelMappingDO();
//        channelMappingDO.setId("1");
//        channelMappingDO.setBizCode("TEST");
//        channelMappingDO.setChannel("EMAIL");
//
//        // When
//        ChannelMapping result = converter.toChannelMapping(channelMappingDO);
//
//        // Then
//        assertThat(result).isNotNull();
//        assertThat(result.getProviderList()).isNull();
//    }
//
//    @Test
//    void testToChannelMappingDO() {
//        // Given
//        ChannelMapping channelMapping = new ChannelMapping();
//        channelMapping.setId("1");
//        channelMapping.setBusinessCode("ORDER_CONFIRM");
//        channelMapping.setChannel("SMS");
//        channelMapping.setTemplateCode("ORDER_CONFIRM_SMS");
//        channelMapping.setProviderList(Arrays.asList("aliyun", "tencent"));
//        channelMapping.setPriority(10);
//        channelMapping.setEnabled(true);
//
//        // When
//        ChannelMappingDO result = converter.toChannelMappingDO(channelMapping);
//
//        // Then
//        assertThat(result).isNotNull();
//        assertThat(result.getId()).isEqualTo("1");
//        assertThat(result.getBizCode()).isEqualTo("ORDER_CONFIRM");
//    }
//
//    @Test
//    void testToChannelMappingList() {
//        // Given
//        ChannelMappingDO do1 = new ChannelMappingDO();
//        do1.setId("1");
//        do1.setBizCode("TEST");
//        do1.setChannel("SMS");
//
//        ChannelMappingDO do2 = new ChannelMappingDO();
//        do2.setId("2");
//        do2.setBizCode("TEST");
//        do2.setChannel("EMAIL");
//
//        List<ChannelMappingDO> doList = Arrays.asList(do1, do2);
//
//        // When
//        List<ChannelMapping> result = converter.toChannelMappingList(doList);
//
//        // Then
//        assertThat(result).hasSize(2);
//        assertThat(result.get(0).getId()).isEqualTo("1");
//        assertThat(result.get(1).getId()).isEqualTo("2");
//    }
//
//    @Test
//    void testToNotificationRecord() {
//        // Given
//        NotificationRecordDO recordDO = new NotificationRecordDO();
//        recordDO.setId("1");
//        recordDO.setTraceId("trace-001");
//        recordDO.setBizCode("ORDER_CONFIRM");
//        recordDO.setBizCode("biz-001");
//        recordDO.setMessageId("msg-001");
//        recordDO.setChannel("SMS");
//        recordDO.setTemplateCode("ORDER_CONFIRM_SMS");
//        recordDO.setProvider("aliyun");
//        recordDO.setStatus("SUCCESS");
//        recordDO.setRetryCount(0);
//        recordDO.setPriority(10);
//        recordDO.setErrorMessage(null);
//        recordDO.setSendTime(LocalDateTime.now());
//
//        // When
//        NotificationRecord result = converter.toNotificationRecord(recordDO);
//
//        // Then
//        assertThat(result).isNotNull();
//        assertThat(result.getId()).isEqualTo("1");
//        assertThat(result.getTraceId()).isEqualTo("trace-001");
//        assertThat(result.getBizId()).isEqualTo("biz-001");
//        assertThat(result.getReceivers()).containsExactly("13800138000", "13900139000");
//        assertThat(result.getProvider()).isEqualTo("aliyun");
//        assertThat(result.getStatus()).isEqualTo("SUCCESS");
//    }
//
//    @Test
//    void testToNotificationRecordWithNullReceivers() {
//        // Given
//        NotificationRecordDO recordDO = new NotificationRecordDO();
//        recordDO.setId("1");
//        recordDO.setBizCode("biz-001");
//
//        // When
//        NotificationRecord result = converter.toNotificationRecord(recordDO);
//
//        // Then
//        assertThat(result).isNotNull();
//        assertThat(result.getReceivers()).isNull();
//    }
//
//    @Test
//    void testToNotificationRecordDO() {
//        // Given
//        NotificationRecord record = new NotificationRecord();
//        record.setId("1");
//        record.setTraceId("trace-001");
//        record.setBizCode("ORDER_CONFIRM");
//        record.setBizId("biz-001");
//        record.setMessageId("msg-001");
//        record.setChannel("SMS");
//        record.setTemplateCode("ORDER_CONFIRM_SMS");
//        record.setTitle("订单确认");
//        record.setContent("您的订单已确认");
//        record.setProvider("aliyun");
//        record.setStatus("SUCCESS");
//        record.setRetryCount(0);
//        record.setPriority(10);
//        record.setSendTime(LocalDateTime.now());
//
//        // When
//        NotificationRecordDO result = converter.toNotificationRecordDO(record);
//
//        // Then
//        assertThat(result).isNotNull();
//        assertThat(result.getId()).isEqualTo("1");
//    }
//
//    @Test
//    void testToNotificationRecordList() {
//        // Given
//        NotificationRecordDO do1 = new NotificationRecordDO();
//        do1.setId("1");
//        do1.setBizCode("biz-1");
//
//        NotificationRecordDO do2 = new NotificationRecordDO();
//        do2.setId("2");
//        do2.setBizCode("biz-2");
//
//        List<NotificationRecordDO> doList = Arrays.asList(do1, do2);
//
//        // When
//        List<NotificationRecord> result = converter.toNotificationRecordList(doList);
//
//        // Then
//        assertThat(result).hasSize(2);
//        assertThat(result.get(0).getId()).isEqualTo("1");
//        assertThat(result.get(1).getId()).isEqualTo("2");
//    }
//
//    @Test
//    void testJsonConversionWithInvalidJson() {
//        // Given
//        ChannelMappingDO channelMappingDO = new ChannelMappingDO();
//        channelMappingDO.setId("1");
//
//        // When
//        ChannelMapping result = converter.toChannelMapping(channelMappingDO);
//
//        // Then - Should handle gracefully
//        assertThat(result).isNotNull();
//        assertThat(result.getProviderList()).isEmpty();
//    }
//
//    @Test
//    void testEmptyList() {
//        // Given
//        List<ChannelMappingDO> emptyList = Arrays.asList();
//
//        // When
//        List<ChannelMapping> result = converter.toChannelMappingList(emptyList);
//
//        // Then
//        assertThat(result).isEmpty();
//    }
//}
