package io.github.loadup.modules.upms.domain.gateway;

import io.github.loadup.modules.upms.domain.entity.UserOAuthBinding;
import java.util.List;
import java.util.Optional;

/**
 * 用户OAuth绑定仓储接口
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
public interface UserOAuthBindingGateway {

    /**
     * 保存绑定
     */
    UserOAuthBinding save(UserOAuthBinding binding);

    /**
     * 根据提供商和OpenID查询绑定
     */
    Optional<UserOAuthBinding> findByProviderAndOpenId(String provider, String openId);

    /**
     * 根据用户ID查询所有绑定
     */
    List<UserOAuthBinding> findByUserId(String userId);

    /**
     * 根据用户ID和提供商查询绑定
     */
    Optional<UserOAuthBinding> findByUserIdAndProvider(String userId, String provider);

    /**
     * 删除绑定
     */
    void delete(String id);

    /**
     * 根据用户ID和提供商删除绑定
     */
    void deleteByUserIdAndProvider(String userId, String provider);
}
