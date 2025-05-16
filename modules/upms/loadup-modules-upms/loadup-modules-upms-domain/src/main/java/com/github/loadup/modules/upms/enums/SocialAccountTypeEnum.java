package com.github.loadup.modules.upms.enums;

import com.github.loadup.commons.enums.GenderEnum;
import com.github.loadup.commons.enums.IEnum;
import com.github.loadup.commons.util.EnumUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SocialAccountTypeEnum implements IEnum {
    MOBILE("MOBILE", "mobile"),
    EMAIL("EMAIL", "email"),
    WECHAT("WECHAT", "wechat"),
    QQ("QQ", "qq"),
    WEIBO("WEIBO", "weibo"),
    XHS("XHS", "xhs"),
    DOUYIN("DOUYIN", "douyin"),
    BAIDU("BAIDU", "baidu"),
    GITHUB("GITHUB", "github"),
    ;

    private final String code;
    private final String description;

    public static GenderEnum getByCode(String code) {
        return EnumUtils.getEnumByCode(GenderEnum.class, code);
    }

}
