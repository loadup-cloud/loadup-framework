/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.modules.upms.domain;

/*-
 * #%L
 * loadup-modules-upms-domain
 * %%
 * Copyright (C) 2022 - 2024 loadup_cloud
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import com.github.loadup.commons.dto.DTO;
import java.util.regex.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

/**
 * @author lise
 * @since 1.0.0
 */
@Getter
@Setter
public class UserEmail extends DTO {
    public static final String EMAIL_SEP = "@";
    private static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@([a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}$";

    private String prefix;
    private String domain;

    public String fullEmail() {
        if (StringUtils.isBlank(prefix) || StringUtils.isBlank(domain)) {
            return StringUtils.EMPTY;
        }
        return prefix + EMAIL_SEP + domain;
    }

    public boolean validatedEmail() {
        String email = fullEmail();
        return Pattern.matches(EMAIL_REGEX, email);
    }

    public static UserEmail of(String email) {
        if (email == null || !Pattern.matches(EMAIL_REGEX, email)) {
            throw new IllegalArgumentException("Invalid email format");
        }

        String[] parts = email.split(EMAIL_SEP);
        UserEmail userEmail = new UserEmail();
        userEmail.setPrefix(parts[0]);
        userEmail.setDomain(parts[1]);
        return userEmail;
    }

    public static UserEmail blankEmail() {
        UserEmail userEmail = new UserEmail();
        userEmail.setPrefix("");
        userEmail.setDomain("");
        return userEmail;
    }

    public boolean isBlank() {
        return (prefix == null || prefix.trim().isEmpty())
                && (domain == null || domain.trim().isEmpty());
    }
}
