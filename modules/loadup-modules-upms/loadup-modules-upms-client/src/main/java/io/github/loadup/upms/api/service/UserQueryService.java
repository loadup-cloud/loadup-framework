package io.github.loadup.upms.api.service;

import io.github.loadup.upms.api.dto.UserDetailDTO;
import java.util.List;

/** UPMS 外部调用接口 */
public interface UserQueryService {

  /** 获取用户基本信息 */
  UserDetailDTO getUserById(Long userId);

  /** 批量获取用户信息 */
  List<UserDetailDTO> listUsersByIds(List<Long> userIds);
}
