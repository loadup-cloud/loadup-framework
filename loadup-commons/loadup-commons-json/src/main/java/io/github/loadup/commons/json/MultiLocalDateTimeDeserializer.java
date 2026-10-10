/*
 * #%L
 * LoadUp Common JSON
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
package io.github.loadup.commons.json;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.ext.javatime.deser.LocalDateTimeDeserializer;

/** Reads strict local temporal text; explicit JsonFormat patterns use Jackson's reader. */
public class MultiLocalDateTimeDeserializer extends LocalDateTimeDeserializer {
    public MultiLocalDateTimeDeserializer() {
        super(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    @Override
    public ValueDeserializer<?> createContextual(DeserializationContext context, BeanProperty property) {
        JsonFormat.Value format = findFormatOverrides(context, property, handledType());
        // Jackson's default ANY shape must not replace this multi-format reader.
        return format != null && format.hasPattern() ? super.createContextual(context, property) : this;
    }

    @Override
    public LocalDateTime deserialize(JsonParser parser, DeserializationContext context) throws JacksonException {
        if (!parser.hasToken(JsonToken.VALUE_STRING)) {
            return (LocalDateTime) context.handleUnexpectedToken(LocalDateTime.class, parser);
        }
        return _fromString(parser, context, parser.getString());
    }

    @Override
    protected LocalDateTime _fromString(JsonParser parser, DeserializationContext context, String value)
            throws JacksonException {
        String text = value.trim();
        try {
            return TemporalFormats.localDateTime(text);
        } catch (DateTimeParseException exception) {
            throw context.weirdStringException(
                    text, LocalDateTime.class, "Unsupported or invalid local temporal format");
        }
    }
}
