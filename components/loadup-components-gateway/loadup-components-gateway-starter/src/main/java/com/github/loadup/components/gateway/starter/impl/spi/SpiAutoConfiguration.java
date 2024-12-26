package com.github.loadup.components.gateway.starter.impl.spi;

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

import com.github.loadup.components.gateway.facade.spi.LimitRuleService;
import com.github.loadup.components.gateway.facade.spi.OauthService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * <p>
 * SpiAutoConfiguration.java, v 0.1 2020-03-13 9:52 AM Laysan Exp $
 * </p>
 *
 * @author Laysan
 * @since SpiAutoConfiguration
 */
public class SpiAutoConfiguration {

    @Bean(name = "defaultOauthServiceService")
    @ConditionalOnMissingBean(value = OauthService.class)
    public OauthService getOauthService() {
        return new OauthServiceImpl();
    }

    @Bean(name = "defaultRuleService")
    @ConditionalOnMissingBean(value = LimitRuleService.class)
    public LimitRuleService getLimitRuleService() {
        return new LimitRuleServiceImpl();
    }

}
