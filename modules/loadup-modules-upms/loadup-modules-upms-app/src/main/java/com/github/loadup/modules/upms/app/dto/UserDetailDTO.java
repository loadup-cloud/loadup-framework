package com.github.loadup.modules.upms.app.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * User Detail DTO
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailDTO {

  private Long id;
  private String username;
  private String nickname;
  private String realName;
  private Long deptId;
  private String deptName;
  private String email;
  private Boolean emailVerified;
  private String phone;
  private Boolean phoneVerified;
  private String avatarUrl;
  private Short gender;
  private LocalDate birthday;
  private Short status;
  private LocalDateTime lastLoginTime;
  private String lastLoginIp;
  private List<RoleDTO> roles;
  private String remark;
  private LocalDateTime createdTime;
  private LocalDateTime updatedTime;
}
