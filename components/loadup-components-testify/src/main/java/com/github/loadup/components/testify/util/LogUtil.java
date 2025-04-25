package com.github.loadup.components.testify.util;

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

import com.github.loadup.components.testify.constant.AnsiColorConstants;
import com.github.loadup.components.testify.driver.TestifyConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.slf4j.Logger;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * 存放Log相关公用方法
 */
public class LogUtil {

    /**
     * 基于throwable获取全部信息为字符串
     *
     * @param e
     * @return
     */
    public static String getErrorMessage(Throwable e) {
        StringWriter errorStack = new StringWriter();
        e.printStackTrace(new PrintWriter(errorStack));
        return errorStack.toString();
    }

    //    /**
    //     * 打印关键错误信息红色
    //     * @param logger
    //     * @param message
    //     * @param e
    //     */
    //    public static void printColoredError(Logger logger, String message, Exception e) {
    //        if (ActsConfiguration.getInstance().isColoredLog()) {
    //            logger.error(AnsiColorConstants.ANSI_RED_BEGIN + message
    //                         + AnsiColorConstants.ANSI_COLOR_END, e);
    //        } else {
    //            logger.error(message, e);
    //        }
    //    }

    //    /**
    //     * 打印关键错误信息红色
    //     * @param logger
    //     * @param message
    //     * @param e
    //     */
    //    public static void printColoredError(Log logger, String message, Exception e) {
    //        if (ActsConfiguration.getInstance().isColoredLog()) {
    //            logger.error(AnsiColorConstants.ANSI_RED_BEGIN + message
    //                         + AnsiColorConstants.ANSI_COLOR_END, e);
    //        } else {
    //            logger.error(message, e);
    //        }
    //    }

    /**
     * 打印关键错误信息，红色
     *
     * @param logger
     * @param message
     */
    public static void printColoredError(Logger logger, String message) {
        if (TestifyConfiguration.getInstance().isColoredLog()) {
            String tmp = replaceCrLf(message);
            logger.error(colorMultiLine(AnsiColorConstants.ANSI_RED_BEGIN, tmp));
        } else {
            logger.error(message);
        }
    }

    public static void printColoredError(Log logger, String message) {
        if (TestifyConfiguration.getInstance().isColoredLog()) {
            String tmp = replaceCrLf(message);
            logger.error(colorMultiLine(AnsiColorConstants.ANSI_RED_BEGIN, tmp));
        } else {
            logger.error(message);
        }
    }


    /**
     * 打印关键info信息，蓝色
     *
     * @param logger
     * @param message
     */
    public static void printColoredInfo(Logger logger, String message) {
        if (TestifyConfiguration.getInstance().isColoredLog()) {
            String tmp = replaceCrLf(message);
            logger.error(colorMultiLine(AnsiColorConstants.ANSI_BLUE_BEGIN, tmp));
        } else {
            logger.error(message);
        }
    }

    /**
     * 将带有换行符的文本正确着色
     *
     * @param color      颜色编码
     * @param textWithLf 文本
     * @return
     */
    private static String colorMultiLine(String color, String textWithLf) {

        /*
         * 假设textWithLf里面带有换行符，需要经过转换才能完美的显示ANSI color。
         * 因为ANSI color的渲染是严格以“行”为单位
         * some text1\nsome text2\nsome text3 这样的文本，转换为下面的格式
         *
         * ANSI_COLOR_BEGIN   some text1  ANSI_COLOR_END\n
         * ANSI_COLOR_BEGIN   some text2  ANSI_COLOR_END\n
         * ANSI_COLOR_BEGIN   some text3  ANSI_COLOR_END\n
         * */
        return color
                + StringUtils.replace(textWithLf, "\n", AnsiColorConstants.ANSI_COLOR_END + "\n"
                + color) + AnsiColorConstants.ANSI_COLOR_END;
    }

    /**
     * 统一将 \r\n 替换为 \n
     * 认为 \n 才是标准的换行符
     *
     * @param input
     * @return
     */
    private static String replaceCrLf(String input) {
        return StringUtils.replace(input, "\r\n", "\n");
    }
}
