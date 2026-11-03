package io.github.loadup.components.webmvc;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.loadup.commons.context.ContextHolder;
import io.github.loadup.commons.context.ContextKeys;
import io.github.loadup.commons.context.ExecutionContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

/**
 * Embedded loopback MVC only; synthetic identities exercise thread isolation, not JWT validation.
 */
class MvcThreadModelTest {
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void realServletRequestsUseTheSelectedThreadModelAndKeepContextsIsolated(boolean virtual) throws Exception {
        try (var context = new SpringApplicationBuilder(Fixture.class).web(WebApplicationType.SERVLET).run(
            "--server.address=127.0.0.1", "--server.port=0", "--server.tomcat.threads.max=2",
            "--server.tomcat.threads.min-spare=1", "--spring.threads.virtual.enabled=" + virtual,
            "--spring.task.execution.propagate-context=true", "--spring.main.keep-alive=true",
            "--spring.main.banner-mode=off", "--logging.level.root=WARN",
            "--management.tracing.enabled=false", "--management.otlp.metrics.export.enabled=false");
             var client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2))
                 .followRedirects(HttpClient.Redirect.NEVER).version(HttpClient.Version.HTTP_1_1).build();
             var callers = Executors.newVirtualThreadPerTaskExecutor()) {
            int port = context.getEnvironment().getRequiredProperty("local.server.port", Integer.class);
            URI endpoint = URI.create("http://127.0.0.1:" + port + "/api/thread-contract");
            var mapper = context.getBean(ObjectMapper.class);
            var work = new ArrayList<Callable<Void>>();
            for (int index = 0; index < 16; index++) {
                String tenant = "tenant-" + index;
                String principal = "principal-" + index;
                work.add(() -> {
                    var response = client.send(HttpRequest.newBuilder(endpoint).timeout(Duration.ofSeconds(5))
                            .header("X-Test-Tenant", tenant).header("X-Test-Principal", principal).GET().build(),
                        HttpResponse.BodyHandlers.ofString());
                    assertThat(response.statusCode()).isEqualTo(200);
                    var state = mapper.readValue(response.body(), State.class);
                    assertThat(state.virtual()).isEqualTo(virtual);
                    assertThat(state.tenant()).isEqualTo(tenant);
                    assertThat(state.principal()).isEqualTo(principal);
                    return null;
                });
            }
            for (var result : callers.invokeAll(work, 10, TimeUnit.SECONDS)) {
                result.get(5, TimeUnit.SECONDS);
            }
            var failure = client.send(HttpRequest.newBuilder(URI.create(endpoint + "?fail=true"))
                .header("X-Test-Tenant", "failed").header("X-Test-Principal", "failed")
                .timeout(Duration.ofSeconds(5)).GET().build(), HttpResponse.BodyHandlers.discarding());
            assertThat(failure.statusCode()).isEqualTo(500);
            var empty = client.send(HttpRequest.newBuilder(endpoint).timeout(Duration.ofSeconds(5)).GET().build(),
                HttpResponse.BodyHandlers.ofString());
            var state = mapper.readValue(empty.body(), State.class);
            assertThat(state.tenant()).isNull();
            assertThat(state.principal()).isNull();
            assertThat(context.getBean(IsolationFilter.class).leaked).hasValue(0);
        }
    }

    record State(boolean virtual, String tenant, String principal) {
    }

    @Configuration(proxyBeanMethods = false)
    @EnableAutoConfiguration
    @Import(Probe.class)
    static class Fixture {
        @Bean
        IsolationFilter isolationFilter() {
            return new IsolationFilter();
        }

        @Bean
        FilterRegistrationBean<IsolationFilter> isolationFilterRegistration(IsolationFilter filter) {
            var registration = new FilterRegistrationBean<>(filter);
            registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
            return registration;
        }
    }

    @RestController
    @ProtocolResponse
    static class Probe {
        @GetMapping("/api/thread-contract")
        State state(@RequestParam(defaultValue = "false") boolean fail) {
            if (fail) {
                throw new IllegalStateException("synthetic failure");
            }
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            return new State(Thread.currentThread().isVirtual(), ContextHolder.get(ContextKeys.TENANT_ID),
                authentication == null ? null : authentication.getName());
        }
    }

    static class IsolationFilter extends OncePerRequestFilter {
        final AtomicInteger leaked = new AtomicInteger();

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
            if (!ContextHolder.isEmpty() || SecurityContextHolder.getContext().getAuthentication() != null) {
                leaked.incrementAndGet();
            }
            String tenant = request.getHeader("X-Test-Tenant");
            String principal = request.getHeader("X-Test-Principal");
            try {
                if (principal != null) {
                    SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(principal, null));
                }
                var metadata = tenant == null ? ExecutionContext.empty()
                    : ExecutionContext.empty().with(ContextKeys.TENANT_ID, tenant);
                ContextHolder.<Void, ServletException>callWith(metadata, () -> {
                    try {
                        chain.doFilter(request, response);
                    } catch (IOException failure) {
                        throw new ServletException(failure);
                    }
                    return null;
                });
            } finally {
                SecurityContextHolder.clearContext();
            }
        }
    }
}
