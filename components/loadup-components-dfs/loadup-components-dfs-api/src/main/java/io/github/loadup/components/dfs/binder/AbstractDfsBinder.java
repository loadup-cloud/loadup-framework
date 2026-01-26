package io.github.loadup.components.dfs.binder;

import io.github.loadup.components.dfs.cfg.DfsBinderCfg;
import io.github.loadup.components.dfs.cfg.DfsBindingCfg;
import io.github.loadup.components.dfs.model.*;
import io.github.loadup.framework.api.binder.AbstractBinder;

// DFS 专属 Binding 抽象
public abstract class AbstractDfsBinder<C extends DfsBinderCfg, S extends DfsBindingCfg>
    extends AbstractBinder<C, S> implements DfsBinder<C, S> {}
