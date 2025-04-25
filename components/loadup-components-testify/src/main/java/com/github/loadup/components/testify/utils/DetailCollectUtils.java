package com.github.loadup.components.testify.utils;

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

import com.github.loadup.components.testify.util.LogUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

@Slf4j
public class DetailCollectUtils {

    private static StringBuffer sb = new StringBuffer();

    public static void appendDetail(String content) {
        sb.append(content);
        sb.append("\r\n");
    }


    public static void appendAndLogColoredError(String content, Logger logger) {
        LogUtil.printColoredError(logger, content);
        sb.append(content);
        sb.append("\r\n");
    }

    public static void appendAndLogColoredError(String content, Log logger) {
        LogUtil.printColoredError(logger, content);
        sb.append(content);
        sb.append("\r\n");
    }

    public static void appendAndLog(String content, Log logger) {
        logger.info(content);
        sb.append(content);
        sb.append("\r\n");
    }
    public static void appendAndLog(String content, Logger logger) {
        logger.info(content);
        sb.append(content);
        sb.append("\r\n");
    }

    public static byte[] bufferToBytes() {
        return sb.toString().getBytes();
    }

    public static void clearBuffer() {
        sb = new StringBuffer();
    }

    public static void saveBuffer(String logPath) {
        try {
            if (StringUtils.isEmpty(logPath)) {

                log.debug("logpath居然为空,先跳过");
                return;
            }

            logPath = StringUtils.replace(logPath, "yaml", "log");
            File caseDetail = new File(logPath);
            FileOutputStream fop = new FileOutputStream(caseDetail);
            byte[] contentInBytes = DetailCollectUtils.bufferToBytes();
            DetailCollectUtils.clearBuffer();
            fop.write(contentInBytes);
            fop.flush();
            fop.close();
        } catch (FileNotFoundException e) {

        } catch (Exception e) {

        }
    }

}
