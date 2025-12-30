package com.github.loadup.components.cache.binding;

/*-
 * #%L
 * loadup-components-cache-api
 * %%
 * Copyright (C) 2022 - 2025 loadup_cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
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

