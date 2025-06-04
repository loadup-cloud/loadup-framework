/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.cache.binding;

/*-
 * #%L
 * loadup-components-cache-api
 * %%
 * Copyright (C) 2022 - 2023 loadup_cloud
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

@Slf4j(topic = "DIGEST_CACHE_LOGGER")
@Component("cacheBinding")
public class DefaultCacheBinding implements CacheBinding {

    @Resource
    private CacheBinder cacheBinder;

    @Override
    public boolean set(String cacheName, String key, Object value) {
        boolean res;
        StopWatch stopWatch = new StopWatch();
        try {
            stopWatch.start();
            res = cacheBinder.set(cacheName, key, value);
            stopWatch.stop();
            log.info(
                    "binder={},method={},key={},result={},cost={}",
                    cacheBinder.getName(),
                    "set",
                    key,
                    "success",
                    stopWatch.getTotalTimeMillis());
        } catch (Exception e) {
            stopWatch.stop();
            log.info(
                    "binder={},method={},key={},result={},cost={}",
                    cacheBinder.getName(),
                    "set",
                    key,
                    "fail",
                    stopWatch.getTotalTimeMillis());
            throw e;
        }
        return res;
    }

    @Override
    public Object get(String cacheName, String key) {
        Object res;
        StopWatch stopWatch = new StopWatch();
        try {
            stopWatch.start();
            res = cacheBinder.get(cacheName, key);
            stopWatch.stop();
            log.info(
                    "binder={},method={},key={},result={},cost={}",
                    cacheBinder.getName(),
                    "get",
                    key,
                    "success",
                    stopWatch.getTotalTimeMillis());
        } catch (Exception e) {
            stopWatch.stop();
            log.info(
                    "binder={},method={},key={},result={},cost={}",
                    cacheBinder.getName(),
                    "get",
                    key,
                    "fail",
                    stopWatch.getTotalTimeMillis());
            throw e;
        }
        return res;
    }

    @Override
    public <T> T get(String cacheName, String key, Class<T> cls) {
        T res;
        StopWatch stopWatch = new StopWatch();
        try {
            stopWatch.start();
            res = cacheBinder.get(cacheName, key, cls);
            stopWatch.stop();
            log.info(
                    "binder={},method={},key={},result={},cost={}",
                    cacheBinder.getName(),
                    "get",
                    key,
                    "success",
                    stopWatch.getTotalTimeMillis());
        } catch (Exception e) {
            stopWatch.stop();
            log.info(
                    "binder={},method={},key={},result={},cost={}",
                    cacheBinder.getName(),
                    "get",
                    key,
                    "fail",
                    stopWatch.getTotalTimeMillis());
            throw e;
        }
        return res;
    }

    @Override
    public boolean delete(String cacheName, String key) {
        boolean res;
        StopWatch stopWatch = new StopWatch();
        try {
            stopWatch.start();
            res = cacheBinder.delete(cacheName, key);
            stopWatch.stop();
            log.info(
                    "binder={},method={},key={},result={},cost={}",
                    cacheBinder.getName(),
                    "delete",
                    key,
                    "success",
                    stopWatch.getTotalTimeMillis());
        } catch (Exception e) {
            stopWatch.stop();
            log.info(
                    "binder={},method={},key={},result={},cost={}",
                    cacheBinder.getName(),
                    "delete",
                    key,
                    "fail",
                    stopWatch.getTotalTimeMillis());
            throw e;
        }
        return res;
    }

    @Override
    public boolean deleteAll(String cacheName) {
        boolean res;
        StopWatch stopWatch = new StopWatch();
        try {
            stopWatch.start();
            res = cacheBinder.deleteAll(cacheName);
            stopWatch.stop();
            log.info(
                    "binder={},method={},key={},result={},cost={}",
                    cacheBinder.getName(),
                    "deleteAll",
                    "all",
                    "success",
                    stopWatch.getTotalTimeMillis());
        } catch (Exception e) {
            stopWatch.stop();
            log.info(
                    "binder={},method={},key={},result={},cost={}",
                    cacheBinder.getName(),
                    "deleteAll",
                    "all",
                    "fail",
                    stopWatch.getTotalTimeMillis());
            throw e;
        }
        return res;
    }
}
