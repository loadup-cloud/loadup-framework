package io.github.loadup.components.scheduler.simplejob.context;

/*-
 * #%L
 * Loadup Scheduler Simplejob Binder
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
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

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class CustomThreadFactory implements ThreadFactory {
    private final String namePrefix;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final boolean daemon;

    /**
     * @param namePrefix 线程名前缀，例如 "task-exec-order"
     */
    public CustomThreadFactory(String namePrefix) {
        this(namePrefix, false);
    }

    /**
     * @param namePrefix 线程名前缀
     * @param daemon     是否为守护线程（如果为 true，主线程退出时该线程会强制退出）
     */
    public CustomThreadFactory(String namePrefix, boolean daemon) {
        this.namePrefix = namePrefix.endsWith("-") ? namePrefix : namePrefix + "-";
        this.daemon = daemon;
    }

    @Override
    public Thread newThread(Runnable r) {
        // 创建线程并设置名字：例如 task-exec-order-1
        Thread t = new Thread(r, namePrefix + threadNumber.getAndIncrement());

        // 设置守护状态
        t.setDaemon(daemon);

        // 确保线程优先级为正常
        if (t.getPriority() != Thread.NORM_PRIORITY) {
            t.setPriority(Thread.NORM_PRIORITY);
        }

        return t;
    }
}
