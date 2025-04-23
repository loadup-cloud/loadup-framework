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
//import com.alipay.test.acts.playback.trservice.exception.BeanNotFoundException;
//import com.alipay.test.acts.playback.trservice.exception.BeanRegisterException;
//import com.alipay.test.acts.playback.trservice.model.TrBeanDescriptor;
//import com.alipay.test.acts.playback.trservice.model.TrBeanProxyReference;
//
//import java.util.HashMap;
//import java.util.Set;
//
///**
// *
// * @author qingqin
// * @version $Id: TrServiceRepository.java, v 0.1 2019年07月31日 下午8:52 qingqin Exp $
// */
//public class TrServiceRepository {
//
//    private static HashMap<TrBeanDescriptor, TrBeanProxyReference> SERVICE_REFERENCE_REPO = new HashMap<TrBeanDescriptor, TrBeanProxyReference>();
//
////    private TrBeanReferenceRegister trBeanReferenceRegister;
//
//    private TrBeanAutowireInjector trBeanAutowireInjector;
//
////    private Class<? extends PlaybackActsTestBase> testClass;
//
//    public TrServiceRepository(TrBeanReferenceRegister register,
//                               TrBeanAutowireInjector injector
////                               Class<? extends PlaybackActsTestBase> testClass
//    ) {
//        this.trBeanReferenceRegister = register;
//        this.trBeanAutowireInjector = injector;
////        this.testClass = testClass;
//    }
//
//    /**
//     * 向 sofa context 注册一个 tr 服务引用
//     *
//     * @param trBeanDescriptor          bean 描述
//     * @throws BeanRegisterException    bean 异常
//     */
//    public void registerTrService(TrBeanDescriptor trBeanDescriptor) throws BeanRegisterException{
//
//        if (SERVICE_REFERENCE_REPO.containsKey(trBeanDescriptor)) {
//            return;
//        }
//
//        TrBeanProxyReference trBeanProxyReference = trBeanReferenceRegister.registerReference(trBeanDescriptor);
//
//        SERVICE_REFERENCE_REPO.put(trBeanDescriptor, trBeanProxyReference);
//
//    }
//
//    /**
//     * 向 sofa context 注册一个 tr 服务引用
//     *
//     * @param trBeanDescriptor         bean 描述
//     * @param testUrl                  test url
//     * @param timeout                  超时时间
//     * @throws BeanRegisterException   bean 异常
//     */
//    public void registerTrService(TrBeanDescriptor trBeanDescriptor, String testUrl, long timeout) throws BeanRegisterException{
//
//        if (SERVICE_REFERENCE_REPO.containsKey(trBeanDescriptor)) {
//            return;
//        }
//
//        TrBeanProxyReference trBeanProxyReference = trBeanReferenceRegister.registerReference(trBeanDescriptor, testUrl, timeout);
//
//        SERVICE_REFERENCE_REPO.put(trBeanDescriptor, trBeanProxyReference);
//
//    }
//
//    /**
//     * 获取一个 tr bean，用于发起服务调用
//     *
//     * 1.从缓存中获取，找到则返回；
//     *
//     * 2.从缓存中找不到，再从 sofa context 中获取；
//     *
//     * 3.若失败则抛出异常
//     *
//     * @param trBeanDescriptor 一个 tr bean 的描述
//     *
//     * @return  返回 tr bean
//     *
//     * @throws BeanNotFoundException 找不到 tr bean
//     */
//    public TrBeanProxyReference injectTrService(TrBeanDescriptor trBeanDescriptor) throws BeanNotFoundException{
//
//        TrBeanProxyReference reference = SERVICE_REFERENCE_REPO.get(trBeanDescriptor);
//
//        if (null == reference) {
////            reference = trBeanAutowireInjector.autowireCandidate(trBeanDescriptor, testClass);
//
//            if (null == reference) {
//                throw new BeanNotFoundException("无法获取 " + trBeanDescriptor);
//            }
//            SERVICE_REFERENCE_REPO.put(trBeanDescriptor, reference);
//        }
//
//        return reference;
//
//    }
//
//    /**
//     *
//     * @param trBeanDescriptor
//     * @param force             彻底移除
//     */
//    public void remove(TrBeanDescriptor trBeanDescriptor, boolean force) {
//
//        SERVICE_REFERENCE_REPO.remove(trBeanDescriptor);
//
//        if (force) {
//            trBeanReferenceRegister.removeReference(trBeanDescriptor);
//        }
//
//    }
//
//    /**
//     *
//     * @param force  彻底移除
//     */
//    public void clear(boolean force) {
//
//        if (force) {
//            Set<TrBeanDescriptor> descriptorSet = SERVICE_REFERENCE_REPO.keySet();
//            for (TrBeanDescriptor descriptor : descriptorSet) {
//                trBeanReferenceRegister.removeReference(descriptor);
//            }
//        }
//
//        SERVICE_REFERENCE_REPO.clear();
//
//    }
//
//    public TrBeanReferenceRegister getTrBeanReferenceRegister() {
//        return trBeanReferenceRegister;
//    }
//
//    public TrBeanAutowireInjector getTrBeanAutowireInjector() {
//        return trBeanAutowireInjector;
//    }
//}
