package com.github.loadup.components.dfs.config;

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
import jakarta.annotation.PostConstruct;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@EnableConfigurationProperties(DfsGroupProperties.class)
// 导入通用的 PostProcessor 以支持 @BindingClient 注解
@Import(BindingPostProcessor.class)
public class DfsBindingAutoConfiguration {

  private final DfsBindingManager dfsBindingManager;

  public DfsBindingAutoConfiguration(DfsGroupProperties props, ApplicationContext context) {
    // 在这里实例化管理器，它会作为 Bean 进入容器
    this.dfsBindingManager = new DfsBindingManager(props, context);
  }

  @Bean
  @ConditionalOnMissingBean
  public DfsBindingManager dfsBindingManager() {
    return this.dfsBindingManager;
  }

  /** S3 模块的自动注册逻辑 只有当 classpath 下存在 S3DfsBinding 类时（即引入了 binder-s3 依赖），这段逻辑才生效 */
  @Configuration
  @ConditionalOnClass(name = "com.github.loadup.components.dfs.s3.binding.S3DfsBinding")
  class S3BindingRegistry {
    @PostConstruct
    public void init() {
      // 注意：这里不需要注册 S3DfsBinder 为 Bean，Manager 会根据 class 自动创建
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
  class LocalBindingRegistry {
    @PostConstruct
    public void init() {
      dfsBindingManager.register(
          "local",
          new BindingMetadata<>(
              BaseBindingCfg.class,
              LocalDfsBinderCfg.class,
              LocalDfsBinder.class,
              ctx -> new LocalDfsBinding()));
    }
  }
}
