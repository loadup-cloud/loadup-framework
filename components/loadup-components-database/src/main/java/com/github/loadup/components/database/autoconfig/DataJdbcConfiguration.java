/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.database.autoconfig;

/*-
 * #%L
 * loadup-modules-upms-infrastructure
 * %%
 * Copyright (C) 2022 - 2024 loadup_cloud
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import com.github.loadup.commons.dataobject.BaseDO;
import com.github.loadup.components.database.config.DatabaseProperties;
import com.github.loadup.components.database.id.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.jdbc.repository.config.*;
import org.springframework.data.relational.core.mapping.event.BeforeConvertCallback;

import java.time.LocalDateTime;
import java.util.Optional;

@AutoConfiguration
@EnableJdbcAuditing
@EnableJdbcRepositories(basePackages = "com.github.loadup")
@EnableConfigurationProperties(DatabaseProperties.class)
public class DataJdbcConfiguration extends AbstractJdbcConfiguration {

    private static final Logger log = LoggerFactory.getLogger(DataJdbcConfiguration.class);

    /**
     * 提供当前时间给审计功能使用
     * <p>用于自动填充 @CreatedDate 和 @LastModifiedDate 注解的字段</p>
     */
    @Bean
    @ConditionalOnMissingBean
    public DateTimeProvider dateTimeProvider() {
        return () -> Optional.of(LocalDateTime.now());
    }

    /**
     * 创建默认的 ID 生成器
     * <p>根据配置创建相应的 ID 生成器实例</p>
     * <p>用户可以通过注册自定义的 IdGenerator Bean 来覆盖默认行为</p>
     */
    @Bean
    @ConditionalOnMissingBean(IdGenerator.class)
    public IdGenerator idGenerator(DatabaseProperties properties) {
        DatabaseProperties.IdGenerator config = properties.getIdGenerator();
        String strategy = config.getStrategy().toLowerCase();

        IdGenerator generator = switch (strategy) {
            case "uuid-v4", "uuid", "uuidv4" -> new UuidV4IdGenerator(config.isUuidWithHyphens());
            case "uuid-v7", "uuidv7" -> new UuidV7IdGenerator(config.isUuidWithHyphens());
            case "snowflake" -> new SnowflakeIdGenerator(config.getSnowflakeWorkerId(), config.getSnowflakeDatacenterId());
            case "random" -> new RandomStringIdGenerator(config.getLength());
            default -> {
                log.warn("Unknown ID generation strategy '{}', falling back to 'random'", strategy);
                yield new RandomStringIdGenerator(config.getLength());
            }
        };

        log.info("Using ID generator: {} (strategy: {})", generator.getName(), strategy);
        return generator;
    }

    /**
     * 创建实体保存前的回调
     * <p>在保存实体前自动生成 ID（如果 ID 为空）</p>
     * <p>支持用户注册的自定义 IdGenerator</p>
     */
    @Bean
    @ConditionalOnMissingBean(name = "beforeSaveCallback")
    @ConditionalOnProperty(prefix = "loadup.database.id-generator", name = "enabled", havingValue = "true", matchIfMissing = true)
    BeforeConvertCallback<BaseDO> beforeSaveCallback(ObjectProvider<IdGenerator> idGeneratorProvider) {
        IdGenerator idGenerator = idGeneratorProvider.getIfAvailable();

        if (idGenerator == null) {
            log.warn("No IdGenerator found, ID generation will be skipped");
            return entity -> entity;
        }

        return (entity) -> {
            if (entity.getId() == null) {
                String id = idGenerator.generate();
                entity.setId(id);
                log.debug("Generated ID for entity {}: {}", entity.getClass().getSimpleName(), id);
            }
            return entity;
        };
    }

    //@Bean
    //@Override
    //public JdbcCustomConversions jdbcCustomConversions() {
    //    return new JdbcCustomConversions(Arrays.asList(
    //            new ObjectToBooleanConverter(),
    //            new BooleanToObjectConverter()
    //    ));
    //}
    //
    //@ReadingConverter
    //static class ObjectToBooleanConverter implements Converter<Object, Boolean> {
    //    @Override
    //    public Boolean convert(Object source) {
    //        if (source == null) {return false;}
    //        String strVal = source.toString().toUpperCase();
    //        return "T".equals(strVal) || "Y".equals(strVal) || "1".equals(strVal);
    //    }
    //}
    //
    //@WritingConverter
    //static class BooleanToObjectConverter implements Converter<Boolean, String> {
    //    @Override
    //    public String convert(Boolean source) {
    //        return source != null && source ? "T" : "F";
    //    }
    //}
}
