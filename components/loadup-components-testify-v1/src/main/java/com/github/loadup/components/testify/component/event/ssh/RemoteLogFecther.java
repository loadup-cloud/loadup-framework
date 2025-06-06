/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.testify.component.event.ssh;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jie.peng
 *
 */
public class RemoteLogFecther {

    /**
     * 日志.
     */
    protected static final Logger logger = LoggerFactory.getLogger(RemoteLogFecther.class);

    /**
     * 默认使用log用户
     */
    private static String USER_NAME = "log";

    private static String PASS_WORD = "log_xixihaha";

    /**
     * 获取服务器日志
     *
     * @param serverHost 目标服务器地址
     * @param commands   日志grep命令，</br>
     * @return
     */
    public String getServerLog(String serverHost, String commands) {
        return "";
        //        Connection conn = null;
        //        Session sess = null;
        //        String result = null;
        //
        //        try {
        //            conn = new Connection(serverHost);
        //            sess = connectToServer(conn, USER_NAME, PASS_WORD);
        //            sess.execCommand(commands);
        //            result = getStringResult(sess);
        //
        //        } catch (Exception e) {
        //            logger.error("Exception obtaining server info" + e.getMessage());
        //        } finally {
        //            disConnect(sess, conn);
        //        }
        //        return result;
    }

    /**
     * 获取执行结果
     *
     * @param sess
     * @return
     */
    /*private String getStringResult(Session sess) {

        InputStream stdout = new StreamGobbler(sess.getStdout());
        BufferedReader reader = new BufferedReader(new InputStreamReader(stdout));
        StringBuffer strbf = new StringBuffer();
        String tempString = null;
        try {
            while ((tempString = reader.readLine()) != null) {
                strbf.append(tempString);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (Exception e) {
                logger.error("Exception closing read buffer", e);
            }
        }
        return strbf.toString();

    }

    */
    /**
     * 连接应用服务器
     * 参数Connection conn用于返回conn
     * @return
     */
    /*
    private Session connectToServer(Connection conn, String userName, String passWord) {

        try {
            conn.connect();
            boolean isAuthenticated = conn.authenticateWithPassword(userName, passWord);
            if (isAuthenticated == false) {
                logger.error("unauthoried log access on server, please check!");
                throw new IOException("Authentication failed.");
            }

            return conn.openSession();
        } catch (Exception e) {
            logger.error("Exception establishing connection to server");
            return null;
        }
    }

    */
    /**
     * 关闭链接
     * @param sess
     * @param conn
     */
    /*
    private void disConnect(Session sess, Connection conn) {
        if (null != sess) {
            sess.close();
        }
        if (null != conn) {
            conn.close();
        }
    }

    */
    /**
     * 从日志信息中提取target字段的值
     *
     * @param log 待提取的原始日志
     * @param target 代提取的字段，如cardNo
     * @return
     */
    /*
    protected String pickup(String log, String target) {

        String pickResult = null;
        if (StringUtils.isEmpty(log) || StringUtils.isEmpty(target)) {
            logger.warn("Log is empty, unable to estract, log=" + log + ",target=" + target);
            return null;
        }
        //构造提取信息的正则表达式
        String regex = target + "=(\\[)*(.*)(,|])*";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(log);

        if (m.find()) {
            pickResult = m.group(2);
        }
        if (StringUtils.isNotEmpty(pickResult)) {
            String frist = StringUtils.substring(pickResult, 0, 1);
            String temp = "";
            //当需要的信息以"{"开头,则只需要取到第一个"}"结束即可
            if (StringUtils.equals(frist, "{")) {
                temp = StringUtils.substringBefore(pickResult, "}");
                temp += "}";
            } else {
                temp = StringUtils.substringBefore(pickResult, ",");
                temp = StringUtils.substringBefore(temp, "，");
                temp = StringUtils.substringBefore(temp, "]");
                String shortDateString = DateUtil.format(new Date(), "yyyy-MM-dd");
                //有些错误日志内容后面为当前时间，这里去掉时间信息，只取需要的信息
                if (StringUtils.contains(temp, shortDateString)) {
                    temp = StringUtils.substringBefore(temp, shortDateString);
                }
            }
            return temp;
        }
        return null;
    }*/

}
