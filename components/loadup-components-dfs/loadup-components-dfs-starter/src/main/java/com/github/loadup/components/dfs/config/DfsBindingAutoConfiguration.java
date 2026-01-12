package com.github.loadup.components.dfs.config;

import com.github.loadup.components.dfs.database.binder.DatabaseDfsBinder;
import com.github.loadup.components.dfs.database.binding.DatabaseDfsBinding;
import com.github.loadup.components.dfs.database.cfg.DatabaseDfsBinderCfg;
import com.github.loadup.components.dfs.local.binder.LocalDfsBinder;
import com.github.loadup.components.dfs.local.binding.LocalDfsBinding;
import com.github.loadup.components.dfs.local.cfg.LocalDfsBinderCfg;
import com.github.loadup.components.dfs.manager.DfsBindingManager;
import com.github.loadup.components.dfs.properties.DfsGroupProperties;
import com.github.loadup.components.dfs.s3.binder.S3DfsBinder;
import com.github.loadup.components.dfs.s3.binding.S3DfsBinding;
import com.github.loadup.components.dfs.s3.cfg.S3DfsBinderCfg;
import com.github.loadup.framework.api.cfg.BaseBindingCfg;
import com.github.loadup.framework.api.core.BindingPostProcessor;
import com.github.loadup.framework.api.manager.BindingMetadata;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@AutoConfiguration
@EnableConfigurationProperties(DfsGroupProperties.class)
public class DfsBindingAutoConfiguration {

  /** 将 DfsBindingManager 定义为一个单例 Bean 依赖的 props 和 context 会由 Spring 自动注入到方法参数中 */
  @Bean
  @ConditionalOnMissingBean
  public DfsBindingManager dfsBindingManager(DfsGroupProperties props, ApplicationContext context) {
    return new DfsBindingManager(props, context);
  }

  @Bean
  public BindingPostProcessor bindingPostProcessor(ApplicationContext context) {
    // 显式传入 context
    return new BindingPostProcessor(context);
  }

  /** S3 模块的自动注册逻辑 只有当 classpath 下存在 S3DfsBinding 类时（即引入了 binder-s3 依赖），这段逻辑才生效 */
  @Configuration
  @ConditionalOnClass(name = "com.github.loadup.components.dfs.s3.binding.S3DfsBinding")
  static class S3BindingRegistry {
    // Spring 会自动注入上面定义的 dfsBindingManager
    public S3BindingRegistry(DfsBindingManager dfsBindingManager) {
      dfsBindingManager.register(
          "s3",
          new BindingMetadata<>(
              BaseBindingCfg.class,
              S3DfsBinderCfg.class,
              S3DfsBinder.class,
              ctx -> new S3DfsBinding()));
    }
  }

  /** Local 模块的自动注册逻辑 */
  @Configuration
  @ConditionalOnClass(name = "com.github.loadup.components.dfs.local.binding.LocalDfsBinding")
  static class LocalBindingRegistry {
    public LocalBindingRegistry(DfsBindingManager dfsBindingManager) {
      dfsBindingManager.register(
          "local",
          new BindingMetadata<>(
              BaseBindingCfg.class,
              LocalDfsBinderCfg.class,
              LocalDfsBinder.class,
              ctx -> new LocalDfsBinding()));
    }
  }

  /** Database 模块的自动注册逻辑 */
  @Configuration
  @ConditionalOnClass(name = "com.github.loadup.components.dfs.database.binding.DatabaseDfsBinding")
  static class DatabaseBindingRegistry {
    public DatabaseBindingRegistry(DfsBindingManager dfsBindingManager) {
      dfsBindingManager.register(
          "database",
          new BindingMetadata<>(
              BaseBindingCfg.class,
              DatabaseDfsBinderCfg.class,
              DatabaseDfsBinder.class,
              ctx -> new DatabaseDfsBinding()));
    }
  }
}
