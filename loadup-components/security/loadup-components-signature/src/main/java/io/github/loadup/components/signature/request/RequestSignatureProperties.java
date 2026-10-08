/*
 * #%L
 * LoadUp Components Signature
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
package io.github.loadup.components.signature.request;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Independent request protocol settings; local JCA configuration remains separate. */
@ConfigurationProperties("loadup.components.signature.request")
public record RequestSignatureProperties(Duration maxAge, Duration futureSkew, Integer maxPayloadBytes) {
    public RequestSignatureProperties {
        maxAge = maxAge == null ? Duration.ofMinutes(5) : maxAge;
        futureSkew = futureSkew == null ? Duration.ofSeconds(30) : futureSkew;
        maxPayloadBytes = maxPayloadBytes == null ? 1024 * 1024 : maxPayloadBytes;
        if (maxAge.compareTo(Duration.ofSeconds(1)) < 0
                || maxAge.compareTo(Duration.ofDays(1)) > 0
                || futureSkew.isNegative()
                || futureSkew.compareTo(Duration.ofMinutes(5)) > 0
                || maxPayloadBytes < 1
                || maxPayloadBytes > 16 * 1024 * 1024) {
            throw new IllegalArgumentException("Invalid request signature settings");
        }
    }

    void validateContent(RequestContent content) {
        if (content == null) throw new RequestSignatureException(RequestSignatureException.Code.MALFORMED);
        if (content.payloadSize() > maxPayloadBytes)
            throw new RequestSignatureException(RequestSignatureException.Code.PAYLOAD_TOO_LARGE);
    }
}
