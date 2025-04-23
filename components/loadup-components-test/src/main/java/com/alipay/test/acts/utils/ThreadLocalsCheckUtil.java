package com.alipay.test.acts.utils;

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
import com.alipay.test.acts.constant.ActsConstants;
import com.alipay.test.acts.driver.ActsConfiguration;
import org.testng.Assert;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 线程上下文校验工具类
 * @author xiaoleicxl
 * @version $Id: ThreadLocalsCheckUtil.java, v 0.1 2017年5月22日 下午8:05:54 xiaoleicxl Exp $
 */
public class ThreadLocalsCheckUtil {

    //格式 THREADLOCAL_CHECK_INFO=com.alipay.fc.loancore.common.util.holder.LoanCoreDataHolder:HOLDER;
    public static String        threadLocalCheckConfig = ActsConfiguration
                                                           .getInstance()
                                                           .getActsConfigMap()
                                                           .get(
                                                               ActsConstants.THREADLOCAL_CHECK_INFO);

    public static String        threadLocalNotCheckScript = ActsConfiguration
            .getInstance()
            .getActsConfigMap()
            .get(
                    ActsConstants.THREADLOCAL_NOT_CHECK_SCRIPT);
    private static final Logger logger                 = LoggerFactory
                                                           .getLogger(ThreadLocalsCheckUtil.class);

    /**
     * 校验acts-confif.properties中配置的线程变量已经清空
     */
    public static void validateConfThreadLocalNotExist(String scriptName ) {
        Map<String, List<String>> confMap = getThreadHolderInfo(scriptName);
        for(String key : confMap.keySet()){
            for(String s:confMap.get(key)){
                validateSpecThreadLocalNotExist(key, s);
            }
        }
    }

    /**
     * 校验指定类下的指定线程变量不存在
     * @param threadHolder 线程变量类全称，如"com.alipay.fc.loancore.common.util.holder.LoanCoreDataHolder"
     * @param threadLocalName 需要校验的线程变量名，一般为线程变量类的一个static属性
     */
    public static void validateSpecThreadLocalNotExist(String threadHolder, String threadLocalName) {
        try {
            Class<?> clz = Class.forName(threadHolder);
            Field f1 = clz.getDeclaredField(threadLocalName);
            f1.setAccessible(true);

            //所有的线程变量都应该是static类型
            ThreadLocal<?> tl = (ThreadLocal<?>) f1.get(null);

            //获取当前线程下所有的线程变量
            Thread t = Thread.currentThread();
            Field f2 = t.getClass().getDeclaredField("threadLocals");
            f2.setAccessible(true);
            Object threadLocals = f2.get(t);
            Method method = threadLocals.getClass().getDeclaredMethod("getEntry",
                new Class[] { ThreadLocal.class });

            method.setAccessible(true);

            Object threadLocalEntry = method.invoke(threadLocals, new Object[] { tl });

            if (null == threadLocalEntry) {
                return;
            } else {
                Object threadLocalEntryValue = tl.get();
//                Assert.assertNull(threadHolder + "中的线程变量" + threadLocalName + "未清理！",
//                    threadLocalEntryValue);
            }

        } catch (Exception e) {
            logger.error("Acts获取应用线程上下文失败！", e);
        }
    }

    public static Map<String, List<String>> getThreadHolderInfo(String scriptName) {

        Map<String, List<String>> threadLocalCheckMap = new HashMap<String, List<String>>();


        //黑名单,无黑名单默认全部开启,兼容老逻辑
        if(StringUtils.isNotBlank(threadLocalNotCheckScript)){

            //判断当前测试脚本是否需要进行线程变量清空校验
            //格式 com.alipay.fc.depcore.acts.test.depositfundfreezeserviceimpl.freeze.OverFreezeActsNormalTest;com.alipay.fc.depcore.acts.test.depositfundunfreezeserviceimpl.unfreeze.UnfreezeActsNormalTest
            //多个脚本名称用分号分割
            List<String> scriptList = Arrays.asList(threadLocalNotCheckScript.split(";"));
            if(scriptList.contains(scriptName)){

                //黑名单匹配成功,直接返回空map,表示check通过
                return threadLocalCheckMap;
            }
        }

        if (StringUtils.isNotBlank(threadLocalCheckConfig)) {
            //格式 THREADLOCAL_CHECK_INFO=com.alipay.fc.loancore.common.util.holder.LoanCoreDataHolder:HOLDER1,HOLDER2; com.alipay.fc.loancore.common.util.holder.LoanCoreExtDataHolder:HOLDER3,HOLDER4  多组用分号分割
            String[] array1 = threadLocalCheckConfig.split(";");
            for (String s : array1) {
                String[] threadLocalCheckItem = s.split(":");
                String[] holderArray = threadLocalCheckItem[1].split(",");
                List<String> holderList = Arrays.asList(holderArray);
                threadLocalCheckMap.put(threadLocalCheckItem[0], holderList);
            }
        }

        return threadLocalCheckMap;
    }
}
