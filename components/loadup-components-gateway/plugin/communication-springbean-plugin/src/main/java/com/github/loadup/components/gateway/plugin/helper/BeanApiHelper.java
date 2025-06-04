/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.gateway.plugin.helper;

/*-
 * #%L
 * communication-springbean-plugin
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

import com.github.loadup.commons.util.JsonUtil;
import com.github.loadup.components.gateway.facade.util.LogUtil;
import com.github.loadup.components.gateway.plugin.exception.IllegalBeanMethodException;
import com.github.loadup.components.gateway.plugin.exception.IllegalBeanUriException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * Helper for SpringBean call communication plugin extension. And bean uri is expected to be: SPRINGBEAN://{spingBean_id}/{method}
 */
@Slf4j
public class BeanApiHelper {
    private String patternStr;
    private Pattern pattern;

    private BeanApiHelper() {
        patternStr = "SPRINGBEAN://[a-zA-Z|0-9|.|_]+/[a-zA-Z|0-9|_]+";
        pattern = Pattern.compile(patternStr);
    }

    public static BeanApiHelper getInstance() {
        return BeanApiHelperSingletonHolder.INSTANCE;
    }

    /**
     * @throws IllegalBeanUriException
     */
    public ApiDefinition parseApiDefinition(String beanUri) throws IllegalBeanUriException {
        Matcher matcher = pattern.matcher(beanUri);
        boolean isMatch = matcher.matches();
        if (isMatch) {
            String[] protocolPath = StringUtils.split(beanUri, "://", 2);

            String[] classNMethod = StringUtils.split(protocolPath[1], "://", 2);
            String beanId = classNMethod[0];
            String method = classNMethod[1];

            return new ApiDefinition(beanId, method);
        } else {
            String errMsg = String.format("Bean uri=%s do not match pattern=%s", beanUri, patternStr);
            throw new IllegalBeanUriException(errMsg);
        }
    }

    /**
     * @throws IllegalBeanMethodException
     */
    public Method getServiceMethod(Object bizService, ApiDefinition api) throws IllegalBeanMethodException {
        String methodName = api.method();

        Set<Method> foundMethods = new HashSet<>();

        for (Method method : bizService.getClass().getMethods()) {
            if (StringUtils.equals(method.getName(), methodName)) {
                foundMethods.add(method);
            }
        }

        // generally valid
        if (foundMethods.size() == 1) {
            return foundMethods.iterator().next();
        }

        if (foundMethods.size() == 0) {
            throw new IllegalBeanMethodException("Could not find target method " + methodName);
        }
        throw new IllegalBeanMethodException(
                "Class method overloading is not allowed now, target method is " + methodName);
    }

    /**
     * parse parameter
     */
    @SuppressWarnings({"static-access", "rawtypes"})
    public Object parseParam(Method method, String requestMessage) {
        Class<?>[] paramTypes = method.getParameterTypes();

        // only one parameter is allowed for standard OpenApi
        if (paramTypes.length != 1) {
            throw new RuntimeException("invalid parameter types");
        }
        LogUtil.info(
                log,
                "The class of parameter is " + paramTypes[0].getName() + "The requestMessage is " + requestMessage);
        return JsonUtil.parseObject(requestMessage, paramTypes[0]);
    }

    private static class BeanApiHelperSingletonHolder {
        private static final BeanApiHelper INSTANCE = new BeanApiHelper();
    }
}
