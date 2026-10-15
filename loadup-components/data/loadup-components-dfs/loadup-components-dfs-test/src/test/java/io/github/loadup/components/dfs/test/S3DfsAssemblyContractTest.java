/*
 * #%L
 * LoadUp Components DFS Test
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
package io.github.loadup.components.dfs.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import io.github.loadup.components.dfs.DfsProvider;
import io.github.loadup.components.dfs.s3.autoconfigure.S3DfsAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

class S3DfsAssemblyContractTest {
    private final ApplicationContextRunner contexts = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(S3DfsAutoConfiguration.class))
            .withPropertyValues("loadup.dfs.binder-type=s3", "loadup.dfs.binder.s3.bucket=test-bucket");

    @Test
    void defaultCredentialChainAndBlankEndpointBuildWithoutCallingRemoteServices() {
        contexts.withPropertyValues("loadup.dfs.binder.s3.endpoint= ")
                .run(context -> assertThat(context)
                        .hasNotFailed()
                        .hasSingleBean(S3Client.class)
                        .hasSingleBean(S3Presigner.class)
                        .hasSingleBean(DfsProvider.class));
    }

    @Test
    void partialCredentialPairsAreRejectedBeforeProvidersArePublished() {
        for (String credential : new String[] {"access-key", "secret-key"}) {
            contexts.withPropertyValues("loadup.dfs.binder.s3." + credential + "=synthetic")
                    .run(context -> assertThat(context).hasFailed());
        }
    }

    @Test
    void callerOwnedSdkClientsAndProviderAreKeptAndDifferentBinderDisablesAssembly() {
        var client = mock(S3Client.class);
        var presigner = mock(S3Presigner.class);
        var provider = mock(DfsProvider.class);
        contexts.withBean(S3Client.class, () -> client)
                .withBean(S3Presigner.class, () -> presigner)
                .withBean(DfsProvider.class, () -> provider)
                .run(context -> {
                    assertThat(context.getBean(S3Client.class)).isSameAs(client);
                    assertThat(context.getBean(S3Presigner.class)).isSameAs(presigner);
                    assertThat(context.getBean(DfsProvider.class)).isSameAs(provider);
                });
        contexts.withPropertyValues("loadup.dfs.binder-type=local")
                .run(context ->
                        assertThat(context).doesNotHaveBean(S3Client.class).doesNotHaveBean(DfsProvider.class));
    }
}
