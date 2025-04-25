package com.github.loadup.components.testify.utils.http;

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
import com.github.loadup.components.testify.exception.TestifyException;
import com.github.loadup.components.testify.util.JsonUtil;
import com.google.common.base.Joiner;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

/**
 * HttpClient工具类
 *
 * @author lizhan.wl
 * @version 1.0
 */

public class HttpUtil {
    // 编码格式。发送编码格式统一用UTF-8
    private static final String ENCODING = "UTF-8";
    // 设置连接超时时间，单位毫秒。
    private static final int CONNECT_TIMEOUT = 8000;
    // 请求获取数据的超时时间(即响应时间)，单位毫秒。
    private static final int SOCKET_TIMEOUT = 8000;

    /**
     * GET请求发起, 不设置header
     *
     * @param url 请求URL
     * @return 返回对象
     */
    public static Map<String, Object> doGet(String url) {

        // 构造HttpClient客户端
        HttpClient client = new DefaultHttpClient();
        // 构造HttpGet请求对象
        HttpGet request = new HttpGet(url);

        try {
            // 发送请求
            HttpResponse response = client.execute(request);

            if (Objects.isNull(response) || Objects.isNull(response.getStatusLine())) {
                throw new IllegalArgumentException("Http响应结果为空");
            }

            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new IllegalArgumentException("Http请求失败");
            }

            Map<String, Object> map = new HashMap<String, Object>();
            String body = EntityUtils.toString(response.getEntity());
            map.put("body", body);

            Map<String, String> cookie = new HashMap<String, String>();
            CookieStore cookieStore = ((DefaultHttpClient) client).getCookieStore();
            for (Cookie c : cookieStore.getCookies()) {
                cookie.put(c.getName(), c.getValue());
            }

            map.put("cookie", cookie);

            return map;

        } catch (Exception e) {
            throw new TestifyException("HttpClient调用异常", e);
        }

    }

    /**
     * GET请求发起, 设置header
     *
     * @param url     请求URL
     * @param headers 请求header
     * @param data    参数列表
     * @return 返回对象
     */
    public static Map<String, Object> doGet(String url, Map headers, Map<String, String> data) {

        // 拼接参数列表
        String joinedData = Joiner.on("&").useForNull("").withKeyValueSeparator("=").join(data);
        // 拼接URL地址
        String joinedUrl = String.format("%s?%s", url, joinedData);

        return doGet(joinedUrl, headers);
    }

    /**
     * GET请求发起, 设置header
     *
     * @param url     请求URL
     * @param headers 请求header
     * @return 返回对象
     */
    public static Map<String, Object> doGet(String url, Map headers) {

        // 构造HttpClient客户端
        HttpClient client = new DefaultHttpClient();
        // 构造HttpGet请求对象
        HttpGet request = new HttpGet(url);

        try {

            for (Iterator iter = headers.keySet().iterator(); iter.hasNext(); ) {
                String name = (String) iter.next();
                String value = String.valueOf(headers.get(name));
                request.setHeader(name, value);
            }

            // 发送请求
            HttpResponse response = client.execute(request);

            if (Objects.isNull(response) || Objects.isNull(response.getStatusLine())) {
                throw new IllegalArgumentException("Http响应结果为空");
            }

            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new IllegalArgumentException("Http请求失败");
            }

            Map<String, Object> map = new HashMap<String, Object>();
            String body = EntityUtils.toString(response.getEntity());
            map.put("body", body);

            Map<String, String> cookie = new HashMap<String, String>();
            CookieStore cookieStore = ((DefaultHttpClient) client).getCookieStore();
            for (Cookie c : cookieStore.getCookies()) {
                cookie.put(c.getName(), c.getValue());
            }

            map.put("cookie", cookie);

            return map;

        } catch (Exception e) {
            throw new TestifyException("HttpClient调用异常", e);
        }
    }

    public static String doPost(String url, Map headers, String json) {

        // 构造HttpClient客户端
        CloseableHttpClient httpclient = HttpClients.createDefault();
        // 创建Post请求对象
        HttpPost httpPost = new HttpPost(url);
        // HttpGet返回对象
        CloseableHttpResponse response = null;
        try {
            for (Iterator iter = headers.keySet().iterator(); iter.hasNext(); ) {
                String name = (String) iter.next();
                String value = String.valueOf(headers.get(name));
                httpPost.setHeader(name, value);
            }
            httpPost.setEntity(new StringEntity(json, Charset.defaultCharset()));

            response = httpclient.execute(httpPost);
            StatusLine status = response.getStatusLine();
            int state = status.getStatusCode();
            if (state != HttpStatus.SC_OK) {
                throw new IllegalArgumentException("HttpClient调用异常, 状态码：" + state);
            }

            HttpEntity responseEntity = response.getEntity();
            return EntityUtils.toString(responseEntity);
        } catch (Exception e) {
            throw new TestifyException("HttpClient调用异常", e);
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (Exception e) {
                    throw new TestifyException("Response IO流关闭异常", e);
                }
            }
            try {
                httpclient.close();
            } catch (Exception e) {
                throw new TestifyException("HttpClient IO流关闭异常", e);
            }
        }
    }

    /**
     * post请求（用于请求json格式的参数）
     *
     * @param url  请求URL
     * @param body 请求内容
     * @return 返回字符串
     */
    public static String doPost(String url, Map body) {

        // 构造HttpClient客户端
        CloseableHttpClient httpclient = HttpClients.createDefault();
        // 创建Post请求对象
        HttpPost httpPost = new HttpPost(url);

        // HttpGet返回对象
        CloseableHttpResponse response = null;
        try {

            HttpEntity entity = new StringEntity(JsonUtil.toJson(body), "utf-8");
            httpPost.setEntity(entity);
            response = httpclient.execute(httpPost);
            StatusLine status = response.getStatusLine();
            int state = status.getStatusCode();
            if (state != HttpStatus.SC_OK) {
                throw new IllegalArgumentException("HttpClient调用异常, 状态码：" + state);
            }

            HttpEntity responseEntity = response.getEntity();
            return EntityUtils.toString(responseEntity);
        } catch (Exception e) {
            throw new TestifyException("HttpClient调用异常", e);
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (Exception e) {
                    throw new TestifyException("Response IO流关闭异常", e);
                }
            }
            try {
                httpclient.close();
            } catch (Exception e) {
                throw new TestifyException("HttpClient IO流关闭异常", e);
            }
        }
    }

    /**
     * in
     * post请求（用于请求json格式的参数）
     *
     * @param url  请求URL
     * @param body 请求内容
     * @return 返回字符串
     */
    public static String doPost(String url, Map headers, Map body) {

        // 构造HttpClient客户端
        CloseableHttpClient httpclient = HttpClients.createDefault();
        // 创建Post请求对象
        HttpPost httpPost = new HttpPost(url);

        // HttpGet返回对象
        CloseableHttpResponse response = null;
        try {
            for (Iterator iter = headers.keySet().iterator(); iter.hasNext(); ) {
                String name = (String) iter.next();
                String value = String.valueOf(headers.get(name));
                httpPost.setHeader(name, value);
            }
            //设置参数
            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            for (Iterator iter = body.keySet().iterator(); iter.hasNext(); ) {
                String name = (String) iter.next();
                String value = String.valueOf(body.get(name));
                nvps.add(new BasicNameValuePair(name, value));

            }
            httpPost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));

            response = httpclient.execute(httpPost);
            StatusLine status = response.getStatusLine();
            int state = status.getStatusCode();
            if (state != HttpStatus.SC_OK) {
                throw new IllegalArgumentException("HttpClient调用异常, 状态码：" + state);
            }

            HttpEntity responseEntity = response.getEntity();
            return EntityUtils.toString(responseEntity);
        } catch (Exception e) {
            throw new TestifyException("HttpClient调用异常", e);
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (Exception e) {
                    throw new TestifyException("Response IO流关闭异常", e);
                }
            }
            try {
                httpclient.close();
            } catch (Exception e) {
                throw new TestifyException("HttpClient IO流关闭异常", e);
            }
        }
    }

    /**
     * post请求 发送raw字符串
     *
     * @param url      请求URL
     * @param headers  请求头
     * @param body     请求内容
     * @param encoding 编码
     * @return
     */
    public static String doPost(String url, Map headers, String body, String encoding) {
        // 构造HttpClient客户端
        CloseableHttpClient httpclient = HttpClients.createDefault();
        // 创建Post请求对象
        HttpPost httpPost = new HttpPost(url);
        Charset charset = Charset.forName("UTF-8");

        // HttpGet返回对象
        CloseableHttpResponse response = null;
        try {
            if (headers != null && headers.size() > 0) {
                for (Iterator iter = headers.keySet().iterator(); iter.hasNext(); ) {
                    String name = (String) iter.next();
                    String value = String.valueOf(headers.get(name));
                    httpPost.setHeader(name, value);
                }
            }

            //设置参数
            if (StringUtils.isNotBlank(encoding)) {
                charset = Charset.forName(encoding);
            }
            httpPost.setEntity(new StringEntity(body, charset.name()));

            response = httpclient.execute(httpPost);
            StatusLine status = response.getStatusLine();
            int state = status.getStatusCode();
            if (state != HttpStatus.SC_OK) {
                throw new IllegalArgumentException("HttpClient调用异常, 状态码：" + state);
            }

            HttpEntity responseEntity = response.getEntity();
            return EntityUtils.toString(responseEntity);
        } catch (UnsupportedCharsetException e) {
            throw new TestifyException("发送报文编码异常.charset:" + encoding, e);
        } catch (Exception e) {
            throw new TestifyException("HttpClient调用异常", e);
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (Exception e) {
                    throw new TestifyException("Response IO流关闭异常", e);
                }
            }
            try {
                httpclient.close();
            } catch (Exception e) {
                throw new TestifyException("HttpClient IO流关闭异常", e);
            }
        }
    }

    /**
     * 发送POST请求，XML格式
     *
     * @param url     请求地址
     * @param headers 请求头
     * @param body    请求体
     */
    public static String doPostXml(String url, Map<String, String> headers, String body) {

        // 构造HttpClient客户端
        CloseableHttpClient httpclient = HttpClients.createDefault();
        //设置请求和传输超时时间
        RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(SOCKET_TIMEOUT)
                .setConnectTimeout(CONNECT_TIMEOUT).build();

        HttpPost httpPost = new HttpPost(url);
        httpPost.setConfig(requestConfig);

        CloseableHttpResponse httpResponse = null;
        String content = "";
        int status = 404;

        try {
            // 封装请求头
            if (headers != null) {
                Set<Entry<String, String>> entrySet = headers.entrySet();
                for (Entry<String, String> entry : entrySet) {
                    // 设置到请求头到HttpRequestBase对象中
                    httpPost.setHeader(entry.getKey(), entry.getValue());
                }
            }
            // 是指请求体，防止中文乱码
            StringEntity stringEntity = new StringEntity(body, ENCODING);
            stringEntity.setContentEncoding(ENCODING);
            httpPost.setEntity(stringEntity);

            httpResponse = httpclient.execute(httpPost);

            // 获取返回结果
            if (httpResponse != null && httpResponse.getStatusLine() != null) {
                if (httpResponse.getEntity() != null) {
                    content = EntityUtils.toString(httpResponse.getEntity(), ENCODING);
                }
            }
            status = httpResponse.getStatusLine().getStatusCode();
            if (status == 200) {
                return content;
            } else {
                return "";
            }
        } catch (Exception e) {
            throw new TestifyException("HttpClient调用异常", e);
        } finally {
            if (httpResponse != null) {
                try {
                    httpResponse.close();
                } catch (Exception e) {
                    throw new TestifyException("httpResponse关闭异常", e);
                }
            }
            if (httpclient != null) {
                try {
                    httpclient.close();
                } catch (Exception e) {
                    throw new TestifyException("httpclient关闭异常", e);
                }
            }
        }

    }

}
