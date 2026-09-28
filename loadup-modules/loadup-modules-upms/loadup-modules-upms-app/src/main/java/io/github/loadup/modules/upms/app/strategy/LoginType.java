package io.github.loadup.modules.upms.app.strategy;

/**
 * 登录类型常量
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
public final class LoginType {

    private LoginType() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * 账号密码登录
     */
    public static final String PASSWORD = "PASSWORD";

    /**
     * 手机验证码登录
     */
    public static final String MOBILE = "MOBILE";

    /**
     * 邮箱验证码登录
     */
    public static final String EMAIL = "EMAIL";

    /**
     * OAuth 登录
     */
    public static final String OAUTH = "OAUTH";
}
