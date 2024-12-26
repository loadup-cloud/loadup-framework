package com.github.loadup.components.upms.dal.repository.impl;

import com.github.loadup.components.upms.dal.repository.UserExtendRepository;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;

public class UserExtendRepositoryImpl implements UserExtendRepository {
    private final JdbcAggregateTemplate template;

    public UserExtendRepositoryImpl(JdbcAggregateTemplate template) {
        this.template = template;
    }

}
