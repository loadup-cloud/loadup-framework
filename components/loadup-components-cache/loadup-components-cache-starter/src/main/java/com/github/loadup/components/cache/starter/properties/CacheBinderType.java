
package com.github.loadup.components.cache.starter.properties;

/**
 * @author lise
 * @version BinderType.java, v 0.1 2026年01月13日 16:40 lise
 */
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum CacheBinderType {
  CAFFEINE("caffeine"),
  REDIS("redis"),
  ;

  @Getter private final String value;

  CacheBinderType(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
  }

  @JsonCreator
  public static CacheBinderType fromString(String value) {
    if (value == null) return null;

    for (CacheBinderType type : CacheBinderType.values()) {
      if (type.value.equalsIgnoreCase(value)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown binder type: " + value);
  }
}
