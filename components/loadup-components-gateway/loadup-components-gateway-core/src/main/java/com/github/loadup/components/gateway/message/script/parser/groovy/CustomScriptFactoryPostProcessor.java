package com.github.loadup.components.gateway.message.script.parser.groovy;

import com.github.loadup.components.gateway.facade.util.LogUtil;
import net.sf.cglib.core.Signature;
import net.sf.cglib.proxy.InterfaceMaker;
import org.apache.commons.lang3.StringUtils;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.AopInfrastructureBean;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.DelegatingIntroductionInterceptor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.*;
import org.springframework.beans.factory.config.*;
import org.springframework.beans.factory.support.*;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.Conventions;
import org.springframework.core.Ordered;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.scripting.ScriptFactory;
import org.springframework.scripting.ScriptSource;
import org.springframework.scripting.support.*;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 修改自ScriptFactoryPostProcessor
 * 实现定制的Groovy脚本后处理器，用来定制数据库的脚本加载处理
 * 实现了针对单独脚本进行更新的功能
 */
@SuppressWarnings({"rawtypes", "unchecked"})
@Component
public class CustomScriptFactoryPostProcessor
        implements
        BeanPostProcessor,
        BeanClassLoaderAware,
        BeanFactoryAware,
        ResourceLoaderAware,
        DisposableBean,
        Ordered {

    /**
     * logger
     */
    private static final Logger logger = LoggerFactory
            .getLogger(CustomScriptFactoryPostProcessor.class);

    protected static final String INLINE_SCRIPT_PREFIX = "inline:";

    protected static final String SCRIPT_FACTORY_NAME_PREFIX = "scriptFactory.";

    protected static final String SCRIPTED_OBJECT_NAME_PREFIX = "scriptedObject.";

    protected static final String REFRESH_CHECK_DELAY_ATTRIBUTE = Conventions
            .getQualifiedAttributeName(
                    ScriptFactoryPostProcessor.class,
                    "refreshCheckDelay");

    private long defaultRefreshCheckDelay = -1;

    private ClassLoader beanClassLoader = ClassUtils
            .getDefaultClassLoader();

    private ConfigurableBeanFactory beanFactory;

    private ResourceLoader resourceLoader = new DefaultResourceLoader();

    protected final DefaultListableBeanFactory scriptBeanFactory = new DefaultListableBeanFactory();

    /**
     * Map from bean name String to ScriptSource object
     */
    private final Map<String, ScriptSource> scriptSourceCache = new ConcurrentHashMap<String, ScriptSource>();

    /**
     * Set the delay between refresh checks, in milliseconds.
     * Default is -1, indicating no refresh checks at all.
     * <p>Note that an actual refresh will only happen when
     * the {@link ScriptSource} indicates
     * that it has been modified.
     *
     * @see ScriptSource#isModified()
     */
    public void setDefaultRefreshCheckDelay(long defaultRefreshCheckDelay) {
        this.defaultRefreshCheckDelay = defaultRefreshCheckDelay;
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.beanClassLoader = classLoader;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        if (!(beanFactory instanceof ConfigurableBeanFactory)) {
            throw new IllegalStateException(
                    "ScriptFactoryPostProcessor doesn't work with a BeanFactory "
                            + "which does not implement ConfigurableBeanFactory: "
                            + beanFactory.getClass());
        }
        this.beanFactory = (ConfigurableBeanFactory) beanFactory;

        // Required so that references (up container hierarchies) are correctly resolved.
        this.scriptBeanFactory.setParentBeanFactory(this.beanFactory);

        // Required so that all BeanPostProcessors, Scopes, etc become available.
        this.scriptBeanFactory.copyConfigurationFrom(this.beanFactory);

        // Filter out BeanPostProcessors that are part of the AOP infrastructure,
        // since those are only meant to apply to beans defined in the original factory.
        List<BeanPostProcessor> beanPostProcessors = this.scriptBeanFactory.getBeanPostProcessors();
        beanPostProcessors.forEach(item -> {
            if (item instanceof AopInfrastructureBean) {
                this.scriptBeanFactory.getBeanPostProcessors().remove(item);
            }
        });

    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public int getOrder() {
        return Integer.MIN_VALUE;
    }

    //    @Override
    public Class predictBeanType(Class beanClass, String beanName) {
        // We only apply special treatment to ScriptFactory implementations here.
        if (!ScriptFactory.class.isAssignableFrom(beanClass)) {
            return null;
        }

        BeanDefinition bd = this.beanFactory.getMergedBeanDefinition(beanName);

        try {
            String scriptFactoryBeanName = SCRIPT_FACTORY_NAME_PREFIX + beanName;
            String scriptedObjectBeanName = SCRIPTED_OBJECT_NAME_PREFIX + beanName;
            prepareScriptBeans(bd, scriptFactoryBeanName, scriptedObjectBeanName);

            ScriptFactory scriptFactory = this.scriptBeanFactory.getBean(scriptFactoryBeanName,
                    ScriptFactory.class);
            ScriptSource scriptSource = getScriptSource(scriptFactoryBeanName,
                    scriptFactory.getScriptSourceLocator());
            Class[] interfaces = scriptFactory.getScriptInterfaces();

            Class scriptedType = scriptFactory.getScriptedObjectType(scriptSource);
            if (scriptedType != null) {
                return scriptedType;
            } else if (!ObjectUtils.isEmpty(interfaces)) {
                return (interfaces.length == 1 ? interfaces[0]
                        : createCompositeInterface(interfaces));
            } else {
                if (bd.isSingleton()) {
                    Object bean = this.scriptBeanFactory.getBean(scriptedObjectBeanName);
                    if (bean != null) {
                        return bean.getClass();
                    }
                }
            }
        } catch (BeanCreationException ex) {
            if (ex.getMostSpecificCause() instanceof BeanCurrentlyInCreationException) {
                LogUtil.debug(logger, "Could not determine scripted object type for bean '" +
                        beanName + "': " + ex.getMessage());
            }
        } catch (Exception ex) {
            LogUtil.debug(logger, "Could not determine scripted object type for bean '" +
                    beanName + "'", ex);
        }

        return null;
    }

    //    @Override
    public Object postProcessBeforeInstantiation(Class beanClass, String beanName) {
        // We only apply special treatment to ScriptFactory implementations here.
        if (!ScriptFactory.class.isAssignableFrom(beanClass)) {
            return null;
        }

        BeanDefinition bd = this.beanFactory.getMergedBeanDefinition(beanName);
        String scriptFactoryBeanName = SCRIPT_FACTORY_NAME_PREFIX + beanName;
        String scriptedObjectBeanName = SCRIPTED_OBJECT_NAME_PREFIX + beanName;
        prepareScriptBeans(bd, scriptFactoryBeanName, scriptedObjectBeanName);

        ScriptFactory scriptFactory = this.scriptBeanFactory.getBean(scriptFactoryBeanName,
                ScriptFactory.class);
        ScriptSource scriptSource = getScriptSource(scriptFactoryBeanName,
                scriptFactory.getScriptSourceLocator());
        boolean isFactoryBean = false;
        try {
            Class scriptedObjectType = scriptFactory.getScriptedObjectType(scriptSource);
            // Returned type may be null if the factory is unable to determine the type.
            if (scriptedObjectType != null) {
                isFactoryBean = FactoryBean.class.isAssignableFrom(scriptedObjectType);
            }
        } catch (Exception ex) {
            throw new BeanCreationException(beanName,
                    "Could not determine scripted object type for " + scriptFactory, ex);
        }

        long refreshCheckDelay = resolveRefreshCheckDelay(bd);
        if (refreshCheckDelay >= 0) {
            Class[] interfaces = scriptFactory.getScriptInterfaces();
            RefreshableScriptTargetSource ts = new RefreshableScriptTargetSource(
                    this.scriptBeanFactory, scriptedObjectBeanName, scriptFactory, scriptSource,
                    isFactoryBean);
            ts.setRefreshCheckDelay(refreshCheckDelay);
            return createRefreshableProxy(ts, interfaces);
        }

        if (isFactoryBean) {
            scriptedObjectBeanName = BeanFactory.FACTORY_BEAN_PREFIX + scriptedObjectBeanName;
        }
        return this.scriptBeanFactory.getBean(scriptedObjectBeanName);
    }

    /**
     * Prepare the script beans in the internal BeanFactory that this
     * post-processor uses. Each original bean definition will be split
     * into a ScriptFactory definition and a scripted object definition.
     */
    protected void prepareScriptBeans(BeanDefinition bd, String scriptFactoryBeanName,
                                      String scriptedObjectBeanName) {
        if (!this.scriptBeanFactory.containsBeanDefinition(scriptedObjectBeanName)) {

            this.scriptBeanFactory.registerBeanDefinition(scriptFactoryBeanName,
                    createScriptFactoryBeanDefinition(bd));
            ScriptFactory scriptFactory = this.scriptBeanFactory.getBean(scriptFactoryBeanName,
                    ScriptFactory.class);
            ScriptSource scriptSource = getScriptSource(scriptFactoryBeanName,
                    scriptFactory.getScriptSourceLocator());
            Class[] interfaces = scriptFactory.getScriptInterfaces();

            Class[] scriptedInterfaces = interfaces;
            if (scriptFactory.requiresConfigInterface() && !bd.getPropertyValues().isEmpty()) {
                Class configInterface = createConfigInterface(bd, interfaces);
                scriptedInterfaces = (Class[]) ObjectUtils.addObjectToArray(interfaces,
                        configInterface);
            }

            BeanDefinition objectBd = createScriptedObjectBeanDefinition(bd,
                    scriptFactoryBeanName, scriptSource, scriptedInterfaces);
            long refreshCheckDelay = resolveRefreshCheckDelay(bd);
            if (refreshCheckDelay >= 0) {
                objectBd.setScope(BeanDefinition.SCOPE_PROTOTYPE);
            }

            this.scriptBeanFactory.registerBeanDefinition(scriptedObjectBeanName, objectBd);
        }
    }

    /**
     * Get the refresh check delay for the given {@link ScriptFactory} {@link BeanDefinition}.
     * If the {@link BeanDefinition} has a
     * {@link org.springframework.core.AttributeAccessor metadata attribute}
     * under the key {@link #REFRESH_CHECK_DELAY_ATTRIBUTE} which is a valid {@link Number}
     * type, then this value is used. Otherwise, the the {@link #defaultRefreshCheckDelay}
     * value is used.
     */
    protected long resolveRefreshCheckDelay(BeanDefinition beanDefinition) {
        long refreshCheckDelay = this.defaultRefreshCheckDelay;
        Object attributeValue = beanDefinition.getAttribute(REFRESH_CHECK_DELAY_ATTRIBUTE);
        if (attributeValue instanceof Number) {
            refreshCheckDelay = ((Number) attributeValue).longValue();
        } else if (attributeValue instanceof String) {
            refreshCheckDelay = Long.parseLong((String) attributeValue);
        } else if (attributeValue != null) {
            throw new BeanDefinitionStoreException("Invalid refresh check delay attribute ["
                    + REFRESH_CHECK_DELAY_ATTRIBUTE
                    + "] with value [" + attributeValue
                    + "]: needs to be of type Number or String");
        }
        return refreshCheckDelay;
    }

    /**
     * Create a ScriptFactory bean definition based on the given script definition,
     * extracting only the definition data that is relevant for the ScriptFactory
     * (that is, only bean class and constructor arguments).
     *
     * @see ScriptFactory
     */
    protected BeanDefinition createScriptFactoryBeanDefinition(BeanDefinition bd) {
        GenericBeanDefinition scriptBd = new GenericBeanDefinition();
        scriptBd.setBeanClassName(bd.getBeanClassName());
        scriptBd.getConstructorArgumentValues()
                .addArgumentValues(bd.getConstructorArgumentValues());
        return scriptBd;
    }

    /**
     * Obtain a ScriptSource for the given bean, lazily creating it
     * if not cached already.
     *
     * @see #convertToScriptSource
     */
    protected ScriptSource getScriptSource(String beanName, String scriptSourceLocator) {
        ScriptSource scriptSource = this.scriptSourceCache.get(beanName);
        if (scriptSource == null) {
            scriptSource = convertToScriptSource(beanName, scriptSourceLocator, this.resourceLoader);
            this.scriptSourceCache.put(beanName, scriptSource);
        }

        return scriptSource;
    }

    /**
     * Create a config interface for the given bean definition, defining setter
     * methods for the defined property values as well as an init method and
     * a destroy method (if defined).
     * <p>This implementation creates the interface via CGLIB's InterfaceMaker,
     * determining the property types from the given interfaces (as far as possible).
     *
     *
     * config interface for
     *
     * getters corresponding to the setters we're supposed to generate)
     *
     * @see InterfaceMaker
     * @see BeanUtils#findPropertyType
     */
    protected Class createConfigInterface(BeanDefinition bd, Class[] interfaces) {
        InterfaceMaker maker = new InterfaceMaker();
        PropertyValue[] pvs = bd.getPropertyValues().getPropertyValues();
        for (PropertyValue pv : pvs) {
            String propertyName = pv.getName();
            Class propertyType = BeanUtils.findPropertyType(propertyName, interfaces);
            String setterName = "set" + StringUtils.capitalize(propertyName);
            Signature signature = new Signature(setterName, Type.VOID_TYPE,
                    new Type[] {Type.getType(propertyType)});
            maker.add(signature, new Type[0]);
        }
        if (bd instanceof AbstractBeanDefinition) {
            AbstractBeanDefinition abd = (AbstractBeanDefinition) bd;
            if (abd.getInitMethodName() != null) {
                Signature signature = new Signature(abd.getInitMethodName(), Type.VOID_TYPE,
                        new Type[0]);
                maker.add(signature, new Type[0]);
            }
            if (abd.getDestroyMethodName() != null) {
                Signature signature = new Signature(abd.getDestroyMethodName(), Type.VOID_TYPE,
                        new Type[0]);
                maker.add(signature, new Type[0]);
            }
        }
        return maker.create();
    }

    /**
     * Create a composite interface Class for the given interfaces,
     * implementing the given interfaces in one single Class.
     * <p>The default implementation builds a JDK proxy class
     * for the given interfaces.
     *
     * @see java.lang.reflect.Proxy#getProxyClass
     */
    protected Class createCompositeInterface(Class[] interfaces) {
        return ClassUtils.createCompositeInterface(interfaces, this.beanClassLoader);
    }

    /**
     * Create a bean definition for the scripted object, based on the given script
     * definition, extracting the definition data that is relevant for the scripted
     * object (that is, everything but bean class and constructor arguments).
     *
     * @see ScriptFactory#getScriptedObject
     */
    protected BeanDefinition createScriptedObjectBeanDefinition(BeanDefinition bd,
                                                                String scriptFactoryBeanName,
                                                                ScriptSource scriptSource,
                                                                Class[] interfaces) {

        GenericBeanDefinition objectBd = new GenericBeanDefinition(bd);
        objectBd.setFactoryBeanName(scriptFactoryBeanName);
        objectBd.setFactoryMethodName("getScriptedObject");
        objectBd.getConstructorArgumentValues().clear();
        objectBd.getConstructorArgumentValues().addIndexedArgumentValue(0, scriptSource);
        objectBd.getConstructorArgumentValues().addIndexedArgumentValue(1, interfaces);
        return objectBd;
    }

    /**
     * Create a refreshable proxy for the given AOP TargetSource.
     *
     *
     *
     * indicate proxying of all interfaces implemented by the target class)
     *
     * @see RefreshableScriptTargetSource
     */
    protected Object createRefreshableProxy(TargetSource ts, Class[] interfaces) {
        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.setTargetSource(ts);

        if (interfaces == null) {
            proxyFactory.setInterfaces(ClassUtils.getAllInterfacesForClass(ts.getTargetClass(),
                    this.beanClassLoader));
        } else {
            proxyFactory.setInterfaces(interfaces);
        }

        DelegatingIntroductionInterceptor introduction = new DelegatingIntroductionInterceptor(ts);
        introduction.suppressInterface(TargetSource.class);
        proxyFactory.addAdvice(introduction);

        return proxyFactory.getProxy(this.beanClassLoader);
    }

    /**
     * 根据beanName转换成ScriptSource
     */
    protected ScriptSource convertToScriptSource(String beanName, String scriptSourceLocator,
                                                 ResourceLoader loader) {
        if (scriptSourceLocator.startsWith(INLINE_SCRIPT_PREFIX)) {
            return new StaticScriptSource(scriptSourceLocator.substring(INLINE_SCRIPT_PREFIX
                    .length()), beanName);
        } else if (scriptSourceLocator.startsWith(GroovyConstant.SCRIPT_SOURCE_PREFIX)) {
            String scriptName = getScriptName(scriptSourceLocator);
            return new DatabaseScriptSource(scriptName);
        } else {
            return new ResourceScriptSource(loader.getResource(scriptSourceLocator));
        }
    }

    /**
     * 获取脚本名称
     */
    private String getScriptName(String scriptSourceLocator) {
        String scriptName = StringUtils.substringAfter(scriptSourceLocator,
                GroovyConstant.SCRIPT_SOURCE_PREFIX);
        return StringUtils.lowerCase(scriptName);
    }

    /**
     * 删除指定的bean
     */
    public void destoryBean(String beanName) {
        String customedBeanName = SCRIPTED_OBJECT_NAME_PREFIX + beanName;
        this.destoryScriptFactoryBean(customedBeanName);
        //destory SCRIPT_FACTORY bean added by yiyao
        customedBeanName = SCRIPT_FACTORY_NAME_PREFIX + beanName;
        this.destoryScriptFactoryBean(customedBeanName);
    }

    /**
     * destory script factory bean
     */
    private void destoryScriptFactoryBean(String beanName) {
        LogUtil.debug(logger, "Destroy bean: " + beanName);
        try {
            this.scriptBeanFactory.removeBeanDefinition(beanName);
        } catch (NoSuchBeanDefinitionException e) {
            //do not throw Exception
            LogUtil.warn(logger, "Destroy bean: " + beanName, e);
            return;
        }
    }

    /**
     * Destroy the inner bean factory (used for scripts) on shutdown.
     */
    @Override
    public void destroy() {
        this.scriptBeanFactory.destroySingletons();
    }

}