/*
 * #%L
 * LoadUp Observability
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package io.github.loadup.components.observability.digest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ch.qos.logback.classic.Logger;
import io.github.loadup.commons.enums.CommonResultCodeEnum;
import io.github.loadup.commons.error.CommonException;
import io.github.loadup.commons.log.LogUtil;
import io.github.loadup.commons.log.annotation.ServiceDigest;
import io.micrometer.tracing.Tracer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.SimpleTransactionStatus;

@SpringBootTest(
        classes = ServiceDigestIT.Application.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
            "spring.application.name=digest-test",
            "management.tracing.sampling.probability=1.0",
            "management.tracing.export.otlp.enabled=false",
            "management.otlp.metrics.export.enabled=false"
        })
class ServiceDigestIT {
    private final PaymentService service;
    private final CommitManager transactions;
    private final Tracer tracer;

    @Autowired
    ServiceDigestIT(PaymentService service, CommitManager transactions, Tracer tracer) {
        this.service = service;
        this.transactions = transactions;
        this.tracer = tracer;
    }

    @Test
    void autoDiscoveryIncludesCommitOutcomeAndMicrometerTraceCorrelation() {
        var appender = new ServiceDigestTest.CapturingAppender();
        appender.start();
        var logger = (Logger) LogUtil.getLogger(ServiceDigestInterceptor.class);
        logger.addAppender(appender);
        var span = tracer.nextSpan().name("digest-integration").start();
        try (var ignored = tracer.withSpan(span)) {
            transactions.failCommit = false;
            assertThat(service.settle()).isEqualTo("accepted");
            assertThat(transactions.commits).isEqualTo(1);
            assertThat(appender.events).hasSize(1);
            var success = appender.events.peek();
            assertThat(success.getFormattedMessage()).contains("outcome=success code=SUCCESS");
            assertThat(success.getMDCPropertyMap())
                    .containsEntry("traceId", span.context().traceId())
                    .containsEntry("spanId", span.context().spanId());
            transactions.failCommit = true;
            assertThatThrownBy(service::settle).isSameAs(transactions.failure);
            assertThat(transactions.commits).isEqualTo(2);
            assertThat(appender.events).hasSize(2);
            var failure = appender.events.stream().toList().getLast();
            assertThat(failure.getFormattedMessage())
                    .contains("outcome=failure code=CONFLICT")
                    .doesNotContain("synthetic-commit-secret");
            assertThat(failure.getThrowableProxy()).isNull();
        } finally {
            transactions.failCommit = false;
            span.end();
            logger.detachAppender(appender);
            appender.stop();
        }
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @EnableTransactionManagement
    static class Application {
        @Bean
        PaymentService paymentService() {
            return new PaymentService();
        }

        @Bean
        CommitManager transactionManager() {
            return new CommitManager();
        }
    }

    @ServiceDigest
    public static class PaymentService {
        @Transactional
        public String settle() {
            return "accepted";
        }
    }

    static class CommitManager implements PlatformTransactionManager {
        final CommonException failure = new CommonException(
                CommonResultCodeEnum.CONFLICT, new IllegalStateException("synthetic-commit-secret"));
        int commits;
        boolean failCommit;

        @Override
        public TransactionStatus getTransaction(TransactionDefinition definition) {
            return new SimpleTransactionStatus();
        }

        @Override
        public void commit(TransactionStatus status) {
            commits++;
            if (failCommit) {
                throw failure;
            }
        }

        @Override
        public void rollback(TransactionStatus status) {}
    }
}
