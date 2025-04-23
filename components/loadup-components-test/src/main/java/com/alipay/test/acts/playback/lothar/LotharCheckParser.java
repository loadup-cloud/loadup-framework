/** Alipay.com Inc. Copyright (c) 2004-2019 All Rights Reserved. */
package com.alipay.test.acts.playback.lothar;

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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.alibaba.fastjson2.JSON;
import com.alipay.test.acts.playback.lothar.check.CaseReport;
import com.alipay.test.acts.playback.lothar.check.CheckPointDiff;
import com.alipay.test.acts.playback.lothar.check.FieldDiff;
import org.testng.Assert;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author qingqin
 * @version $Id: LotharCheckParser.java, v 0.1 2019年08月21日 下午9:29 qingqin Exp $
 */
public class LotharCheckParser {

    private static final Logger logger = LoggerFactory.getLogger(LotharCheckParser.class);

    /**
     * 对mock校验结果进行断言
     *
     * @param report
     * @param callBack
     */
    public static void print(String report, IgnoreCallBack callBack) {

        if (StringUtils.isEmpty(report)) {
            return;
        }

        CaseReport caseReport = JSON.parseObject(report, CaseReport.class);

        LinkedList<CheckPointDiff> diffList = caseReport.getPointDiffList();

        LinkedList<CheckPointDiff> exclueIgnore = new LinkedList<CheckPointDiff>();

        for (CheckPointDiff diff : diffList) {
            boolean ignore = callBack != null && callBack.ignore(diff);
            if (ignore) {
                continue;
            }

            exclueIgnore.add(diff);
        }

        if (exclueIgnore.isEmpty()) {
            return;
        }

        caseReport.setPointDiffList(exclueIgnore);

        Assert.fail(JSON.toJSONString(caseReport));
    }

    /**
     * mock是否校验回调类
     */
    public abstract static class IgnoreCallBack {

        boolean ignore(CheckPointDiff diff) {

            // 先校验最上级class是否过滤
            List<String> ignoreClasses = ignoreClasses();

            if (ignoreClasses != null) {
                for (String ignoreClass : ignoreClasses) {
                    if (diff.getClassName().contains(ignoreClass)) {
                        return true;
                    }
                }
            }

            // 次校验method是否过滤
            List<String> ignoreMethods = ignoreMethods();

            if (ignoreMethods != null) {
                for (String ignoreMethod : ignoreMethods) {
                    if (diff.getMethod().contains(ignoreMethod)) {
                        return true;
                    }
                }
            }

            // 若无上述过滤条件，再校验细粒度字段是否过滤
            List<String> ignoreCheckInfo = ignoreCheck();
            if (ignoreCheckInfo != null) {
                for (String ignoreInfo : ignoreCheckInfo) {
                    if (hitIgnoreInfo(ignoreInfo, diff)) {
                        return true;
                    }
                }
            }

            // 都没命中过滤条件
            return false;
        }

        private boolean hitIgnoreInfo(String ignoreInfo, CheckPointDiff diff) {
            ActsLotharMockCheckModel checkModel = new ActsLotharMockCheckModel(ignoreInfo);

            // checkModel初始化对于没设值的默认为空字符串，contains默认包含空字符串，无需对checkModel字段判空
            if (diff.getClassName().contains(checkModel.getClassName())
                    && diff.getMethod().contains(checkModel.getMethodName())) {

                // 未精确到字段索引，可直接过滤
                if (null == checkModel.getIndex()) {
                    return true;
                }

                if (checkModel.getIndex().intValue() == diff.getIndex()) {
                    // 未明确字段名，可直接过滤
                    if (checkModel.getFieldName().isEmpty()) {
                        return true;
                    }

                    // 尝试匹配字段，并移除目标过滤字段
                    Iterator<FieldDiff> iterator = diff.getDifference().iterator();
                    while (iterator.hasNext()) {
                        FieldDiff field = iterator.next();
                        if (null == field.getField()) {
                            logger.warn("hitIgnoreInfo check field error:" + field.toString());
                        }
                        if (null != field.getField() && field.getField().contains(checkModel.getFieldName())) {
                            iterator.remove();
                        }
                    }

                    if (diff.getDifference().isEmpty()) {
                        return true;
                    }
                }
            }

            return false;
        }

        /**
         * 框架对于各个mock节点的入参输入进行校验，该方法设置不校验的类
         *
         * @return
         */
        public abstract List<String> ignoreClasses();

        /**
         * 框架对于各个mock节点的入参输入进行校验，该方法设置不校验的方法
         *
         * @return
         */
        public abstract List<String> ignoreMethods();

        /**
         * 是否校验MOCK的相关内容，这个地方较为复杂，四层结构：类#方法#第几个参数#某个属性 简单对象只需要指定到第三层即可
         *
         * @return
         */
        public abstract List<String> ignoreCheck();

        /** 设置对于结果对象的某个字段不校验，格式:类(子类)#属性名#是否校验(Y,N) */
        public abstract void ignoreResultCheck();
    }
}
