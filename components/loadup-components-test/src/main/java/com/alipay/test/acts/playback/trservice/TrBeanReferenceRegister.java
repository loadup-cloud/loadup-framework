///**
// * Alipay.com Inc.
// * Copyright (c) 2004-2019 All Rights Reserved.
// */
//package com.alipay.test.acts.playback.trservice;
//
///*-
// * #%L
// * loadup-components-test
// * %%
// * Copyright (C) 2022 - 2025 loadup_cloud
// * %%
// * Permission is hereby granted, free of charge, to any person obtaining a copy
// * of this software and associated documentation files (the "Software"), to deal
// * in the Software without restriction, including without limitation the rights
// * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// * copies of the Software, and to permit persons to whom the Software is
// * furnished to do so, subject to the following conditions:
// *
// * The above copyright notice and this permission notice shall be included in
// * all copies or substantial portions of the Software.
// *
// * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// * THE SOFTWARE.
// * #L%
// */
//
//import com.alipay.test.acts.playback.trservice.exception.BeanRegisterException;
//import com.alipay.test.acts.playback.trservice.model.TrBeanDescriptor;
//import com.alipay.test.acts.playback.trservice.model.TrBeanProxyReference;
//import com.alipay.test.acts.utils.config.ConfigrationFactory;
//
///**
// *
// * @author qingqin
// * @version $Id: TrBeanReferenceRegister.java, v 0.1 2019年07月31日 下午8:51 qingqin Exp $
// */
//public class TrBeanReferenceRegister {
//
//    private ReferenceClient referenceClient;
//
//    private ServiceClient serviceClient;
//
//    private ClientFactory clientFactory;
//
//    public TrBeanReferenceRegister(ClientFactory clientFactory) {
//        this.clientFactory = clientFactory;
//        //fix npe
//        this.referenceClient = this.clientFactory.getClient(ReferenceClient.class);
//        this.serviceClient = this.clientFactory.getClient(ServiceClient.class);
//    }
//
//    /**
//     * 构造 ws 服务的引用描述。
//     *
//     * @see TrBeanDescriptor#bindingCode
//     *
//     * @param trBeanDescriptor 一个 tr 服务的基本描述
//     * @return                  tr 的引用描述
//     */
//    private TrBeanProxyReference registerWsReference(TrBeanDescriptor trBeanDescriptor) {
//
//        //reference param
//        ReferenceParam referenceParam = new ReferenceParam();
//        referenceParam.setInterfaceType(trBeanDescriptor.getInterfaceType());
//        referenceParam.setUniqueId(trBeanDescriptor.getUniqueId());
//        referenceParam.setLocalFirst(false);
//
//        //获取自定义配置
//        String timeout = ConfigrationFactory.getConfigration().getPropertyValue(
//                "mock_ws_timeout", "20000");
//        String serviceUrl = ConfigrationFactory.getConfigration().getPropertyValue(
//                "mock_ws_url", "127.0.0.1:8080");
//        String testUrl = serviceUrl + "?_TIMEOUT=" + timeout;
//
//        //构造bind param
//        WsBindingParam alipayWsBindingParam = new WsBindingParam();
//        alipayWsBindingParam.setHttpTimeout(timeout);
//        alipayWsBindingParam.setHttpConnectionTimeout(timeout);
//        alipayWsBindingParam.setTestUrl(testUrl);
//        alipayWsBindingParam.setVipEnforce(true);
//        alipayWsBindingParam.setVipOnly(true);
//
//        //填充 bind param
//        referenceParam.setBindingParam(alipayWsBindingParam);
//
//        //reference
//        Object serviceBean = referenceClient.reference(referenceParam);
//
//        TrBeanProxyReference reference = new TrBeanProxyReference();
//        reference.setUniqueId(trBeanDescriptor.getUniqueId());
//        reference.setInterfaceType(trBeanDescriptor.getInterfaceType());
//        reference.setTarget(serviceBean);
//
//        return reference;
//    }
//
//    /**
//     * api 引用一个 tr 服务
//     *
//     * @param trBeanDescriptor 一个 tr 服务的基本描述
//     *
//     */
//    public TrBeanProxyReference registerReference(TrBeanDescriptor trBeanDescriptor) throws BeanRegisterException {
//
//        if (referenceClient == null) {
//            throw new BeanRegisterException("reference client is null!");
//        }
//
//        if (trBeanDescriptor == null) {
//            throw new BeanRegisterException("wrong bean descriptor!");
//        }
//
//        if (trBeanDescriptor.getBindingCode() == TrBeanDescriptor.WS) {
//            return registerWsReference(trBeanDescriptor);
//        }
//
//        ReferenceParam referenceParam = new ReferenceParam();
//        referenceParam.setInterfaceType(trBeanDescriptor.getInterfaceType());
//        referenceParam.setUniqueId(trBeanDescriptor.getUniqueId());
//        referenceParam.setLocalFirst(false);
//
//        TrBindingParam trBindingParam = new TrBindingParam();
//        trBindingParam.setTestUrl(ConfigrationFactory.getConfigration().getPropertyValue(
//                "mock_rpc_url", "127.0.0.1:12200"));
//        trBindingParam.setTargetUrl(ConfigrationFactory.getConfigration().getPropertyValue(
//                "mock_rpc_url", "127.0.0.1:12200"));
//
//        String timeout = ConfigrationFactory.getConfigration().getPropertyValue(
//                "mock_rpc_timeout", "10000");
//        trBindingParam.setClientTimeout(Long.parseLong(timeout));
//        referenceParam.setBindingParam(trBindingParam);
//
//        Object serviceBean = referenceClient.reference(referenceParam);
//
//        TrBeanProxyReference reference = new TrBeanProxyReference();
//        reference.setUniqueId(trBeanDescriptor.getUniqueId());
//        reference.setInterfaceType(trBeanDescriptor.getInterfaceType());
//        reference.setTarget(serviceBean);
//
//        return reference;
//    }
//
//    /**
//     * api 引用一个 tr 服务
//     *
//     * @param trBeanDescriptor 一个 tr 服务的基本描述
//     *
//     */
//    public TrBeanProxyReference registerReference(TrBeanDescriptor trBeanDescriptor, String testUrl, long timeout)
//            throws BeanRegisterException {
//
//        if (referenceClient == null) {
//            throw new BeanRegisterException("reference client is null!");
//        }
//
//        if (trBeanDescriptor == null) {
//            throw new BeanRegisterException("wrong bean descriptor!");
//        }
//
//        ReferenceParam referenceParam = new ReferenceParam();
//        referenceParam.setInterfaceType(trBeanDescriptor.getInterfaceType());
//        referenceParam.setUniqueId(trBeanDescriptor.getUniqueId());
//        referenceParam.setLocalFirst(false);
//
//        TrBindingParam trBindingParam = new TrBindingParam();
//        trBindingParam.setTestUrl(testUrl);
//
//        timeout = timeout < 0 ? 3000 : timeout;
//        trBindingParam.setClientTimeout(timeout);
//        referenceParam.setBindingParam(trBindingParam);
//
//        Object serviceBean = referenceClient.reference(referenceParam);
//
//        TrBeanProxyReference reference = new TrBeanProxyReference();
//        reference.setUniqueId(trBeanDescriptor.getUniqueId());
//        reference.setInterfaceType(trBeanDescriptor.getInterfaceType());
//        reference.setTarget(serviceBean);
//
//        return reference;
//    }
//
//    /**
//     * api 移除一个 tr 服务
//     *
//     * @param trBeanDescriptor 一个 tr 服务的基本描述
//     *
//     */
//    public void removeReference(TrBeanDescriptor trBeanDescriptor) {
//
//        ReferenceParam referenceParam = new ReferenceParam();
//        referenceParam.setInterfaceType(trBeanDescriptor.getInterfaceType());
//        referenceParam.setUniqueId(trBeanDescriptor.getUniqueId());
//        referenceParam.setLocalFirst(false);
//
//        TrBindingParam trBindingParam = new TrBindingParam();
//        trBindingParam.setTestUrl("127.0.0.1:12200");
//        trBindingParam.setClientTimeout(10000L);
//        referenceParam.setBindingParam(trBindingParam);
//
//        referenceClient.removeReference(referenceParam);
//
//    }
//
//    public void setClientFactory(ClientFactory clientFactory) {
//        this.clientFactory = clientFactory;
//        this.referenceClient = this.clientFactory.getClient(ReferenceClient.class);
//        this.serviceClient = this.clientFactory.getClient(ServiceClient.class);
//    }
//
//    public ClientFactory getClientFactory() {
//        return clientFactory;
//    }
//
//    public ReferenceClient getReferenceClient() {
//        return referenceClient;
//    }
//
//    public ServiceClient getServiceClient() {
//        return serviceClient;
//    }
//}
