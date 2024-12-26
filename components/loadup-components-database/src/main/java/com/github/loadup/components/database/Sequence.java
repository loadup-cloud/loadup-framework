package com.github.loadup.components.database;

import com.github.loadup.commons.dataobject.BaseDO;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@Table("sys_sequence")
public class Sequence extends BaseDO {
    private String id;
    private String name;
    private Long   value;
    private Long   minValue;
    private Long   maxValue;
    private Long   step;

}
