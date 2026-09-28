package io.github.loadup.modules.upms.domain.valueobject;

import java.util.Arrays;
import java.util.Optional;

public enum DataScope {
    ALL((short) 1),
    CUSTOM_DEPARTMENTS((short) 2),
    DEPARTMENT((short) 3),
    DEPARTMENT_TREE((short) 4),
    OWNER((short) 5);

    private final short code;

    DataScope(short code) {
        this.code = code;
    }

    public short code() {
        return code;
    }

    public static Optional<DataScope> fromCode(Short code) {
        return Arrays.stream(values())
                .filter(scope -> code != null && scope.code == code)
                .findFirst();
    }
}
