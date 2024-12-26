package com.github.loadup.components.gateway.service.template;

/**
 * service callback
 */
public interface ServiceCallback {

    /**
     * check parameter
     */
    void checkParameter();

    /**
     * pre process
     */
    void preProcess();

    /**
     * process
     */
    void process();

}