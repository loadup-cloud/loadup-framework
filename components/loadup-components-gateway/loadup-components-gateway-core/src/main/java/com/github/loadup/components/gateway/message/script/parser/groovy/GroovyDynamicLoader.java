package com.github.loadup.components.gateway.message.script.parser.groovy;

import com.github.loadup.components.gateway.common.exception.GatewayException;
import com.github.loadup.components.gateway.core.common.GatewayliteErrorCode;
import com.github.loadup.components.gateway.core.prototype.util.ExceptionUtil;
import com.github.loadup.components.gateway.facade.util.LogUtil;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import org.apache.commons.lang3.time.StopWatch;
import org.codehaus.groovy.control.CompilationFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.*;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * <P>copy from supergw,modified by gang.caogang
 * <p>
 * Groovy脚本的动态加载器，同时观察<br>
 * 将类、脚本等动态加载到Spring Context中，交由Spring统一维护
 */
@Component("liteGroovyDynamicLoader")
public class GroovyDynamicLoader implements ApplicationContextAware, DisposableBean {

    /**
     * 日志类
     */
    private static final Logger logger = LoggerFactory.getLogger(GroovyDynamicLoader.class);

    /**
     * Spring 上下文环境
     */
    private ConfigurableApplicationContext applicationContext;

    /**
     * groovy lock be used to update groovy bean concurrently
     */
    public static final ReentrantReadWriteLock GROOVY_BEAN_LOCK = new ReentrantReadWriteLock();
    public static       GroovyClassLoader      classLoader      = new GroovyClassLoader();

