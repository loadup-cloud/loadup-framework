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

import com.github.loadup.components.gateway.common.error.LimitRuleErrorCode;
import com.github.loadup.commons.error.CommonException;
import com.github.loadup.components.gateway.common.exception.util.AssertUtil;
import com.github.loadup.components.gateway.core.common.Constant;
import com.github.loadup.components.gateway.core.common.GatewayErrorCode;
import com.github.loadup.components.gateway.facade.enums.*;
import com.github.loadup.components.gateway.facade.model.LimitConfig;
import com.github.loadup.components.gateway.facade.spi.LimitRuleService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Limit Rule service
 * contains standalone and distributed service, chosen by config
 *
 * @author Laysan
 * @Date: 2020/8/11 4:01 PM
 */
@Service
public class LimitRuleServiceImpl implements LimitRuleService {

	private static final Logger logger = LoggerFactory
			.getLogger(LimitRuleService.class);

	/**
	 * limit util
	 */
	//@Autowired
	//private LimitUtil           limitUtil;

	/**
	 * Distributed Limit ServiceL
	 */
	public boolean applyTokenDistributed(LimitConfig limitConfig) {

		LimitTimeRuleEnum timeRule = limitConfig.getLimitTimeRule();
		if (timeRule == null) {
			//LogUtil.error(logger, "Please input the correct limit time rule (QPS/QPM)");
			throw new CommonException(GatewayErrorCode.CONFIGURATION_NOT_FOUND,
					"Wrong limit time rule type!");
		}

		int expire = 0;
		String wrappedKey = null;
		switch (timeRule) {
			case QPM:
				long minute = System.currentTimeMillis() / 60000L;
				wrappedKey = limitConfig.getEntryKeyId() + ":" + minute;
				expire = Constant.EXPIRE_TIME_QPM;
				break;
			case QPS:
				long sec = System.currentTimeMillis() / 1000L;
				wrappedKey = limitConfig.getEntryKeyId() + ":" + sec;
				expire = Constant.EXPIRE_TIME_QPS;
				break;
			case CONCURRENCY:
				wrappedKey = limitConfig.getEntryKeyId();
				expire = Constant.EXPIRE_TIME_QPM;
				break;
			default:
				//LogUtil.error(logger, "Please input the correct limit time rule (QPS/QPM)");
				throw new CommonException(GatewayErrorCode.CONFIGURATION_NOT_FOUND,
						"Wrong limit time rule type!");
		}

		try {
			// kv binding as the counter
			int res = 0;//kvBinding.incr(wrappedKey, Constant.INCR_VAL, Constant.INCR_DEFAULT_VAL,
			//expire);
			if (res > limitConfig.getLimitValue()) {
				//LogUtil.info(logger,
				//    "In distributed mode, GATEWAY is limited at: " + limitConfig.getLimitValue());
			}
			return res <= limitConfig.getLimitValue();
		} catch (Exception exception) {
			//LogUtil.error(logger, "Kv Binding cache exception! Distributed limit service not work!",
			//    exception);
			// check and task fallback strategy
			return takeFallbackStrategy(limitConfig);
		}
	}

	/**
	 * Handle the cache exception
	 */
	private boolean takeFallbackStrategy(LimitConfig limitConfig) {
		DistributedExceptionStrategy strategy = limitConfig.getDistributedFallbackStrategy();
		if (strategy == null) {
			return true;
		}
		switch (strategy) {
			case STANDALONE:
				//LogUtil.info(logger, "Fallback to standalone limit service.");
				LimitTimeRuleEnum timeRuleEnum = limitConfig.getLimitTimeRule();
				if (timeRuleEnum != LimitTimeRuleEnum.QPS
						&& timeRuleEnum != LimitTimeRuleEnum.CONCURRENCY) {
					//should not fallback to standalone in other mode but QPS and CONCURRENCY
					return true;
				}
				return doFallbackStandalone(limitConfig);
			case BLOCK:
				//LogUtil.info(logger, "Block this request.");
				return false;
			case PASS:
				//LogUtil.info(logger, "No limit service now.");
				return true;
			default:
				return true;
		}
	}

	/**
	 * do fallback to standalone
	 */
	private boolean doFallbackStandalone(LimitConfig limitConfig) {
		if (limitConfig.getDistributedFallbackStrategyLimitValue() == null) {
			return true;
		}
		AssertUtil.isTrue(limitConfig.getDistributedFallbackStrategyLimitValue() != 0,
				LimitRuleErrorCode.LIMIT_NO_TOKEN,
				"do fallback standalone no token:" + limitConfig.getEntryKeyId());
		if (limitConfig.getDistributedFallbackStrategyLimitValue() < 0) {
			return true;
		}
		return applyTokenStandalone(limitConfig.getEntryKeyId(), limitConfig.getLimitTimeRule(),
				limitConfig.getDistributedFallbackStrategyLimitValue());
	}

