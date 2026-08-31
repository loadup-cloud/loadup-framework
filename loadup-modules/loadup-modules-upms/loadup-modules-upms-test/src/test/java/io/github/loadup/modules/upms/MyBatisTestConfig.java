// package io.github.loadup.modules.upms;

/*-
 * #%L
 * Loadup UPMS Test
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
//
// import com.zaxxer.hikari.HikariConfig;
// import com.zaxxer.hikari.HikariDataSource;
// import javax.sql.DataSource;
// import org.apache.ibatis.session.SqlSessionFactory;
// import org.mybatis.spring.SqlSessionFactoryBean;
// import org.mybatis.spring.annotation.MapperScan;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.boot.test.context.TestConfiguration;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Profile;
// import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
// import org.springframework.jdbc.datasource.DataSourceTransactionManager;
// import org.springframework.transaction.PlatformTransactionManager;
//
// @TestConfiguration
// @Profile("test")
// @MapperScan("io.github.loadup.modules.upms.infrastructure.mapper")
// public class MyBatisTestConfig {
//
//  @Bean
//  public DataSource dataSource(
//      @Value("${spring.datasource.url}") String url,
//      @Value("${spring.datasource.username}") String username,
//      @Value("${spring.datasource.password}") String password,
//      @Value("${spring.datasource.driver-class-name}") String driverClassName) {
//    HikariConfig config = new HikariConfig();
//    config.setJdbcUrl(url);
//    config.setUsername(username);
//    config.setPassword(password);
//    config.setDriverClassName(driverClassName);
//    config.setMaximumPoolSize(10);
//    config.setMinimumIdle(2);
//    config.setConnectionTimeout(30000);
//    config.setIdleTimeout(600000);
//    config.setMaxLifetime(1800000);
//    return new HikariDataSource(config);
//  }
//
//  @Bean
//  public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
//    SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
//    factoryBean.setDataSource(dataSource);
//    factoryBean.setMapperLocations(
//        new PathMatchingResourcePatternResolver().getResources("classpath*:mapper/**/*.xml"));
//
//    org.apache.ibatis.session.Configuration configuration =
//        new org.apache.ibatis.session.Configuration();
//    configuration.setMapUnderscoreToCamelCase(true);
//    factoryBean.setConfiguration(configuration);
//
//    return factoryBean.getObject();
//  }
//
//  @Bean
//  public PlatformTransactionManager transactionManager(DataSource dataSource) {
//    return new DataSourceTransactionManager(dataSource);
//  }
// }
