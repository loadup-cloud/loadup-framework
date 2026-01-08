package com.github.loadup.modules.upms.security.util;

import com.github.loadup.modules.upms.security.core.SecurityUser;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityContextHelper {

  public static String getCurUserName() {
    SecurityUser user = getCurUser();
    return null != user ? user.getUsername() : null;
  }

  public static SecurityUser getCurUser() {
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    if (principal instanceof SecurityUser user) {
      return user;
    }
    return null;
  }

  public static String getCurUserId() {
    SecurityUser user = getCurUser();
    return null != user ? user.getUserId() : null;
  }
  // 也可以根据需要在这里通过 AuthGateway 获取更完整的用户信息
}
