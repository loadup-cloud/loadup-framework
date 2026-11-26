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
package io.github.loadup.components.kms;

import java.util.Map;
import tools.jackson.databind.JsonNode;

final class OpenBaoKmsSecretManager implements KmsSecretManager, AutoCloseable {
    private final OpenBaoClient client;

    OpenBaoKmsSecretManager(OpenBaoClient client) {
        this.client = client;
    }

    @Override
    public void create(String name, String value) {
        KmsSecretManager.validateName(name);
        KmsSecretManager.validateValue(value);
        JsonNode result = client.call("create", name, Map.of("options", Map.of("cas", 0), "data", data(value)), true);
        requireVersion(result.path("version"));
    }

    @Override
    public void update(String name, String value) {
        KmsSecretManager.validateName(name);
        KmsSecretManager.validateValue(value);
        var metadata = client.call("secret-metadata", name, null, true);
        int version = requireVersion(metadata.path("current_version"));
        JsonNode result =
                client.call("update", name, Map.of("options", Map.of("cas", version), "data", data(value)), true);
        requireVersion(result.path("version"));
    }

    @Override
    public String get(String name) {
        KmsSecretManager.validateName(name);
        String value = OpenBaoClient.text(client.call("get", name, null, true).path("data"), "value");
        try {
            KmsSecretManager.validateValue(value);
        } catch (IllegalArgumentException invalid) {
            throw new KmsException(KmsException.Code.INVALID_RESPONSE);
        }
        return value;
    }

    private static Map<String, String> data(String value) {
        return Map.of("value", value);
    }

    private static int requireVersion(JsonNode version) {
        if (!version.isIntegralNumber() || !version.canConvertToInt() || version.asInt() < 1) {
            throw new KmsException(KmsException.Code.INVALID_RESPONSE);
        }
        return version.asInt();
    }

    @Override
    public void close() {
        client.close();
    }
}
