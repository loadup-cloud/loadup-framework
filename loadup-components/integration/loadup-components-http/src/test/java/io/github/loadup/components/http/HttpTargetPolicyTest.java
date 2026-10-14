/*
 * #%L
 * LoadUp Http
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
package io.github.loadup.components.http;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.InetAddress;
import java.net.UnknownHostException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class HttpTargetPolicyTest {
    @ParameterizedTest
    @ValueSource(
            strings = {
                "0.0.0.0",
                "0.1.2.3",
                "127.0.0.1",
                "169.254.1.1",
                "10.1.2.3",
                "172.16.0.1",
                "192.168.1.1",
                "224.0.0.1",
                "240.0.0.1",
                "100.64.0.1",
                "100.127.255.254",
                "198.18.0.1",
                "198.19.255.254",
                "::",
                "::1",
                "fe80::1",
                "fec0::1",
                "ff02::1",
                "fc00::1",
                "fdff::1"
            })
    void rejectsNonPublicDestinationsIncludingSharedAndBenchmarkNetworks(String literal) throws Exception {
        assertThat(HttpTargetPolicy.blocked(InetAddress.getByName(literal))).isTrue();
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "1.1.1.1",
                "8.8.8.8",
                "100.63.255.254",
                "100.128.0.1",
                "198.17.0.1",
                "198.20.0.1",
                "2001:4860:4860::8888",
                "fe00::1"
            })
    void permitsPublicDestinationsAndAdjacentUnblockedRanges(String literal) throws Exception {
        assertThat(HttpTargetPolicy.blocked(InetAddress.getByName(literal))).isFalse();
    }

    @Test
    void resolutionRejectsLoopbackUnlessPrivateTargetsAreExplicitlyAllowed() throws Exception {
        assertThatThrownBy(() -> new HttpTargetPolicy(false).resolve("127.0.0.1"))
                .isInstanceOf(UnknownHostException.class)
                .hasMessage("HTTP target address is not allowed");
        assertThat(new HttpTargetPolicy(true).resolve("127.0.0.1")).containsExactly(InetAddress.getByName("127.0.0.1"));
    }

    @Test
    void publicLiteralResolutionIsAllowedWithoutExternalDns() throws Exception {
        assertThat(new HttpTargetPolicy(false).resolve("1.1.1.1")).containsExactly(InetAddress.getByName("1.1.1.1"));
    }
}
