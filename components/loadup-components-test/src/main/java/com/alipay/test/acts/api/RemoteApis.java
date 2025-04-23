package com.alipay.test.acts.api;

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

import com.alipay.test.acts.util.InvokeSofaTrServices;

/**
 * 远程服务调用API
 * 
 * @author baishuo.lp
 * @version $Id: RemoteApis.java, v 0.1 2015年3月7日 下午2:46:22 baishuo.lp Exp $
 */
public class RemoteApis {

    /**
     * @param serviceUrl 调用tr服务的服务器地址+<b>端口号</b>可以是域名或者Ip地址, eg. 10.211.128.247:12200
     * @param service 调用的服务 e.g. xxxFacade
     * @param method 调用的方法名 
     * @param objects 调用的服务的入参数组
     * @return 服务响应对象
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static <T> T getTRResult(String servicesUrl, Class service, String methodname,
                                    Object... parameters) {
        return (T) InvokeSofaTrServices.invoke(servicesUrl, service, methodname, parameters);
    }
}
