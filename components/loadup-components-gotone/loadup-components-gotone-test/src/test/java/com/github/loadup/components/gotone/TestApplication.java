package com.github.loadup.components.gotone;

/*-
 * #%L
 * loadup-components-gotone-test
 * %%
 * Copyright (C) 2026 LoadUp Cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.github.loadup.commons.dataobject.BaseDO;
import com.github.loadup.components.database.id.IdGenerator;
import com.github.loadup.components.database.id.UuidV4IdGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.jdbc.repository.config.EnableJdbcAuditing;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.data.relational.core.mapping.event.BeforeConvertCallback;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 测试配置类
 */
@SpringBootApplication
@EnableJdbcRepositories(basePackages = "com.github.loadup.components.gotone.repository")
@EnableJdbcAuditing
public class TestApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }

    /**
     * ID 生成器
     */
    @Bean
    public IdGenerator idGenerator() {
        return new UuidV4IdGenerator(false);
    }

    /**
     * 日期时间提供者
     */
    @Bean
    public DateTimeProvider dateTimeProvider() {
        return () -> Optional.of(LocalDateTime.now());
    }

    /**
     * 保存前回调 - 自动生成 ID
     */
    @Bean
    BeforeConvertCallback<BaseDO> beforeSaveCallback(IdGenerator idGenerator) {
        return (entity) -> {
            if (entity.getId() == null) {
                entity.setId(idGenerator.generate());
            }
            return entity;
        };
    }
}

