package io.github.loadup.commons.util;

/*-
 * #%L
 * loadup-commons-util
 * %%
 * Copyright (C) 2022 - 2024 loadup_cloud
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

/*
@author Lise
 * @since 1.0.0
 */

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class ToStringUtils {
    static {
        ToStringBuilder.setDefaultStyle(ToStringStyle.JSON_STYLE);
    }

    public static String reflectionToString(Object object) {
        return ToStringBuilder.reflectionToString(object);
    }

    public static String reflectionToString(Object object, ToStringStyle style) {
        return ToStringBuilder.reflectionToString(object, style);
    }
}
