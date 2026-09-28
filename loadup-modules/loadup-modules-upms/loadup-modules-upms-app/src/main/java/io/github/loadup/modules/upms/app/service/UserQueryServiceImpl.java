package io.github.loadup.modules.upms.app.service;

import io.github.loadup.commons.request.query.IdQuery;
import io.github.loadup.modules.upms.client.dto.UserDetailDTO;
import io.github.loadup.modules.upms.client.service.UserQueryService;
import org.springframework.stereotype.Service;

@Service
public class UserQueryServiceImpl implements UserQueryService {
    private final UserService userService;

    public UserQueryServiceImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetailDTO getUserById(String userId) {
        return userService.getUserById(new IdQuery(userId));
    }
}
