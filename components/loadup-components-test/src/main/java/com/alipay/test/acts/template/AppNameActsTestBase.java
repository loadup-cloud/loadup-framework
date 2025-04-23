///**
// * Alipay.com Inc.
// * Copyright (c) 2004-2015 All Rights Reserved.
// */
//package com.alipay.test.acts.template;
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
//import com.alipay.test.acts.api.ActsTestBaseOld;
//
///**
// *
// * @author tianzhu.wtzh
// * @version $Id: AppNameActsTestBase.java, v 0.1 2015年10月15日 上午11:36:37 tianzhu.wtzh Exp $
// */
//public class AppNameActsTestBase extends ActsTestBaseOld {
//
//    static {
//        System.setProperty("test_artifacts",
//            "ats-common-,ats-api-,opencsv-,ats-db-component-,zdal-datasource-,zdal-common,ognl-");
//    }
//
//    @Override
//    public String[] getConfigPattern() {
//        return null;
//
//        // return PropertyConfig.getTestConfigPattern();
//    }
//
//    /**
//     * RPC模式获取配置文件xml
//     * @see com.alipay.sofa.runtime.test.testng.TestNGEclipseSofaTest#getRpcServiceConfigurationLocations()
//     */
//    @Override
//    public String[] getRpcServiceConfigurationLocations() {
//        return new String[] { "servicetest/rpc-service-bean.xml",
//                "META-INF/spring/common-dal-front.xml", "META-INF/spring/common-dal-trans.xml",
//                "META-INF/spring/common-dal-config.xml" };
//    }
//
//    /**
//     * JVM 模式获取配置文件xml
//     * @see com.alipay.sofa.runtime.test.testng.TestNGEclipseSofaTest#getVirtualBundleConfigurationLocations()
//     */
//    @Override
//    public String[] getVirtualBundleConfigurationLocations() {
//        return new String[] { "servicetest/jvm-service-bean.xml" };
//    }
//
//    @Override
//    public String[] getExtendedXmls() {
//        return new String[] { "servicetest/fcdebittrans-test-interceptor.xml" };
//    }
//
//}
