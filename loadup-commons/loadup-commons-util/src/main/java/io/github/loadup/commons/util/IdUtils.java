package io.github.loadup.commons.util;

/*-
 * #%L
 * loadup-commons-util
 * %%
 * Copyright (C) 2026 LoadUp Cloud
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

import io.github.loadup.commons.util.internal.InternalThreadLocalMap;
import java.security.SecureRandom;
import java.util.UUID;

/**
 * 封装各种生成唯一性ID算法的工具类.
 */
public class IdUtils {

    private static final SecureRandom random = new SecureRandom();

    /**
     * 封装JDK自带的UUID, 通过Random数字生成, 中间有-分割.
     */
    public static String uuid() {
        return UUID.randomUUID().toString();
    }

    /**
     * 封装JDK自带的UUID, 通过Random数字生成, 中间无-分割.
     */
    public static String uuid2() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    /**
     * 使用SecureRandom随机生成Long.
     */
    public static long randomLong() {
        return Math.abs(random.nextLong());
    }

    /**
     * 获取随机数生成器
     *
     * @return {@link SecureRandom}
     */
    public static SecureRandom getSecureRandom() {
        return random;
    }

    /**
     * @return
     */
    public static String getRequestd() {
        return InternalThreadLocalMap.get().getRequestId();
    }

    /**
     * @param requestId
     */
    public static void setRequestId(String requestId) {
        InternalThreadLocalMap.get().setRequestId(requestId);
    }
}
