package com.github.loadup.components.scheduler.powerjob.cfg;

import io.github.loadup.components.scheduler.cfg.SchedulerBinderCfg;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PowerJobSchedulerBinderCfg extends SchedulerBinderCfg {
    @Override
    public Object getIdentity() {
        return "powerjob:" + getName();
    }
}
