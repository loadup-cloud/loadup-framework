/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2019 All Rights Reserved.
 */
package com.alipay.test.acts.playback.lothar.health;

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

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 *
 * @author qingqin
 * @version $Id: Scanner.java, v 0.1 2019年09月03日 上午10:14 qingqin Exp $
 */
public class Scanner {

    /**
     * 负责 lothar 的健康检查
     *
     * @throws Exception 健康检查失败的异常，否则不进行任何操作
     */
    public static void scan() throws Exception{

        //String[] command = new String[] {"curl", "-XPOST", "http://127.0.0.1:17700/sandbox/module/http/supermock-agent/communicate", "-d",
        //        "topic=GLOBAL&content={\"topic\":\"GLOBAL\",\"value\":2,\"option\":\"check\"}"};
        String command = "curl -X POST http://127.0.0.1:17700/sandbox/module/http/supermock-agent/communicate --data topic=GLOBAL&content={\"topic\":\"GLOBAL\",\"value\":2,\"option\":\"check\"}";

        Process targetProcess = Runtime.getRuntime().exec(command);

        InputStreamReader isr = new InputStreamReader(targetProcess.getInputStream());
        BufferedReader brd = new BufferedReader(isr);

        StringBuilder sb = new StringBuilder("");
        for (String line = brd.readLine(); line != null; line = brd.readLine()) {
            sb.append(line);
        }

        String res = sb.toString();
        if (!res.contains("\"success\":true")) {
            throw new Exception("Failed to launch lothar!");
        }
    }

    /**
     * sofa 应用健康检查，不适用于 sofaboot
     *
     * @throws Exception 抛出一个异常提示
     */
    public static void appHealth() throws Exception {
        String[] command = new String[]{"curl", "http://127.0.0.1:9500/checkService"};
        Process targetProcess = Runtime.getRuntime().exec(command);

        InputStreamReader isr = new InputStreamReader(targetProcess.getInputStream());
        BufferedReader brd = new BufferedReader(isr);

        StringBuilder sb = new StringBuilder("");
        for (String line = brd.readLine(); line != null; line = brd.readLine()) {
            sb.append(line);
        }

        String res = sb.toString();
        if (res.contains("passed:false")) {
            throw new Exception("App health check failed, please check logs/sofa/sofa-healthcheck.log !");
        }
    }

}
