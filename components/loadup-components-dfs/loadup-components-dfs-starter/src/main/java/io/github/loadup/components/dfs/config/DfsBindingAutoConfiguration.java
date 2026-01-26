package io.github.loadup.components.dfs.config;

import io.github.loadup.components.dfs.binding.DefaultDfsBinding;
import io.github.loadup.components.dfs.cfg.DfsBindingCfg;
import io.github.loadup.components.dfs.database.binder.DatabaseDfsBinder;
import io.github.loadup.components.dfs.database.cfg.DatabaseDfsBinderCfg;
import io.github.loadup.components.dfs.local.binder.LocalDfsBinder;
import io.github.loadup.components.dfs.local.cfg.LocalDfsBinderCfg;
import io.github.loadup.components.dfs.manager.DfsBindingManager;
import io.github.loadup.components.dfs.properties.DfsGroupProperties;
import io.github.loadup.components.dfs.s3.binder.S3DfsBinder;
import io.github.loadup.components.dfs.s3.cfg.S3DfsBinderCfg;
import io.github.loadup.framework.api.core.BindingPostProcessor;
import io.github.loadup.framework.api.manager.BindingMetadata;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

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

  /** Local 模块的自动注册逻辑 */
  @Bean
  @ConditionalOnClass(name = "io.github.loadup.components.dfs.local.binder.LocalDfsBinder")
  public BindingMetadata<?, ?, ?, ?> localMetadata() {
    return new BindingMetadata<>(
        "local",
        DefaultDfsBinding.class,
        LocalDfsBinder.class,
        DfsBindingCfg.class,
        LocalDfsBinderCfg.class,
        ctx -> new DefaultDfsBinding());
  }

  /** S3 模块的自动注册逻辑 只有当 classpath 下存在 S3DfsBinding 类时（即引入了 binder-s3 依赖），这段逻辑才生效 */
  @Bean
  @ConditionalOnClass(name = "io.github.loadup.components.dfs.s3.binder.S3DfsBinder")
  public BindingMetadata<?, ?, ?, ?> s3Metadata() {
    return new BindingMetadata<>(
        "s3",
        DefaultDfsBinding.class,
        S3DfsBinder.class,
        DfsBindingCfg.class,
        S3DfsBinderCfg.class,
        ctx -> new DefaultDfsBinding());
  }

  /** Database 模块的自动注册逻辑 */
  @Bean
  @ConditionalOnClass(name = "io.github.loadup.components.dfs.database.binder.DatabaseDfsBinder")
  public BindingMetadata<?, ?, ?, ?> dbMetadata() {
    return new BindingMetadata<>(
        "database",
        DefaultDfsBinding.class,
        DatabaseDfsBinder.class,
        DfsBindingCfg.class,
        DatabaseDfsBinderCfg.class,
        ctx -> new DefaultDfsBinding());
  }
}
