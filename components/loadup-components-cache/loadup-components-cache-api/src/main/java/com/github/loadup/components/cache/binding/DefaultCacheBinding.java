/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.cache.binding;

/*-
 * #%L
 * loadup-components-cache-api
 * %%
 * Copyright (C) 2022 - 2025 loadup_cloud
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import com.github.loadup.components.cache.api.CacheBinder;
import com.github.loadup.components.cache.api.CacheBinding;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.util.function.Supplier;

@Slf4j(topic = "DIGEST_CACHE_LOGGER")
@Component("cacheBinding")
public class DefaultCacheBinding implements CacheBinding {

    @Resource
    private CacheBinder cacheBinder;

    @Override
    public boolean set(String cacheName, String key, Object value) {
        return execute(() -> cacheBinder.set(cacheName, key, value), "set", cacheName, key);
    }

    @Override
    public Object get(String cacheName, String key) {
        return execute(() -> cacheBinder.get(cacheName, key), "get", cacheName, key);
    }

    @Override
    public <T> T get(String cacheName, String key, Class<T> cls) {
        return execute(() -> cacheBinder.get(cacheName, key, cls), "get", cacheName, key);
    }

    @Override
    public boolean delete(String cacheName, String key) {
        return execute(() -> cacheBinder.delete(cacheName, key), "delete", cacheName, key);
    }

    @Override
    public boolean deleteAll(String cacheName) {
        return execute(() -> cacheBinder.deleteAll(cacheName), "deleteAll", cacheName, "all");
    }

    private <T> T execute(Supplier<T> supplier, String method, String cacheName, String key) {
        StopWatch stopWatch = new StopWatch();
        String resultStatus = "success";
        try {
            stopWatch.start();
            return supplier.get();
        } catch (Exception e) {
            resultStatus = "fail";
            throw e;
        } finally {
            stopWatch.stop();
            log.info(
                "binder={},method={},key={},result={},cost={}",
                cacheBinder.getName(),
                method,
                key,
                resultStatus,
                stopWatch.getTotalTimeMillis());
        }
    }
}

