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
import com.github.loadup.framework.api.annotation.BindingSpec;
import com.github.loadup.framework.api.binding.AbstractBinding;
import com.github.loadup.framework.api.config.BaseBindingCfg;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StopWatch;

@Slf4j(topic = "CACHE_BINDING_DIGEST")
@BindingSpec(type = "cache")
public class DefaultCacheBinding extends AbstractBinding<CacheBinder, BaseBindingCfg> implements CacheBinding {

    @Override
    public boolean set(String key, Object value, int exp) {
        boolean res;
        StopWatch stopWatch = new StopWatch();
        try {
            stopWatch.start();
            res = getBinder().set(key, value, exp);
            stopWatch.stop();
            log.info("binder={},method={},key={},result={},cost={}", getBinder().getName(), "set", key, "success",
                    stopWatch.getTotalTimeMillis());
        } catch (Exception e) {
            stopWatch.stop();
            log.info("binder={},method={},key={},result={},cost={}", getBinder().getName(), "set", key, "fail",
                    stopWatch.getTotalTimeMillis());
            throw e;
        }
        return res;
    }

    @Override
    public Object get(String key) {
        Object res;
        StopWatch stopWatch = new StopWatch();
        try {
            stopWatch.start();
            res = getBinder().get(key);
            stopWatch.stop();
            log.info("binder={},method={},key={},result={},cost={}", getBinder().getName(), "get", key, "success",
                    stopWatch.getTotalTimeMillis());
        } catch (Exception e) {
            stopWatch.stop();
            log.info("binder={},method={},key={},result={},cost={}", getBinder().getName(), "get", key, "fail",
                    stopWatch.getTotalTimeMillis());
            throw e;
        }
        return res;
    }

    @Override
    public boolean delete(String key) {
        boolean res;
        StopWatch stopWatch = new StopWatch();
        try {
            stopWatch.start();
            res = getBinder().delete(key);
            stopWatch.stop();
            log.info("binder={},method={},key={},result={},cost={}", getBinder().getName(), "delete", key, "success",
                    stopWatch.getTotalTimeMillis());
        } catch (Exception e) {
            stopWatch.stop();
            log.info("binder={},method={},key={},result={},cost={}", getBinder().getName(), "delete", key, "fail",
                    stopWatch.getTotalTimeMillis());
            throw e;
        }
        return res;
    }

    @Override
    public boolean deleteAll() {
        boolean res;
        StopWatch stopWatch = new StopWatch();
        try {
            stopWatch.start();
            res = getBinder().deleteAll();
            stopWatch.stop();
            log.info("binder={},method={},key={},result={},cost={}", getBinder().getName(), "deleteAll", "all", "success",
                    stopWatch.getTotalTimeMillis());
        } catch (Exception e) {
            stopWatch.stop();
            log.info("binder={},method={},key={},result={},cost={}", getBinder().getName(), "deleteAll", "all", "fail",
                    stopWatch.getTotalTimeMillis());
            throw e;
        }
        return res;
    }

}
