package io.github.loadup.components.extension.test.service;

import io.github.loadup.components.extension.annotation.Extension;
import io.github.loadup.components.extension.api.IExtensionPoint;

@Extension
public interface GreetingService extends IExtensionPoint {
    String greet();
}
