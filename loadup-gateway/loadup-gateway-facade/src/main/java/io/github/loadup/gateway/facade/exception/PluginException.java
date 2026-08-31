package io.github.loadup.gateway.facade.exception;

/*-
 * #%L
 * LoadUp Gateway Facade
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

/**
 * Plugin related exception
 */
public class PluginException extends GatewayException {

    private static final String MODULE = "PLUGIN";

    public PluginException(ErrorCode errorCode, String message) {
        super(errorCode.getCode(), ErrorType.PLUGIN, MODULE, errorCode.getMessage() + ":" + message);
    }

    public PluginException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode.getCode(), ErrorType.PLUGIN, MODULE, errorCode.getMessage() + ":" + message, cause);
    }

    // Convenience methods
    public static PluginException notFound(String pluginName) {
        return new PluginException(ErrorCode.PLUGIN_NOT_FOUND, pluginName);
    }

    public static PluginException initFailed(String pluginName, Throwable cause) {
        return new PluginException(ErrorCode.PLUGIN_INIT_FAILED, pluginName, cause);
    }

    public static PluginException executionFailed(String pluginName, Throwable cause) {
        return new PluginException(ErrorCode.PLUGIN_EXECUTION_FAILED, pluginName, cause);
    }

    public static PluginException configInvalid(String pluginName, String reason) {
        return new PluginException(ErrorCode.PLUGIN_CONFIG_INVALID, pluginName + " - " + reason);
    }
}
