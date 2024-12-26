package com.github.loadup.components.captcha.enmus;

import com.github.loadup.capability.common.enums.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CharFontEnum implements IEnum {
    /**
     * actionj
     */
    ACTIONJ("actionj.ttf", "actionj.ttf"),
    /**
     * epilog
     */
    EPILOG("epilog.ttf", "epilog.ttf"),
    /**
     * fresnel
     */
    FRESNEL("fresnel.ttf", "fresnel.ttf"),
    /**
     * headache
     */
    HEADACHE("headache.ttf", "headache.ttf"),
    /**
     * lexo
     */
    LEXO("lexo.ttf", "lexo.ttf"),
    /**
     * prefix
     */
    PREFIX("prefix.ttf", "prefix.ttf"),
    /**
     * progbot
     */
    PROGBOT("progbot.ttf", "progbot.ttf"),
    /**
     * ransom
     */
    RANSOM("ransom.ttf", "ransom.ttf"),
    /**
     * robot
     */
    ROBOT("robot.ttf", "robot.ttf"),
    /**
     * scandal
     */
    SCANDAL("scandal.ttf", "scandal.ttf"),
    ;

    private String code;
    private String description;
}
