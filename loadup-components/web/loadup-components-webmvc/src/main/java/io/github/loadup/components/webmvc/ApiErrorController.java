/*
 * #%L
 * LoadUp Web MVC
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package io.github.loadup.components.webmvc;

import io.github.loadup.commons.enums.CommonResultCodeEnum;
import io.github.loadup.commons.log.LogUtil;
import io.github.loadup.commons.result.FailureResponse;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.webmvc.error.ErrorAttributes;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.ServletWebRequest;

/** Handles API errors while preserving ordinary HTTP status codes outside the API prefix. */
@RestController
public class ApiErrorController implements ErrorController {

    private final ApiPathMatcher pathMatcher;
    private final ErrorAttributes errorAttributes;

    public ApiErrorController(ApiPathMatcher pathMatcher, ErrorAttributes errorAttributes) {
        this.pathMatcher = pathMatcher;
        this.errorAttributes = errorAttributes;
    }

    @RequestMapping("${spring.web.error.path:${error.path:/error}}")
    public ResponseEntity<?> error(HttpServletRequest request) {
        int status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE) instanceof Integer value ? value : 500;
        String originalUri = (String) request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        if (!pathMatcher.matches(request, originalUri)) {
            return ResponseEntity.status(status)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errorAttributes.getErrorAttributes(
                            new ServletWebRequest(request), ErrorAttributeOptions.defaults()));
        }
        if (status >= 500) {
            LogUtil.error(
                    ApiErrorController.class,
                    "API request failed: {}",
                    originalUri,
                    errorAttributes.getError(new ServletWebRequest(request)));
        }
        CommonResultCodeEnum code =
                switch (status) {
                    case 400, 405, 415 -> CommonResultCodeEnum.PARAM_ILLEGAL;
                    case 401 -> CommonResultCodeEnum.UNAUTHENTICATED;
                    case 403 -> CommonResultCodeEnum.ACCESS_DENIED;
                    case 404 -> CommonResultCodeEnum.NOT_FOUND;
                    default -> CommonResultCodeEnum.SYS_ERROR;
                };
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(FailureResponse.of(code));
    }
}
