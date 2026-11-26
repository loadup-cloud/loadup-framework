/*
 * Copyright (C) 2026 LoadUp Cloud
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
package io.github.loadup.components.configcenter.apollo;

import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
import com.ctrip.framework.apollo.openapi.client.exception.ApolloOpenApiException;
import com.ctrip.framework.apollo.openapi.dto.NamespaceReleaseDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenItemDTO;
import java.net.URI;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

final class ApolloConfigPublisher {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApolloConfigPublisher.class);

    private final ApolloOpenApiClient client;
    private final ApolloConfigCenterProperties properties;

    private ApolloConfigPublisher(ApolloOpenApiClient client, ApolloConfigCenterProperties properties) {
        this.client = client;
        this.properties = properties;
    }

    static ApolloConfigPublisher create(ApolloConfigCenterProperties properties) {
        boolean hasUrl = StringUtils.hasText(properties.getPortalUrl());
        boolean hasToken = StringUtils.hasText(properties.getPortalToken());
        boolean hasOperator = StringUtils.hasText(properties.getPortalOperator());
        if (!hasUrl && !hasToken && !hasOperator) {
            return null;
        }
        Assert.isTrue(
                hasUrl && hasToken && hasOperator,
                "Apollo publishing requires portal-url, portal-token and portal-operator");
        Assert.isTrue(
                StringUtils.hasText(properties.getAppId())
                        && StringUtils.hasText(properties.getEnv())
                        && StringUtils.hasText(properties.getCluster())
                        && StringUtils.hasText(properties.getNamespace()),
                "Apollo publishing requires app-id, env, cluster and namespace");
        URI portal = URI.create(properties.getPortalUrl());
        Assert.isTrue(
                portal.getHost() != null
                        && ("https".equals(portal.getScheme()) || "http".equals(portal.getScheme()))
                        && portal.getUserInfo() == null
                        && portal.getQuery() == null
                        && portal.getFragment() == null,
                "Apollo portal-url must be an HTTP(S) service base URI");
        var client = ApolloOpenApiClient.newBuilder()
                .withPortalUrl(portal.toString().replaceFirst("/+$", ""))
                .withToken(properties.getPortalToken())
                .withConnectTimeout(3000)
                .withReadTimeout(5000)
                .withRetryCount(0)
                .build();
        return new ApolloConfigPublisher(client, properties);
    }

    synchronized boolean publish(String key, String value) {
        Objects.requireNonNull(value, "Configuration value is required");
        var item = new OpenItemDTO();
        item.setKey(key);
        item.setValue(value);
        item.setDataChangeCreatedBy(properties.getPortalOperator());
        return editAndRelease(() -> {
            client.createOrUpdateItem(
                    properties.getAppId(),
                    properties.getEnv(),
                    properties.getCluster(),
                    properties.getNamespace(),
                    item);
            return true;
        });
    }

    synchronized boolean remove(String key) {
        return editAndRelease(() -> {
            String app = properties.getAppId();
            String env = properties.getEnv();
            String cluster = properties.getCluster();
            String namespace = properties.getNamespace();
            if (client.getItem(app, env, cluster, namespace, key) != null) {
                client.removeItem(app, env, cluster, namespace, key, properties.getPortalOperator());
                return true;
            }
            try {
                var released = client.getLatestActiveRelease(app, env, cluster, namespace);
                return released != null
                        && released.getConfigurations() != null
                        && released.getConfigurations().containsKey(key);
            } catch (RuntimeException e) {
                if (e.getCause() instanceof ApolloOpenApiException httpError && httpError.getStatus() == 404) {
                    return false;
                }
                throw e;
            }
        });
    }

    private boolean editAndRelease(BooleanSupplier edit) {
        try {
            if (!edit.getAsBoolean()) {
                return true;
            }
        } catch (RuntimeException e) {
            LOGGER.warn(
                    "Apollo Portal configuration edit failed ({})", e.getClass().getSimpleName());
            return false;
        }

        try {
            var release = new NamespaceReleaseDTO();
            release.setReleaseTitle("LoadUp configuration update");
            release.setReleasedBy(properties.getPortalOperator());
            client.publishNamespace(
                    properties.getAppId(),
                    properties.getEnv(),
                    properties.getCluster(),
                    properties.getNamespace(),
                    release);
            return true;
        } catch (RuntimeException e) {
            LOGGER.warn(
                    "Apollo Portal release failed after editing the namespace ({})",
                    e.getClass().getSimpleName());
            return false;
        }
    }
}