    /**
     * 根据GROOVY脚本类名获取Groovy bean
     */
    @SuppressWarnings("unchecked")
    public <T> T getGroovyBean(String gclassName) {
        Lock readLock = GROOVY_BEAN_LOCK.readLock();
        readLock.lock();
        try {
            return (T) applicationContext.getBean(gclassName);
        } catch (Exception e) {
            throw new GatewayException(GatewayliteErrorCode.SCRIPT_LOAD_ERROR, e);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * 根据GROOVY脚本类名获取
     */
    @SuppressWarnings("unchecked")
    public <T> T getGroovyByName(String gclassName, Class<T> clazz) {
        try {
            return (T) applicationContext.getBean(gclassName, clazz);
        } catch (Exception e) {
            throw new GatewayException(GatewayliteErrorCode.SCRIPT_LOAD_ERROR, e);
        }
    }

    /**
     * @see DisposableBean#destroy()
     */
    public void destroy() {
        //do nothing
    }

    /**
     * 初始化
     */
    public void init(boolean clear, List<GroovyInfo> groovyInfos) {
        if (groovyInfos == null || groovyInfos.isEmpty()) {
            return;
        }
        try {
            ConfigurableListableBeanFactory beanFactory = applicationContext.getBeanFactory();
            for (GroovyInfo groovyInfo : groovyInfos) {
                Class groovyClass = classLoader.parseClass(groovyInfo.getGroovyContent());
                if (GroovyObject.class.isAssignableFrom(groovyClass)) {
                    Class<GroovyObject> clazz = (Class<GroovyObject>) groovyClass;
                    GroovyObject instance = clazz.getDeclaredConstructor().newInstance();
                    if (beanFactory.containsBean(groovyInfo.getClassName())) {
                        throw new GatewayException(GatewayliteErrorCode.CONFIGURATION_LOAD_ERROR,
                                groovyInfo.getClassName() + " bean exist!");
                    }
                    beanFactory.registerSingleton(groovyInfo.getClassName(), instance);
                    LogUtil.info(logger, "initialize groovy bean:" + groovyInfo.getClassName());
                } else {
                    //                    throw new GatewayliteException(GatewayliteErrorCode.CONFIGURATION_LOAD_ERROR, groovyInfo
                    //                    .getClassName() + " not a groovy script!");
                }

            }

            // 把脚本缓存一下，在删除的时候需要使用脚本源代码
            GroovyInnerCache.put2map(clear, groovyInfos);
        } catch (GatewayException e) {
            LogUtil.error(logger, e, "initialize groovy script error:" + groovyInfos);
            throw e;
        } catch (Exception e) {
            LogUtil.error(logger, e, "initialize groovy script error:" + groovyInfos);
            throw new GatewayException(GatewayliteErrorCode.UNKNOWN_EXCEPTION, e);
        }
    }

    /**
     * updaet groovy infos
     */
    public void update(List<GroovyInfo>[] groovyInfos) {
        List<GroovyInfo> addedGroovyInfos = groovyInfos[0];
        List<GroovyInfo> updatedGroovyInfos = groovyInfos[1];
        List<GroovyInfo> deletedGroovyInfos = groovyInfos[2];

        Lock writeLock = GROOVY_BEAN_LOCK.writeLock();
        writeLock.lock();

        try {
            //            Profiler.enter("update groovy script");
            List<GroovyInfo> destroyTemplates = new ArrayList<GroovyInfo>();
            destroyTemplates.addAll(updatedGroovyInfos);
            destroyTemplates.addAll(deletedGroovyInfos);

            //删除bean
            destroyBeanDefinition(destroyTemplates);
            //新增bean的spring定义
            //更新缓存
            GroovyInnerCache.update2map(groovyInfos);
            //加载spring bean
        } catch (GatewayException e) {
            LogUtil.error(logger, e, "update groovy script error:" + groovyInfos);
            throw e;
        } catch (Exception e) {
            LogUtil.error(logger, e, "update groovy script error:" + groovyInfos);
            throw new GatewayException(GatewayliteErrorCode.UNKNOWN_EXCEPTION, e);
        } finally {
            //            Profiler.release();
            writeLock.unlock();
        }

    }

    /**
     * 刷新全部
     */
    public void refreshAll(boolean clear, List<GroovyInfo> groovyInfos) {
        if (groovyInfos == null || groovyInfos.isEmpty()) {
            return;
        }

        Lock writeLock = GROOVY_BEAN_LOCK.writeLock();
        writeLock.lock();

        try {
            //            Profiler.enter(" refresh groovy");

            GroovyInfoGroup group = new GroovyInfoGroup(groovyInfos);

            // 销毁删除的BEAN实例
            destroyBeanDefinition(group);

            // 销毁脚本Bean工厂,这里调用一下可以触发同名的bean重新加载,但在重新加载前老的bean仍然能继续使用
            destroyScriptBeanFactory();

            // 把脚本缓存一下，必须在执行销毁脚本工厂之后执行
            GroovyInnerCache.put2map(clear, groovyInfos);

            // 加载脚本Bean配置
        } catch (GatewayException e) {
            ExceptionUtil.caught(e, "groovy script initialize error:", groovyInfos);
            throw e;
        } catch (Exception e) {
            ExceptionUtil.caught(e, "groovy script initialize error:", groovyInfos);
            throw new GatewayException(GatewayliteErrorCode.UNKNOWN_EXCEPTION, e);
        } finally {
            //            Profiler.release();
            writeLock.unlock();
        }
    }

    /**
     * 销毁删除的BEAN定义，变更的BEAN在重新以后会自动生效
     */
    private void destroyBeanDefinition(GroovyInfoGroup group) {
        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();
        List<GroovyInfo> destoryList = new ArrayList();
        destoryList.addAll(group.getDeletedGroovy());
        destoryList.addAll(group.getModifiedGroovy());
        for (GroovyInfo groovy : destoryList) {
            try {
                beanFactory.removeBeanDefinition(groovy.getClassName());
            } catch (Exception e) {
                LogUtil.warn(logger, "delete groovy exception. skip:" + groovy.getClassName());
            }
        }
    }

    /**
     * 销毁删除的BEAN定义，变更的BEAN在重新以后会自动生效
     */
    private void destroyBeanDefinition(List<GroovyInfo> groovyInfos) {
        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();
        String[] postProcessorNames = applicationContext.getBeanFactory().getBeanNamesForType(CustomScriptFactoryPostProcessor.class, true,
                false);
        CustomScriptFactoryPostProcessor processor = (CustomScriptFactoryPostProcessor) applicationContext.getBean(postProcessorNames[0]);
        for (GroovyInfo groovy : groovyInfos) {
            try {
                beanFactory.removeBeanDefinition(groovy.getClassName());
                processor.destoryBean(groovy.getClassName());
            } catch (Exception e) {
                LogUtil.info(logger, "remove groovy error,skip:" + groovy.getClassName());
            }
        }
    }

    /**
     * 对groovy脚本做语法检查
     */
    private boolean checkSyntax(GroovyInfo groovyInfo) {
        try {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();

            try (GroovyClassLoader groovyClassLoader = new GroovyClassLoader(GroovyDynamicLoader.class.getClassLoader())) {
                groovyClassLoader.parseClass(groovyInfo.getGroovyContent());

                stopWatch.split();

                LogUtil.info(logger,
                        "Groovy syntax check success,class=" + groovyInfo.getClassName() + ",cost:" + stopWatch.getSplitTime() + "ms");
            }

        } catch (CompilationFailedException e) {
            ExceptionUtil.caught(e, "Groovy syntax check error,class=" + groovyInfo.getClassName());
            return false;
        } catch (NullPointerException e) {
            LogUtil.error(logger, e, "Groovy content empty error,class=" + groovyInfo.getClassName());
            return false;
        } catch (Exception e) {
            ExceptionUtil.caught(e, "Groovy syntax check error,class=" + groovyInfo.getClassName());
            return false;
        }

        return true;
    }

    /**
     * 根据脚本信息构建脚本BEAN配置
     */
    private DynamicBean composeDynamicBean(GroovyInfo groovyInfo) {
        DynamicBean bean = new DynamicBean();
        String scriptName = groovyInfo.getClassName();

        Assert.notNull(scriptName, "parser className cannot be empty!");

        //设置bean的属性，这里只有id和script-source。
        bean.put("id", scriptName);
        bean.put("script-source", GroovyConstant.SCRIPT_SOURCE_PREFIX + scriptName);

        return bean;
    }

    /**
     * 销毁脚本Bean工厂
     */
    private void destroyScriptBeanFactory() {
        String[] postProcessorNames = applicationContext.getBeanFactory().getBeanNamesForType(CustomScriptFactoryPostProcessor.class, true,
                false);

        //将所有的解析脚本从spring上下文销毁
        for (String postProcessorName : postProcessorNames) {
            CustomScriptFactoryPostProcessor processor = (CustomScriptFactoryPostProcessor) applicationContext.getBean(postProcessorName);
            processor.destroy();
        }
    }

    /**
     * CE 兼容用方法
     */
    @SuppressWarnings("unchecked")
    //    private static <T> T getFrameworkService(BundleContext bundleContext, Class<T> clazz) {
    //        final ServiceReference<?> reference = bundleContext.getServiceReference(clazz.getName());
    //        if (reference != null) {
    //            return (T) bundleContext.getService(reference);
    //        } else {
    //            return null;
    //        }
    //    }

    /**
     * @see ApplicationContextAware#setApplicationContext(ApplicationContext)
     */ public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = (ConfigurableApplicationContext) applicationContext;
    }

}
