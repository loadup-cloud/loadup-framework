/*-
 * #%L
 * Loadup Common Log
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
package io.github.loadup.commons.log;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Convenience facade for parameterized application logging. */
public final class LogUtil {

    private static final Logger FALLBACK_LOGGER = LoggerFactory.getLogger(LogUtil.class);
    private static final StackWalker CALLER_WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

    private LogUtil() {}

    /** Returns a logger named after the supplied source class. */
    public static Logger getLogger(Class<?> sourceClass) {
        return LoggerFactory.getLogger(sourceClass);
    }

    /** Returns a logger with the supplied name. */
    public static Logger getLogger(String loggerName) {
        return LoggerFactory.getLogger(loggerName);
    }

    public static void trace(Class<?> sourceClass, String message, Object... arguments) {
        getLogger(sourceClass).trace(message, arguments);
    }

    public static void debug(Class<?> sourceClass, String message, Object... arguments) {
        getLogger(sourceClass).debug(message, arguments);
    }

    public static void info(Class<?> sourceClass, String message, Object... arguments) {
        getLogger(sourceClass).info(message, arguments);
    }

    public static void warn(Class<?> sourceClass, String message, Object... arguments) {
        getLogger(sourceClass).warn(message, arguments);
    }

    public static void error(Class<?> sourceClass, String message, Object... arguments) {
        getLogger(sourceClass).error(message, arguments);
    }

    /** Logs with the calling class as the logger name. Intended for short-lived development diagnostics. */
    public static void trace(String message, Object... arguments) {
        callerLogger().trace(message, arguments);
    }

    /** Logs with the calling class as the logger name. Intended for short-lived development diagnostics. */
    public static void debug(String message, Object... arguments) {
        callerLogger().debug(message, arguments);
    }

    /** Logs with the calling class as the logger name. Intended for short-lived development diagnostics. */
    public static void info(String message, Object... arguments) {
        callerLogger().info(message, arguments);
    }

    /** Logs with the calling class as the logger name. Intended for short-lived development diagnostics. */
    public static void warn(String message, Object... arguments) {
        callerLogger().warn(message, arguments);
    }

    /** Logs with the calling class as the logger name. Intended for short-lived development diagnostics. */
    public static void error(String message, Object... arguments) {
        callerLogger().error(message, arguments);
    }

    private static Logger callerLogger() {
        Optional<Class<?>> caller = CALLER_WALKER.walk(frames -> frames.map(StackWalker.StackFrame::getDeclaringClass)
                .filter(type -> type != LogUtil.class)
                .findFirst());
        return caller.map(LoggerFactory::getLogger).orElse(FALLBACK_LOGGER);
    }
}
