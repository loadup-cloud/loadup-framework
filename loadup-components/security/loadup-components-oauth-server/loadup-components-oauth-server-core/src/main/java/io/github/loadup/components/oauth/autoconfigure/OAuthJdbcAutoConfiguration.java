/*
 * Copyright (C) 2025-2026 LoadUp Cloud
 *
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
 */
package io.github.loadup.components.oauth.autoconfigure;

import io.github.loadup.components.authserver.sas.SasAuthServerAutoConfiguration;
import io.github.loadup.components.oauth.client.LifecycleRegisteredClientRepository;
import io.github.loadup.components.oauth.client.OAuthClientManager;
import io.github.loadup.components.oauth.client.OAuthClientPolicy;
import io.github.loadup.components.oauth.client.OAuthClientTokenValidator;
import io.github.loadup.components.oauth.management.OAuthClientManagement;
import io.github.loadup.components.oauth.management.OAuthClientManagementConverter;
import io.github.loadup.components.oauth.management.OAuthClientManagementConverterImpl;
import java.time.Clock;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.transaction.support.TransactionTemplate;

/** Standard JDBC stores. Never imports or synchronizes a client catalog. */
@AutoConfiguration(
        before = SasAuthServerAutoConfiguration.class,
        afterName = {
            "org.springframework.boot.jdbc.autoconfigure.JdbcTemplateAutoConfiguration",
            "org.springframework.boot.jdbc.autoconfigure.DataSourceTransactionManagerAutoConfiguration",
            "io.github.loadup.components.runtime.time.ClockAutoConfiguration"
        })
@ConditionalOnClass({JdbcOperations.class, DependsOnDatabaseInitialization.class})
@ConditionalOnBean(JdbcOperations.class)
@ConditionalOnProperty(prefix = "loadup.security.oauth.persistence", name = "enabled", havingValue = "true")
@Import(OAuthClientManagementConverterImpl.class)
public class OAuthJdbcAutoConfiguration {
    @Bean
    @DependsOnDatabaseInitialization
    @ConditionalOnMissingBean(RegisteredClientRepository.class)
    public LifecycleRegisteredClientRepository registeredClientRepository(
            JdbcOperations jdbc, DataSourceTransactionManager transactions, Clock clock) {
        requireSameDataSource(jdbc, transactions);
        return new LifecycleRegisteredClientRepository(
                new JdbcRegisteredClientRepository(jdbc), jdbc, new TransactionTemplate(transactions), clock);
    }

    @Bean
    @DependsOnDatabaseInitialization
    @ConditionalOnMissingBean(OAuth2AuthorizationService.class)
    public OAuth2AuthorizationService authorizationService(JdbcOperations jdbc, RegisteredClientRepository clients) {
        return new JdbcOAuth2AuthorizationService(jdbc, clients);
    }

    @Bean
    @DependsOnDatabaseInitialization
    @ConditionalOnMissingBean(OAuth2AuthorizationConsentService.class)
    public OAuth2AuthorizationConsentService authorizationConsentService(
            JdbcOperations jdbc, RegisteredClientRepository clients) {
        return new JdbcOAuth2AuthorizationConsentService(jdbc, clients);
    }

    @Bean
    @ConditionalOnBean(LifecycleRegisteredClientRepository.class)
    @ConditionalOnSingleCandidate(DataSourceTransactionManager.class)
    @ConditionalOnMissingBean(OAuthClientManager.class)
    public OAuthClientManager oauthClientManager(
            LifecycleRegisteredClientRepository clients,
            JdbcOperations jdbc,
            DataSourceTransactionManager transactions,
            Clock clock,
            ObjectProvider<OAuthClientPolicy> policies) {
        requireSameDataSource(jdbc, transactions);
        return new OAuthClientManager(
                clients,
                jdbc,
                new TransactionTemplate(transactions),
                clock,
                policies.orderedStream().toList());
    }

    @Bean
    @ConditionalOnBean(OAuthClientManager.class)
    @ConditionalOnMissingBean(OAuthClientManagement.class)
    public OAuthClientManagement oauthClientManagement(
            OAuthClientManager manager, OAuthClientManagementConverter converter) {
        return new OAuthClientManagement(manager, converter);
    }

    @Bean
    @ConditionalOnMissingBean(OAuthClientTokenValidator.class)
    public OAuthClientTokenValidator oauthClientTokenValidator(RegisteredClientRepository clients, Clock clock) {
        return new OAuthClientTokenValidator(clients, clock);
    }

    private static void requireSameDataSource(JdbcOperations jdbc, DataSourceTransactionManager transactions) {
        if (!(jdbc instanceof JdbcTemplate template) || template.getDataSource() != transactions.getDataSource()) {
            throw new IllegalArgumentException("OAuth clients require JDBC and transactions on the same DataSource");
        }
    }
}
