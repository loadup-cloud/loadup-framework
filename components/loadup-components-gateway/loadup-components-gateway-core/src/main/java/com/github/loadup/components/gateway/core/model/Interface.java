/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.gateway.core.model;

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

import com.github.loadup.components.gateway.core.common.enums.InterfaceType;

/**
 *
 */
public class Interface {

    /**
     * interface config
     */
    private InterfaceConfig apiConfig;

    /**
     * api sender config
     */
    private MessageSenderConfig apiSenderConfig;

    /**
     * api receiver config
     */
    private MessageReceiverConfig apiReceiverConfig;

    /**
     * api message process config
     */
    private MessageProcessConfig apiProcessConfig;

    /**
     * api communication config
     */
    private CommunicationConfig apiCommunicationConfig;

    /**
     * interface status
     */
    private String status;

    /**
     * interface type
     */
    private InterfaceType type;

    /**
     * spi interface config
     */
    private InterfaceConfig spiConfig;

    /**
     * spi receiver config
     */
    private MessageReceiverConfig spiReceiverConfig;

    /**
     * spi message process config
     */
    private MessageProcessConfig spiProcessConfig;

    /**
     * spi communication config
     */
    private CommunicationConfig spiCommunicationConfig;

    /**
     *
     */
    public InterfaceConfig getApiConfig() {
        return apiConfig;
    }

    /**
     *
     */
    public void setApiConfig(InterfaceConfig apiConfig) {
        this.apiConfig = apiConfig;
    }

    /**
     *
     */
    public MessageSenderConfig getApiSenderConfig() {
        return apiSenderConfig;
    }

    /**
     *
     */
    public void setApiSenderConfig(MessageSenderConfig apiSenderConfig) {
        this.apiSenderConfig = apiSenderConfig;
    }

    /**
     *
     */
    public String getStatus() {
        return status;
    }

    /**
     *
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     *
     */
    public InterfaceType getType() {
        return type;
    }

    /**
     *
     */
    public void setType(InterfaceType type) {
        this.type = type;
    }

    /**
     *
     */
    public MessageReceiverConfig getApiReceiverConfig() {
        return apiReceiverConfig;
    }

    /**
     *
     */
    public void setApiReceiverConfig(MessageReceiverConfig apiReceiverConfig) {
        this.apiReceiverConfig = apiReceiverConfig;
    }

    /**
     *
     */
    public MessageProcessConfig getApiProcessConfig() {
        return apiProcessConfig;
    }

    /**
     *
     */
    public void setApiProcessConfig(MessageProcessConfig apiProcessConfig) {
        this.apiProcessConfig = apiProcessConfig;
    }

    /**
     *
     */
    public CommunicationConfig getApiCommunicationConfig() {
        return apiCommunicationConfig;
    }

    /**
     *
     */
    public void setApiCommunicationConfig(CommunicationConfig apiCommunicationConfig) {
        this.apiCommunicationConfig = apiCommunicationConfig;
    }

    /**
     *
     */
    public InterfaceConfig getSpiConfig() {
        return spiConfig;
    }

    /**
     *
     */
    public void setSpiConfig(InterfaceConfig spiConfig) {
        this.spiConfig = spiConfig;
    }

    /**
     *
     */
    public MessageReceiverConfig getSpiReceiverConfig() {
        return spiReceiverConfig;
    }

    /**
     *
     */
    public void setSpiReceiverConfig(MessageReceiverConfig spiReceiverConfig) {
        this.spiReceiverConfig = spiReceiverConfig;
    }

    /**
     *
     */
    public MessageProcessConfig getSpiProcessConfig() {
        return spiProcessConfig;
    }

    /**
     *
     */
    public void setSpiProcessConfig(MessageProcessConfig spiProcessConfig) {
        this.spiProcessConfig = spiProcessConfig;
    }

    /**
     *
     */
    public CommunicationConfig getSpiCommunicationConfig() {
        return spiCommunicationConfig;
    }

    /**
     *
     */
    public void setSpiCommunicationConfig(CommunicationConfig spiCommunicationConfig) {
        this.spiCommunicationConfig = spiCommunicationConfig;
    }
}
