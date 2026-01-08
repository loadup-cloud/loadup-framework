package com.github.loadup.upms.api.gateway;

import com.github.loadup.upms.api.dto.AuthUserDTO;
import java.util.Set;

/** UPMS 安全数据网关 供 Security 模块调用，由 Infrastructure 模块实现 */
public interface AuthGateway {

  /**
   * 根据用户名获取用于认证的用户信息
   *
   * @param username 账号
   * @return 包含密码和权限的 DTO
   */
  AuthUserDTO getAuthUserByUsername(String username);

  /** 根据手机号获取认证信息 (用于短信登录) */
  AuthUserDTO getAuthUserByMobile(String mobile);

  /** 更新用户最后登录时间等静态信息 */
  void updateLastLoginTime(Long userId);

  /** 获取权限 */
  Set<String> getUserPermissionCodes(String userId);

  AuthUserDTO getAuthUserByUserId(String userId);
}
