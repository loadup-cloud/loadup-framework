package io.github.loadup.modules.upms.client.service;

import io.github.loadup.modules.upms.client.dto.UserDetailDTO;

/**
 * UPMS 外部调用接口
 */
public interface UserQueryService {

    /**
     * 获取用户基本信息
     */
    UserDetailDTO getUserById(String userId);
}
