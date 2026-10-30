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
import static org.mockito.Mockito.mockStatic;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import io.github.loadup.commons.enums.CommonResultCodeEnum;
import io.github.loadup.commons.error.CommonException;
import io.github.loadup.commons.log.LogUtil;
import io.github.loadup.commons.log.annotation.ServiceDigest;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.aop.support.AopUtils;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

class ServiceDigestTest {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AopAutoConfiguration.class, ServiceDigestAutoConfiguration.class))
            .withUserConfiguration(Services.class);
    private final Logger logger = (Logger) LogUtil.getLogger(ServiceDigestInterceptor.class);
    private final CapturingAppender appender = new CapturingAppender();
    private Level previousLevel;

    @BeforeEach
    void capture() {
        previousLevel = logger.getLevel();
        logger.setLevel(Level.INFO);
        appender.start();
        logger.addAppender(appender);
    }

    @AfterEach
    void restore() {
        logger.detachAppender(appender);
        logger.setLevel(previousLevel);
        appender.stop();
    }

    @Test
    void classAnnotationPreservesValuesAndLogsExactlyOneSafeSummaryPerCall() {
        runner.run(context -> {
            assertThat(context).hasNotFailed();
            var service = context.getBean(WholeService.class);
            assertThat(AopUtils.isCglibProxy(service)).isTrue();
            Object secret = new Object() {
                @Override
                public String toString() {
                    throw new AssertionError("must never inspect payloads");
                }
            };
            try (var ignored = MDC.putCloseable("traceId", "synthetic-trace")) {
                assertThat(service.echo(secret)).isSameAs(secret);
            }
            service.nothing();
            assertThat(service.echo(null)).isNull();
            assertThat(appender.events).hasSize(3);
            assertThat(appender.events.peek().getMDCPropertyMap()).containsEntry("traceId", "synthetic-trace");
            for (var event : appender.events) {
                assertThat(event.getFormattedMessage())
                        .contains(
                                "service_digest service=",
                                "WholeService",
                                "outcome=success code=SUCCESS",
                                "exceptionType=none")
                        .containsPattern("elapsedMs=\\d+");
                assertThat(event.getThrowableProxy()).isNull();
            }
        });
    }

    @Test
    void methodAnnotationLeavesUnmarkedMethodsAndBeansAlone() {
        runner.run(context -> {
            var service = context.getBean(MethodService.class);
            assertThat(service.tracked()).isEqualTo("tracked");
            assertThat(service.untracked()).isEqualTo("plain");
            assertThat(service.toString()).isEqualTo("method-service");
            assertThat(AopUtils.isAopProxy(context.getBean(UnmarkedService.class)))
                    .isFalse();
            assertThat(appender.events).hasSize(1);
            assertThat(appender.events.peek().getFormattedMessage()).contains("tracked()");
        });
    }

    @Test
    void businessCheckedAndFatalFailuresKeepTheirOriginalIdentityAndHideMessages() {
        runner.run(context -> {
            var service = context.getBean(WholeService.class);
            var business = new CommonException(
                    CommonResultCodeEnum.CONFLICT, new IllegalStateException("synthetic-private-payload"));
            var checked = new IOException("synthetic-private-payload");
            var fatal = new AssertionError("synthetic-private-payload");
            assertThatThrownBy(() -> service.fail(business)).isSameAs(business);
            assertThatThrownBy(() -> service.fail(checked)).isSameAs(checked);
            assertThatThrownBy(() -> service.fail(fatal)).isSameAs(fatal);
            assertThat(appender.events).hasSize(3);
            List<ILoggingEvent> events = new ArrayList<>(appender.events);
            assertThat(events.getFirst().getFormattedMessage()).contains("outcome=failure code=CONFLICT");
            assertThat(events.get(1).getFormattedMessage())
                    .contains("code=SYS_ERROR", "exceptionType=java.io.IOException");
            assertThat(events.getLast().getFormattedMessage()).contains("exceptionType=java.lang.AssertionError");
            for (var event : events) {
                assertThat(event.getFormattedMessage()).doesNotContain("synthetic-private-payload");
                assertThat(event.getThrowableProxy()).isNull();
            }
        });
    }

    @Test
    void annotationWorksForJdkProxiesAndImplementationMethods() {
        runner.withPropertyValues("spring.aop.proxy-target-class=false").run(context -> {
            var service = context.getBean(ServiceApi.class);
            assertThat(AopUtils.isJdkDynamicProxy(service)).isTrue();
            assertThat(service.fromInterface()).isEqualTo("interface");
            assertThat(service.fromImplementation()).isEqualTo("implementation");
            assertThat(appender.events).hasSize(2);
        });
    }

    @Test
    void inheritedAndComposedClassAnnotationsAreSupported() {
        runner.run(context -> {
            assertThat(context.getBean(InheritedService.class).echo("inherited"))
                    .isEqualTo("inherited");
            assertThat(context.getBean(ComposedService.class).call()).isEqualTo("composed");
            assertThat(appender.events).hasSize(2);
        });
    }

    @Test
    void selfInvocationIsOneProxyBoundaryAndExternalNestedCallsAreSeparate() {
        runner.run(context -> {
            context.getBean(WholeService.class).selfInvoke();
            assertThat(appender.events).hasSize(1);
            context.getBean(WholeService.class).nothing();
            assertThat(appender.events).hasSize(2);
        });
    }

    @Test
    void globalSwitchAndStandardAopSwitchDisableInterception() {
        runner.withPropertyValues("loadup.observability.service-digest.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean("loadUpServiceDigestAdvisor");
                    assertThat(AopUtils.isAopProxy(context.getBean(WholeService.class)))
                            .isFalse();
                });
        runner.withPropertyValues("spring.aop.auto=false").run(context -> {
            assertThat(AopUtils.isAopProxy(context.getBean(WholeService.class))).isFalse();
        });
        assertThat(appender.events).isEmpty();
    }

    @Test
    void loggingBackendFailureDoesNotReplaceBusinessResultsOrFailures() {
        runner.run(context -> {
            var service = context.getBean(WholeService.class);
            var failure = new IOException("synthetic failure");
            try (var ignored = mockStatic(LogUtil.class, invocation -> {
                throw new IllegalStateException("synthetic logging backend failure");
            })) {
                assertThat(service.echo("retained")).isEqualTo("retained");
                assertThatThrownBy(() -> service.fail(failure)).isSameAs(failure);
            }
        });
    }

    @Test
    void stopwatchAndTraceMetadataRemainIndependentOnVirtualThreads() {
        runner.run(context -> {
            var service = context.getBean(WholeService.class);
            try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
                var results = new ArrayList<java.util.concurrent.Future<Object>>();
                for (int i = 0; i < 8; i++) {
                    String id = "trace-" + i;
                    results.add(executor.submit(() -> {
                        try (var ignored = MDC.putCloseable("traceId", id)) {
                            return service.echo(id);
                        }
                    }));
                }
                for (int i = 0; i < results.size(); i++) {
                    assertThat(results.get(i).get()).isEqualTo("trace-" + i);
                }
            }
            assertThat(appender.events).hasSize(8);
            assertThat(appender.events)
                    .extracting(event -> event.getMDCPropertyMap().get("traceId"))
                    .containsExactlyInAnyOrder(
                            "trace-0", "trace-1", "trace-2", "trace-3", "trace-4", "trace-5", "trace-6", "trace-7");
        });
    }

    @Test
    void pointcutExcludesNonPublicStaticAndObjectMethods() throws Exception {
        var pointcut = new ServiceDigestPointcut();
        assertThat(pointcut.matches(WholeService.class.getDeclaredMethod("hidden"), WholeService.class))
                .isFalse();
        assertThat(pointcut.matches(WholeService.class.getDeclaredMethod("utility"), WholeService.class))
                .isFalse();
        assertThat(pointcut.matches(Object.class.getMethod("hashCode"), WholeService.class))
                .isFalse();
    }

    public interface ServiceApi {
        @ServiceDigest
        String fromInterface();

        String fromImplementation();
    }

    @ServiceDigest
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Observed {}

    @Configuration(proxyBeanMethods = false)
    static class Services {
        @Bean
        @org.springframework.context.annotation.Primary
        WholeService wholeService() {
            return new WholeService();
        }

        @Bean
        MethodService methodService() {
            return new MethodService();
        }

        @Bean
        UnmarkedService unmarkedService() {
            return new UnmarkedService();
        }

        @Bean
        ServiceApi implementation() {
            return new ServiceImplementation();
        }

        @Bean
        InheritedService inheritedService() {
            return new InheritedService();
        }

        @Bean
        ComposedService composedService() {
            return new ComposedService();
        }
    }

    @ServiceDigest
    public static class WholeService {
        public static void utility() {}

        public Object echo(Object value) {
            return value;
        }

        public void nothing() {}

        public void fail(Throwable failure) throws Throwable {
            throw failure;
        }

        public void selfInvoke() {
            nothing();
        }

        private void hidden() {}
    }

    public static class MethodService {
        @ServiceDigest
        public String tracked() {
            return "tracked";
        }

        public String untracked() {
            return "plain";
        }

        @Override
        public String toString() {
            return "method-service";
        }
    }

    public static final class UnmarkedService {}

    public static class ServiceImplementation implements ServiceApi {
        @Override
        public String fromInterface() {
            return "interface";
        }

        @Override
        @ServiceDigest
        public String fromImplementation() {
            return "implementation";
        }
    }

    public static class InheritedService extends WholeService {}

    @Observed
    public static class ComposedService {
        public String call() {
            return "composed";
        }
    }

    static class CapturingAppender extends AppenderBase<ILoggingEvent> {
        final ConcurrentLinkedQueue<ILoggingEvent> events = new ConcurrentLinkedQueue<>();

        @Override
        protected void append(ILoggingEvent event) {
            event.prepareForDeferredProcessing();
            events.add(event);
        }
    }
}
