package com.github.loadup.components.gateway.facade.request;

import jakarta.validation.constraints.NotBlank;

/**
 *
 */

public class MessageProcessConfigRequest {

    /**
     * Optional
     */
    private String senderRequestParserTemplate;

    /**
     * Optional
     */
    private String senderResponseBodyAssembleTemplate;

    /**
     * Optional
     */
    private String senderResponseHeaderAssembleTemplate;

    /**
     * Required
     */
    @NotBlank(message = "receiverResponseParserTemplate can not be blank")
    private String receiverResponseParserTemplate;

    /**
     * Required
     */
    @NotBlank(message = "receiverRequestBodyAssembleTemplate can not be blank")
    private String receiverRequestBodyAssembleTemplate;

    /**
     * Required
     */
    @NotBlank(message = "receiverRequestHeaderAssembleTemplate can not be blank")
    private String receiverRequestHeaderAssembleTemplate;

    /**
     * Getter method for property <tt>senderRequestParserTemplate</tt>.
     */
    public String getSenderRequestParserTemplate() {
        return senderRequestParserTemplate;
    }

    /**
     * Setter method for property <tt>senderRequestParserTemplate</tt>.
     */
    public void setSenderRequestParserTemplate(String senderRequestParserTemplate) {
        this.senderRequestParserTemplate = senderRequestParserTemplate;
    }

    /**
     * Getter method for property <tt>senderResponseBodyAssembleTemplate</tt>.
     */
    public String getSenderResponseBodyAssembleTemplate() {
        return senderResponseBodyAssembleTemplate;
    }

    /**
     * Setter method for property <tt>senderResponseBodyAssembleTemplate</tt>.
     */
    public void setSenderResponseBodyAssembleTemplate(String senderResponseBodyAssembleTemplate) {
        this.senderResponseBodyAssembleTemplate = senderResponseBodyAssembleTemplate;
    }

    /**
     * Getter method for property <tt>senderResponseHeaderAssembleTemplate</tt>.
     */
    public String getSenderResponseHeaderAssembleTemplate() {
        return senderResponseHeaderAssembleTemplate;
    }

    /**
     * Setter method for property <tt>senderResponseHeaderAssembleTemplate</tt>.
     */
    public void setSenderResponseHeaderAssembleTemplate(String senderResponseHeaderAssembleTemplate) {
        this.senderResponseHeaderAssembleTemplate = senderResponseHeaderAssembleTemplate;
    }

    /**
     * Getter method for property <tt>receiverResponseParserTemplate</tt>.
     */
    public String getReceiverResponseParserTemplate() {
        return receiverResponseParserTemplate;
    }

    /**
     * Setter method for property <tt>receiverResponseParserTemplate</tt>.
     */
    public void setReceiverResponseParserTemplate(String receiverResponseParserTemplate) {
        this.receiverResponseParserTemplate = receiverResponseParserTemplate;
    }

    /**
     * Getter method for property <tt>receiverRequestBodyAssembleTemplate</tt>.
     */
    public String getReceiverRequestBodyAssembleTemplate() {
        return receiverRequestBodyAssembleTemplate;
    }

    /**
     * Setter method for property <tt>receiverRequestBodyAssembleTemplate</tt>.
     */
    public void setReceiverRequestBodyAssembleTemplate(String receiverRequestBodyAssembleTemplate) {
        this.receiverRequestBodyAssembleTemplate = receiverRequestBodyAssembleTemplate;
    }

    /**
     * Getter method for property <tt>receiverRequestHeaderAssembleTemplate</tt>.
     */
    public String getReceiverRequestHeaderAssembleTemplate() {
        return receiverRequestHeaderAssembleTemplate;
    }

    /**
     * Setter method for property <tt>receiverRequestHeaderAssembleTemplate</tt>.
     */
    public void setReceiverRequestHeaderAssembleTemplate(String receiverRequestHeaderAssembleTemplate) {
        this.receiverRequestHeaderAssembleTemplate = receiverRequestHeaderAssembleTemplate;
    }
}