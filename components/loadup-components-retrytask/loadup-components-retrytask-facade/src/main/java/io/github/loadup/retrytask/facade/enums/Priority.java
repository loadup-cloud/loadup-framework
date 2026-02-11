package io.github.loadup.retrytask.facade.enums;

import lombok.Getter;

/**
 * Represents the priority of a retry task.
 */
@Getter
public enum Priority {
    HIGH("H"),
    LOW("L");

    private final String code;

    Priority(String code) {
        this.code = code;
    }
}
