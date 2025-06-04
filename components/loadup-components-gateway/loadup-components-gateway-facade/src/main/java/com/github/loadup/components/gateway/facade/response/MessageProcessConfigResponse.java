/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.gateway.facade.response;

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

/**
 *
 */
public class MessageProcessConfigResponse {

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
    private String receiverResponseParserTemplate;

    /**
     * Required
     */
    private String receiverRequestBodyAssembleTemplate;

    /**
     * Required
     */
    private String receiverRequestHeaderAssembleTemplate;

    /**
     *
     */
    public String getSenderRequestParserTemplate() {
        return senderRequestParserTemplate;
    }

    /**
     *
     */
    public void setSenderRequestParserTemplate(String senderRequestParserTemplate) {
        this.senderRequestParserTemplate = senderRequestParserTemplate;
    }

    /**
     *
     */
    public String getSenderResponseBodyAssembleTemplate() {
        return senderResponseBodyAssembleTemplate;
    }

    /**
     *
     */
    public void setSenderResponseBodyAssembleTemplate(String senderResponseBodyAssembleTemplate) {
        this.senderResponseBodyAssembleTemplate = senderResponseBodyAssembleTemplate;
    }

    /**
     *
     */
    public String getSenderResponseHeaderAssembleTemplate() {
        return senderResponseHeaderAssembleTemplate;
    }

    /**
     *
     */
    public void setSenderResponseHeaderAssembleTemplate(String senderResponseHeaderAssembleTemplate) {
        this.senderResponseHeaderAssembleTemplate = senderResponseHeaderAssembleTemplate;
    }

    /**
     *
     */
    public String getReceiverResponseParserTemplate() {
        return receiverResponseParserTemplate;
    }

    /**
     *
     */
    public void setReceiverResponseParserTemplate(String receiverResponseParserTemplate) {
        this.receiverResponseParserTemplate = receiverResponseParserTemplate;
    }

    /**
     *
     */
    public String getReceiverRequestBodyAssembleTemplate() {
        return receiverRequestBodyAssembleTemplate;
    }

    /**
     *
     */
    public void setReceiverRequestBodyAssembleTemplate(String receiverRequestBodyAssembleTemplate) {
        this.receiverRequestBodyAssembleTemplate = receiverRequestBodyAssembleTemplate;
    }

    /**
     *
     */
    public String getReceiverRequestHeaderAssembleTemplate() {
        return receiverRequestHeaderAssembleTemplate;
    }

    /**
     *
     */
    public void setReceiverRequestHeaderAssembleTemplate(String receiverRequestHeaderAssembleTemplate) {
        this.receiverRequestHeaderAssembleTemplate = receiverRequestHeaderAssembleTemplate;
    }
}
