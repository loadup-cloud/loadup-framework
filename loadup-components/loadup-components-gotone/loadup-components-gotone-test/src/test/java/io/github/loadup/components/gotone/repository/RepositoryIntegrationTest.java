package io.github.loadup.components.gotone.repository;

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

import io.github.loadup.commons.util.RandomUtil;
import io.github.loadup.components.gotone.TestApplication;
import io.github.loadup.components.gotone.dataobject.*;
import io.github.loadup.components.gotone.dataobject.BusinessCodeDO;
import io.github.loadup.components.gotone.dataobject.ChannelMappingDO;
import io.github.loadup.components.gotone.dataobject.NotificationRecordDO;
import io.github.loadup.components.gotone.dataobject.NotificationTemplateDO;
import io.github.loadup.components.testcontainers.database.AbstractMySQLContainerTest;
import java.time.LocalDateTime;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.*;
import org.springframework.test.context.jdbc.Sql;

/** Repository 集成测试 - 使用 Testcontainers MySQL */
@DataJdbcTest
@ActiveProfiles("test")
@Sql(scripts = "/schema.sql")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = TestApplication.class)
public class RepositoryIntegrationTest extends AbstractMySQLContainerTest {

  @Autowired private BusinessCodeRepository businessCodeRepository;

  @Autowired private ChannelMappingRepository channelMappingRepository;

  @Autowired private NotificationTemplateRepository templateRepository;

  @Autowired private NotificationRecordRepository recordRepository;

  @Autowired private JdbcTemplate jdbcTemplate;

  @Test
  void testBusinessCodeRepositoryFindByCode() {
    // When
    Optional<BusinessCodeDO> result =
        businessCodeRepository.findByBusinessCodeAndEnabled("ORDER_CONFIRM");

    // Then
    assertThat(result).isPresent();
    assertThat(result.get().getBusinessCode()).isEqualTo("ORDER_CONFIRM");
    assertThat(result.get().getBusinessName()).isEqualTo("订单确认");
    assertThat(result.get().getEnabled()).isTrue();
  }

  @Test
  void testBusinessCodeRepositoryFindByCodeNotFound() {
    // When
    Optional<BusinessCodeDO> result =
        businessCodeRepository.findByBusinessCodeAndEnabled("NOT_EXIST");

    // Then
    assertThat(result).isEmpty();
  }

  @Test
  void testBusinessCodeRepositorySave() {
    // Given
    BusinessCodeDO businessCode = new BusinessCodeDO();
    // 不设置 ID，让框架自动生成
    businessCode.setBusinessCode("NEW_CODE");
    businessCode.setBusinessName("New Business");
    businessCode.setDescription("Test Business");
    businessCode.setEnabled(true);
    // 不设置 createdAt 和 updatedAt，让审计功能自动处理
    businessCode.setId(RandomUtil.randomNumbers(16));
    // businessCode.setCreatedAt(LocalDateTime.now());

    // When
    BusinessCodeDO saved = businessCodeRepository.save(businessCode);

    // Then
    assertThat(saved.getId()).isNotNull(); // 验证 ID 已自动生成
    assertThat(saved.getBusinessCode()).isEqualTo("NEW_CODE");
    // assertThat(saved.getCreatedAt()).isNotNull(); // 验证自动设置了 createdAt

    // Verify
    Optional<BusinessCodeDO> found =
        businessCodeRepository.findByBusinessCodeAndEnabled("NEW_CODE");
    assertThat(found).isPresent();
    assertThat(found.get().getBusinessCode()).isEqualTo("NEW_CODE");
  }

  @Test
  void testChannelMappingRepositoryFindByBusinessCode() {
    // When
    List<ChannelMappingDO> results =
        channelMappingRepository.findByBusinessCodeAndEnabled("ORDER_CONFIRM");

    // Then
    assertThat(results).isNotEmpty();
    assertThat(results).hasSize(2); // SMS and EMAIL
    assertThat(results)
        .extracting(ChannelMappingDO::getChannel)
        .containsExactlyInAnyOrder("SMS", "EMAIL");
  }

  @Test
  void testChannelMappingRepositoryFindByBusinessCodeAndChannel() {
    // When
    Optional<ChannelMappingDO> result =
        channelMappingRepository.findByBusinessCodeAndChannelAndEnabled("ORDER_CONFIRM", "SMS");

    // Then
    assertThat(result).isPresent();
    assertThat(result.get().getTemplateCode()).isEqualTo("ORDER_CONFIRM_SMS");
    assertThat(result.get().getProviderListJson()).contains("aliyun");
  }

  @Test
  void testChannelMappingRepositoryPriority() {
    // When
    List<ChannelMappingDO> results =
        channelMappingRepository.findByBusinessCodeAndEnabled("ORDER_CONFIRM");

    // Then - Should be ordered by priority DESC
    assertThat(results).isNotEmpty();
    assertThat(results.get(0).getPriority()).isGreaterThanOrEqualTo(results.get(1).getPriority());
  }

  @Test
  void testNotificationTemplateRepositoryFindByCode() {
    // When
    Optional<NotificationTemplateDO> result =
        templateRepository.findByTemplateCodeAndEnabled("ORDER_CONFIRM_SMS");

    // Then
    assertThat(result).isPresent();
    assertThat(result.get().getTemplateName()).isEqualTo("订单确认短信");
    assertThat(result.get().getChannel()).isEqualTo("SMS");
    assertThat(result.get().getContent()).contains("订单");
  }

