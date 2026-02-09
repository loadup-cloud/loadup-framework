package io.github.loadup.components.gotone.core.processor;

/*-
 * #%L
 * loadup-components-gotone-core
 * %%
 * Copyright (C) 2026 LoadUp Cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;

/**
 * Template Processor.
 *
 * <p>Renders template content by replacing placeholders with actual values.
 * Supports ${varName} syntax.
 */
@Slf4j
public class TemplateProcessor {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{([^}]+)}");

    /**
     * Render template with parameters.
     *
     * @param template template string with ${...} placeholders
     * @param params parameter map
     * @return rendered content
     */
    public String render(String template, Map<String, Object> params) {
        if (template == null || template.isEmpty()) {
            return template;
        }

        if (params == null || params.isEmpty()) {
            return template;
        }

        StringBuffer result = new StringBuffer();
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);

        while (matcher.find()) {
            String key = matcher.group(1);
            Object value = params.get(key);
            String replacement = value != null ? value.toString() : "";
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * Render title template.
     *
     * @param titleTemplate title template string
     * @param params parameter map
     * @return rendered title
     */
    public String renderTitle(String titleTemplate, Map<String, Object> params) {
        return render(titleTemplate, params);
    }
}
