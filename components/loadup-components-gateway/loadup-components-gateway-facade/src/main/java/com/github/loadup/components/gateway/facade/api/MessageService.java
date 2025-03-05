package com.github.loadup.components.gateway.facade.api;

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

import com.github.loadup.components.gateway.facade.request.SPIRequest;
import com.github.loadup.components.gateway.facade.response.SPIResponse;

import java.util.Map;

/**
 * <p>Message Service Send Service</p>
 */
public interface MessageService {

    /**
     * <p>
     * message send
     * interfaceOrUrl can be set as below
     * message can be set as below
     * Map param = new HashMap();
     * param.put("id","abc");
     * param.put("authorzation","123");
     * </p>
     *
     *
     * <ol>
     *  <li> http://domaneName/path</li>
     *  <li> https://domainName/path</li>
     *  <li> TR://domainName:12000/class/method</li>
     *  <li> SPRINGBEAN://class/method</li>
     *  <li> testRestful</li>
     * </ol>
     */
    @Deprecated
    String send(String interfaceOrUrl, Map<String, String> message);

    /**
     * message send
     * SPIRequest.interfaceOrUrl can be set as below
     * SPIRequest.message can be set as below
     * Map param = new HashMap();
     * param.put("id","abc");
     * param.put("authorzation","123");
     * <p></p>
     *
     * <ol>
     *  <li> http://domaneName/path</li>
     *  <li> https://domainName/path</li>
     *  <li> TR://domainName:12000/class/method</li>
     *  <li> SPRINGBEAN://class/method</li>
     *  <li> testRestful</li>
     * </ol>
     */
    SPIResponse send(SPIRequest request);
}
