package io.github.loadup.components.cache.codec;

/*-
 * #%L
 * Loadup Cache Components API
 * %%
 * Copyright (C) 2025 - 2026 loadup_cloud
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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import java.io.UncheckedIOException;
import org.springframework.cache.support.NullValue;

/**
 * Single JSON value codec shared by every remote-capable binder (redis and jetcache).
 *
 * <p><b>Serializer decision:</b> JSON with {@code @class} default typing is the one and only cache
 * value format. JDK serialization is intentionally not offered: it forces every cached DTO to
 * implement {@link java.io.Serializable}, bloats payloads, and opens deserialization risk. JSON
 * keeps business types as plain records/POJOs and still restores the concrete type on read.
 *
 * <p>The codec works on a copy of the application {@link ObjectMapper} (so the global mapper is
 * never mutated) with {@code @class} type hints enabled for every value, including final records.
 * Spring's {@link NullValue} marker (used by the Cache abstraction for {@code null} results) is
 * written as a stable {@code @class}-only object and resolves back to {@link NullValue#INSTANCE}
 * on read, mirroring Spring Data Redis' null-value handling.
 */
public final class CacheJsonCodec {

    private final ObjectMapper mapper;

    public CacheJsonCodec(ObjectMapper source) {
        this.mapper = typedCopy(source);
    }

    /** Serializes a cached value (or the JetCache value holder) to JSON bytes. */
    public byte[] serialize(Object value) {
        try {
            return mapper.writeValueAsBytes(value);
        } catch (JsonProcessingException ex) {
            throw new UncheckedIOException("Cache value serialization failed", ex);
        }
    }

    /** Deserializes JSON bytes back to the concrete type recorded in the {@code @class} hint. */
    public Object deserialize(byte[] bytes) {
        try {
            return mapper.readValue(bytes, Object.class);
        } catch (IOException ex) {
            throw new UncheckedIOException("Cache value deserialization failed", ex);
        }
    }

    /** The internal typed {@link ObjectMapper}, e.g. to build a Spring Data Redis serializer. */
    public ObjectMapper objectMapper() {
        return mapper;
    }

    private static ObjectMapper typedCopy(ObjectMapper source) {
        ObjectMapper copy = source.copy();
        copy.activateDefaultTypingAsProperty(
                copy.getPolymorphicTypeValidator(), ObjectMapper.DefaultTyping.EVERYTHING, "@class");
        copy.registerModule(new SimpleModule().addSerializer(NullValue.class, new NullValueSerializer()));
        return copy;
    }

    private static final class NullValueSerializer extends StdSerializer<NullValue> {

        private NullValueSerializer() {
            super(NullValue.class);
        }

        @Override
        public void serialize(NullValue value, JsonGenerator generator, SerializerProvider provider)
                throws IOException {
            generator.writeStartObject();
            generator.writeStringField("@class", NullValue.class.getName());
            generator.writeEndObject();
        }

        @Override
        public void serializeWithType(
                NullValue value, JsonGenerator generator, SerializerProvider provider, TypeSerializer typeSerializer)
                throws IOException {
            serialize(value, generator, provider);
        }
    }
}
