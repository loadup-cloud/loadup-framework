package com.github.loadup.components.gateway.facade.api;

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
