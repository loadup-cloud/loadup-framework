package io.github.loadup.components.scheduler.quartz.cfg;

/*-
 * #%L
 * Loadup Scheduler Quartz Binder
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

import io.github.loadup.components.scheduler.cfg.SchedulerBinderCfg;
import lombok.Getter;
import lombok.Setter;

import java.util.Properties;


@Getter
@Setter
public class QuartzBinderCfg extends SchedulerBinderCfg {
    // 数据源名称（如果有多个数据源）
    private String dataSourceName;
    // 是否开启持久化
    private boolean overwriteExistingJobs = true;
    // Quartz 专属属性 (如 org.quartz.jobStore.class 等)
    private Properties quartzProperties = new Properties();

    public QuartzBinderCfg() {
        // 设置默认属性，防止启动报错
        quartzProperties.setProperty("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
        quartzProperties.setProperty("org.quartz.threadPool.threadCount", "10");
        quartzProperties.setProperty("org.quartz.scheduler.instanceName", "LoadupScheduler");
        quartzProperties.setProperty("org.quartz.scheduler.instanceId", "AUTO");
    }

    @Override
    public Object getIdentity() {
        return "QUARTZ:" + quartzProperties.getProperty("org.quartz.threadPool.threadCount");
    }
}
