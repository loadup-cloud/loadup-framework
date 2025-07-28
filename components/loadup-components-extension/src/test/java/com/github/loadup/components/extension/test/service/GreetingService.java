/* Copyright (C) LoadUp Cloud 2025 */
package com.github.loadup.components.extension.test.service;

import com.github.loadup.components.extension.annotation.Extension;
import com.github.loadup.components.extension.api.IExtensionPoint;

@Extension
public interface GreetingService extends IExtensionPoint {
    String greet();
}