	/**
	 * Check if the string of number is illegal
	 */
	public int checkMaxLimit(String strMaxLimit) {
		int maxLimit;
		try {
			maxLimit = Integer.parseInt(strMaxLimit);
		} catch (NumberFormatException e) {
			//LogUtil.error(logger, "The value has to be an Integer!", e);
			throw new CommonException(GatewayErrorCode.PARAM_ILLEGAL,
					"Please input an Integer!");
		}

		return maxLimit;
	}

	/**
	 * Standalone mode limit service
	 */
	public synchronized boolean applyTokenStandalone(String entryKeyId,
													LimitTimeRuleEnum limitTimeRuleEnum,
													Integer limitValue) {
		initFlowRules(entryKeyId, limitTimeRuleEnum, limitValue);
		//Entry entry = null;
		//try {
		//    // define the resource to limit
		//    entry = SphU.entry(entryKeyId);
		//} catch (BlockException exception) {
		//    // already limited
		//    //LogUtil.info(logger, "In standalone mode, GATEWAY is limited at: " + entryKeyId);
		//    return false;
		//} finally {
		//    if (entry != null) {
		//        entry.exit();
		//    }
		//}
		return true;
	}

	/**
	 * Standalone mode reset token limit service
	 */
	public synchronized boolean resetTokenStandalone(String entityId) {
		//Entry entry = null;
		//try {
		//    // define the resource to limit
		//    entry = SphU.entry(entityId, Constant.DEFAULT_RELEASE_LIMIT_VALUE);
		//} catch (BlockException exception) {
		//    // already limited
		//    //LogUtil.info(logger, "In release token mode, GATEWAY is limited at: " + entityId);
		//    return false;
		//} finally {
		//    if (entry != null) {
		//        entry.exit();
		//    }
		//}
		return true;
	}

	/**
	 * Configuration of flow rules of Sentinel for standalone limit service
	 */
	public void initFlowRules(String entryKeyId, LimitTimeRuleEnum limitTimeRuleEnum,
							Integer limitValue) {
		//List<FlowRule> rules = new ArrayList<>();
		//FlowRule rule = new FlowRule();
		//rule.setResource(entryKeyId);
		//
		//if (limitTimeRuleEnum == null) {
		//    //LogUtil.error(logger, "Please input the correct limit time rule (QPS/QPM)");
		//    throw new GatewayException(GatewayliteErrorCode.CONFIGURATION_NOT_FOUND,
		//        "Wrong limit time rule type!");
		//}
		//if (limitTimeRuleEnum != LimitTimeRuleEnum.QPS
		//    && limitTimeRuleEnum != LimitTimeRuleEnum.CONCURRENCY) {
		//    //LogUtil.warn(logger, "Standalone do not support other rule but QPS or CONCURRENCY");
		//    throw new GatewayException(GatewayliteErrorCode.PARAM_ILLEGAL,
		//        "Wrong limit time rule type!");
		//}
		//
		//if (limitTimeRuleEnum == LimitTimeRuleEnum.QPS) {
		//    rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
		//} else {
		//    rule.setGrade(RuleConstant.FLOW_GRADE_THREAD);
		//}
		//// Set limit to maxLimit.
		//rule.setCount(limitValue);
		//rules.add(rule);
		//FlowRuleManager.loadRules(rules);
	}

	/**
	 * The Limit service
	 */
	@Override
	public boolean applyToken(LimitConfig limitConfig) {
		if (!limitConfig.isEnableLimit()) {
			return true;
		}

		// limit service standalone or distributed
		LimitTypeEnum limitTypeEnum = limitConfig.getLimitType();
		if (limitTypeEnum == null) {
			return applyTokenStandalone(limitConfig.getEntryKeyId(), limitConfig.getLimitTimeRule(),
					limitConfig.getLimitValue());
		}

		switch (limitTypeEnum) {
			case DISTRIBUTED:
				return applyTokenDistributed(limitConfig);
			case STANDALONE:
			default:
				return applyTokenStandalone(limitConfig.getEntryKeyId(),
						limitConfig.getLimitTimeRule(), limitConfig.getLimitValue());
		}
	}

	/**
	 * release the token
	 */
	@Override
	public void resetToken(LimitConfig limitConfig) {
		String entryKeyId = limitConfig.getEntryKeyId();
		LimitTypeEnum limitType = limitConfig.getLimitType();
		Boolean openLimitService = null;// limitUtil.getOpenLimitService();
		if (StringUtils.isBlank(entryKeyId) || openLimitService == null || !openLimitService) {
			return;
		}

		if (limitType == LimitTypeEnum.DISTRIBUTED) {
			//kvBinding.incr(entryKeyId, Constant.DEFAULT_RELEASE_LIMIT_VALUE, 0,
			//    Constant.EXPIRE_TIME_QPS);
		} else {
			// reset token standalone
			resetTokenStandalone(entryKeyId);
		}
	}

	/**
	 * Get the current connection number of the gateway by entity id
	 */
	public String getConnectionNumber(String entityId) {
		return "";//(String) kvBinding.get(entityId);
	}
}
