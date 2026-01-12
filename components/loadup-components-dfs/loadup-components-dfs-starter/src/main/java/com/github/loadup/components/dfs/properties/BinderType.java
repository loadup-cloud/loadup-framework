package com.github.loadup.components.dfs.properties;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum BinderType {
  LOCAL("local"),
  S3("s3"),
  DATABASE("database");

  @Getter private final String value;

  BinderType(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
  }

  @JsonCreator
  public static BinderType fromString(String value) {
    if (value == null) return null;

    for (BinderType type : BinderType.values()) {
      if (type.value.equalsIgnoreCase(value)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown binder type: " + value);
  }
}
