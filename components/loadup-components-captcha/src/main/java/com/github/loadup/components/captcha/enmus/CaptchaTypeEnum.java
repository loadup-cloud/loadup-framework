package com.github.loadup.components.captcha.enmus;

import com.github.loadup.capability.common.enums.IEnum;

import java.util.Arrays;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

@Getter
@AllArgsConstructor
public enum CaptchaTypeEnum implements IEnum {
    /**
     * PNG
     */
    PNG_IMG("PNG_IMG", "PNG_IMG"),

    /**
     * GIF
     */
    GIF_IMG("GIF_IMG", "GIF_IMG"),

    /**
     * 中文  PNG
     */
    PNG_CN_IMG("PNG_CN_IMG", "PNG_CN_IMG"),
    /**
     * 中文 GIF
     */
    GIF_CN_IMG("GIF_CN_IMG", "GIF_CN_IMG"),
    /**
     * 算数图片
     */
    PNG_MATH_IMG("PNG_MATH_IMG", "PNG_MATH_IMG"),
    ;

    private String code;
    private String description;

    public static CaptchaTypeEnum getByCode(String code) {
        return Arrays.stream(CaptchaTypeEnum.values())
                .filter(v -> StringUtils.equals(v.getCode(), code))
                .findFirst()
                .orElse(null);
    }
}
