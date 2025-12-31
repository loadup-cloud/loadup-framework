package com.github.loadup.modules.upms.app.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * User Info DTO
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoDTO {

  private Long id;

  private String username;

  private String nickname;

  private String realName;

  private String email;

  private String phone;

  private String avatarUrl;

  private Long deptId;

  private String deptName;

  private List<String> roles;

  private List<String> permissions;

  private LocalDateTime lastLoginTime;
}
