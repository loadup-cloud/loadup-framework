package com.github.loadup.components.scheduler.starter.config;

import com.github.loadup.components.scheduler.binding.DefaultSchedulerBinding;
import com.github.loadup.components.scheduler.cfg.SchedulerBindingCfg;
import com.github.loadup.components.scheduler.core.SchedulerTaskRegistry;
import com.github.loadup.components.scheduler.simplejob.binder.SimpleJobSchedulerBinder;
import com.github.loadup.components.scheduler.simplejob.cfg.SimpleJobSchedulerBinderCfg;
import com.github.loadup.components.scheduler.starter.initializer.SchedulerJobInitializer;
import com.github.loadup.components.scheduler.starter.manager.SchedulerBindingManager;
import com.github.loadup.components.scheduler.starter.properties.SchedulerGroupProperties;
import com.github.loadup.framework.api.core.BindingPostProcessor;
import com.github.loadup.framework.api.manager.BindingMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(SchedulerGroupProperties.class)
public class SchedulerBindingAutoConfiguration {
  /** 将 SchedulerBindingManager 定义为一个单例 Bean 依赖的 props 和 context 会由 Spring 自动注入到方法参数中 */
  @Bean
  @ConditionalOnMissingBean
  public SchedulerBindingManager bindingManager(
      SchedulerGroupProperties props, ApplicationContext context) {
    return new SchedulerBindingManager(props, context);
  }

  @Bean
  public BindingPostProcessor bindingPostProcessor(ApplicationContext context) {
    // 显式传入 context
    return new BindingPostProcessor(context);
  }

  /** 核心：注册初始化器，它会自动处理注解任务 */
  @Bean
  public SchedulerJobInitializer schedulerJobInitializer(
      ApplicationContext context, SchedulerBindingManager manager) {
    return new SchedulerJobInitializer(context, manager);
  }

  /** 当 classpath 中存在 Caffeine 时，注册其元数据 */
  @Bean
  @ConditionalOnClass(
      name = "com.github.loadup.components.scheduler.simplejob.binder.SimpleJobSchedulerBinder")
  public BindingMetadata<?, ?, ?, ?> simpleJobMetadata() {
    return new BindingMetadata<>(
        "simplejob",
        DefaultSchedulerBinding.class,
        SimpleJobSchedulerBinder.class,
        SchedulerBindingCfg.class,
        SimpleJobSchedulerBinderCfg.class,
        ctx -> new DefaultSchedulerBinding());
  }
}
