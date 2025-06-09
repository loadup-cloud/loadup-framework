/* Copyright (C) LoadUp Cloud 2022-2025 */
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

import ch.qos.logback.core.rolling.RollingFileAppender;
import com.github.loadup.components.testify.exception.TestifyException;
import java.io.File;
import org.slf4j.Logger;

/**
 * 自定义log，基于log4j。
 *
 *
 *
 */
class Log4jLogger extends com.github.loadup.components.testify.manage.log.Logger {

    /**
     * log4j
     */
    Logger logger;

    /**
     * 指定appender
     */
    private RollingFileAppender appender;

    Log4jLogger(String name, File file) {
        this(name, file.getAbsolutePath());
    }

    Log4jLogger(String name, String file) {

        //        logger = Logger.getLogger(name);
        //
        //        if (file != null && file.length() > 0) {
        //            initAppender(file);
        //        }
        //
        //        logger.setLevel(Level.ALL);
    }

    /**
     * @see com.github.loadup.components.testify.manage.log.Logger#info(Object[])
     */
    @Override
    public void info(Object... objs) {
        if (logger.isInfoEnabled()) {
            logger.info(getLogString(objs));
        }
    }

    /**
     * @see com.github.loadup.components.testify.manage.log.Logger#info(Throwable, Object[])
     */
    @Override
    public void info(Throwable e, Object... objs) {
        if (logger.isInfoEnabled()) {
            logger.info(getLogString(objs), e);
        }
    }

    /**
     * @see com.github.loadup.components.testify.manage.log.Logger#warn(Object[])
     */
    @Override
    public void warn(Object... objs) {
        logger.warn(getLogString(objs));
    }

    /**
     * @see com.github.loadup.components.testify.manage.log.Logger#warn(Throwable, Object[])
     */
    @Override
    public void warn(Throwable e, Object... objs) {
        logger.warn(getLogString(objs), e);
    }

    /**
     * @see com.github.loadup.components.testify.manage.log.Logger#error(Throwable, Object[])
     */
    @Override
    public void error(Throwable e, Object... objs) {
        logger.error(getLogString(objs), e);
    }

    /**
     * @see com.github.loadup.components.testify.manage.log.Logger#error(Object[])
     */
    @Override
    public void error(Object... objs) {
        logger.error(getLogString(objs));
    }

    /**
     * @see com.github.loadup.components.testify.manage.log.Logger#error(String, Object[])
     */
    @Override
    public void error(String messageName, Object... objs) {
        logger.error(getLogString(messageName, objs));
    }

    /**
     * @see com.github.loadup.components.testify.manage.log.Logger#debug(Object[])
     */
    @Override
    public void debug(Object... objs) {
        if (logger.isDebugEnabled()) {
            logger.debug(getLogString(objs));
        }
    }

    /**
     * @see com.github.loadup.components.testify.manage.log.Logger#debug(Throwable, Object[])
     */
    @Override
    public void debug(Throwable e, Object... objs) {
        if (logger.isDebugEnabled()) {
            logger.debug(getLogString(objs), e);
        }
    }

    /**
     * 增加appender
     *
     * @param appender
     */
    void addRemoteAppender(RemoteAppender appender) {
        //        logger.addAppender(appender);
    }

    @Override
    protected void finalize() throws Throwable {
        if (appender != null) {
            //            logger.removeAppender(appender);
            //            appender.close();
            appender = null;
        }
    }

    /**
     * This tries to create a file appender for the specified file name.
     *
     * @param file
     */
    private void initAppender(String file) {
        try {
            // to add the appender
            //            PatternLayout layout = new PatternLayout("%d - %m%n");
            //            this.appender = new RollingFileAppender(layout, file, true);
            //
            //             if we successfully created the file appender,
            //             configure it and set the logger to use only it
            //            appender.setMaxBackupIndex(1);
            //            appender.setMaximumFileSize(100000);
            //
            // don't inherit appenders from higher in the logger heirarchy
            //            logger.setAdditivity(true);
            //            logger.addAppender(appender);
        } catch (Exception ioe) {
            throw new TestifyException("error configuring Log4JLogChute[file=" + file + "]", ioe);
        }
    }
}
