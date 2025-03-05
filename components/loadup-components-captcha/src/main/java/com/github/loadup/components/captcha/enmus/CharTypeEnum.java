package com.github.loadup.components.captcha.enmus;

import com.github.loadup.capability.common.enums.IEnum;

import java.util.Arrays;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

@Getter
@AllArgsConstructor
public enum CharTypeEnum implements IEnum {
    /**
     * 字母数字混合
     */
    DEFAULT("DEFAULT", "DEFAULT"),

    /**
     * 纯数字
     */
    NUMBER_ONLY("NUMBER_ONLY", "NUMBER_ONLY"),

    /**
     * CHAR_ONLY
     */
    CHAR_ONLY("CHAR_ONLY", "CHAR_ONLY"),
    /**
     * 纯大写字母
     */
    CHAR_UPPER_ONLY("CHAR_UPPER_ONLY", "CHAR_UPPER_ONLY"),
    /**
     * 纯小写字母
     */
    CHAR_LOWER_ONLY("CHAR_LOWER_ONLY", "CHAR_LOWER_ONLY"),
    /**
     * 数字大写字母
     */
    NUMBER_AND_UPPER("NUMBER_AND_UPPER", "NUMBER_AND_UPPER"),
    ;

    private String code;
    private String description;

    public static CharTypeEnum getByCode(String code) {
        return Arrays.stream(CharTypeEnum.values())
                .filter(v -> StringUtils.equals(v.getCode(), code))
                .findFirst()
                .orElse(null);
    }
}
