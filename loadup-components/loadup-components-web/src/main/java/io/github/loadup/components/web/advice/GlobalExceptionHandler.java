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

import io.github.loadup.commons.enums.CommonResultCodeEnum;
import io.github.loadup.commons.error.CommonException;
import io.github.loadup.commons.result.FailureResponse;
import io.github.loadup.commons.result.IResponse;
import io.github.loadup.framework.api.util.TraceUtils;
import jakarta.validation.ConstraintViolationException;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  /** 1. 捕获自定义业务异常 */
  @ExceptionHandler(CommonException.class) // 假设你有自定义业务异常基类
  public IResponse<?> handleBusinessException(CommonException e) {
    log.warn("业务异常: code={}, message={}", e.getResultCode(), e.getMessage());
    String currentTraceId = TraceUtils.getTraceId();

    // 返回失败密封类
    return FailureResponse.of(e.getResultCode(), e.getMessage(), currentTraceId);
  }

  /** 2. 捕获参数校验异常 (Bean Validation / Hibernate Validator) */
  @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public IResponse<?> handleValidationException(Exception e) {
    String message;
    if (e instanceof MethodArgumentNotValidException ex) {
      message =
          ex.getBindingResult().getFieldErrors().stream()
              .map(error -> error.getField() + ": " + error.getDefaultMessage())
              .collect(Collectors.joining("; "));
    } else {
      message = ((BindException) e).getAllErrors().get(0).getDefaultMessage();
    }
    log.warn("参数校验失败: {}", message);
    return FailureResponse.of(CommonResultCodeEnum.PARAM_ILLEGAL.getCode(), message);
  }

  /** 3. 捕获单参数校验异常 (@RequestParam @NotBlank 等) */
  @ExceptionHandler(ConstraintViolationException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public IResponse<?> handleConstraintViolationException(ConstraintViolationException e) {
    log.warn("参数约束校验失败: {}", e.getMessage());
    return FailureResponse.of(CommonResultCodeEnum.PARAM_ILLEGAL.getCode(), e.getMessage());
  }

  /** 4. 捕获所有其他未知异常 (兜底) */
  @ExceptionHandler(Throwable.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public IResponse<?> handleThrowable(Throwable e) {
    log.error("系统未识别异常: ", e);
    // 生产环境不建议将 e.getMessage() 直接给前端，这里用通用的错误码
    return FailureResponse.of(CommonResultCodeEnum.SYS_ERROR.getCode(), "服务器内部错误，请联系管理员");
  }
}
