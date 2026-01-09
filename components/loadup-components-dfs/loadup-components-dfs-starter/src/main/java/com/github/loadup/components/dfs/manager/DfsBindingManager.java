package com.github.loadup.components.dfs.manager;

import com.github.loadup.components.dfs.api.DfsBinder;
import com.github.loadup.components.dfs.api.DfsBinding;
import com.github.loadup.components.dfs.properties.DfsGroupProperties;
import com.github.loadup.framework.api.manager.BindingManagerSupport;
import java.util.*;
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
    return groupProps.getDefaultBinder();
  }

  /** 实现内核要求的钩子：指定驱动接口类型，用于容器查找原型 Bean */
  @Override
  protected Class<DfsBinder> getBinderInterface() {
    return DfsBinder.class;
  }
}
