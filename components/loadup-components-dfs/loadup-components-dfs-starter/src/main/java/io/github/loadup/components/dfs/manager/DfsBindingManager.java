package io.github.loadup.components.dfs.manager;

import io.github.loadup.components.dfs.binder.DfsBinder;
import io.github.loadup.components.dfs.binding.DfsBinding;
import io.github.loadup.components.dfs.properties.DfsGroupProperties;
import io.github.loadup.framework.api.manager.BindingManagerSupport;
import org.springframework.context.ApplicationContext;

/** DFS 绑定管理器 继承通用内核，指定驱动类型为 DfsBinder，业务接口为 Binding */
public class DfsBindingManager extends BindingManagerSupport<DfsBinder, DfsBinding> {

  private final DfsGroupProperties groupProps;

  public DfsBindingManager(DfsGroupProperties props, ApplicationContext context) {
    // 传入 Spring 上下文和配置前缀：loadup.dfs
    super(context, "loadup.dfs");
    this.groupProps = props;
  }

  /** 实现内核要求的钩子：获取默认 Binder 类型 */
  @Override
  protected String getDefaultBinderType() {
    return groupProps.getDefaultBinder().getValue();
  }

  /** 实现内核要求的钩子：指定驱动接口类型，用于容器查找原型 Bean */
  @Override
  public Class<DfsBinder> getBinderInterface() {
    return DfsBinder.class;
  }

  @Override
  public Class<DfsBinding> getBindingInterface() {
    return DfsBinding.class;
  }
}
