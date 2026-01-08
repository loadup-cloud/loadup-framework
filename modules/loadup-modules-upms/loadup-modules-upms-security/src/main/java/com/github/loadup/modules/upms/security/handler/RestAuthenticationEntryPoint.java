package com.github.loadup.modules.upms.security.handler;

import com.github.loadup.commons.result.FailureResponse;
import com.github.loadup.commons.util.JsonUtil;
import com.github.loadup.framework.api.util.TraceUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {
  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException)
      throws IOException {

    response.setCharacterEncoding("UTF-8");
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

    // 利用你的 FailureResponse 契约返回
    FailureResponse<Void> failure =
        FailureResponse.of("UNAUTHORIZED", "访问令牌无效或已过期", TraceUtils.getTraceId());

    // 利用你的 JsonUtil 序列化输出
    response.getWriter().println(JsonUtil.toJsonString(failure));
  }
}
