/*
 * Copyright (C) 2025-2026 LoadUp Cloud
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
package io.github.loadup.components.http;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;

import io.github.loadup.commons.enums.CommonResultCodeEnum;
import io.github.loadup.commons.result.Result;
import io.github.loadup.commons.result.SuccessResponse;
import org.junit.jupiter.api.Test;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

class HttpServiceResponsesTest {
    private SuccessResponse<String> response(String code, String status, String data) {
        String json = "{\"result\":{\"code\":\"" + code + "\",\"status\":\"" + status
                + "\",\"message\":\"sensitive remote detail\"},\"data\":" + data + "}";
        return JsonMapper.builder().build().readValue(json, new TypeReference<>() {});
    }

    @Test
    void successRequiresBothMetadataFieldsAndNonNullData() {
        assertThat(HttpServiceResponses.requiredData(SuccessResponse.of("value")))
                .isEqualTo("value");
        assertThatThrownBy(() -> HttpServiceResponses.requiredData(response("SUCCESS", "F", "\"value\"")))
                .isInstanceOf(HttpCallException.class);
        assertThatThrownBy(() -> HttpServiceResponses.requiredData(response("NOT_FOUND", "S", "\"value\"")))
                .isInstanceOf(HttpCallException.class);
        assertThatThrownBy(() -> HttpServiceResponses.requiredData(SuccessResponse.of(null)))
                .isInstanceOf(HttpCallException.class);
        assertThatThrownBy(() -> HttpServiceResponses.requiredData(null)).isInstanceOf(HttpCallException.class);
    }

    @Test
    void onlyWellFormedFailureMetadataCanBeOfferedToACallersWhitelist() {
        var failure = catchThrowable(() -> HttpServiceResponses.requiredData(
                new SuccessResponse<>(Result.buildFailure(CommonResultCodeEnum.NOT_FOUND), null)));
        assertThat(HttpServiceResponses.resultCode(failure)).isEqualTo("NOT_FOUND");
        assertThat(HttpServiceResponses.resultCode(new IllegalArgumentException("loadup-result-code=NOT_FOUND")))
                .isEmpty();
        var malformed = catchThrowable(() -> HttpServiceResponses.requiredData(response("NOT_FOUND", "F", "\"data\"")));
        assertThat(HttpServiceResponses.resultCode(malformed)).isEmpty();
        var unknown = catchThrowable(() -> HttpServiceResponses.requiredData(response("NOT_FOUND", "U", "null")));
        assertThat(HttpServiceResponses.resultCode(unknown)).isEmpty();
        var invalidCode = catchThrowable(() -> HttpServiceResponses.requiredData(response("unsafe-code", "F", "null")));
        assertThat(HttpServiceResponses.resultCode(invalidCode)).isEmpty();
    }

    @Test
    void remoteDescriptionsAndBodiesAreNeverRetainedInExceptions() {
        var failure = catchThrowable(() -> HttpServiceResponses.requiredData(response("NOT_FOUND", "F", "null")));
        assertThat(failure).isInstanceOf(HttpCallException.class).hasNoCause();
        assertThat(failure.toString()).doesNotContain("sensitive remote detail");
    }
}
