// package io.github.loadup.modules.upms;

/*-
 * #%L
 * Loadup UPMS Test
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
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
