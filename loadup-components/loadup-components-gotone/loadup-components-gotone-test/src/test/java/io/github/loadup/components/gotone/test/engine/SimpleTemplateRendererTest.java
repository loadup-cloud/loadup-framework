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
