package io.github.loadup.components.extension.test.service.impl;

/*-
 * #%L
 * loadup-components-extension
 * %%
 * Copyright (C) 2025 LoadUp Cloud
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

import io.github.loadup.components.extension.annotation.Extension;
import io.github.loadup.components.extension.test.service.GreetingService;
import org.springframework.stereotype.Service;

@Service
@Extension
public class DefaultGreeting implements GreetingService {
    @Override
    public String greet() {
        return "Default";
    }
}
