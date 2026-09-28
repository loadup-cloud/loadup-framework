package io.github.loadup.modules.upms.app.service;

import io.github.loadup.modules.upms.client.dto.UserDetailDTO;
import io.github.loadup.modules.upms.client.service.UserQueryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserQueryServiceImpl implements UserQueryService {

    private final UserService userService;

    public UserQueryServiceImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetailDTO getUserById(String userId) {
        return null;
    }

    @Override
    public List<UserDetailDTO> listUsersByIds(List<String> userIds) {
        return List.of();
    }
}
