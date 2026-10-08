package io.github.loadup.components.gotone.engine;

import io.github.loadup.components.gotone.template.TemplateRenderer;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Minimal {@code ${placeholder}} template renderer.
 *
 * <p>Registered by default when no other {@link TemplateRenderer} bean exists. Integrators that
 * need richer rendering (SpEL, Thymeleaf, FreeMarker) can provide their own renderer bean.
 */
public class SimpleTemplateRenderer implements TemplateRenderer {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\$\\{([^}]+)}");

    @Override
    public String render(String template, Map<String, Object> params) {
        if (template == null) {
            return "";
        }
        Matcher matcher = PLACEHOLDER.matcher(template);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            Object value = params.get(matcher.group(1).trim());
            matcher.appendReplacement(result, Matcher.quoteReplacement(value == null ? "" : String.valueOf(value)));
        }
        matcher.appendTail(result);
        return result.toString();
    }
}
