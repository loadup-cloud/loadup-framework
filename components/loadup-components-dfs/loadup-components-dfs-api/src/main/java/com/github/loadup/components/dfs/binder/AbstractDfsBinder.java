package com.github.loadup.components.dfs.binder;

import com.github.loadup.components.dfs.binding.DfsBinding;
import com.github.loadup.components.dfs.cfg.DfsBinderCfg;
import com.github.loadup.components.dfs.cfg.DfsBindingCfg;
import com.github.loadup.components.dfs.model.*;
import com.github.loadup.framework.api.binder.AbstractBinder;
import com.github.loadup.framework.api.binding.AbstractBinding;

// DFS 专属 Binding 抽象
public abstract class AbstractDfsBinder<C extends DfsBinderCfg, S extends DfsBindingCfg>
    extends AbstractBinder<C, S> implements DfsBinder<C, S> {}
