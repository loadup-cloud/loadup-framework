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
package io.github.loadup.gateway.test.webmvcapp;

import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * Backend bean targeted by the {@code bean://} route used in integration tests.
 */
@Service("demoEchoService")
public class DemoEchoService {

    public String echo(Map<String, Object> body) {
        Object name = body == null ? null : body.get("name");
        return "echo:" + (name == null ? "null" : name);
    }
}
