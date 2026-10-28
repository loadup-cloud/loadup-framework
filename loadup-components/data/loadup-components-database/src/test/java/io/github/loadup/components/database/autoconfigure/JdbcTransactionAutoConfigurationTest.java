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
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.defaults.DefaultSqlSessionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.transaction.SpringManagedTransactionFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class JdbcTransactionAutoConfigurationTest {
    @Test
    void myBatisUsesTheSameDataSourceAndSpringTransactionFactory() {
        var source = mock(DataSource.class);
        var beans = new DefaultListableBeanFactory();
        beans.registerSingleton("dataSource", source);
        beans.registerSingleton("transactionManager", new JdbcTransactionManager(source));
        var configuration = new Configuration();
        configuration.setEnvironment(new Environment("original", new JdbcTransactionFactory(), mock(DataSource.class)));
        var factory = new DefaultSqlSessionFactory(configuration);
        var processor = JdbcTransactionAutoConfiguration.springManagedMyBatisTransactions(
                beans.getBeanProvider(DataSource.class), beans.getBeanProvider(PlatformTransactionManager.class));

        assertSame(factory, processor.postProcessAfterInitialization(factory, "sqlSessionFactory"));
        assertSame(source, configuration.getEnvironment().getDataSource());
        assertEquals("original", configuration.getEnvironment().getId());
        assertInstanceOf(SpringManagedTransactionFactory.class, configuration.getEnvironment().getTransactionFactory());
    }

    @Test
    void rejectsTransactionManagerForAnotherDataSource() {
        var beans = new DefaultListableBeanFactory();
        beans.registerSingleton("dataSource", mock(DataSource.class));
        beans.registerSingleton("transactionManager", new JdbcTransactionManager(mock(DataSource.class)));
        var configuration = new Configuration();
        configuration.setEnvironment(new Environment("original", new JdbcTransactionFactory(), mock(DataSource.class)));
        var processor = JdbcTransactionAutoConfiguration.springManagedMyBatisTransactions(
                beans.getBeanProvider(DataSource.class), beans.getBeanProvider(PlatformTransactionManager.class));
        assertThrows(BeanCreationException.class,
                () -> processor.postProcessAfterInitialization(new DefaultSqlSessionFactory(configuration), "sqlSessionFactory"));
    }
}
