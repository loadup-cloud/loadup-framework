package com.github.loadup.components.captcha.properties;

import com.github.loadup.components.captcha.enmus.CaptchaTypeEnum;
import com.github.loadup.components.captcha.enmus.CharFontEnum;
import com.github.loadup.components.captcha.enmus.CharTypeEnum;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@ConfigurationProperties(prefix = "loadup.captcha")
@Component
public class CaptchaProperties {
    /**
     * 验证码显示宽度
     */
    private int    width       = 130;
    /**
     * 验证码显示高度
     */
    private int    height      = 48;
    private String charType    = CharTypeEnum.DEFAULT.getCode();
    private int    length      = 4;
    private String captchaType = CaptchaTypeEnum.PNG_IMG.getCode();

    private Font font;

    public CharTypeEnum getCharTypeEnum() {
        return CharTypeEnum.getByCode(charType);
    }

    public Font getFont() {
        if (font == null) {
            try {
                setFont(CharFontEnum.PREFIX);
            } catch (Exception e) {
                setFont(new Font("Arial", Font.BOLD, 32));
            }
        }
        return font;
    }

    public void setFont(Font font) {
        this.font = font;
    }

    public void setFont(CharFontEnum font) throws IOException, FontFormatException {
        setFont(font, 32f);
    }

    public void setFont(CharFontEnum font, float size) throws IOException, FontFormatException {
        setFont(font, Font.BOLD, size);
    }

    public void setFont(CharFontEnum font, int style, float size) throws IOException, FontFormatException {
        this.font = Font.createFont(Font.TRUETYPE_FONT, getClass().getResourceAsStream("/fonts/" + font.getCode())).deriveFont(style,
                size);
    }

}