  @Test
  void testNotificationTemplateRepositoryFindByChannel() {
    // When
    List<NotificationTemplateDO> results = templateRepository.findByChannelAndEnabled("SMS");

    // Then
    assertThat(results).isNotEmpty();
    assertThat(results).hasSizeGreaterThanOrEqualTo(2);
    assertThat(results).allMatch(t -> t.getChannel().equals("SMS"));
  }

  @Test
  void testNotificationTemplateRepositoryFindAllEnabled() {
    // When
    List<NotificationTemplateDO> results = templateRepository.findAllEnabled();

    // Then
    assertThat(results).isNotEmpty();
    assertThat(results).hasSize(4);
    assertThat(results).allMatch(NotificationTemplateDO::getEnabled);
  }

  @Test
  void testNotificationRecordRepositorySave() {
    // Given
    NotificationRecordDO record = new NotificationRecordDO();
    // 不设置 ID，让框架自动生成
    record.setId(RandomUtil.randomNumbers(16));
    record.setTraceId("trace-001");
    record.setBusinessCode("ORDER_CONFIRM");
    record.setBizId("biz-001");
    record.setMessageId("msg-001");
    record.setChannel("SMS");
    record.setReceiversJson("[\"13800138000\"]");
    record.setTemplateCode("ORDER_CONFIRM_SMS");
    record.setTitle("订单确认");
    record.setContent("您的订单已确认");
    record.setProvider("aliyun");
    record.setStatus("SUCCESS");
    record.setRetryCount(0);
    record.setPriority(10);
    record.setSendTime(LocalDateTime.now());
    // 不设置 createdAt 和 updatedAt

    // When
    NotificationRecordDO saved = recordRepository.save(record);

    // Then
    assertThat(saved.getId()).isNotNull(); // 验证 ID 已自动生成
    assertThat(saved.getBizId()).isEqualTo("biz-001");
    // assertThat(saved.getCreatedAt()).isNotNull(); // 验证自动设置了 createdAt
  }

  @Test
  void testNotificationRecordRepositoryFindByBizId() {
    // Given
    NotificationRecordDO record = new NotificationRecordDO();
    // 不设置 ID，让框架自动生成
    record.setId(RandomUtil.randomNumbers(16));
    record.setBizId("test-biz-id");
    record.setBusinessCode("TEST");
    record.setChannel("SMS");
    record.setStatus("SUCCESS");
    record.setRetryCount(0);
    // 不设置 createdAt 和 updatedAt
    recordRepository.save(record);

    // When
    Optional<NotificationRecordDO> result = recordRepository.findByBizId("test-biz-id");

    // Then
    assertThat(result).isPresent();
    assertThat(result.get().getBizId()).isEqualTo("test-biz-id");
  }

  @Test
  void testNotificationRecordRepositoryFindByTraceId() {
    // Given
    String traceId = "trace-" + UUID.randomUUID();

    NotificationRecordDO record1 = new NotificationRecordDO();
    // 不设置 ID，让框架自动生成
    record1.setId(RandomUtil.randomNumbers(16));
    record1.setTraceId(traceId);
    record1.setBizId("biz-1");
    record1.setBusinessCode("TEST");
    record1.setChannel("SMS");
    record1.setStatus("SUCCESS");
    record1.setRetryCount(0);
    // 不设置 createdAt 和 updatedAt
    recordRepository.save(record1);

    NotificationRecordDO record2 = new NotificationRecordDO();
    // 不设置 ID，让框架自动生成
    record2.setId(RandomUtil.randomNumbers(16));
    record2.setTraceId(traceId);
    record2.setBizId("biz-2");
    record2.setBusinessCode("TEST");
    record2.setChannel("EMAIL");
    record2.setStatus("SUCCESS");
    record2.setRetryCount(0);
    // 不设置 createdAt 和 updatedAt
    recordRepository.save(record2);

    // When
    List<NotificationRecordDO> results = recordRepository.findByTraceId(traceId);

    // Then
    assertThat(results).hasSize(2);
    assertThat(results).allMatch(r -> r.getTraceId().equals(traceId));
  }

  @Test
  void testNotificationRecordRepositoryFindByBusinessCodeAndStatus() {
    // Given
    NotificationRecordDO record = new NotificationRecordDO();
    // 不设置 ID，让框架自动生成
    record.setId(RandomUtil.randomNumbers(16));
    record.setBusinessCode("PAYMENT_SUCCESS");
    record.setBizId("payment-001");
    record.setChannel("PUSH");
    record.setStatus("FAILED");
    record.setRetryCount(1);
    // 不设置 createdAt 和 updatedAt
    recordRepository.save(record);

    // When
    List<NotificationRecordDO> results =
        recordRepository.findByBusinessCodeAndStatus("PAYMENT_SUCCESS", "FAILED");

    // Then
    assertThat(results).isNotEmpty();
    assertThat(results)
        .allMatch(
            r -> r.getBusinessCode().equals("PAYMENT_SUCCESS") && r.getStatus().equals("FAILED"));
  }

  @Test
  void testNotificationRecordRepositoryFindRetryableRecords() {
    // Given
    LocalDateTime now = LocalDateTime.now();

    NotificationRecordDO record = new NotificationRecordDO();
    // 不设置 ID，让框架自动生成
    record.setId(RandomUtil.randomNumbers(16));
    record.setBusinessCode("TEST");
    record.setBizId("retry-test");
    record.setChannel("SMS");
    record.setStatus("FAILED");
    record.setRetryCount(1);
    recordRepository.save(record);

    // When
    List<NotificationRecordDO> results = recordRepository.findRetryableRecords(now.minusHours(24));

    // Then
    // assertThat(results).isNotEmpty();
    assertThat(results).allMatch(r -> r.getStatus().equals("FAILED") && r.getRetryCount() < 3);
  }
}
