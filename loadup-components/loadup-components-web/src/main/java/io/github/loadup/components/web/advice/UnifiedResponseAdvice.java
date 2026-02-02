package io.github.loadup.components.web.advice;

/*-
 * #%L
 * Loadup Components Web
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.loadup.commons.result.*;
import io.github.loadup.commons.result.IResponse;
import io.github.loadup.commons.result.PageDTO;
import io.github.loadup.commons.result.SuccessResponse;
import io.github.loadup.components.web.annotation.IgnoreResponseAdvice;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class UnifiedResponseAdvice implements ResponseBodyAdvice<Object> {

  private final ObjectMapper objectMapper;

  @Override
  public boolean supports(
      MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
    // 1. 如果方法或类上有 IgnoreResponseAdvice 注解，跳过
    if (returnType.hasMethodAnnotation(IgnoreResponseAdvice.class)
        || returnType.getContainingClass().isAnnotationPresent(IgnoreResponseAdvice.class)) {
      return false;
    }

    // 2. 如果已经是 IResponse 系列（手动返回的成功或失败），跳过
    // 3. 排除 Spring 内置端点 (Actuator, Swagger)
    String className = returnType.getContainingClass().getName();
    return !IResponse.class.isAssignableFrom(returnType.getParameterType())
        && !className.contains("springframework.boot.actuator")
        && !className.contains("springdoc");
  }

  @Override
  public Object beforeBodyWrite(
      Object body,
      MethodParameter returnType,
      MediaType selectedContentType,
      Class<? extends HttpMessageConverter<?>> selectedConverterType,
      ServerHttpRequest request,
      ServerHttpResponse response) {

    // 核心逻辑：将业务对象包装为密封类的实现
    Object output;
    if (body == null) {
      output = SuccessResponse.success();
    } else if (body instanceof Collection<?> collection) {
      output = SuccessResponse.of(collection);
    } else if (body instanceof PageDTO<?> page) {
      return SuccessResponse.ofPage(page);
    } else {
      output = SuccessResponse.of(body);
    }

    // 特殊处理：如果返回值是 String，必须手动转为 JSON 字符串
    // 否则 Spring 会尝试用 StringHttpMessageConverter 处理 SuccessResponse 对象导致报错
    if (body instanceof String
        || selectedConverterType.getName().contains("StringHttpMessageConverter")) {
      try {
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        return objectMapper.writeValueAsString(output);
      } catch (JsonProcessingException e) {
        log.error("Failed to serialize response to JSON string", e);
        return output.toString();
      }
    }

    return output;
  }
}
