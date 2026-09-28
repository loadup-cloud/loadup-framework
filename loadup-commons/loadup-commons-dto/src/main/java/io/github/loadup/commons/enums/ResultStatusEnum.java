package io.github.loadup.commons.enums;

import java.util.Arrays;
import java.util.Objects;

public enum ResultStatusEnum {
    SUCCESS("S"),
    FAIL("F"),
    UNKNOWN("U");

    private String code;

    public static ResultStatusEnum getByCode(String code) {
        return Arrays.stream(ResultStatusEnum.values())
                .filter(resultStatusEnum -> Objects.equals(resultStatusEnum.getCode(), code))
                .findFirst()
                .orElse(null);
    }

    private ResultStatusEnum(String code) {
        this.code = code;
    }

    public String getCode() {
        return this.code;
    }
}
