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

import java.util.List;
import java.util.Map;

/** Values are encoded as URI variables; byte[] and String bodies are sent without JSON wrapping. */
public record HttpCall(
        Map<String, ?> pathVariables, Map<String, List<String>> query, Map<String, String> headers, Object body) {
    public HttpCall {
        pathVariables = pathVariables == null ? Map.of() : Map.copyOf(pathVariables);
        query = query == null
                ? Map.of()
                : query.entrySet().stream()
                        .collect(java.util.stream.Collectors.toUnmodifiableMap(
                                Map.Entry::getKey, entry -> List.copyOf(entry.getValue())));
        headers = headers == null ? Map.of() : Map.copyOf(headers);
    }

    public static HttpCall empty() {
        return new HttpCall(null, null, null, null);
    }
}
