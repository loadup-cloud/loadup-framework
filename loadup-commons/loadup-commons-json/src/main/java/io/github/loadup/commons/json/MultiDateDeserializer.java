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

import java.time.DateTimeException;
import java.util.Date;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

/** Reads strict date/time text or an explicit epoch-millisecond JSON integer. */
public class MultiDateDeserializer extends StdDeserializer<Date> {
    public MultiDateDeserializer() {
        super(Date.class);
    }

    @Override
    public Date deserialize(JsonParser parser, DeserializationContext context) throws JacksonException {
        if (parser.hasToken(JsonToken.VALUE_NUMBER_INT)) {
            return new Date(parser.getLongValue());
        }
        if (!parser.hasToken(JsonToken.VALUE_STRING)) {
            return (Date) context.handleUnexpectedToken(Date.class, parser);
        }
        String text = parser.getString().trim();
        try {
            return Date.from(TemporalFormats.instant(text, context.getTimeZone().toZoneId()));
        } catch (DateTimeException | IllegalArgumentException exception) {
            throw context.weirdStringException(text, Date.class, "Unsupported or invalid date format");
        }
    }
}
