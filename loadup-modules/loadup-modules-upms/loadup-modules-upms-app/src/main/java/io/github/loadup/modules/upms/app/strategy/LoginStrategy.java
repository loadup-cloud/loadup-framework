package io.github.loadup.modules.upms.app.strategy;

import io.github.loadup.modules.upms.client.dto.AuthenticatedUser;

/**
 * 登录策略接口（SPI）
 * 每种登录方式实现此接口
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
public interface LoginStrategy {

    /**
     * 登录类型标识
     *
     * @return PASSWORD | MOBILE | EMAIL | OAUTH
     */
    String getLoginType();

    /**
     * 执行认证逻辑
     *
     * @param credentials 登录凭证（通用对象，不同策略自行解析）
     * @return 认证结果（用户信息）
     * @throws RuntimeException 认证失败
     */
    AuthenticatedUser authenticate(LoginCredentials credentials);

    /**
     * 优先级（用于同类型多实现时的选择）
     *
     * @return 优先级，数值越小优先级越高
     */
    default int getOrder() {
        return 0;
    }
}
