package com.github.loadup.components.gateway.plugin.exception;

/**
 * SpringBean uri is not in the proper format, and can't be parsed
 */
public class IllegalBeanUriException extends Exception {

    public IllegalBeanUriException(String msg) {
        super(msg);
    }

    public IllegalBeanUriException(String msg, Throwable throwable) {
        super(msg, throwable);
    }
}
