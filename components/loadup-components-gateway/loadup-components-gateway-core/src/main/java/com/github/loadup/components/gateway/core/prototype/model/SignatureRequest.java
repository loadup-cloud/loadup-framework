package com.github.loadup.components.gateway.core.prototype.model;

/*-
 * #%L
 * loadup-components-gateway-core
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
 * Define the content to get the raw
 */
public class SignatureRequest {

    private String httpMethod;
    private String httpUri;
    private String clientId;
    private String requestTime;
    private String responseTime;
    private String message;
    private String securityStrategyCode;

    /**
     * Get the message that be prepared to be verify
     */
    public String getVerifyRawStr() {
        StringBuilder rawMessage = new StringBuilder();
        rawMessage.append(clientId);
        rawMessage.append(".");
        rawMessage.append(requestTime);
        rawMessage.append(".");
        rawMessage.append(httpMethod);
        rawMessage.append(".");
        rawMessage.append(httpUri);
        rawMessage.append(".");
        rawMessage.append(message);
        return rawMessage.toString();

    }

    /**
     * Getter method for property <tt>httpMethod</tt>.
     */
    public String getHttpMethod() {
        return httpMethod;
    }

    /**
     * Setter method for property <tt>httpMethod</tt>.
     */
    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    /**
     * Getter method for property <tt>httpUri</tt>.
     */
    public String getHttpUri() {
        return httpUri;
    }

    /**
     * Setter method for property <tt>httpUri</tt>.
     */
    public void setHttpUri(String httpUri) {
        this.httpUri = httpUri;
    }

    /**
     * Getter method for property <tt>clientId</tt>.
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * Setter method for property <tt>clientId</tt>.
     */
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /**
     * Getter method for property <tt>requestTime</tt>.
     */
    public String getRequestTime() {
        return requestTime;
    }

    /**
     * Setter method for property <tt>requestTime</tt>.
     */
    public void setRequestTime(String requestTime) {
        this.requestTime = requestTime;
    }

    /**
     * Getter method for property <tt>responseTime</tt>.
     */
    public String getResponseTime() {
        return responseTime;
    }

    /**
     * Setter method for property <tt>responseTime</tt>.
     */
    public void setResponseTime(String responseTime) {
        this.responseTime = responseTime;
    }

    /**
     * Getter method for property <tt>message</tt>.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Setter method for property <tt>message</tt>.
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Gets get security strategy code.
     */
    public String getSecurityStrategyCode() {
        return securityStrategyCode;
    }

    /**
     * Sets set security strategy code.
     */
    public void setSecurityStrategyCode(String securityStrategyCode) {
        this.securityStrategyCode = securityStrategyCode;
    }
}
