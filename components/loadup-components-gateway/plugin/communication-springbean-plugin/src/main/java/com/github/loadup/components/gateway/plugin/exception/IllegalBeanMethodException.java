package com.github.loadup.components.gateway.plugin.exception;

/**
 * Illegal bean method exception: there is not such method in bean or could not been access
 */
public class IllegalBeanMethodException extends Exception {
    public IllegalBeanMethodException(String msg) {
        super(msg);
    }

}
