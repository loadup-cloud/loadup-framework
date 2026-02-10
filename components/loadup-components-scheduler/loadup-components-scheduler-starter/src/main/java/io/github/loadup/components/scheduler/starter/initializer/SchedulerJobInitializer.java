package io.github.loadup.components.scheduler.starter.initializer;

/*-
 * #%L
 * Loadup Scheduler Starter
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

import io.github.loadup.components.scheduler.annotation.DistributedScheduler;
import io.github.loadup.components.scheduler.binding.SchedulerBinding;
import io.github.loadup.components.scheduler.model.SchedulerTask;
import io.github.loadup.components.scheduler.starter.manager.SchedulerBindingManager;
import java.lang.reflect.Method;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.StringUtils;

@Slf4j
public class SchedulerJobInitializer implements SmartInitializingSingleton {

    private final ApplicationContext context;
    private final SchedulerBindingManager manager;

    public SchedulerJobInitializer(ApplicationContext context, SchedulerBindingManager manager) {
        this.context = context;
        this.manager = manager;
    }

    @Override
    public void afterSingletonsInstantiated() {
        // 扫描 Spring 容器中所有的 Bean
        String[] beanNames = context.getBeanNamesForType(Object.class);

        for (String beanName : beanNames) {
            Object bean = context.getBean(beanName);
            Class<?> targetClass = bean.getClass();

            // 查找所有标注了 @DistributedScheduler 的方法
            Map<Method, DistributedScheduler> annotatedMethods = MethodIntrospector.selectMethods(
                    targetClass, (MethodIntrospector.MetadataLookup<DistributedScheduler>)
                            method -> AnnotatedElementUtils.findMergedAnnotation(method, DistributedScheduler.class));

            annotatedMethods.forEach((method, annotation) -> registerJob(bean, method, annotation));
        }
    }

    private void registerJob(Object bean, Method method, DistributedScheduler ann) {
        String bizTag = ann.bizTag();
        // 如果没指定 name，则使用 "类名#方法名" 确保唯一性
        String taskName =
                StringUtils.hasText(ann.name()) ? ann.name() : bean.getClass().getSimpleName() + "#" + method.getName();

        // 这里的 manager.getBinding 会从缓存中获取实例
        SchedulerBinding binding = manager.getBinding(bizTag);

        if (binding == null) {
            log.error("Failed to register task [{}]: No binding config found for tag [{}]", taskName, bizTag);
            return;
        }

        // 提交任务
        log.info("Auto-registering distributed task: {} on binding: {}", taskName, bizTag);
        SchedulerTask task = SchedulerTask.builder()
                .taskName(taskName)
                .cron(ann.cron())
                .method(method)
                .targetBean(bean)
                .annotation(DistributedScheduler.class)
                .build();
        // 提交任务到驱动 (Spring/Quartz/XXL-Job)
        binding.registerTask(task);
    }
}
