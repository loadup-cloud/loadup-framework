package io.github.loadup.commons.util.context;

/*-
 * #%L
 * loadup-commons-util
 * %%
 * Copyright (C) 2022 - 2024 loadup_cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author Lise
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
     * <p>这里做一个特殊的处理,在特殊的CPU上面，会有获取不到CPU数量的情况，所以这里做一个保护; 默认给一个7，真实的CPU基本都是偶数，方便区分。
     * 如果不做处理，会出现创建线程池时{@link ThreadPoolExecutor}，抛出异常：{@link IllegalArgumentException}
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
