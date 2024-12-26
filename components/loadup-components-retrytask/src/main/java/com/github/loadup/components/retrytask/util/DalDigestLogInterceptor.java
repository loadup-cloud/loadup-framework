package com.github.loadup.components.retrytask.util;

/*-
 * #%L
 * loadup-components-retrytask
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

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Collection;

import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Dal Digest Log Interceptor
 */
@Slf4j
public class DalDigestLogInterceptor {

    /**
     * 摘要日志的内容分隔符
     */
    private static final String SEP = ",";

    /**
     * 摘要日志中NULL内容的替代福
     */
    private static final String NULL_REPLACE = "-";

    /**
     * 摘要日志中DO对象的替代福
     */
    private static final String DO_REPLACE = "*";

    /**
     * 摘要日志中默认替代符
     */
    private static final String DEFAULT_REPLACE = "(-)";

    /**
     * 构建摘要日志
     */
    private static String buildDigestLog(boolean isSucess, long elapseTime,
                                         MethodInvocation invocation, String resultSize) {

        // 被拦截的DAO方法
        Method method = invocation.getMethod();

        // 被拦截方法签名："类名.方法名"
        String invocationSignature = method.getDeclaringClass().getSimpleName() + "."
                + method.getName();

        StringBuilder sb = new StringBuilder("[(");

        // 调用方法签名："类名.方法名"
        sb.append(invocationSignature).append(SEP);

        // 方法执行是否成功
        sb.append(isSucess ? "Y" : "N").append(SEP);

        // 方法耗时
        sb.append(elapseTime).append("ms)");

        fillArgumets(sb, invocation.getArguments());

        sb.append(resultSize);

        sb.append("]");

        return sb.toString();
    }

    /**
     * 获取结果大小
     */
    private static String getResultSize(Object returnValue) {
        int size = 0;
        if (returnValue == null) {
            size = 0;
        } else if (returnValue instanceof Collection) {
            size = ((Collection<?>) returnValue).size();
        } else if (returnValue.getClass().isArray()) {
            size = Array.getLength(returnValue);
        } else {
            size = 1;
        }
        StringBuilder builder = new StringBuilder();

        builder.append("(");
        builder.append(size);
        builder.append(")");

        return builder.toString();
    }

    /**
     * 打印参数信息
     */
    private static void fillArgumets(StringBuilder sb, Object[] argumets) {

        if (ArrayUtils.isEmpty(argumets)) {
            return;
        }

        // 打印参数信息
        sb.append("(");

        for (Object arg : argumets) {

            if (arg == null) {
                arg = NULL_REPLACE;
            }

            // 如果是DO对象，就不打印出来了，其他值还是可以打印出来的，便于查问题
            if (StringUtils.contains(arg.getClass().getSimpleName(), "DO")
                    || StringUtils.contains(arg.getClass().getSimpleName(), "List")) {
                arg = DO_REPLACE;
            }

            sb.append(arg).append(SEP);
        }

        if (argumets.length > 0) {
            sb.deleteCharAt(sb.lastIndexOf(SEP));
        }

        sb.append(")");
    }

    /**
     *
     */
    public Object businessInvoke(MethodInvocation invocation) throws Throwable {
        // DAO执行是否成功
        boolean success = true;
        long startTime = System.currentTimeMillis();
        String resultSize = DEFAULT_REPLACE;

        try {
            Object result = invocation.proceed();
            resultSize = getResultSize(result);
            return result;

        } catch (Exception e) {

            success = false;
            throw e;

        } finally {

            long elapseTime = System.currentTimeMillis() - startTime;
            log
                    .info("DIGEST_LOGGER", buildDigestLog(success, elapseTime, invocation, resultSize));
        }
    }
}
