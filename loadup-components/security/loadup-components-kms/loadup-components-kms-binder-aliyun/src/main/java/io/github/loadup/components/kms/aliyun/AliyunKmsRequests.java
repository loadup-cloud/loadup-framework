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
package io.github.loadup.components.kms.aliyun;

import com.aliyun.tea.TeaException;
import com.aliyun.teautil.models.RuntimeOptions;
import io.github.loadup.components.kms.KmsException;
import io.github.loadup.components.kms.KmsException.Code;
import io.github.loadup.components.kms.aliyun.config.AliyunKmsProperties;
import java.util.Objects;

final class AliyunKmsRequests {
    private final AliyunKmsProperties properties;

    AliyunKmsRequests(AliyunKmsProperties properties) {
        this.properties = Objects.requireNonNull(properties);
    }

    RuntimeOptions runtime() {
        return new RuntimeOptions()
                .setAutoretry(false)
                .setIgnoreSSL(false)
                .setConnectTimeout((int) properties.connectTimeout().toMillis())
                .setReadTimeout((int) properties.readTimeout().toMillis());
    }

    static <T> T cloud(SdkCall<T> call) {
        T result;
        try {
            result = call.invoke();
        } catch (TeaException failure) {
            throw new KmsException(errorCode(failure));
        } catch (InterruptedException failure) {
            Thread.currentThread().interrupt();
            throw new KmsException(Code.TRANSPORT);
        } catch (Exception failure) {
            throw new KmsException(Code.TRANSPORT);
        }
        return requireResponse(result);
    }

    private static Code errorCode(TeaException failure) {
        String code = Objects.toString(failure.getCode(), "");
        int status = failure.getStatusCode() == null ? 0 : failure.getStatusCode();
        if (status == 401
                || code.startsWith("InvalidAccessKeyId")
                || code.startsWith("InvalidSecurityToken")
                || code.equals("SignatureDoesNotMatch")) {
            return Code.AUTHENTICATION;
        }
        return switch (status) {
            case 400 -> Code.INVALID_REQUEST;
            case 403 -> Code.ACCESS_DENIED;
            case 404 -> Code.NOT_FOUND;
            case 409, 429 -> Code.UNAVAILABLE;
            default -> status >= 500 ? Code.UNAVAILABLE : Code.TRANSPORT;
        };
    }

    static <T> T requireResponse(T value) {
        if (value == null) {
            throw new KmsException(Code.INVALID_RESPONSE);
        }
        return value;
    }

    @FunctionalInterface
    interface SdkCall<T> {
        T invoke() throws Exception;
    }
}
