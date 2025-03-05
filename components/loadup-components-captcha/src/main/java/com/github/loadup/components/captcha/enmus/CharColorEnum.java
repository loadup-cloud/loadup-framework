package com.github.loadup.components.captcha.enmus;

import com.github.loadup.capability.common.enums.IEnum;
import com.github.loadup.capability.common.util.core.StringPool;

import java.awt.Color;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;

@Getter
@AllArgsConstructor
public enum CharColorEnum implements IEnum {

    /**
     * color
     */
    COLOR_1("0,135,255", "color"),
    /**
     * color
     */
    COLOR_2("51,153,51", "color"),
    /**
     * color
     */
    COLOR_3("255,102,102", "color"),
    /**
     * color
     */
    COLOR_4("255,153,0", "color"),
    /**
     * color
     */
    COLOR_5("153,102,0", "color"),
    /**
     * color
     */
    COLOR_6("153,102,153", "color"),
    /**
     * color
     */
    COLOR_7("51,153,153", "color"),
    /**
     * color
     */
    COLOR_8("102,102,255", "color"),
    /**
     * color
     */
    COLOR_9("0,102,204", "color"),
    /**
     * color
     */
    COLOR_10("204,51,51", "color"),
    /**
     * color
     */
    COLOR_11("0,153,204", "color"),
    /**
     * color
     */
    COLOR_12("0,51,102", "color"),
    ;

    private String code;
    private String description;

    public static Color randomColor() {
        int i = RandomUtils.nextInt(0, 11);
        CharColorEnum[] values = CharColorEnum.values();
        CharColorEnum value = values[i];
        String[] color = StringUtils.split(value.getCode(), StringPool.COMMA, 3);
        return new Color(Integer.parseInt(color[0]), Integer.parseInt(color[1]), Integer.parseInt(color[2]));
    }
}
