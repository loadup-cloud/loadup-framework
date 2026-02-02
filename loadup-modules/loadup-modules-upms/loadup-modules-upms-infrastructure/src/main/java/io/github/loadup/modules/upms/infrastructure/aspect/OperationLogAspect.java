// package io.github.loadup.modules.upms.infrastructure.aspect;

/*-
 * #%L
 * Loadup Modules UPMS Infrastructure Layer
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
//
// import com.fasterxml.jackson.databind.ObjectMapper;
// import io.github.loadup.modules.upms.domain.repository.OperationLogRepository;
// import io.github.loadup.upms.security.core.SecurityUser;
// import jakarta.servlet.http.HttpServletRequest;
// import java.time.LocalDateTime;
// import java.util.Arrays;
// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
// import org.aspectj.lang.ProceedingJoinPoint;
// import org.aspectj.lang.annotation.Around;
// import org.aspectj.lang.annotation.Aspect;
// import org.aspectj.lang.annotation.Pointcut;
// import org.springframework.scheduling.annotation.Async;
// import org.springframework.security.core.Authentication;
// import org.springframework.security.core.context.SecurityContextHolder;
// import org.springframework.stereotype.Component;
// import org.springframework.web.context.request.RequestContextHolder;
// import org.springframework.web.context.request.ServletRequestAttributes;
//
/// **
// * Operation Log Aspect AOP-based async operation logging
// *
// * @author LoadUp Framework
// * @since 1.0.0
// */
// @Slf4j
// @Aspect
// @Component
// @RequiredArgsConstructor
// public class OperationLogAspect {
//
//  private final OperationLogRepository operationLogRepository;
//  private final ObjectMapper objectMapper;
//
//  @Pointcut("@annotation(aspect.infrastructure.upms.modules.loadup.github.io.OperationLog)")
//  public void operationLogPointcut() {}
//
//  @Around("operationLogPointcut() && @annotation(operationLog)")
//  public Object around(ProceedingJoinPoint joinPoint, OperationLog operationLog) throws Throwable
// {
//    long startTime = System.currentTimeMillis();
//
//    ServletRequestAttributes attributes =
//        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
//    HttpServletRequest request = attributes != null ? attributes.getRequest() : null;
//
//    entity.domain.upms.modules.loadup.github.io.OperationLog.OperationLogBuilder logBuilder =
//        entity.domain.upms.modules.loadup.github.io.OperationLog.builder();
//
//    // Get user info from security context
//    //Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//    //if (authentication != null
//    //    && authentication.getPrincipal() instanceof SecurityUser securityUser) {
//    //  logBuilder.userId(securityUser.getUserId());
//    //  logBuilder.username(securityUser.getUsername());
//    //}
//
//    // Set operation info
//    logBuilder.operationType(operationLog.type());
//    logBuilder.operationModule(operationLog.module());
//    logBuilder.operationDesc(operationLog.description());
//
//    // Set request info
//    if (request != null) {
//      logBuilder.requestMethod(request.getMethod());
//      logBuilder.requestUrl(request.getRequestURI());
//      logBuilder.ipAddress(getClientIp(request));
//      logBuilder.userAgent(request.getHeader("User-Agent"));
//
//      // Get trace ID if available
//      String traceId = request.getHeader("X-Trace-Id");
//      if (traceId != null) {
//        logBuilder.traceId(traceId);
//      }
//    }
//
//    // Get request params
//    try {
//      Object[] args = joinPoint.getArgs();
//      if (args != null && args.length > 0) {
//        String params = objectMapper.writeValueAsString(Arrays.asList(args));
//        logBuilder.requestParams(params);
//      }
//    } catch (Exception e) {
//      log.warn("Failed to serialize request params", e);
//    }
//
//    Object result = null;
//    try {
//      // Execute target method
//      result = joinPoint.proceed();
//
//      // Record success
//      logBuilder.status((short) 1);
//
//      // Set response result (only if configured)
//      if (operationLog.recordResponse()) {
//        try {
//          String response = objectMapper.writeValueAsString(result);
//          if (response.length() > 2000) {
//            response = response.substring(0, 2000) + "...";
//          }
//          logBuilder.responseResult(response);
//        } catch (Exception e) {
//          log.warn("Failed to serialize response", e);
//        }
//      }
//
//      return result;
//    } catch (Throwable throwable) {
//      // Record failure
//      logBuilder.status((short) 0);
//      logBuilder.errorMessage(throwable.getMessage());
//      throw throwable;
//    } finally {
//      // Calculate execution time
//      long executionTime = System.currentTimeMillis() - startTime;
//      logBuilder.executionTime(executionTime);
//      logBuilder.createdTime(LocalDateTime.now());
//
//      // Async save log
//      saveLogAsync(logBuilder.build());
//    }
//  }
//
//  @Async
//  public void saveLogAsync(entity.domain.upms.modules.loadup.github.io.OperationLog operationLog)
// {
//    try {
//      operationLogRepository.save(operationLog);
//    } catch (Exception e) {
//      log.error("Failed to save operation log", e);
//    }
//  }
//
//  private String getClientIp(HttpServletRequest request) {
//    String ip = request.getHeader("X-Forwarded-For");
//    if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
//      ip = request.getHeader("X-Real-IP");
//    }
//    if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
//      ip = request.getRemoteAddr();
//    }
//    return ip;
//  }
// }
