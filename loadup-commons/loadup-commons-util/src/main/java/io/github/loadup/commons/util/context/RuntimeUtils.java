/*
 * #%L
 * Loadup Common Utils
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
package io.github.loadup.commons.util.context;

/**
 * @author Lise
 * @since 1.0.0
 */
public class RuntimeUtils {

    /**
     *
     * @since 4.0.5
     */
    public static void addShutdownHook(Runnable hook) {
        Runtime.getRuntime().addShutdownHook((hook instanceof Thread) ? (Thread) hook : new Thread(hook));
    }

    /**
     *
     *
     * @since 5.3.0
     */
    public static int getProcessorCount() {
        int cpu = Runtime.getRuntime().availableProcessors();
        if (cpu <= 0) {
            cpu = 7;
        }
        return cpu;
    }

    /**
     *
     * @since 5.3.0
     */
    public static long getFreeMemory() {
        return Runtime.getRuntime().freeMemory();
    }

    /**
     *
     * @since 5.3.0
     */
    public static long getTotalMemory() {
        return Runtime.getRuntime().totalMemory();
    }

    /**
     *
     * @since 5.3.0
     */
    public static long getMaxMemory() {
        return Runtime.getRuntime().maxMemory();
    }

    public static long getUsableMemory() {
        return getMaxMemory() - getTotalMemory() + getFreeMemory();
    }
}
