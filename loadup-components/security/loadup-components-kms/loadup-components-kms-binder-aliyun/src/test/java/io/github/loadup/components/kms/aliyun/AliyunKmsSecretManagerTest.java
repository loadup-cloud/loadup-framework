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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.aliyun.kms20160120.Client;
import com.aliyun.kms20160120.models.CreateSecretRequest;
import com.aliyun.kms20160120.models.CreateSecretResponse;
import com.aliyun.kms20160120.models.CreateSecretResponseBody;
import com.aliyun.kms20160120.models.GetSecretValueRequest;
import com.aliyun.kms20160120.models.GetSecretValueResponse;
import com.aliyun.kms20160120.models.GetSecretValueResponseBody;
import com.aliyun.kms20160120.models.PutSecretValueRequest;
import com.aliyun.kms20160120.models.PutSecretValueResponse;
import com.aliyun.kms20160120.models.PutSecretValueResponseBody;
import com.aliyun.tea.TeaException;
import com.aliyun.teautil.models.RuntimeOptions;
import io.github.loadup.components.kms.KmsException;
import io.github.loadup.components.kms.aliyun.config.AliyunKmsProperties;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class AliyunKmsSecretManagerTest {
    private final Client client = mock(Client.class);
    private final AliyunKmsProperties properties = new AliyunKmsProperties("cn-hangzhou", null, null, null, null);
    private final AliyunKmsSecretManager secrets = new AliyunKmsSecretManager(client, properties);

    @Test
    void createsAndRotatesThroughTheSdkAndReadsTheCurrentVersionFromANewManager() throws Exception {
        var currentValue = new AtomicReference<String>();
        var currentVersion = new AtomicReference<String>();
        when(client.createSecretWithOptions(any(), any())).thenAnswer(invocation -> {
            CreateSecretRequest request = invocation.getArgument(0);
            RuntimeOptions runtime = invocation.getArgument(1);
            assertThat(request.getSecretType()).isEqualTo("Generic");
            assertThat(request.getSecretDataType()).isEqualTo("text");
            assertThat(request.getEncryptionKeyId()).isNull();
            assertThat(runtime.getAutoretry()).isFalse();
            currentValue.set(request.getSecretData());
            currentVersion.set(request.getVersionId());
            return new CreateSecretResponse()
                    .setBody(new CreateSecretResponseBody()
                            .setSecretName(request.getSecretName())
                            .setVersionId(request.getVersionId()));
        });
        when(client.putSecretValueWithOptions(any(), any())).thenAnswer(invocation -> {
            PutSecretValueRequest request = invocation.getArgument(0);
            assertThat(request.getSecretDataType()).isEqualTo("text");
            assertThat(request.getVersionStages()).isNull();
            currentValue.set(request.getSecretData());
            currentVersion.set(request.getVersionId());
            return new PutSecretValueResponse()
                    .setBody(new PutSecretValueResponseBody()
                            .setSecretName(request.getSecretName())
                            .setVersionId(request.getVersionId()));
        });
        when(client.getSecretValueWithOptions(any(), any())).thenAnswer(invocation -> {
            GetSecretValueRequest request = invocation.getArgument(0);
            assertThat(request.getVersionId()).isNull();
            assertThat(request.getVersionStage()).isNull();
            return new GetSecretValueResponse()
                    .setBody(new GetSecretValueResponseBody()
                            .setSecretName(request.getSecretName())
                            .setSecretType("Generic")
                            .setSecretDataType("text")
                            .setSecretData(currentValue.get()));
        });

        secrets.create("payments-token", "初始值");
        assertThat(secrets.get("payments-token")).isEqualTo("初始值");
        String initialVersion = currentVersion.get();
        secrets.update("payments-token", "轮换值");
        assertThat(currentVersion.get()).isNotBlank().isNotEqualTo(initialVersion);
        assertThat(new AliyunKmsSecretManager(client, properties).get("payments-token"))
                .isEqualTo("轮换值");
    }

    @Test
    void rejectsInvalidInputBeforeSendingAnyRequest() {
        assertThatThrownBy(() -> secrets.get("../other")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> secrets.create("secret", "")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> secrets.update("secret", "值".repeat(10241)))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(client);
    }

    @Test
    void invalidResponsesAndTransportErrorsDoNotExposeValues() throws Exception {
        when(client.getSecretValueWithOptions(any(), any()))
                .thenReturn(new GetSecretValueResponse()
                        .setBody(new GetSecretValueResponseBody()
                                .setSecretName("different-secret")
                                .setSecretType("Generic")
                                .setSecretDataType("text")
                                .setSecretData("synthetic-sensitive-data")));
        assertThatThrownBy(() -> secrets.get("secret"))
                .isInstanceOf(KmsException.class)
                .hasMessage("KMS operation failed: INVALID_RESPONSE")
                .hasNoCause();

        when(client.putSecretValueWithOptions(any(), any()))
                .thenThrow(new IllegalStateException("synthetic-sensitive-data"));
        assertThatThrownBy(() -> secrets.update("secret", "synthetic-sensitive-data"))
                .isInstanceOf(KmsException.class)
                .hasMessage("KMS operation failed: TRANSPORT")
                .hasNoCause();
        verify(client).putSecretValueWithOptions(any(), any());
    }

    @Test
    void mapsMissingSecretsWithoutExposingTheServiceError() throws Exception {
        var missing = new TeaException();
        missing.setStatusCode(404);
        missing.setMessage("synthetic-service-error");
        when(client.getSecretValueWithOptions(any(), any())).thenThrow(missing);
        assertThatThrownBy(() -> secrets.get("missing"))
                .isInstanceOf(KmsException.class)
                .hasMessage("KMS operation failed: NOT_FOUND")
                .hasNoCause();
    }
}
