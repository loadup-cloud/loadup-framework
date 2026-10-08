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

import java.net.InetAddress;
import java.net.UnknownHostException;
import org.apache.hc.client5.http.SystemDefaultDnsResolver;

/** Enforces address policy on every connection's DNS resolution, not just configuration loading. */
final class HttpTargetPolicy extends SystemDefaultDnsResolver {
    private final boolean allowPrivate;

    HttpTargetPolicy(boolean allowPrivate) {
        this.allowPrivate = allowPrivate;
    }

    @Override
    public InetAddress[] resolve(String host) throws UnknownHostException {
        InetAddress[] addresses = super.resolve(host);
        if (!allowPrivate)
            for (InetAddress address : addresses) {
                if (blocked(address)) throw new UnknownHostException("HTTP target address is not allowed");
            }
        return addresses;
    }

    static boolean blocked(InetAddress address) {
        if (address.isAnyLocalAddress()
                || address.isLoopbackAddress()
                || address.isLinkLocalAddress()
                || address.isSiteLocalAddress()
                || address.isMulticastAddress()) return true;
        byte[] bytes = address.getAddress();
        if (bytes.length == 16) return (bytes[0] & 0xfe) == 0xfc;
        int a = bytes[0] & 255;
        int b = bytes[1] & 255;
        return a == 0 || a >= 224 || (a == 100 && b >= 64 && b <= 127) || (a == 198 && (b == 18 || b == 19));
    }
}
