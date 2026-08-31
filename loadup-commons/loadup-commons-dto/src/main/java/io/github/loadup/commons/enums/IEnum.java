package io.github.loadup.commons.enums;

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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public interface IEnum {

    String getCode();

    String getDescription();

    /**
     * 优化的枚举转换工具
     */
    class EnumLookup {
        // 如果对内存极其敏感，可考虑使用 WeakHashMap 或添加清理机制
        private static final Map<Class<?>, Map<String, Object>> NAME_CACHE = new ConcurrentHashMap<>();
        private static final Map<Class<?>, Map<String, Object>> CODE_CACHE = new ConcurrentHashMap<>();

        /**
         * 核心查找逻辑：支持按 Name 或 Code 查找
         *
         * @param isName 为 true 按 Enum.name() 查找，为 false 按 IEnum.getCode() 查找
         */
        @SuppressWarnings("unchecked")
        private static <T extends Enum<T> & IEnum> T get(Class<T> enumClass, String value, boolean isName) {
            if (value == null || value.trim().isEmpty()) return null;

            Map<Class<?>, Map<String, Object>> targetRootCache = isName ? NAME_CACHE : CODE_CACHE;

            Map<String, Object> lookupMap = targetRootCache.computeIfAbsent(enumClass, key -> {
                T[] constants = enumClass.getEnumConstants();
                Map<String, Object> map = new HashMap<>(constants.length * 2);
                for (T constant : constants) {
                    String lookupKey = isName ? constant.name() : constant.getCode();
                    if (lookupKey != null) {
                        map.put(lookupKey.toUpperCase(), constant);
                    }
                }
                return map;
            });

            T result = (T) lookupMap.get(value.trim().toUpperCase());
            if (result == null) {
                throw new IllegalArgumentException(
                        String.format("Unknown enum [%s] for value: %s", enumClass.getSimpleName(), value));
            }
            return result;
        }

        public static <T extends Enum<T> & IEnum> T fromName(Class<T> enumClass, String name) {
            return get(enumClass, name, true);
        }

        public static <T extends Enum<T> & IEnum> T fromCode(Class<T> enumClass, String code) {
            return get(enumClass, code, false);
        }
    }
}
