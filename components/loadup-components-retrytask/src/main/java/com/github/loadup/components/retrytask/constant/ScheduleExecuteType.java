package com.github.loadup.components.retrytask.constant;

import com.github.loadup.commons.enums.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ScheduleExecuteType implements IEnum {
    DEFAULT("DEFAULT", "execute until the schedule call"),
    SYNC("SYNC", "execute immediately synchronized when the task is commited"),
    ASYNC("ASYNC", "execute immediately asynchronized when the task is commited");

    private String code;
    private String description;

}
