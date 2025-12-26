package com.github.loadup.components.captcha.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author lengleng
 * @date 2020/7/31
 * 验证码配置属性
 */
@Data
@ConfigurationProperties(prefix = "captcha")
public class CaptchaProperties {

    /**
     * 默认长度，默认值： 4
     */
    private int len = 4;

    /**
     * 默认宽度，默认值： 130
     */
    private int width = 130;

    /**
     * 默认高度，默认值：48
     */
    private int height = 48;

    /**
     * 自定义字符集，如果不设置则使用默认字符集（已移除容易混淆的字符：0, O, 1, I, L, i, l, o）
     * 示例：customCharacters=23456789ABCDEFGHJKMNPQRSTUVWXYZ
     */
    private String customCharacters;

}
