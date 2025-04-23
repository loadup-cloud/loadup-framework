/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.alipay.test.acts.datarule.handler;

/*-
 * #%L
 * loadup-components-test
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;



import org.apache.commons.lang3.StringUtils;
import com.alipay.test.acts.datarule.RangeRule;
import com.alipay.test.acts.datarule.RULE.ReferenceHandler;
import com.alipay.test.acts.exception.RuleExecuteException;

/**
 * 范围规则处理器。
 * 
 * @author dasong.jds
 * @version $Id: RangeRuleHandler.java, v 0.1 2015年9月24日 下午1:02:54 dasong.jds Exp $
 */
public class RangeRuleHandler implements RuleHandler<RangeRule>, BatchRuleHandler<RangeRule> {

    /** 
     * @see RuleHandler#handle(com.alipay.test.acts.datarule.RuleObject, ReferenceHandler)
     */
    @Override
    public String handle(RangeRule rule, ReferenceHandler refHandler) {
        double random = Math.random();

        String max = rule.getMaxValue();
        String min = rule.getMinValue();

        BigDecimal maxValue;
        BigDecimal minValue;

        // 通过加减整数，而保留其原始状态
        boolean isMaxNull = false;
        if (StringUtils.isEmpty(max)) {
            int margin = (int) (random * 1000);
            // 保证大于0，否则边界问题
            if (margin == 0) {
                margin = 10;
            }
            maxValue = BigDecimal.valueOf(margin);
            isMaxNull = true;
        } else {
            maxValue = new BigDecimal(max);
        }

        if (StringUtils.isEmpty(min)) {
            int margin = maxValue.multiply(BigDecimal.valueOf(random)).intValue();

            // 保证大于0，否则边界问题
            if (margin <= 0) {
                margin = 10;
            }
            minValue = maxValue.subtract(BigDecimal.valueOf(margin));
        } else {
            minValue = new BigDecimal(min);
            if (isMaxNull) {
                maxValue = minValue.add(maxValue);
            }
        }

        if (minValue.compareTo(maxValue) > 0) {
            throw new RuleExecuteException("range rule config error[minValue=" + minValue
                                           + ",maxValue=" + maxValue + "]");
        }

        // 如果包含边界，保证高频率返回
        if (rule.isContainsMax() && random > 0.7) {
            return maxValue.toPlainString();
        }
        if (rule.isContainsMin() && random < 0.3) {
            return minValue.toPlainString();
        }

        if (minValue.compareTo(maxValue) == 0 && (!rule.isContainsMax() || !rule.isContainsMin())) {
            throw new RuleExecuteException(
                "range rule config error, same value but not allowed[minValue=" + minValue
                        + ",maxValue=" + maxValue + "]");
        }

        // 不包含边界，居中取一个
        BigDecimal margin = maxValue.subtract(minValue);

        BigDecimal m2 = margin.multiply(BigDecimal.valueOf(random)).setScale(2,
            BigDecimal.ROUND_CEILING);

        if (m2.compareTo(BigDecimal.ZERO) == 0 || m2.compareTo(margin) == 0) {
            m2 = margin.multiply(BigDecimal.valueOf(0.5));
        }

        BigDecimal data = minValue.add(m2);

        if (data.compareTo(minValue) < 0 || data.compareTo(maxValue) > 0) {
            throw new RuleExecuteException("range rule execute error[minValue=" + minValue
                                           + ",maxValue=" + maxValue + ",value=" + data + "]");
        }

        return data.toPlainString();
    }

    /** 
     * @see BatchRuleHandler#batchHandle(com.alipay.test.acts.datarule.RuleObject, ReferenceHandler)
     */
    @Override
    public List<String> batchHandle(RangeRule rule, ReferenceHandler refHandler) {

        List<String> items = new ArrayList<String>();
        if (rule.isContainsMax() && !StringUtils.isBlank(rule.getMaxValue())) {
            items.add(rule.getMaxValue());
        }

        if (rule.isContainsMin() && !StringUtils.isBlank(rule.getMinValue())) {
            items.add(rule.getMinValue());
        }

        String mid;
        for (;;) {
            mid = handle(rule, refHandler);
            if (!items.contains(mid)) {
                break;
            }
        }
        items.add(mid);

        return items;
    }

}
