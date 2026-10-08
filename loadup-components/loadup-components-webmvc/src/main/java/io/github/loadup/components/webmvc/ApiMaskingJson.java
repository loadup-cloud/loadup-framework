/*
 * #%L
 * LoadUp Web MVC
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
package io.github.loadup.components.webmvc;

import io.github.loadup.commons.masking.MaskType;
import io.github.loadup.commons.masking.Masked;
import io.github.loadup.commons.masking.Masking;
import java.util.List;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.BeanDescription;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationConfig;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.BeanPropertyWriter;
import tools.jackson.databind.ser.ValueSerializerModifier;
import tools.jackson.databind.ser.std.StdSerializer;

/** A private response mapper, never a global ObjectMapper or JacksonModule bean. */
public final class ApiMaskingJson {
    private final JsonMapper mapper;

    public ApiMaskingJson(ObjectMapper source) {
        if (!(source instanceof JsonMapper json))
            throw new IllegalArgumentException("API masking requires a Jackson 3 JsonMapper");
        var module = new SimpleModule("loadup-api-masking").setSerializerModifier(new MaskedProperties());
        mapper = json.rebuild().addModule(module).build();
    }

    JsonMapper mapper() {
        return mapper;
    }

    public String write(Object value) {
        return mapper.writeValueAsString(value);
    }

    private static final class MaskedProperties extends ValueSerializerModifier {
        @Override
        public List<BeanPropertyWriter> changeProperties(
                SerializationConfig config, BeanDescription.Supplier bean, List<BeanPropertyWriter> properties) {
            for (var property : properties) {
                Masked rule = property.getAnnotation(Masked.class);
                if (rule == null) continue;
                if (property.getType().getRawClass() != String.class
                        || rule.prefix() < 0
                        || rule.suffix() < 0
                        || rule.prefix() > 32
                        || rule.suffix() > 32
                        || (rule.value() != MaskType.CUSTOM && (rule.prefix() != 0 || rule.suffix() != 0))) {
                    throw new IllegalArgumentException("Invalid @Masked declaration");
                }
                property.assignSerializer(new MaskedString(rule));
            }
            return properties;
        }
    }

    private static final class MaskedString extends StdSerializer<Object> {
        private final MaskType type;
        private final int prefix;
        private final int suffix;

        MaskedString(Masked rule) {
            super(Object.class);
            type = rule.value();
            prefix = rule.prefix();
            suffix = rule.suffix();
        }

        @Override
        public void serialize(Object value, JsonGenerator generator, SerializationContext context) {
            if (!(value instanceof String text)) throw new IllegalArgumentException("Masked value must be a String");
            generator.writeString(
                    type == MaskType.CUSTOM ? Masking.keep(text, prefix, suffix) : Masking.mask(text, type));
        }
    }
}
