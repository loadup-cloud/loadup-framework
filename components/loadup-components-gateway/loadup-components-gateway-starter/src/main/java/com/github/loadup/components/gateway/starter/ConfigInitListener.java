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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Configuration initialization when ApplicationStartedEvent
 *
 * @author Lise
 */
@Component
@Order(0)
public class ConfigInitListener implements ApplicationListener<ApplicationStartedEvent> {
    /**
     * logger
     */
    private static final Logger logger = LoggerFactory.getLogger(ConfigInitListener.class);

    @Autowired
    @Qualifier("gatewayCacheManager")
    private CacheManager cacheManager;

    @Autowired
    @Qualifier("gateway.cache.manager.sensitivityManager")
    private SensitivityManager sensitivityManager;

    /**
     * init configuration when ApplicationStartedEvent
     */
    @Override
    public void onApplicationEvent(ApplicationStartedEvent applicationEvent) {
        long timeCost = System.currentTimeMillis();
        LogUtil.info(logger, "========= Begin load configuration===========");
        // assemble all the paramter from the out side

        // validate if each of config file path is blank

        cacheManager.init();
        sensitivityManager.init();

        LogUtil.info(logger, "========= Finish load configuration version 2.0.10 ===========");
        LogUtil.info(logger, "========= time cost is ", System.currentTimeMillis() - timeCost, " ms===========");
    }
}
