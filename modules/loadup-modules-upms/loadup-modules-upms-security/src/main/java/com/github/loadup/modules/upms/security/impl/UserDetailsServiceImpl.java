package com.github.loadup.modules.upms.security.impl;

import com.github.loadup.modules.upms.security.core.SecurityUser;
import com.github.loadup.upms.api.dto.AuthUserDTO;
import com.github.loadup.upms.api.gateway.AuthGateway;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

  private final AuthGateway authGateway; // 注入 api 中定义的网关

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    // 1. 调用 api 层定义的契约获取数据
    AuthUserDTO authUser = authGateway.getAuthUserByUsername(username);
    if (authUser == null) {
      throw new UsernameNotFoundException("用户 " + username + " 不存在");
    }

    if (!authUser.actived()) {
      throw new UsernameNotFoundException("User is not active: " + username);
    }

    Set<String> permissions = authGateway.getUserPermissionCodes(authUser.getUserId());

    return new SecurityUser(authUser, permissions);
  }
}
