package io.github.loadup.modules.upms.app.strategy;

/**
 * OAuth 提供商常量
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
public final class OAuthProviderCode {

    private OAuthProviderCode() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * GitHub
     */
    public static final String GITHUB = "github";

    /**
     * 微信
     */
    public static final String WECHAT = "wechat";

    /**
     * Google
     */
    public static final String GOOGLE = "google";
}
