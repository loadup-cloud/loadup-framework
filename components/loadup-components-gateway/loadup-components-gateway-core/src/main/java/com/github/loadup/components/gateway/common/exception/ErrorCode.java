package com.github.loadup.components.gateway.common.exception;

/**
 * Define the error code parent
 */
public interface ErrorCode {

    /**
     * get the error code
     */
    public String getCode();

    /**
     * get the error message
     */
    public String getMessage();

    /**
     * get the error status
     * S is success
     * F is fail
     * U is unknown
     */
    public String getStatus();
}