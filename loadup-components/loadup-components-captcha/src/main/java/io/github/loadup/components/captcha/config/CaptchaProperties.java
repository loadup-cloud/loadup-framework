package io.github.loadup.components.captcha.config;

/*-
 * #%L
 * loadup-components-captcha
 * %%
 * Copyright (C) 2026 LoadUp Cloud
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

    public CaptchaProperties() {}

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
    public String toString() {
        return org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString(
                this, org.apache.commons.lang3.builder.ToStringStyle.JSON_STYLE);
    }
}
