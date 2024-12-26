package com.github.loadup.commons.util.context;

/*-
 * #%L
 * loadup-commons-util
 * %%
 * Copyright (C) 2022 - 2024 loadup_cloud
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

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author Laysan
 * @since 1.0.0
 */
public class RuntimeUtils {

    /**
     * 增加一个JVM关闭后的钩子，用于在JVM关闭时执行某些操作
     *
     * @since 4.0.5
     */
    public static void addShutdownHook(Runnable hook) {
        Runtime.getRuntime().addShutdownHook((hook instanceof Thread) ? (Thread) hook : new Thread(hook));
    }

    /**
     * 获得JVM可用的处理器数量（一般为CPU核心数）
     *
     * <p>
     * 这里做一个特殊的处理,在特殊的CPU上面，会有获取不到CPU数量的情况，所以这里做一个保护;
     * 默认给一个7，真实的CPU基本都是偶数，方便区分。
     * 如果不做处理，会出现创建线程池时{@link ThreadPoolExecutor}，抛出异常：{@link IllegalArgumentException}
     * </p>
     *
     * @since 5.3.0
     */
    public static int getProcessorCount() {
        int cpu = Runtime.getRuntime().availableProcessors();
        if (cpu <= 0) {
            cpu = 7;
        }
        return cpu;
    }

    /**
     * 获得JVM中剩余的内存数，单位byte
     *
     * @since 5.3.0
     */
    public static long getFreeMemory() {
        return Runtime.getRuntime().freeMemory();
    }

    /**
     * 获得JVM已经从系统中获取到的总共的内存数，单位byte
     *
     * @since 5.3.0
     */
    public static long getTotalMemory() {
        return Runtime.getRuntime().totalMemory();
    }

    /**
     * 获得JVM中可以从系统中获取的最大的内存数，单位byte，以-Xmx参数为准
     *
     * @since 5.3.0
     */
    public static long getMaxMemory() {
        return Runtime.getRuntime().maxMemory();
    }

    /**
     * 获得JVM最大可用内存，计算方法为：<br>
     * 最大内存-总内存+剩余内存
     */
    public static long getUsableMemory() {
        return getMaxMemory() - getTotalMemory() + getFreeMemory();
    }

}
