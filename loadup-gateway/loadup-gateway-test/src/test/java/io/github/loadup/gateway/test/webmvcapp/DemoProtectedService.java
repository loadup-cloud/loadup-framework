package io.github.loadup.gateway.test.webmvcapp;

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

import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

/**
 * Backend bean protected with method-level Spring Security authorization, targeted by the
 * {@code bean://} route used to prove that gateway authentication flows into
 * {@code @PreAuthorize} on business methods.
 */
@Service("demoProtectedService")
public class DemoProtectedService {

    @PreAuthorize("hasAuthority('user:write')")
    public String secureEcho(Map<String, Object> body) {
        Object name = body == null ? null : body.get("name");
        return "secure:" + (name == null ? "null" : name);
    }
}
