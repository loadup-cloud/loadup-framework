package io.github.loadup.components.captcha.config;

/*-
 * #%L
 * loadup-components-captcha
 * %%
 * Copyright (C) 2026 LoadUp Cloud
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

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author lengleng
 * @date 2020/7/31 验证码配置属性
 */
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

    public CaptchaProperties(int len, int width, int height, String customCharacters) {
        this.len = len;
        this.width = width;
        this.height = height;
        this.customCharacters = customCharacters;
    }

    public CaptchaProperties() {
    }

    public int getLen() {
        return this.len;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public String getCustomCharacters() {
        return this.customCharacters;
    }

    public void setLen(int len) {
        this.len = len;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setCustomCharacters(String customCharacters) {
        this.customCharacters = customCharacters;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(len, width, height, customCharacters);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CaptchaProperties other = (CaptchaProperties) o;
        if (!java.util.Objects.equals(len, other.len)) return false;
        if (!java.util.Objects.equals(width, other.width)) return false;
        if (!java.util.Objects.equals(height, other.height)) return false;
        if (!java.util.Objects.equals(customCharacters, other.customCharacters)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "CaptchaProperties(" + "len=" + len + ", " + "width=" + width + ", " + "height=" + height + ", " + "customCharacters=" + customCharacters + ")";
    }
}
