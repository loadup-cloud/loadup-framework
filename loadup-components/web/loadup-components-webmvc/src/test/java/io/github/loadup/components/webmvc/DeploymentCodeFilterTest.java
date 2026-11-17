/*
 * Copyright (C) 2026 LoadUp Cloud
 *
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
 */
package io.github.loadup.components.webmvc;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.loadup.components.runtime.deployment.DeploymentIdentity;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import tools.jackson.databind.json.JsonMapper;

class DeploymentCodeFilterTest {

    private static final String HEADER = "X-Tenant-Code";

    private final DeploymentCodeFilter filter = new DeploymentCodeFilter(
            new DeploymentIdentity("customer-a"),
            new DeploymentCodeProperties(),
            JsonMapper.builder().build());

    private MockHttpServletRequest request(String path) {
        var request = new MockHttpServletRequest("POST", path);
        request.setRequestURI(path);
        return request;
    }

    @Test
    void acceptsTheConfiguredDeploymentCodeAndContinuesTheChain() throws Exception {
        var request = request("/api/payments");
        request.addHeader(HEADER, "customer-a");
        var chain = new AtomicInteger();
        var response = new MockHttpServletResponse();

        filter.doFilter(request, response, (r, s) -> chain.incrementAndGet());

        assertThat(chain).hasValue(1);
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getContentAsString()).isEmpty();
    }

    @Test
    void rejectsMissingHeaderWithBadRequest() throws Exception {
        assertRejected(request("/api/payments"), "DEPLOYMENT_CODE_REQUIRED", 400);
    }

    @Test
    void rejectsBlankMalformedDuplicatedAndCommaCombinedValues() throws Exception {
        for (String invalid : new String[] {
            "",
            "   ",
            "Customer-A",
            " customer-a",
            "customer-a ",
            "customer_a",
            "1customer",
            "customer-a,customer-b",
            "a".repeat(65)
        }) {
            var request = request("/api/payments");
            request.addHeader(HEADER, invalid);
            assertRejected(request, "DEPLOYMENT_CODE_INVALID", 400);
        }

        var duplicated = request("/api/payments");
        duplicated.addHeader(HEADER, "customer-a");
        duplicated.addHeader(HEADER, "customer-a");
        assertRejected(duplicated, "DEPLOYMENT_CODE_INVALID", 400);
    }

    @Test
    void rejectsAnotherDeploymentWithForbidden() throws Exception {
        var request = request("/api/payments");
        request.addHeader(HEADER, "customer-b");
        assertRejected(request, "DEPLOYMENT_CODE_MISMATCH", 403);
    }

    @Test
    void exemptsPreflightProbesAndConfiguredStaticEntries() throws Exception {
        var preflight = request("/api/payments");
        preflight.setMethod("OPTIONS");
        preflight.addHeader("Origin", "https://merchant.example");
        preflight.addHeader("Access-Control-Request-Method", "POST");
        assertContinues(preflight);

        assertContinues(request("/actuator/health"));
        assertContinues(request("/actuator/health/readiness"));
        assertContinues(request("/actuator/prometheus"));
        assertContinues(request("/error"));
    }

    @Test
    void doesNotExemptBusinessPathsSharingTheActuatorPrefix() throws Exception {
        var business = request("/actuator/orders");
        assertRejected(business, "DEPLOYMENT_CODE_REQUIRED", 400);
    }

    @Test
    void honoursApplicationSpecificExemptions() throws Exception {
        var properties = new DeploymentCodeProperties();
        properties.setExcludedPaths(List.of("/api/callback/**"));
        var customized = new DeploymentCodeFilter(
                new DeploymentIdentity("customer-a"),
                properties,
                JsonMapper.builder().build());

        var callback = request("/api/callback/alipay");
        var chain = new AtomicInteger();
        customized.doFilter(callback, new MockHttpServletResponse(), (r, s) -> chain.incrementAndGet());

        assertThat(chain).hasValue(1);
        assertRejected(customized, request("/api/payments"), "DEPLOYMENT_CODE_REQUIRED", 400);
    }

    @Test
    void reusesTheRequestDecisionOnAsyncAndErrorDispatches() throws Exception {
        var request = request("/api/payments");
        request.addHeader(HEADER, "customer-a");
        var chain = new AtomicInteger();
        filter.doFilter(request, new MockHttpServletResponse(), (r, s) -> chain.incrementAndGet());

        request.setDispatcherType(DispatcherType.ASYNC);
        filter.doFilter(request, new MockHttpServletResponse(), (r, s) -> chain.incrementAndGet());
        request.setDispatcherType(DispatcherType.ERROR);
        filter.doFilter(request, new MockHttpServletResponse(), (r, s) -> chain.incrementAndGet());

        assertThat(chain).hasValue(3);
    }

    @Test
    void doesNotContinueTheChainAfterARejection() throws Exception {
        var request = request("/api/payments");
        request.addHeader(HEADER, "customer-b");
        var chain = new AtomicInteger();

        filter.doFilter(request, new MockHttpServletResponse(), (r, s) -> chain.incrementAndGet());

        assertThat(chain).hasValue(0);
    }

    @Test
    void propagatesDownstreamFailures() {
        var request = request("/api/payments");
        request.addHeader(HEADER, "customer-a");
        var failure = new ServletException("synthetic downstream failure");

        Assertions.assertThatThrownBy(() -> filter.doFilter(request, new MockHttpServletResponse(), (r, s) -> {
                    throw failure;
                }))
                .isSameAs(failure);
    }

    private void assertContinues(MockHttpServletRequest request) throws ServletException, IOException {
        var chain = new AtomicInteger();
        var response = new MockHttpServletResponse();
        filter.doFilter(request, response, (r, s) -> chain.incrementAndGet());
        assertThat(chain).hasValue(1);
        assertThat(response.getStatus()).isEqualTo(200);
    }

    private void assertRejected(MockHttpServletRequest request, String code, int status) throws Exception {
        assertRejected(filter, request, code, status);
    }

    private void assertRejected(DeploymentCodeFilter subject, MockHttpServletRequest request, String code, int status)
            throws Exception {
        var chain = new AtomicInteger();
        var response = new MockHttpServletResponse();

        subject.doFilter(request, response, (r, s) -> chain.incrementAndGet());

        assertThat(chain).hasValue(0);
        assertThat(response.getStatus()).isEqualTo(status);
        assertThat(response.getContentType()).startsWith("application/json");
        assertThat(response.getContentAsString()).contains(code);
    }
}
