package io.github.loadup.components.dfs.properties;

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
