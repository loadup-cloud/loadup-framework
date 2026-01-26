package io.github.loadup.components.dfs.properties;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.Setter;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "type",
    visible = true)
@JsonSubTypes({
  @JsonSubTypes.Type(value = LocalBinderConfig.class, name = "local"),
  @JsonSubTypes.Type(value = S3BinderConfig.class, name = "s3"),
  @JsonSubTypes.Type(value = DatabaseBinderConfig.class, name = "database")
})
@Getter
@Setter
public abstract class BinderConfig {

  /** 存储类型，用于反序列化时识别具体子类 */
  private BinderType type;
}
