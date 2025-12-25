/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.cache.common.model;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Test User entity
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    private String        id;
    private String        name;
    private String        email;
    private Integer       age;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public static User createTestUser(String id) {
        return User.builder()
            .id(id)
            .name("User" + id)
            .email("user" + id + "@test.com")
            .age(25)
            .createTime(LocalDateTime.now())
            .updateTime(LocalDateTime.now())
            .build();
    }
}

