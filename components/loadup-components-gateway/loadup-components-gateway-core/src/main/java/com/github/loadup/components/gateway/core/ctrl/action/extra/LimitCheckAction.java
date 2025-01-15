package com.github.loadup.components.gateway.core.ctrl.action.extra;

/*-
 * #%L
 * loadup-components-gateway-core
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

import com.github.loadup.components.gateway.common.error.LimitRuleErrorCode;
import com.github.loadup.components.gateway.common.exception.GatewayException;
import com.github.loadup.components.gateway.common.exception.util.AssertUtil;
import com.github.loadup.components.gateway.core.common.util.LimitUtil;
import com.github.loadup.components.gateway.core.ctrl.action.AbstractBusinessAction;
import com.github.loadup.components.gateway.core.ctrl.action.BusinessAction;
import com.github.loadup.components.gateway.core.ctrl.context.GatewayRuntimeProcessContext;
import com.github.loadup.components.gateway.core.model.InterfaceConfig;
import com.github.loadup.components.gateway.core.prototype.util.DigestLoggerUtil;
import com.github.loadup.components.gateway.facade.model.LimitConfig;
import com.github.loadup.components.gateway.facade.spi.LimitRuleService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * <p>
 * LimitCheckAction.java
 * </p>
 */
@Component("limitCheckAction")
public class LimitCheckAction extends AbstractBusinessAction {

	/**
	 * limit product center query service
	 */
	@Resource
	private LimitUtil limitUtil;

	@Resource
	private LimitRuleService limitRuleService;

	@Override
	public void doBusiness(GatewayRuntimeProcessContext context) {

		InterfaceConfig interfaceConfig = context.getIntegratorInterfaceConfig();

		List<LimitConfig> limitConfigList =
				limitUtil.getLimitConfig(interfaceConfig, context.getTransactionType());

		int size = limitConfigList.size();

		for (int i = 0; i < size; i++) {
			LimitConfig limitConfig = limitConfigList.get(i);
			if (limitConfig.isEnableLimit()) {
				try {
					applyLimitToken(limitConfig);
				} catch (GatewayException ex) {
					DigestLoggerUtil.printLimitDigestLog(interfaceConfig.getInterfaceId(),
							limitConfig.getLimitValue() + "", ex.getErrorCode());
					throw ex;
				}
			}
		}

		context.setLimitConfigList(limitConfigList);
	}

	/**
	 * apply the limit service
	 */
	private void applyLimitToken(LimitConfig limitConfig) {

		AssertUtil.isTrue(limitConfig.getLimitValue() != 0, LimitRuleErrorCode.LIMIT_NO_TOKEN,
				"limit value is zero, no token:" + limitConfig.getEntryKeyId());
		if (limitConfig.getLimitValue() > 0) {
			boolean result = limitRuleService.applyToken(limitConfig);
			AssertUtil.isTrue(result, LimitRuleErrorCode.LIMIT_NO_TOKEN,
					"limit result is not pass, no token:" + limitConfig.getEntryKeyId());
		}
	}

	@Override
	@Resource
	@Qualifier("sendToIntegratorAction")
	public void setNextAction(BusinessAction sendToIntegratorAction) {
		this.nextAction = sendToIntegratorAction;
	}

}
