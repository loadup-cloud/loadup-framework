package io.github.loadup.components.gotone.template;

import java.util.Map;

public interface TemplateRenderer {
    String render(String template, Map<String, Object> params);
}
