/*
 * #%L
 * LoadUp Web MVC
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
package io.github.loadup.components.webmvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.annotation.JsonView;
import io.github.loadup.commons.masking.MaskType;
import io.github.loadup.commons.masking.Masked;
import io.github.loadup.commons.result.PageDTO;
import io.github.loadup.commons.result.PageResponse;
import io.github.loadup.commons.result.SuccessResponse;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

class ApiMaskingJsonTest {
    static Stream<Object> invalidDeclarations() {
        return Stream.of(
                new NegativePrefix("synthetic"),
                new NegativeSuffix("synthetic"),
                new OversizedPrefix("synthetic"),
                new OversizedSuffix("synthetic"),
                new NonCustomPrefix("13812345678"),
                new NonCustomSuffix("13812345678"));
    }

    @Test
    void masksNestedRecordsEnvelopesAndGettersWithoutMutatingSource() {
        var original = JsonMapper.builder().build();
        var masking = new ApiMaskingJson(original);
        var contact = new Contact("13812345678", "alice@example.com");
        var json = masking.write(SuccessResponse.of(List.of(contact)));
        assertThat(json)
                .contains("138****5678", "a****@example.com", "\"result\"", "\"data\"")
                .doesNotContain("13812345678", "alice@example.com");
        assertThat(masking.write(new GetterContact())).contains("138****5678");
        assertThat(masking.write(new FieldContact())).contains("138****5678");
        assertThat(masking.write(PageResponse.of(PageDTO.of(List.of(contact), 1L, 1, 20))))
                .contains("138****5678", "\"pageInfo\"")
                .doesNotContain("13812345678");
        assertThat(original.writeValueAsString(contact)).contains("13812345678", "alice@example.com");
        assertThat(contact.phone()).isEqualTo("13812345678");
        assertThat(masking.mapper()
                        .readValue("{\"phone\":\"13812345678\",\"email\":\"alice@example.com\"}", Contact.class))
                .isEqualTo(contact);
    }

    @Test
    void retainsJacksonViews() {
        var mapper = new ApiMaskingJson(JsonMapper.builder().build()).mapper();
        assertThat(mapper.writerWithView(PublicView.class).writeValueAsString(new Viewed("13812345678", "hidden")))
                .contains("138****5678")
                .doesNotContain("hidden", "13812345678");
    }

    @Test
    void pageResponseKeepsFlatEnvelopeAndMasksMappedEntries() {
        var masking = new ApiMaskingJson(JsonMapper.builder().build());
        var page = PageDTO.of(List.of(new Contact("13812345678", "alice@example.com")), 41L, 2, 20);
        var response = PageResponse.of(page.map(contact -> new Contact(contact.phone(), contact.email())));
        var tree = masking.mapper().readTree(masking.write(response));
        assertThat(tree.get("result").get("status").asString()).isEqualTo("S");
        assertThat(tree.get("data").isArray()).isTrue();
        assertThat(tree.get("data").get(0).get("phone").asString()).isEqualTo("138****5678");
        assertThat(tree.get("pageInfo").get("totalCount").asLong()).isEqualTo(41L);
        assertThat(tree.get("pageInfo").get("pageIndex").asLong()).isEqualTo(2L);
        assertThat(tree.get("pageInfo").get("pageSize").asLong()).isEqualTo(20L);
        assertThat(tree.get("pageInfo").get("totalPages").asLong()).isEqualTo(3L);
        assertThat(masking.write(PageResponse.of(PageDTO.of(List.of(), 0L, 1, 20))))
                .contains("\"data\":[]", "\"pageInfo\"");
    }

    @Test
    void failsClosedForWrongAnnotationType() {
        var masking = new ApiMaskingJson(JsonMapper.builder().build());
        assertThatThrownBy(() -> masking.write(new Invalid(123))).isInstanceOf(RuntimeException.class);
    }

    @ParameterizedTest
    @MethodSource("invalidDeclarations")
    void rejectsInvalidMaskLengthsAndNonCustomOverridesWithoutReturningPlaintext(Object model) {
        var masking = new ApiMaskingJson(JsonMapper.builder().build());
        assertThatThrownBy(() -> masking.write(model)).isInstanceOf(RuntimeException.class);
    }

    @Test
    void acceptsMaximumCustomLengthsAndPreservesNullFields() {
        var original = JsonMapper.builder().build();
        var masking = new ApiMaskingJson(original);
        var value = new CustomBoundary("a".repeat(70));
        assertThat(original.readTree(masking.write(value)).get("value").asString())
                .isEqualTo("a".repeat(32) + "*".repeat(6) + "a".repeat(32));
        assertThat(original.readTree(masking.write(new CustomBoundary(null)))
                        .get("value")
                        .isNull())
                .isTrue();
        assertThat(value.value()).isEqualTo("a".repeat(70));
    }

    @Test
    void requiresAJsonMapperToKeepTheApplicationMapperIndependent() {
        assertThatThrownBy(() -> new ApiMaskingJson(new ObjectMapper()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("API masking requires a Jackson 3 JsonMapper");
    }

    public record Contact(
            @Masked(MaskType.PHONE) String phone,
            @Masked(MaskType.EMAIL) String email) {}

    public record Invalid(@Masked(MaskType.PHONE) int phone) {}

    public record NegativePrefix(
            @Masked(value = MaskType.CUSTOM, prefix = -1) String value) {}

    public record NegativeSuffix(
            @Masked(value = MaskType.CUSTOM, suffix = -1) String value) {}

    public record OversizedPrefix(
            @Masked(value = MaskType.CUSTOM, prefix = 33) String value) {}

    public record OversizedSuffix(
            @Masked(value = MaskType.CUSTOM, suffix = 33) String value) {}

    public record NonCustomPrefix(
            @Masked(value = MaskType.PHONE, prefix = 1) String value) {}

    public record NonCustomSuffix(
            @Masked(value = MaskType.PHONE, suffix = 1) String value) {}

    public record CustomBoundary(
            @Masked(value = MaskType.CUSTOM, prefix = 32, suffix = 32)
            String value) {}

    public static class PublicView {}

    public static class InternalView extends PublicView {}

    public record Viewed(
            @JsonView(PublicView.class) @Masked(MaskType.PHONE)
            String phone,

            @JsonView(InternalView.class) String internal) {}

    public static class GetterContact {
        @Masked(MaskType.PHONE)
        public String getPhone() {
            return "13812345678";
        }
    }

    public static class FieldContact {
        @Masked(MaskType.PHONE)
        private final String phone = "13812345678";

        public String getPhone() {
            return phone;
        }
    }
}
