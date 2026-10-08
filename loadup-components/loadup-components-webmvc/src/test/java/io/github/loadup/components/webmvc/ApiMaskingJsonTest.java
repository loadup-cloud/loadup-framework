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

import static org.assertj.core.api.Assertions.*;

import com.fasterxml.jackson.annotation.JsonView;
import io.github.loadup.commons.masking.MaskType;
import io.github.loadup.commons.masking.Masked;
import io.github.loadup.commons.result.PageDTO;
import io.github.loadup.commons.result.SuccessResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class ApiMaskingJsonTest {
    public record Contact(
            @Masked(MaskType.PHONE) String phone,
            @Masked(MaskType.EMAIL) String email) {}

    public record Invalid(@Masked(MaskType.PHONE) int phone) {}

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
        assertThat(masking.write(SuccessResponse.ofPage(PageDTO.of(List.of(contact), 1L, 1, 20))))
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
    void failsClosedForWrongAnnotationType() {
        var masking = new ApiMaskingJson(JsonMapper.builder().build());
        assertThatThrownBy(() -> masking.write(new Invalid(123))).isInstanceOf(RuntimeException.class);
    }
}
