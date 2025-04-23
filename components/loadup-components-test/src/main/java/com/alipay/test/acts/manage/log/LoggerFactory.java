/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.alipay.test.acts.manage.log;

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

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.alipay.test.acts.manage.enums.LoggerType;
import com.alipay.test.acts.manage.log.impl.HttpClientLogServiceImpl;

/**
 * 获取日志对象。
 * 
 * @author dasong.jds
 * @version $Id: LoggerFactory.java, v 0.1 2015年9月28日 下午3:28:05 dasong.jds Exp $
 */
public class LoggerFactory {

    private static String                    SERVER_HOST = "10.244.42.84";

    private static String                    SERVER_URL  = "/logmonitor/remoteLog";

    /** logger管理 */
    private static final Map<String, Logger> loggers     = new HashMap<String, Logger>();

    final static String                      appName     = "appName";

    /**
     * 获取日志。
     * 
     * @param loggerType
     * @return
     */
    public static Logger getLogger(LoggerType loggerType) {

        String key = appName + loggerType;

        Logger logger = loggers.get(key);
        if (logger == null) {
            logger = createLogger(appName, loggerType);
            loggers.put(key, logger);
        }
        return logger;
    }

    /**
     * 创建logger
     * 
     * @param appName
     * @param loggerType
     * @return
     */
    private static Logger createLogger(String appName, LoggerType loggerType) {

        Log4jLogger logger = new Log4jLogger(loggerType.getName(), new File(getLogDir(),
            loggerType.getName() + ".log"));

        RemoteAppender ra = new RemoteAppender();
//        ra.setAppName(appName);
//        ra.setLoggerType(loggerType);
//        ra.setLogService(new HttpClientLogServiceImpl(SERVER_HOST, SERVER_URL));

        logger.addRemoteAppender(ra);

        return logger;
    }

    /**
     * 获取可用日志目录。
     * <p>
     * 兼容不同sofa版本逻辑。
     * 
     * @return
     */
    private static File getLogDir() {
        File logDir = null;

        // 类装载器的通用方式
        if (logDir == null) {
            try {
                URL url = Thread.currentThread().getContextClassLoader()
                    .getResource("sofaconfig/sofa-test-config.properties");
                File configFile = new File(url.toURI());

                logDir = new File(configFile.getParentFile().getParentFile().getParentFile()
                    .getParentFile(), "/logs/");
            } catch (Throwable e) {
            }
        }

        if (logDir == null) {
            logDir = new File("/logs/");
        }

        return logDir;
    }

}
