/*
 * #%L
 * Loadup Launcher
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
package io.github.loadup.application.arch;

import static org.junit.jupiter.api.Assertions.*;

import io.github.loadup.commons.result.ResultCode;
import java.util.HashSet;
import org.junit.jupiter.api.Test;

class ErrorCodeContractTest {
    @Test
    void moduleErrorCodesAreValidUniqueAndNamespaced() throws Exception {
        var seen = new HashSet<String>();
        for (String module : java.util.List.of(
                "audit", "contract", "dictionary", "file", "merchant", "notification", "transfer", "upms")) {
            String name = module.substring(0, 1).toUpperCase(java.util.Locale.ROOT) + module.substring(1);
            Class<?> type =
                    Class.forName("io.github.loadup.modules." + module + ".client.error." + name + "ResultCodeEnum");
            assertTrue(type.isEnum());
            for (Object value : type.getEnumConstants()) {
                ResultCode code = ResultCode.requireFailure((ResultCode) value);
                assertTrue(code.getCode().startsWith(module.toUpperCase(java.util.Locale.ROOT) + "_"));
                assertTrue(seen.add(code.getCode()), "Duplicate code: " + code.getCode());
            }
        }
    }
}
