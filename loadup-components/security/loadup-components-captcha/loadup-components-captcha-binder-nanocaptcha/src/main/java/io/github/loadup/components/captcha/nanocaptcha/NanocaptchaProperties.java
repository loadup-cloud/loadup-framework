package io.github.loadup.components.captcha.nanocaptcha;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Nanocaptcha binder configuration ({@code loadup.captcha.binder.nanocaptcha.*}).
 */
@ConfigurationProperties(prefix = "loadup.captcha.binder.nanocaptcha")
public class NanocaptchaProperties {

    /** Image width in pixels. */
    private int width = 130;

    /** Image height in pixels. */
    private int height = 48;

    /** Number of characters. */
    private int length = 4;

    /** Character set: {@code numbers} (default) / {@code latin} / {@code chinese}. */
    private String content = "numbers";

    /** Answer expiration in seconds; default 5 minutes. */
    private long expirationSeconds = 300;

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getExpirationSeconds() {
        return expirationSeconds;
    }

    public void setExpirationSeconds(long expirationSeconds) {
        this.expirationSeconds = expirationSeconds;
    }
}
