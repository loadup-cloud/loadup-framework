/*
 * #%L
 * LoadUp Components Database
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package io.github.loadup.components.database.autoconfigure;

import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.spring.boot.ConfigurationCustomizer;
import com.mybatisflex.spring.boot.MyBatisFlexCustomizer;
import javax.sql.DataSource;
import org.apache.ibatis.session.LocalCacheScope;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(afterName = "com.mybatisflex.spring.boot.v4.MybatisFlexAutoConfiguration")
@ConditionalOnClass({MyBatisFlexCustomizer.class, BaseMapper.class, MapperScan.class})
@ConditionalOnSingleCandidate(DataSource.class)
@MapperScan(basePackages = "io.github.loadup", markerInterface = BaseMapper.class, lazyInitialization = "true")
public class MyBatisFlexAutoConfiguration {

    @Bean
    public MyBatisFlexCustomizer loadUpMyBatisFlexCustomizer() {
        return config -> {
            config.setPrintBanner(false);
            config.setLogicDeleteColumn("deleted");
            config.setNormalValueOfLogicDelete(false);
            config.setDeletedValueOfLogicDelete(true);
            config.setDefaultPageSize(20);
            config.setDefaultMaxPageSize(100);
        };
    }

    @Bean
    public ConfigurationCustomizer loadUpMyBatisConfigurationCustomizer() {
        return configuration -> configuration.setLocalCacheScope(LocalCacheScope.STATEMENT);
    }
}
