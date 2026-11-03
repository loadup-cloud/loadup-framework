/*
 * Copyright (C) 2025-2026 LoadUp Cloud
 *
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
 */
package io.github.loadup.components.cache.codec;

import org.springframework.cache.support.NullValue;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.DefaultTyping;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import tools.jackson.databind.jsontype.PolymorphicTypeValidator;
import tools.jackson.databind.jsontype.TypeSerializer;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.std.StdSerializer;

public final class CacheJsonCodec {

    private static final PolymorphicTypeValidator CACHE_TYPE_VALIDATOR =
            BasicPolymorphicTypeValidator.builder().allowIfSubType(Object.class).build();

    private final ObjectMapper mapper;

    public CacheJsonCodec(ObjectMapper source) {
        this.mapper = typedCopy(source);
    }

    private static ObjectMapper typedCopy(ObjectMapper source) {
        return source.rebuild()
                .activateDefaultTypingAsProperty(CACHE_TYPE_VALIDATOR, DefaultTyping.NON_FINAL_AND_RECORDS, "@class")
                .addModule(new SimpleModule().addSerializer(NullValue.class, new NullValueSerializer()))
                .build();
    }

    /**
     * Serializes a cached value (or the JetCache value holder) to JSON bytes.
     */
    public byte[] serialize(Object value) {
        try {
            return mapper.writeValueAsBytes(value);
        } catch (JacksonException ex) {
            throw new IllegalStateException("Cache value serialization failed", ex);
        }
    }

    /**
     * Deserializes JSON bytes back to the concrete type recorded in the {@code @class} hint.
     */
    public Object deserialize(byte[] bytes) {
        try {
            return mapper.readValue(bytes, Object.class);
        } catch (JacksonException ex) {
            throw new IllegalStateException("Cache value deserialization failed", ex);
        }
    }

    /**
     * The internal typed {@link ObjectMapper}, e.g. to build a Spring Data Redis serializer.
     */
    public ObjectMapper objectMapper() {
        return mapper;
    }

    private static final class NullValueSerializer extends StdSerializer<NullValue> {

        private NullValueSerializer() {
            super(NullValue.class);
        }

        @Override
        public void serialize(NullValue value, JsonGenerator generator, SerializationContext context)
                throws JacksonException {
            generator.writeStartObject();
            generator.writeName("@class");
            generator.writeString(NullValue.class.getName());
            generator.writeEndObject();
        }

        @Override
        public void serializeWithType(
                NullValue value, JsonGenerator generator, SerializationContext context, TypeSerializer typeSerializer)
                throws JacksonException {
            serialize(value, generator, context);
        }
    }
}
