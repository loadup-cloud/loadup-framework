/*
 * Copyright 2026 LoadUp Cloud
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.loadup.components.database.autoconfigure;

import javax.sql.DataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.transaction.SpringManagedTransactionFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionManager;

/** Keeps MyBatis and JDBC work on the same single-data-source Spring transaction. */
@AutoConfiguration(
        afterName = "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration",
        beforeName = "com.mybatisflex.spring.boot.v4.FlexTransactionAutoConfiguration")
@ConditionalOnSingleCandidate(DataSource.class)
public class JdbcTransactionAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(TransactionManager.class)
    public JdbcTransactionManager transactionManager(DataSource dataSource) {
        return new JdbcTransactionManager(dataSource);
    }

    @Bean
    public static BeanPostProcessor springManagedMyBatisTransactions(
            ObjectProvider<DataSource> sources, ObjectProvider<PlatformTransactionManager> managers) {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) {
                if (bean instanceof SqlSessionFactory factory) {
                    var dataSource = sources.getObject();
                    var manager = managers.getObject();
                    if (!(manager instanceof DataSourceTransactionManager jdbcManager)
                            || jdbcManager.getDataSource() != dataSource)
                        throw new BeanCreationException(beanName,
                                "MyBatis and local JDBC work require a transaction manager for the same DataSource");
                    var configuration = factory.getConfiguration();
                    var existing = configuration.getEnvironment();
                    configuration.setEnvironment(new Environment(existing.getId(),
                            new SpringManagedTransactionFactory(), dataSource));
                }
                return bean;
            }
        };
    }
}
