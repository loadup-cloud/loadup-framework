package io.github.loadup.commons.util.date;

/*-
 * #%L
 * loadup-commons-util
 * %%
 * Copyright (C) 2022 - 2025 loadup_cloud
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

import java.time.Duration;

public class DurationUtils {
    private DurationUtils() {}

    /**
     * 解析字符串为Duration
     *
     * @param duration 字符串，格式为 PT1H2M3S
     * @return Duration
     */
    public static Duration parse(String duration) {
        String upper = duration.toUpperCase();
        if (!upper.startsWith("PT")) {
            upper = "PT" + upper;
        }
        return Duration.parse(upper);
    }

    public static long parseSeconds(String duration) {
        String upper = duration.toUpperCase();
        if (!upper.startsWith("PT")) {
            upper = "PT" + upper;
        }
        return Duration.parse(upper).getSeconds();
    }
}
