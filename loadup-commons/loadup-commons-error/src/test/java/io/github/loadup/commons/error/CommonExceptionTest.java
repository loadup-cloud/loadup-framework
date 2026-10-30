/*
 * #%L
 * LoadUp Common Error
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
package io.github.loadup.commons.error;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import io.github.loadup.commons.enums.CommonResultCodeEnum;
import io.github.loadup.commons.result.ResultCode;
import org.junit.jupiter.api.Test;

class CommonExceptionTest {
    @Test
    void retainsCauseWithoutUsingItsMessageAsPublicOutput() {
        Throwable cause = new IllegalStateException("private-password-and-backend-path");
        var failure = new CommonException(CommonResultCodeEnum.SYS_ERROR, cause);
        assertThat(failure.getCause()).isSameAs(cause);
        assertThat(failure.getMessage()).isEqualTo(CommonResultCodeEnum.SYS_ERROR.getMessage());
        assertThat(failure.toString()).contains("SYS_ERROR").doesNotContain(cause.getMessage());
    }

    @Test
    void rejectsSuccessCodesAndUndeclaredErrorObjects() {
        assertThatIllegalArgumentException().isThrownBy(() -> new CommonException(CommonResultCodeEnum.SUCCESS));
        ResultCode adHoc = new ResultCode() {
            public String getCode() {
                return "AD_HOC";
            }

            public String getStatus() {
                return "F";
            }

            public String getMessage() {
                return "Free text";
            }
        };
        assertThatIllegalArgumentException().isThrownBy(() -> new CommonException(adHoc));
    }
}
