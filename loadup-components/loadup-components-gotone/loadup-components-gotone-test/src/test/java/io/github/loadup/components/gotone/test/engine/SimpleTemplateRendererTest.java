/*-
 * #%L
 * Loadup Gotone Test
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
package io.github.loadup.components.gotone.test.engine;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.loadup.components.gotone.engine.SimpleTemplateRenderer;
import java.util.Map;
import org.junit.jupiter.api.Test;

class SimpleTemplateRendererTest {

    private final SimpleTemplateRenderer renderer = new SimpleTemplateRenderer();

    @Test
    void replacesPlaceholders() {
        String rendered =
                renderer.render("Hello ${userName}, order ${orderNo}", Map.of("userName", "Alice", "orderNo", "42"));

        assertThat(rendered).isEqualTo("Hello Alice, order 42");
    }

    @Test
    void missingPlaceholderRendersEmpty() {
        String rendered = renderer.render("Hi ${missing}", Map.of());

        assertThat(rendered).isEqualTo("Hi ");
    }

    @Test
    void nullTemplateRendersEmpty() {
        assertThat(renderer.render(null, Map.of())).isEmpty();
    }
}
