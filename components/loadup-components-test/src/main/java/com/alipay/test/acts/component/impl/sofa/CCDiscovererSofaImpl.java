///**
// * Alipay.com Inc.
// * Copyright (c) 2004-2015 All Rights Reserved.
// */
//package com.alipay.test.acts.component.impl.sofa;
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
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import org.apache.commons.lang3.StringUtils;
//import com.alipay.test.acts.component.CCDiscoverer;
//import org.springframework.stereotype.Component;
//
///**
// * 用例组件发现器的sofa实现。
// *
// * @author dasong.jds
// * @version $Id: CCDiscovererSofaImpl.java, v 0.1 2015年5月11日 下午8:33:00 dasong.jds Exp $
// */
//public class CCDiscovererSofaImpl implements CCDiscoverer {
//
//    /** 扩展点常量 */
//    private final static String       DISCOVERER  = "discoverer";
//
//    /** 探索器集合 */
//    private Map<String, CCDiscoverer> discoverers = new HashMap<String, CCDiscoverer>();
//
//    /** 内置discover */
//    private CCDiscoverer              annotationCCDiscoverer;
//
//    /**
//     * @see com.ipay.itest.common.repository.CCDiscoverer#discover()
//     */
//    @Override
//    public Object[] discover() {
//
//        if (discoverers.isEmpty()) {
//            return annotationCCDiscoverer.discover();
//        }
//
//        List<Object> beans = new ArrayList<Object>();
//
//        for (CCDiscoverer discoverer : discoverers.values()) {
//            for (Object bean : discoverer.discover()) {
//                beans.add(bean);
//            }
//        }
//
//        return beans.toArray();
//    }
//
//    /**
//     * @see com.alipay.sofa.service.api.component.Extensible#registerExtension(com.alipay.sofa.service.api.component.Extension)
//     */
//    @Override
//    public void registerExtension(Extension extension) throws Exception {
//
//        Object[] contribs = extension.getContributions();
//        if (contribs == null || contribs.length == 0
//            || !StringUtils.equals(DISCOVERER, extension.getExtensionPoint())) {
//            return;
//        }
//
//        for (Object contribution : contribs) {
//            CCDiscovererDescriptor descriptor = (CCDiscovererDescriptor) contribution;
//            discoverers.put(descriptor.getDiscovererName(), descriptor.getDiscoverer());
//        }
//
//    }
//
//    /**
//     * @see com.alipay.sofa.service.api.component.Extensible#unregisterExtension(com.alipay.sofa.service.api.component.Extension)
//     */
//    @Override
//    public void unregisterExtension(Extension extension) throws Exception {
//    }
//
//    /**
//     * @see com.alipay.sofa.service.api.component.Component#activate(com.alipay.sofa.service.api.component.ComponentContext)
//     */
//    @Override
//    public void activate(ComponentContext context) throws Exception {
//    }
//
//    /**
//     * @see com.alipay.sofa.service.api.component.Component#deactivate(com.alipay.sofa.service.api.component.ComponentContext)
//     */
//    @Override
//    public void deactivate(ComponentContext context) throws Exception {
//    }
//
//    /**
//     * @see com.alipay.sofa.service.api.component.Component#deployCompletion()
//     */
//    @Override
//    public void deployCompletion() throws Exception {
//    }
//
//    /**
//     * @see com.alipay.sofa.service.api.component.Component#registerIdentity()
//     */
//    @Override
//    public String registerIdentity() {
//        return null;
//    }
//
//    /**
//     * @see com.alipay.sofa.service.api.component.Component#getStartLevel()
//     */
//    @Override
//    public int getStartLevel() {
//        return 0;
//    }
//
//    /**
//     * Setter method for property <tt>annotationCCDiscoverer</tt>.
//     *
//     * @param annotationCCDiscoverer value to be assigned to property annotationCCDiscoverer
//     */
//    public void setAnnotationCCDiscoverer(CCDiscoverer annotationCCDiscoverer) {
//        this.annotationCCDiscoverer = annotationCCDiscoverer;
//    }
//
//}
