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

import com.github.loadup.commons.domain.BaseDomain;
import com.github.loadup.commons.util.ToStringUtils;
import com.github.loadup.modules.upms.enums.SocialAccountTypeEnum;
import java.io.Serial;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * @author lise
 * @since 1.0.0
 */
@Getter
@Setter
public class UpmsUser extends BaseDomain {
    @Serial
    private static final long serialVersionUID = 5534160252441899151L;

    private String id;
    private String account;
    private String nickName;
    private UserName englishName;
    private UserName officialName;
    private String password;
    private String avatar;
    private LocalDate birthday;
    private LocalDateTime registeredTime;
    private String userType;
    private List<UpmsRole> roleList;
    private List<UpmsPosition> positionList;
    private List<UpmsDepart> departList;
    private List<UpmsSocial> socialList;

    public String mobile() {
        if (CollectionUtils.isNotEmpty(socialList)) {
            for (UpmsSocial social : socialList) {
                if (SocialAccountTypeEnum.MOBILE.equals(social.getAccountType())) {
                    return social.getSocialAccount();
                }
            }
        }
        return StringUtils.EMPTY;
    }

    public UserEmail email() {
        if (CollectionUtils.isNotEmpty(socialList)) {
            for (UpmsSocial social : socialList) {
                if (SocialAccountTypeEnum.EMAIL.equals(social.getAccountType())) {
                    return UserEmail.of(social.getSocialAccount());
                }
            }
        }
        return UserEmail.blankEmail();
    }

    @Override
    public String toString() {
        return ToStringUtils.reflectionToString(this);
    }
}
