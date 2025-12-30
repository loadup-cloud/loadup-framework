package com.github.loadup.commons.error;

/*-
 * #%L
 * loadup-commons-lang
 * %%
 * Copyright (C) 2022 - 2024 loadup_cloud
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

import com.github.loadup.commons.result.ResultCode;
import lombok.Getter;

import java.io.Serial;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Lise
 * @since 1.0.0
 */
@Getter
public class CommonException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 2713503013175560520L;

    private final ResultCode resultCode;

    public CommonException(ResultCode resultCode) {
        this.resultCode = resultCode;
    }

    public CommonException(ResultCode resultCode, String msg) {
        super(msg);
        this.resultCode = resultCode;
    }

    public CommonException(ResultCode resultCode, Throwable cause) {
        super(cause);
        this.resultCode = resultCode;
    }

    public CommonException(ResultCode resultCode, String msg, Throwable cause) {
        super(msg, cause);
        this.resultCode = resultCode;
    }

    @Override
    public String toString() {
        Map<String, String> map = new HashMap<>();
        if (resultCode != null) {
            map.put("code", resultCode.getCode());
            map.put("message", resultCode.getMessage());
        }
        map.put("extraMessage", getMessage());

        return "{\"code\":\""
            + map.getOrDefault("code", "")
            + "\",\"message\":\"\""
            + map.getOrDefault("message", "")
            + "\"\",\"extraMessage\":\"\""
            + map.getOrDefault("extraMessage", "")
            + "\"\"}";
    }
}
