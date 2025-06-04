/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.framework.api.binder;

/**
 *
 */
public interface BaseBinder {
    String getName();

    default void init() {}

    default void postProcessAfterInstantiation() {}

    default void destroy() {}
}
