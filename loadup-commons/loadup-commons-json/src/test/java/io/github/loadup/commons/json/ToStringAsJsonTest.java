/*
 * #%L
 * LoadUp Common JSON
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
package io.github.loadup.commons.json;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.loadup.commons.masking.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class ToStringAsJsonTest {
    private final JsonMapper mapper = JsonMapper.builder().build();

    @Test
    void protectsCredentialsAndHonorsMaskingOnRecords() {
        String json = ToStringAsJson.reflectionToString(new Credentials("secret-value", "13800138000"));
        var node = mapper.readTree(json);
        assertThat(node.get("password").asString()).isEqualTo("******");
        assertThat(json).doesNotContain("secret-value", "13800138000");
    }

    @Test
    void preservesValidJsonForInheritedFieldsCyclesAndLimits() {
        var value = new Child();
        value.self = value;
        value.entries = Collections.nCopies(1000, "x".repeat(1000));
        String json = ToStringAsJson.reflectionToString(value);
        assertThat(mapper.readTree(json).get("parent").asString()).isEqualTo("parent-value");
        assertThat(json).contains("<cycle>", "<truncated>").hasSizeLessThan(20000);
    }

    @Test
    void neverInvokesResourceGetters() {
        assertThat(mapper.readTree(ToStringAsJson.reflectionToString(new Resource()))
                        .get("id")
                        .asString())
                .isEqualTo("id");
    }

    @Test
    void diagnosticProtectionDoesNotMaskAnAuthorizedHttpResponse() {
        var value = new Plaintext("person@example.com");
        assertThat(mapper.writeValueAsString(value)).contains("person@example.com");
        assertThat(ToStringAsJson.reflectionToString(java.util.Map.of("data", value)))
                .doesNotContain("person@example.com");
    }

    private record Plaintext(@DiagnosticHidden String email) {}

    private record Credentials(
            String password, @Masked(MaskType.PHONE) String phone) {}

    private static class Parent {
        private String parent = "parent-value";
    }

    private static class Child extends Parent {
        private Child self;
        private List<String> entries;
    }

    private static class Resource {
        private String id = "id";

        public String getContent() {
            throw new AssertionError("Getter called");
        }
    }
}
