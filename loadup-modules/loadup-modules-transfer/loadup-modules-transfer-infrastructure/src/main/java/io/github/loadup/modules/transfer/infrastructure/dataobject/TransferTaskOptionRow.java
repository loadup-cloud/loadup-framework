package io.github.loadup.modules.transfer.infrastructure.dataobject;

import com.mybatisflex.annotation.Table;
import io.github.loadup.commons.dataobject.BaseDO;
import java.time.LocalDateTime;

/**
 * Row model for transfer task options.
 */
@Table("transfer_task_option")
public class TransferTaskOptionRow extends BaseDO {
    private String taskId;
    private String optionName;
    private String optionValue;

    public TransferTaskOptionRow() {}

    public TransferTaskOptionRow(
            String id,
            String taskId,
            String optionName,
            String optionValue,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            boolean deleted) {
        setId(id);
        this.taskId = taskId;
        this.optionName = optionName;
        this.optionValue = optionValue;
        setCreatedAt(createdAt);
        setUpdatedAt(updatedAt);
        setDeleted(deleted);
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String value) {
        this.taskId = value;
    }

    public String getOptionName() {
        return optionName;
    }

    public void setOptionName(String value) {
        this.optionName = value;
    }

    public String getOptionValue() {
        return optionValue;
    }

    public void setOptionValue(String value) {
        this.optionValue = value;
    }
}
