package com.github.loadup.components.gateway.facade.response;

import com.github.loadup.components.gateway.facade.model.Result;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Map;

/**
 *
 */
public abstract class BaseResponse {

    /**
     * capability result
     */
    private Result result;

    /**
     * tenant id
     */
    private String tntInstId;

    /**
     * success or not
     */
    private boolean success;

    /**
     * error message
     */
    private String errorMessage;

    /**
     * extend info
     */
    private Map<String, String> extendInfo;

    /**
     * Getter method for property <tt>success</tt>.
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Setter method for property <tt>success</tt>.
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }

    /**
     * Getter method for property <tt>errorMessage</tt>.
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Setter method for property <tt>errorMessage</tt>.
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * Getter method for property <tt>extendInfo</tt>.
     */
    public Map<String, String> getExtendInfo() {
        return extendInfo;
    }

    /**
     * Setter method for property <tt>extendInfo</tt>.
     */
    public void setExtendInfo(Map<String, String> extendInfo) {
        this.extendInfo = extendInfo;
    }

    /**
     * Getter method for property <tt>result</tt>.
     */
    public Result getResult() {
        return result;
    }

    /**
     * Setter method for property <tt>result</tt>.
     */
    public void setResult(Result result) {
        this.result = result;
    }

    /**
     * Getter method for property <tt>tntInstId</tt>.
     */
    public String getTntInstId() {
        return tntInstId;
    }

    /**
     * Setter method for property <tt>tntInstId</tt>.
     */
    public void setTntInstId(String tntInstId) {
        this.tntInstId = tntInstId;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}