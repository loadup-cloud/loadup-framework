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
//import com.alipay.sofa.runtime.model.ComponentType;
//import com.alipay.sofa.runtime.spi.component.ComponentInfo;
//import com.alipay.sofa.runtime.spi.component.ComponentManager;
//import com.alipay.sofa.runtime.spi.component.SofaRuntimeContext;
//import com.alipay.sofa.runtime.spi.util.ComponentNameFactory;
//import com.alipay.sofa.runtime.test.SofaTestConstants;
//import com.alipay.sofa.runtime.test.autowire.annotation.XMode;
//import com.alipay.test.acts.playback.trservice.exception.BeanNotFoundException;
//import com.alipay.test.acts.playback.trservice.model.TrBeanDescriptor;
//import com.alipay.test.acts.playback.trservice.model.TrBeanProxyReference;
//
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
//import org.springframework.context.ApplicationContext;
//
///**
// *
// * @author qingqin
// * @version $Id: TrBeanAutowireInjector.java, v 0.1 2019年07月31日 下午8:50 qingqin Exp $
// */
//public class TrBeanAutowireInjector {
//
//    private static final Log LOGGER = LogFactory.getLog(TrBeanAutowireInjector.class);
//
//    private SofaRuntimeContext sofaRuntimeContext;
//
//    private String bundleName;
//
//    public TrBeanAutowireInjector(SofaRuntimeContext sofaRuntimeContext, String bundleName) {
//
//        this.sofaRuntimeContext = sofaRuntimeContext;
//        this.bundleName = bundleName;
//    }
//
//    /**
//     * 直接将某个对象作为 on-the-fly-module 的 Spring Bean 进行注入
//     */
//    public void autowireAsSpringBean(Object instance, Class<?> instanceType) throws Exception {
//        ApplicationContext applicationContext = getSpringContext(bundleName);
//        AutowireCapableBeanFactory beanFactory = applicationContext.getAutowireCapableBeanFactory();
//        beanFactory.autowireBeanProperties(instance, AutowireCapableBeanFactory.AUTOWIRE_NO, false);
//        beanFactory.initializeBean(instance, instanceType.getName());
//    }
//
//    /**
//     * 从指定 module 扫描（并以 the-on-the-fly-module 模块为兜底）
//     *
//     * @param name           bean 的名字；可以为 null
//     * @param requiredType   bean 的类型，注意 proxy bean 需要填写 interface；可以为 null
//     * @param moduleName     bean 发布所在的模块；默认为 the-on-the-fly-module
//     * @return               bean 目标
//     */
//    public Object getBean(String name, Class<?> requiredType, String moduleName) {
//        //不能同时为空
//        if (StringUtils.isEmpty(name) && requiredType == null) {
//            return null;
//        }
//
//        // 如果bundle名字没有写，默认从虚拟bundle的上下文中取bean
//        if (moduleName == null || moduleName.length() == 0) {
//            moduleName = bundleName;
//        }
//        ApplicationContext applicationContext = getSpringContext(moduleName);
//        if (applicationContext != null) {
//
//            return StringUtils.isEmpty(name) ?
//                    getBeanFromContext(applicationContext, requiredType) :
//                    applicationContext.getBean(name, requiredType);
//        }
//        return null;
//    }
//
//    public Object getBeanFromContext(ApplicationContext applicationContext, Class<?> requiredType) {
//        Object target = null;
//        try {
//            target = applicationContext.getBean(requiredType);
//        } catch (Exception e) {
//
//        }
//        if (null == target) {
//            Class<?>[] interfaceList = requiredType.getInterfaces();
//            if (interfaceList.length == 0) {
//                return target;
//            } else {
//                for (Class clazz : interfaceList ) {
//                    try {
//                        target = applicationContext.getBean(clazz);
//                        if (null != target) {
//                            return target;
//                        }
//                    } catch(Exception e) {
//                    }
//                }
//            }
//        }
//        return target;
//    }
//
//    private Object getService(Class<?> interfaceClass) {
//        if (sofaRuntimeContext == null) {
//            throw new RuntimeException();
//        }
//
//        ComponentManager componentManager = sofaRuntimeContext.getComponentManager();
//        ComponentInfo serviceComponent = componentManager.getComponentInfo(ComponentNameFactory.createComponentName(new ComponentType("service"),
//                interfaceClass));
//
//        if (serviceComponent == null) {
//            LOGGER.error("无法找到 interface 为 " + interfaceClass + " 的 JVM 服务");
//            return null;
//        }
//
//        return serviceComponent.getImplementation().getTarget();
//    }
//
//    private ApplicationContext getSpringContext(String bundleName) {
//        if (sofaRuntimeContext == null) {
//            LOGGER.error("Failed to get Spring context of module " + bundleName);
//            throw new RuntimeException();
//        }
//
//        ComponentManager componentManager = sofaRuntimeContext.getComponentManager();
//
//        // 由于直接new ComponentType("Spring")会报NoClassDefFoundError，
//        // 所以避开通过创建componentName去查找，而是通过循环比对获得对应的组件信息
//        // 绕了个弯但是能实现最终的目的，后续若有更好的方案可优化
//        ComponentInfo springComponent = null;
//        for(ComponentInfo c : componentManager.getComponents()){
//            if (c.getName().getName().equals(bundleName)){
//                springComponent = c;
//                break;
//            }
//        }
//
//        // 使用以下方案需要在rating（业务系统）的
//        // [rating/assembly/template/src/main/resources/META-INF/MANIFEST.MF]中新增：
//        //      Import-Package: com.alipay.sofa.runtime.model
//        // 暂时不考虑对业务系统进行入侵
//        //ComponentInfo springComponent = componentManager.getComponentInfo(ComponentNameFactory.createComponentName(new ComponentType("Spring"),
//        //        bundleName));
//        if (springComponent == null) {
//            LOGGER.error("Failed to get Spring context of module " + bundleName);
//            return null;
//        }
//        return (ApplicationContext) (springComponent.getImplementation().getTarget());
//    }
//
//    public boolean isJvmMode(Class<?> testClass) {
//
//        // 元数据判断
//        int xmodeValue;
//
//        // 如果sofa-config的配置文件中设置了test_xmode，则以此为准
//        String testXmode = sofaRuntimeContext.getAppConfiguration().getPropertyValue(SofaTestConstants.XMODE);
//
//        if (null != testXmode && !"".equals(testXmode)) {
//            if (SofaTestConstants.RPC_MODE.equalsIgnoreCase(testXmode)) {
//                xmodeValue = XMode.RPC;
//            } else if (SofaTestConstants.WS_MODE.equalsIgnoreCase(testXmode)) {
//                xmodeValue = XMode.WEBSERVICE;
//            } else {
//                xmodeValue = XMode.JVM;
//            }
//        } else {
//            XMode xmode = testClass.getAnnotation(XMode.class);
//            if (xmode == null) {
//                xmodeValue = XMode.JVM;
//            } else {
//                xmodeValue = xmode.value();
//            }
//        }
//
//        return xmodeValue == XMode.JVM;
//    }
//
//    /**
//     * 从 sofa context 中获取 tr bean
//     *
//     * @param descriptor 需要注入的 bean 的描述
//     * @param testClass 被测试类
//     */
//    public TrBeanProxyReference autowireCandidate(TrBeanDescriptor descriptor, Class<?> testClass) throws BeanNotFoundException {
//
//        if (!isJvmMode(testClass)) {
//            throw new BeanNotFoundException("不支持非 JVM 模式");
//        }
//
//        Object bean = getBean(descriptor.getBeanName(), descriptor.getInterfaceType(), bundleName);
//
//        TrBeanProxyReference reference = new TrBeanProxyReference();
//        reference.setUniqueId(descriptor.getUniqueId());
//        reference.setInterfaceType(descriptor.getInterfaceType());
//        reference.setTarget(bean);
//
//        return reference;
//    }
//
//}
