package com.github.loadup.components.gateway.facade.model;

/**
 *
 */
public class Result {
    private String resultCode;
    private String resultStatus;
    private String resultMessage;

    public Result() {
    }

    public String getResultCode() {
        return this.resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public String getResultStatus() {
        return this.resultStatus;
    }

    public void setResultStatus(String resultStatus) {
        this.resultStatus = resultStatus;
    }

    public String getResultMessage() {
        return this.resultMessage;
    }

    public void setResultMessage(String resultMessage) {
        this.resultMessage = resultMessage;
    }

}