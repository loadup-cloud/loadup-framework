package com.github.loadup.commons.util;

import java.util.Objects;

public class ObjectUtil {

    /**
     * check if the object is not null
     *
     * @param obj obj
     * @return true if not null
     */
    public static boolean isNotNull(Object obj) {
        return !Objects.isNull(obj);
    }
}

