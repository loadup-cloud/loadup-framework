/*
 * #%L
 * LoadUp Components JobRunr
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
package io.github.loadup.components.jobrunr;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.JarURLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.jar.JarFile;
import org.jobrunr.jobs.Job;
import org.junit.jupiter.api.Test;

class JobRunrDistributionIT {
    private static final String PATCHED_CLASS = "org/jobrunr/server/tasks/zookeeper/ProcessRecurringJobsTask.class";
    private static final String PATCHED_SOURCE = PATCHED_CLASS.replace(".class", ".java");

    @Test
    void changesOnlyThePatchedClassAndPreservesAllOtherUpstreamResources() throws Exception {
        var upstreamResource =
                Collections.list(Job.class.getClassLoader().getResources("org/jobrunr/jobs/Job.class")).stream()
                        .filter(url -> url.getProtocol().equals("jar"))
                        .filter(url -> url.toExternalForm().contains("/jobrunr-"))
                        .findFirst()
                        .orElseThrow();
        var connection = (JarURLConnection) upstreamResource.openConnection();
        try (var upstream =
                        new JarFile(Path.of(connection.getJarFileURL().toURI()).toFile());
                var patched = new JarFile(artifact(false).toFile())) {
            for (var entry : Collections.list(upstream.entries())) {
                if (entry.isDirectory() || entry.getName().equals("META-INF/MANIFEST.MF")) continue;
                var replacement = patched.getJarEntry(entry.getName());
                assertThat(replacement).as(entry.getName()).isNotNull();
                byte[] originalBytes;
                byte[] patchedBytes;
                try (var original = upstream.getInputStream(entry);
                        var output = patched.getInputStream(replacement)) {
                    originalBytes = original.readAllBytes();
                    patchedBytes = output.readAllBytes();
                }
                if (entry.getName().equals(PATCHED_CLASS)) {
                    assertThat(Arrays.equals(patchedBytes, originalBytes))
                            .as(entry.getName())
                            .isFalse();
                    assertThat(new String(patchedBytes, StandardCharsets.ISO_8859_1))
                            .contains("RecurringJobCacheInvalidator");
                } else {
                    assertThat(Arrays.equals(patchedBytes, originalBytes))
                            .as(entry.getName())
                            .isTrue();
                }
            }
            var manifest = patched.getManifest().getMainAttributes();
            assertThat(manifest.getValue("Multi-Release")).isEqualTo("true");
            assertThat(manifest.getValue("Automatic-Module-Name")).isEqualTo("org.jobrunr.core");
        }
    }

    @Test
    void shipsCompletePatchedSourcesAndLicenseNotices() throws Exception {
        try (var sources = new JarFile(artifact(true).toFile())) {
            assertThat(sources.getJarEntry("org/jobrunr/jobs/Job.java")).isNotNull();
            assertThat(sources.getJarEntry("io/github/loadup/components/jobrunr/RecurringJobCacheInvalidator.java"))
                    .isNotNull();
            assertThat(sources.getJarEntry("META-INF/JOBRUNR-LICENSE.md")).isNotNull();
            assertThat(sources.getJarEntry("META-INF/License-lgpl.md")).isNotNull();
            assertThat(sources.getJarEntry("META-INF/GPL-3.0.txt")).isNotNull();
            assertThat(sources.getJarEntry("META-INF/JOBRUNR-PATCH-NOTICE.txt")).isNotNull();
            try (var source = sources.getInputStream(sources.getJarEntry(PATCHED_SOURCE))) {
                assertThat(new String(source.readAllBytes(), StandardCharsets.UTF_8))
                        .contains("RecurringJobCacheInvalidator.refresh(recurringJobRuns, recurringJobs,");
            }
        }
    }

    private Path artifact(boolean sources) throws Exception {
        try (var paths = Files.list(Path.of("target"))) {
            return paths.filter(path -> path.getFileName().toString().startsWith("loadup-components-jobrunr-"))
                    .filter(path -> path.toString().endsWith(sources ? "-sources.jar" : ".jar"))
                    .filter(path -> sources || !path.toString().endsWith("-sources.jar"))
                    .filter(path -> !path.toString().endsWith("-javadoc.jar"))
                    .findFirst()
                    .orElseThrow();
        }
    }
}
