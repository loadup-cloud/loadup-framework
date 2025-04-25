/**

 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.github.loadup.components.testify.constant;

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
 * velocity 扩展属性。
 * <p>
 * 丰富velocity默认处理能力。
 *
 * 
 *
 */
public class VelocityExtendProperties {

    /**
     * 内置获取当前时间毫秒数变量
     */
    public static final String CUR_TIME_MILLIS_STR = "curTimeMillis";

    /**
     * 内置当前日期变量，格式：yyyy-MM-dd
     */
    public static final String CUR_DATE = "curDate";

    /**
     * 内置当前时间变量，格式：yyyy-MM-dd HH:mm:ss
     */
    public static final String CUR_DATE_TIME = "curDateTime";

    /**
     * 内置当前年份变量，格式：yyyy
     */
    public static final String CUR_YEAR = "curYear";

    /**
     * 内置当前版本，格式：2.x.x
     */
    public static final String CUR_VER = "curVer";

}
