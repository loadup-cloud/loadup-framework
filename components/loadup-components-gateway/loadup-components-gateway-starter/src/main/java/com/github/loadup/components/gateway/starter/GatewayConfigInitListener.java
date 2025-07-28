/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.gateway.starter;

/*-
 * #%L
 * loadup-components-gateway-starter
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

import com.github.loadup.components.gateway.cache.manager.CacheManager;
import com.github.loadup.components.gateway.cache.manager.SensitivityManager;
import com.github.loadup.components.gateway.facade.util.LogUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

/**
 * Configuration initialization when ApplicationStartedEvent
 *
 * @author Lise
 */
@Component
@Order(0)
@Slf4j
public class GatewayConfigInitListener implements ApplicationListener<ApplicationStartedEvent> {

    @Autowired
    @Qualifier("gatewayCacheManager")
    private CacheManager cacheManager;

    @Autowired
    @Qualifier("gatewaySensitivityManager")
    private SensitivityManager sensitivityManager;

    /**
     * init configuration when ApplicationStarted
     */
    @Override
    public void onApplicationEvent(ApplicationStartedEvent applicationEvent) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        log.info("========= Begin load configuration===========");

        cacheManager.init();
        sensitivityManager.init();
        stopWatch.stop();
        log.info("=========Finish load configuration,time cost is {} ms", stopWatch.getTotalTimeMillis());
    }
}
