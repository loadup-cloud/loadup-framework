package com.github.loadup.upms.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.util.Set;
import lombok.Data;

/** 安全上下文中的用户信息契约 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthUserDTO implements Serializable {
  private String userId;
  private String username;
  @JsonIgnore private String password;
  private String nickname;
  private Integer status; // 0-正常, 1-锁定

  /** 权限标识集合 (如: sys:user:add) */
  private Set<String> permissions;

  /** 角色标识集合 (如: ROLE_ADMIN) */
  private Set<String> roles;

  public boolean actived() {
    return status == 0;
  }
}
