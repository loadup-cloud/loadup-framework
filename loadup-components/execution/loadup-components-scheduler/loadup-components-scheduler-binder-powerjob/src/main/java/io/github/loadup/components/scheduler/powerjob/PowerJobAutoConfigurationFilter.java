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
package io.github.loadup.components.scheduler.powerjob;

import org.springframework.boot.autoconfigure.AutoConfigurationImportFilter;
import org.springframework.boot.autoconfigure.AutoConfigurationMetadata;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

/** Gates the native starter when another scheduler binder is selected. */
public final class PowerJobAutoConfigurationFilter implements AutoConfigurationImportFilter, EnvironmentAware {
    private static final String NATIVE_WORKER = "tech.powerjob.worker.autoconfigure.PowerJobAutoConfiguration";
    private static final String NATIVE_INTEGRATION =
            "io.github.loadup.components.scheduler.powerjob.LoadUpPowerJobAutoConfiguration";

    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public boolean[] match(String[] candidates, AutoConfigurationMetadata metadata) {
        String binder = environment.getProperty("loadup.scheduler.binder-type");
        boolean usePowerJob = "powerjob".equalsIgnoreCase(binder);
        boolean nativeMode = binder == null;
        boolean nativeWorkerConfigured = environment.containsProperty("powerjob.worker.server-address")
                || environment.containsProperty("powerjob.server-address")
                || "true".equalsIgnoreCase(environment.getProperty("powerjob.worker.enabled"));
        var matches = new boolean[candidates.length];
        for (int index = 0; index < candidates.length; index++) {
            String candidate = candidates[index];
            if (NATIVE_WORKER.equals(candidate)) {
                matches[index] = usePowerJob || (nativeMode && nativeWorkerConfigured);
            } else if (NATIVE_INTEGRATION.equals(candidate)) {
                matches[index] = usePowerJob || nativeMode;
            } else {
                matches[index] = true;
            }
        }
        return matches;
    }
}
