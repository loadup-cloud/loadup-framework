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
//import com.alipay.sofa.common.xmap.annotation.XNode;
//import com.alipay.sofa.common.xmap.annotation.XObject;
//import com.alipay.sofa.common.xmap.spring.XNodeSpring;
//import com.alipay.test.acts.component.CCDiscoverer;
//
///**
// * 用例组件发现器描述信息。
// *
// * @author dasong.jds
// * @version $Id: CCDiscovererDescriptor.java, v 0.1 2015年5月11日 下午7:05:44 dasong.jds Exp $
// */
//@XObject("ccDiscoverer")
//public class CCDiscovererDescriptor {
//
//    @XNode("@discovererName")
//    private String       discovererName;
//
//    /** manager */
//    @XNodeSpring("@discoverer")
//    private CCDiscoverer discoverer;
//
//    /**
//     * Getter method for property <tt>discovererName</tt>.
//     *
//     * @return property value of discovererName
//     */
//    public String getDiscovererName() {
//        return discovererName;
//    }
//
//    /**
//     * Setter method for property <tt>discovererName</tt>.
//     *
//     * @param discovererName value to be assigned to property discovererName
//     */
//    public void setDiscovererName(String discovererName) {
//        this.discovererName = discovererName;
//    }
//
//    /**
//     * Getter method for property <tt>discoverer</tt>.
//     *
//     * @return property value of discoverer
//     */
//    public CCDiscoverer getDiscoverer() {
//        return discoverer;
//    }
//
//    /**
//     * Setter method for property <tt>discoverer</tt>.
//     *
//     * @param discoverer value to be assigned to property discoverer
//     */
//    public void setDiscoverer(CCDiscoverer discoverer) {
//        this.discoverer = discoverer;
//    }
//
//}
