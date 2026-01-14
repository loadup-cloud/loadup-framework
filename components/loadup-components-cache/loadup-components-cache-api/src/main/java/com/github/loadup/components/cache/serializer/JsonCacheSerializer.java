
package com.github.loadup.components.cache.serializer;

import com.github.loadup.commons.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

/**
 * @author lise
 * @version JsonCacheSerializer.java, v 0.1 2026年01月13日 18:01 lise
 */
@Slf4j
public class JsonCacheSerializer implements CacheSerializer {
  @Override
  public byte[] serialize(Object obj) {
    if (obj == null) {
      return new byte[0];
    }
    try {
      // 将对象转为 JSON 字符串，再转为字节数组
      String json = JsonUtil.toJson(obj);
      return json.getBytes(StandardCharsets.UTF_8);
    } catch (Exception e) {
      log.error("Cache 序列化失败: {}", obj.getClass().getName(), e);
      throw new RuntimeException("Serialization failed", e);
    }
  }

  @Override
  public <T> T deserialize(byte[] bytes, Class<T> clazz) {
    if (bytes == null || bytes.length == 0) {
      return null;
    }
    try {
      // 将字节数组转为 JSON 字符串，再反序列化为对象
      String json = new String(bytes, StandardCharsets.UTF_8);
      return JsonUtil.fromJson(json, clazz);
    } catch (Exception e) {
      log.error("Cache 反序列化失败，目标类型: {}", clazz.getName(), e);
      throw new RuntimeException("Deserialization failed", e);
    }
  }
}
