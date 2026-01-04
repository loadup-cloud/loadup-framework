package com.github.loadup.modules.upms.app.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * User Info DTO - Basic user information for authentication
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoDTO {

  private String id;
  private String username;
  private String nickname;
  private String realName;
  private String email;
  private String phone;
  private String avatarUrl;
  private String deptId;
  private List<String> roles;
  private List<String> permissions;
  private LocalDateTime lastLoginTime;
}
