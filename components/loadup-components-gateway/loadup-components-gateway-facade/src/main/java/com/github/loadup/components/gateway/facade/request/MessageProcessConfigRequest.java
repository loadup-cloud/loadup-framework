package com.github.loadup.components.gateway.facade.request;

/*-
 * #%L
 * loadup-components-gateway-facade
 * %%
 * Copyright (C) 2022 - 2025 loadup_cloud
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

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
