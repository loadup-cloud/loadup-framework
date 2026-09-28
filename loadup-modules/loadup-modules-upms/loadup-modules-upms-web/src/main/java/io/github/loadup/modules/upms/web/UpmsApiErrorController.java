/*
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 *
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
 */
package io.github.loadup.modules.upms.web;

import io.github.loadup.commons.enums.CommonResultCodeEnum;
import io.github.loadup.commons.result.FailureResponse;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Ensures errors outside a mapped controller still use the JSON response contract. */
@RestController
public class UpmsApiErrorController implements ErrorController {
    @RequestMapping(path = "/error")
    public FailureResponse<Void> error(HttpServletRequest request, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        if (status instanceof Integer code) {
            if (code == 404) return FailureResponse.of(CommonResultCodeEnum.NOT_FOUND);
            if (code == 400 || code == 405 || code == 415) return FailureResponse.of(CommonResultCodeEnum.PARAM_ILLEGAL);
            if (code == 401) return FailureResponse.of(CommonResultCodeEnum.UNAUTHENTICATED);
            if (code == 403) return FailureResponse.of(CommonResultCodeEnum.ACCESS_DENIED);
        }
        return FailureResponse.of(CommonResultCodeEnum.SYS_ERROR);
    }
}
