/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.testify.manage.log.impl;

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

import com.github.loadup.components.testify.manage.enums.LoggerType;
import com.github.loadup.components.testify.manage.log.RemoteLogService;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 基于HttpClient写入远程日志
 *
 * @author liudian.li
 *
 */
public class HttpClientLogServiceImpl implements RemoteLogService {

    /**
     * 日志
     */
    private static final Logger logger = LoggerFactory.getLogger(HttpClientLogServiceImpl.class);

    /**
     * 服务端接口URL
     */
    private final String serverURL;

    /**
     * 服务端主机地址
     */
    private final String host;

    /**
     * @param host
     * @param serverURL
     */
    public HttpClientLogServiceImpl(String host, String serverURL) {
        super();
        this.serverURL = serverURL;
        this.host = host;
    }

    // ~~~ 接口实现

    /**
     * @see RemoteLogService#writeLog(String, String)
     */
    @Override
    public void writeLog(String appName, String logData) {

        postLogData(LoggerType.TESTIFY_LOG, appName, logData);
    }

    /**
     * @see RemoteLogService#writeProfile(String, String)
     */
    @Override
    public void writeProfile(String appName, String profileData) {

        postLogData(LoggerType.TESTIFY_PROFILE, appName, profileData);
    }

    /**
     * @see RemoteLogService#writeDigest(String, String)
     */
    @Override
    public void writeDigest(String appName, String digestData) {

        postLogData(LoggerType.TESTIFY_DIGEST, appName, digestData);
    }

    /**
     * 发送post请求，将日志写入远程.
     *
     * @param logType   日志类型
     * @param projectId 项目名称
     * @param logData   日志内容
     * @return
     * @throws IOException
     */
    private void postLogData(LoggerType logType, String projectId, String logData) {
        //        HttpClient httpClient = new HttpClient();
        //        httpClient.getHostConfiguration().setHost(host, 80, "http");
        //
        //        PostMethod post = getPostMethod(logType, projectId, logData);
        //
        //        try {
        //            httpClient.executeMethod(post);
        //        } catch (Throwable e) {
        //            logger.error("httpClient post log data fail[host=" + host + ",url=" +
        // serverURL + "]",
        //                e);
        //        } finally {
        //            post.releaseConnection();
        //        }
    }

    /**
     * 构造post请求和参数
     *
     * @param logType       日志类型
     * @param appName       项目名称
     * @param logData       日志内容
     * @return
     */
    //    private PostMethod getPostMethod(LoggerType logType, String appName, String logData) {
    //        NameValuePair[] param = new NameValuePair[3];
    //        param[0] = new NameValuePair("logType", logType.toString());
    //        param[1] = new NameValuePair("appName", appName);
    //        param[2] = new NameValuePair("logData", logData);
    //
    //        PostMethod post = new PostMethod(serverURL);
    //        post.setRequestHeader("Content-Type",
    // "application/x-www-form-urlencoded;charset=gbk");
    //        post.setRequestBody(param);
    //
    //        return post;
    //    }

    //  ~~setter & getter

    /**
     *
     *
     * @return property value of serverURL
     */
    public String getServerURL() {
        return serverURL;
    }

    /**
     *
     *
     * @return property value of host
     */
    public String getHost() {
        return host;
    }
}
