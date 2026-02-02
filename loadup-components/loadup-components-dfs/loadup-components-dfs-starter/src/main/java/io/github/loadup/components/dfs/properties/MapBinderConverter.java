package io.github.loadup.components.dfs.properties;

/*-
 * #%L
 * LoadUp Components DFS Starter
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MapBinderConverter extends JsonDeserializer<Map<BinderType, BinderConfig>> {

  @Override
  public Map<BinderType, BinderConfig> deserialize(JsonParser p, DeserializationContext ctxt)
      throws IOException {

    ObjectMapper mapper = (ObjectMapper) p.getCodec();
    JsonNode node = mapper.readTree(p);
    Map<BinderType, BinderConfig> result = new HashMap<>();

    // 遍历每个key（local, s3, database）
    node.fields()
        .forEachRemaining(
            entry -> {
              try {
                String key = entry.getKey();
                BinderType binderType = BinderType.fromString(key);
                JsonNode valueNode = entry.getValue();

                // 根据key决定使用哪个配置类
                BinderConfig config;
                switch (binderType) {
                  case LOCAL:
                    config = mapper.treeToValue(valueNode, LocalBinderConfig.class);
                    break;
                  case S3:
                    config = mapper.treeToValue(valueNode, S3BinderConfig.class);
                    break;
                  case DATABASE:
                    config = mapper.treeToValue(valueNode, DatabaseBinderConfig.class);
                    break;
                  default:
                    throw new IllegalArgumentException("Unsupported binder type: " + key);
                }

                // 确保type被正确设置
                config.setType(binderType);
                result.put(binderType, config);

              } catch (Exception e) {
                throw new RuntimeException(
                    "Failed to deserialize binder config for key: " + entry.getKey(), e);
              }
            });

    return result;
  }
}
