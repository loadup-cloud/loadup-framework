/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.testify.object.comparer.impl;

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

/**
 *
 * Copyright (c) 2004-2014 All Rights Reserved.
 */
import com.github.loadup.components.testify.log.TestifyLogUtil;
import com.github.loadup.components.testify.object.comparer.UnitComparer;
import com.github.loadup.components.testify.util.DateUtil;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * "D" flag 校验器（日期）
 *
 *
 * <p>备注：引入此比较器后其实compare接口应该增加参数传入额外信息，如flag为D5，那么这个数字5就应该传入进来标识日期误差范围（秒），
 * 这里为避免侵入修改原有接口暂时将误差数字作为属性在此comparer中存在，可通过此compare构造器传入（当然这样控制粒度就没有在比较时传入额外参数细了），
 * 同时由于此额外参数，此校验器默认没有加入ObjectCheck的comparers，使用时自己传入</p>
 *
 *
 *
 */
@Slf4j
public class DateComparer implements UnitComparer {

    long errSec = 0;

    public DateComparer() {
        super();
    }

    public DateComparer(long errSec) {
        super();
        this.errSec = errSec;
    }

    /**
     *
     */
    @Override
    public boolean compare(Object expect, Object actual, String comparerFlagCode) {
        // 支持误差与忽略某位两种时间校对方式,两种方式互斥,以采用忽略某位方式优先,支持now(14位)/today(8位),支持null
        // 误差范围,以秒计算
        String exp = String.valueOf(expect);
        if (StringUtils.isBlank(exp) && actual == null) {
            return true;
        }

        long errSec = this.errSec;
        // 将 now today 转化为当前日期
        if (exp.toLowerCase().equals("now")) {
            exp = DateUtil.getLongDateString(new Date());
        } else if (exp.toLowerCase().startsWith("today")) {
            if (exp.equalsIgnoreCase("today")) {
                exp = DateUtil.getDateString(new Date());
            } else {
                String diffString;
                if (exp.contains("+")) {
                    diffString = exp.substring(exp.lastIndexOf("+") + 1).trim();
                } else {
                    diffString = exp.substring(exp.lastIndexOf("today") + 5).trim();
                }
                int diff = 0;
                try {
                    diff = Integer.valueOf(diffString);
                } catch (NumberFormatException e) {
                    TestifyLogUtil.fail(log, "时间格式后缀解析错误" + expect);
                }
                exp = DateUtil.getDateString(DateUtil.addDays(new Date(), diff));
            }
        }

        Date actDate = null;
        try {
            actDate = (Date) actual;
        } catch (Exception e1) {
            try {
                //                Timestamp tt = ((TIMESTAMP) actual).timestampValue();
                actDate = new Date();
            } catch (Exception e2) {
                TestifyLogUtil.fail(log, "时间格式转化未知错误" + actual, e1);
            }
        }
        String actString = null;

        if (actDate != null) {
            // 6位:只校验时间 8位:只校验日期 其它:校验日期和时间
            switch (exp.length()) {
                case 6:
                    actString = DateUtil.getTimeString(actDate);
                    break;
                case 8:
                    actString = DateUtil.getDateString(actDate);
                    break;
                default:
                    actString = DateUtil.getLongDateString(actDate);
                    break;
            }
        }
        if (exp.contains(".") && StringUtils.isNotBlank(actString)) {
            // 如果包含 '.' 则说明采用忽略某些位
            char[] actChars = actString.toCharArray();
            for (int i = 0; i < exp.length() && i < actChars.length; i++) {
                if (exp.charAt(i) == '.') {
                    actChars[i] = '.';
                }
            }
            actString = new String(actChars);
        }

        if (null == actDate || null == actString) {
            actString = "null";
        }
        if (StringUtils.isNumeric(exp)
                && StringUtils.isNumeric(actString)
                && exp.length() == 14
                && actString.length() == 14
                && errSec > 0) {
            // 对于 exp 为长时间格式的时候,支持误差判断
            long expTime = DateUtil.parseDateLongFormat(exp).getTime() / 1000;
            long actTime = DateUtil.parseDateLongFormat(actString).getTime() / 1000;
            if (Math.abs(expTime - actTime) > errSec) {
                return false;
            }
            // errSec = Integer.valueOf(errorSeconds);
        } else if (!exp.equals(actString)) {
            return false;
        }

        return true;
    }
}
