package io.github.loadup.components.cache.caffeine.binder;

/*-
 * #%L
 * loadup-components-cache-binder-caffeine
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

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import io.github.loadup.components.cache.binder.AbstractCacheBinder;
import io.github.loadup.components.cache.binder.CacheBinder;
import io.github.loadup.components.cache.caffeine.cfg.CaffeineCacheBinderCfg;
import io.github.loadup.components.cache.cfg.CacheBindingCfg;
import io.github.loadup.components.cache.model.CacheValueWrapper;
import io.github.loadup.framework.api.manager.ConfigurationResolver;
import java.time.Duration;
import java.util.Collection;
import java.util.concurrent.ThreadLocalRandom;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

@Slf4j
public class CaffeineCacheBinder extends AbstractCacheBinder<CaffeineCacheBinderCfg, CacheBindingCfg>
        implements CacheBinder<CaffeineCacheBinderCfg, CacheBindingCfg> {

    // 每一个 Binder 实例持有一个物理上的 Caffeine Cache 对象
    private Cache<String, Object> nativeCache;

    @Override
    public String getBinderType() {
        return "caffeine";
    }

    @Override
    protected void onInit() {
        // 这里的 binderCfg 是根据具体的 bizTag 路由过来的
        // 比如 loadup.cache.bindings.user-cache -> 对应的驱动配置

        Caffeine<Object, Object> builder = Caffeine.newBuilder();
        builder.ticker(ticker::read);
        // 1. 设置最大容量
        if (binderCfg.getMaximumSize() > 0) {
            builder.maximumSize(binderCfg.getMaximumSize());
        }
        if (binderCfg.isEnableRandomExpiry()) {
            // 使用自定义 Expiry 实现随机化
            builder.expireAfter(new Expiry<>() {
                @Override
                public long expireAfterCreate(Object key, Object value, long currentTime) {
                    return calculateRandomNanos(ConfigurationResolver.resolve(
                            bindingCfg.getExpireAfterWrite(), binderCfg.getExpireAfterWrite()));
                }

                @Override
                public long expireAfterUpdate(Object key, Object value, long currentTime, long currentDuration) {
                    if (binderCfg.getExpireAfterAccess() != null) {
                        return binderCfg.getExpireAfterAccess().toNanos();
                    }
                    return currentDuration;
                }

                @Override
                public long expireAfterRead(Object key, Object value, long currentTime, long currentDuration) {
                    if (binderCfg.getExpireAfterAccess() != null) {
                        return binderCfg.getExpireAfterAccess().toNanos();
                    }
                    return currentDuration;
                }
            });
        } else {
            if (binderCfg.getExpireAfterWrite() != null) {
                builder.expireAfterWrite(binderCfg.getExpireAfterWrite());
            }
            if (binderCfg.getExpireAfterAccess() != null) {
                builder.expireAfterAccess(binderCfg.getExpireAfterAccess());
            }
        }

        this.nativeCache = builder.build();
        log.info(
                "Caffeine Binder [{}] 初始化成功: max={}, expire={}",
                name,
                binderCfg.getMaximumSize(),
                binderCfg.getExpireAfterWrite());
    }

    private long calculateRandomNanos(Duration baseDuration) {
        if (baseDuration == null || baseDuration.isZero()) {
            return Long.MAX_VALUE;
        }

        long baseNanos = baseDuration.toNanos();
        double factor = getBinderCfg().getRandomFactor();

        // 计算范围: [base, base * (1+factor)]
        long max = (long) (baseNanos * (1 + factor));

        return ThreadLocalRandom.current().nextLong(baseNanos, max);
    }

    @Override
    public boolean set(String key, CacheValueWrapper value) {
        Assert.notNull(value, "Caffeine cache does not support null values");
        nativeCache.put(key, wrapValue(value));
        return true;
    }

    @Override
    public CacheValueWrapper get(String key) {
        Object value = nativeCache.get(key, k -> new CacheValueWrapper("NULL", null, null));
        // 如果配置了序列化器，且存入的是字节数组，则反序列化（实现深拷贝保护）
        if (value instanceof byte[] && serializer != null) {
            return serializer.deserialize((byte[]) value, Object.class);
        }
        return (CacheValueWrapper) value;
    }

    @Override
    public boolean delete(String key) {
        nativeCache.invalidate(key);
        return true;
    }

    @Override
    public boolean deleteAll(Collection keys) {
        nativeCache.invalidateAll(keys);
        return true;
    }

    @Override
    public void cleanUp() {
        nativeCache.cleanUp();
    }

    @Override
    protected void afterDestroy() {
        if (nativeCache != null) {
            nativeCache.cleanUp();
            log.info("Caffeine Binder [{}] 已销毁", name);
        }
    }
}
