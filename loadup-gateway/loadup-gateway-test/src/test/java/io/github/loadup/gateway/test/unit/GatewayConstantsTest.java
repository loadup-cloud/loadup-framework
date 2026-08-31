/*-
 * #%L
 * LoadUp Gateway Test
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
package io.github.loadup.gateway.test.unit;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.loadup.gateway.facade.constants.GatewayConstants;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("GatewayConstants")
class GatewayConstantsTest {

    @Test
    @DisplayName("Protocol has HTTP, RPC, BEAN")
    void protocolConstants() {
        assertThat(GatewayConstants.Protocol.HTTP).isEqualTo("HTTP");
        assertThat(GatewayConstants.Protocol.RPC).isEqualTo("RPC");
        assertThat(GatewayConstants.Protocol.BEAN).isEqualTo("BEAN");
    }

    @Test
    @DisplayName("Storage has FILE, DATABASE")
    void storageConstants() {
        assertThat(GatewayConstants.Storage.FILE).isEqualTo("FILE");
        assertThat(GatewayConstants.Storage.DATABASE).isEqualTo("DATABASE");
    }

    @Test
    @DisplayName("ContentType constants")
    void contentTypeConstants() {
        assertThat(GatewayConstants.ContentType.JSON).isEqualTo("application/json;charset=UTF-8");
        assertThat(GatewayConstants.ContentType.FORM).isEqualTo("application/x-www-form-urlencoded");
        assertThat(GatewayConstants.ContentType.XML).isEqualTo("application/xml");
    }

    @Test
    @DisplayName("HttpMethod constants")
    void httpMethodConstants() {
        assertThat(GatewayConstants.HttpMethod.GET).isEqualTo("GET");
        assertThat(GatewayConstants.HttpMethod.POST).isEqualTo("POST");
        assertThat(GatewayConstants.HttpMethod.PUT).isEqualTo("PUT");
        assertThat(GatewayConstants.HttpMethod.DELETE).isEqualTo("DELETE");
    }

    @Test
    @DisplayName("Status codes")
    void statusCodes() {
        assertThat(GatewayConstants.Status.SUCCESS).isEqualTo(200);
        assertThat(GatewayConstants.Status.BAD_REQUEST).isEqualTo(400);
        assertThat(GatewayConstants.Status.NOT_FOUND).isEqualTo(404);
        assertThat(GatewayConstants.Status.INTERNAL_ERROR).isEqualTo(500);
    }
}
