package com.alipay.test.acts.util;

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
 * @Author: yuning.syn
 * @Date: 10:36 AM 2019/4/9 04 2019
 * @ClassName HttpUtil
 */

import org.apache.commons.lang3.StringUtils;
import com.google.common.base.Joiner;
import org.apache.http.*;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

public class HttpUtil {

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
                throw new IllegalArgumentException("Http请求失败" + EntityUtils.toString(response.getEntity()));
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
            throw new RuntimeException("HttpClient调用异常", e);
        }

    }

    /**
     * GET请求发起, 指定读取编码
     *
     * @param url 请求URL
     * @param charset 指定读取编码
     * @return 返回对象
     */
    public static String doGet(String url, String charset) {

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
            String body = EntityUtils.toString(response.getEntity(), charset);

            return body;

        } catch (Exception e) {
            throw new RuntimeException("HttpClient调用异常", e);
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

            for (Iterator iter = headers.keySet().iterator(); iter.hasNext();) {
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
            throw new RuntimeException("HttpClient调用异常", e);
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

            httpPost.addHeader("Accept", "application/json, text/javascript, */*");
            httpPost.addHeader("Content-Type", "application/json; charset=UTF-8");
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
            throw new RuntimeException("HttpClient调用异常", e);
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (Exception e) {
                    throw new RuntimeException("Response IO流关闭异常", e);
                }
            }
            try {
                httpclient.close();
            } catch (Exception e) {
                throw new RuntimeException("HttpClient IO流关闭异常", e);
            }
        }
    }

    /**
     * post请求（Miu平台交互使用）
     *
     * @param url  请求URL
     * @param body 请求内容
     * @return 返回字符串
     */
    public static String doPostObject(String url, Object body) {

        // 构造HttpClient客户端
        CloseableHttpClient httpclient = HttpClients.createDefault();
        // 创建Post请求对象
        HttpPost httpPost = new HttpPost(url);

        // HttpGet返回对象
        CloseableHttpResponse response = null;
        try {

            httpPost.addHeader("Accept", "application/json, text/javascript, */*");
            httpPost.addHeader("Content-Type", "application/json; charset=UTF-8");
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
            throw new RuntimeException("HttpClient调用异常", e);
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (Exception e) {
                    throw new RuntimeException("Response IO流关闭异常", e);
                }
            }
            try {
                httpclient.close();
            } catch (Exception e) {
                throw new RuntimeException("HttpClient IO流关闭异常", e);
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
    public static String doPost(String url, Map headers, Map body) {

        // 构造HttpClient客户端
        CloseableHttpClient httpclient = HttpClients.createDefault();
        // 创建Post请求对象
        HttpPost httpPost = new HttpPost(url);

        // HttpGet返回对象
        CloseableHttpResponse response = null;
        try {
            for (Iterator iter = headers.keySet().iterator(); iter.hasNext();) {
                String name = (String) iter.next();
                String value = String.valueOf(headers.get(name));
                httpPost.setHeader(name, value);
            }

            //设置参数
            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            for (Iterator iter = body.keySet().iterator(); iter.hasNext();) {
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
            throw new RuntimeException("HttpClient调用异常", e);
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (Exception e) {
                    throw new RuntimeException("Response IO流关闭异常", e);
                }
            }
            try {
                httpclient.close();
            } catch (Exception e) {
                throw new RuntimeException("HttpClient IO流关闭异常", e);
            }
        }
    }

    /**
     * 构造 uri
     */
    public static URI constructUri(String scheme, String host, int port, String path)
                                                                                     throws URISyntaxException {

        return new URIBuilder().setScheme(scheme).setHost(host).setPort(port).setPath(path).build();
    }

    /**
     * 获取 mock data repository url
     */
    public static synchronized String contructUrl(String scheme, String host, int port, String path) {

        String uk = uk(scheme, host, port, path);
        String url = URL_REPOSITORY.get(uk);

        if (StringUtils.isEmpty(url)) {
            try {

                URI uri = constructUri(scheme, host, port, path);

                url = uri == null ? "" : uri.toURL().toString();

                URL_REPOSITORY.put(uk, url);
            } catch (Exception ex) {

            }
        }
        return url;
    }

    private static String uk(String scheme, String host, int port, String path) {
        return scheme + host + port + path;
    }

    private static Map<String, String> URL_REPOSITORY = new HashMap<String, String>();

}
