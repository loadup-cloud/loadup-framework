package com.github.loadup.components.testify.manage.log;

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
import com.github.loadup.components.testify.manage.enums.LoggerType;

/**
 * 远程日志 appender
 *
 * 
 *
 */
public class RemoteAppender {

    /**
     * 内部日志
     */
    private static Logger logger = LoggerFactory.getLogger(RemoteAppender.class);

    /**
     * 应用名
     */
    private String appName;

    /**
     * 日志类型
     */
    private LoggerType loggerType;

    /**
     * 远程服务
     */
    private RemoteLogService logService;

//    @Override
//    protected void append(LoggingEvent event) {
//        init();
//
//        String logData = this.getLayout().format(event);
//
//        try {
//            switch (loggerType) {
//                case ACTS_LOG:
//                    logService.writeLog(appName, logData);
//                    break;
//                case ACTS_PROFILE:
//                    logService.writeProfile(appName, logData);
//                    break;
//                case ACTS_DIEGEST:
//                    logService.writeDigest(appName, logData);
//                    break;
//            }
//        } catch (Throwable e) {
//            logger.warn("remote write error", e);
//        }
//
//    }
//
//    @Override
//    public void activateOptions() {
//        init();
//    }
//
//    @Override
//    public void close() {
//    }
//
//    @Override
//    public boolean requiresLayout() {
//        return true;
//    }
//
//    // ~~~ 内部方法
//    /**
//     * 初始化
//     */
//    private void init() {
//        if (this.layout == null) {
//            String pattern = "%d - %m%n";
//            this.setLayout(new org.apache.log4j.PatternLayout(pattern));
//        }
//    }
//
//    // ~~~ getter & setter
//    /**
//     * 
//     *
//     * @return property value of appName
//     */
//    public String getAppName() {
//        return appName;
//    }
//
//    /**
//     *
//     *
//     * @param appName value to be assigned to property appName
//     */
//    public void setAppName(String appName) {
//        this.appName = appName;
//    }
//
//    /**
//     * 
//     *
//     * @return property value of loggerType
//     */
//    public LoggerType getLoggerType() {
//        return loggerType;
//    }
//
//    /**
//     *
//     *
//     * @param loggerType value to be assigned to property loggerType
//     */
//    public void setLoggerType(LoggerType loggerType) {
//        this.loggerType = loggerType;
//    }
//
//    /**
//     * 
//     *
//     * @return property value of logService
//     */
//    public RemoteLogService getLogService() {
//        return logService;
//    }
//
//    /**
//     *
//     *
//     * @param logService value to be assigned to property logService
//     */
//    public void setLogService(RemoteLogService logService) {
//        this.logService = logService;
//    }

}
