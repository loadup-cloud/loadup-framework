package com.github.loadup.modules.upms.app.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Department DTO (Tree Node)
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentDTO {

  private Long id;
  private Long parentId;
  private String deptName;
  private String deptCode;
  private Integer deptLevel;
  private Integer sortOrder;
  private Long leaderUserId;
  private String leaderUserName;
  private String phone;
  private String email;
  private Short status;
  private List<DepartmentDTO> children;
  private String remark;
  private LocalDateTime createdTime;
  private LocalDateTime updatedTime;
}
