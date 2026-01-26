package io.github.loadup.modules.upms.app.dto;

import io.github.loadup.upms.api.dto.RoleDTO;
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

  private String id;
  private String username;
  private String nickname;
  private String realName;
  private String deptId;
  private String deptName;
  private String email;
  private Boolean emailVerified;
  private String mobile;
  private Boolean mobileVerified;
  private String avatar;
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
