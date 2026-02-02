package io.github.loadup.commons.util.context;

/*-
 * #%L
 * loadup-commons-util
 * %%
 * Copyright (C) 2022 - 2024 loadup_cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/** Spring上下文工具类 */
@Component
public class ApplicationContextUtils implements ApplicationContextAware {

  /** Spring应用上下文 */
  private static ApplicationContext applicationContext;

  /** */
  public static ApplicationContext getApplicationContext() {
    return applicationContext;
  }

  public static Environment getEnvironment() {
    return applicationContext.getEnvironment();
  }

  /**
   * 实现ApplicationContextAware接口的回调方法，设置上下文环境
   *
   * @throws BeansException
   */
  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    ApplicationContextUtils.applicationContext = applicationContext;
  }

  /**
   * 获取对象
   *
   * @throws BeansException
   */
  public static Object getBean(String name) throws BeansException {
    Object bean = applicationContext.getBean(name);
    if (Objects.isNull(bean)) {
      if (!StringUtils.endsWith(name, "Impl")) {
        bean = applicationContext.getBean(name + "Impl");
      }
    }
    return bean;
  }

  /** 获取所有定义的bean的名字 */
  public static String[] getBeanDefinitionNames() {
    return applicationContext.getBeanDefinitionNames();
  }

  /**
   * 获取类型为requiredType的对象 如果bean不能被类型转换，相应的异常将会被抛出（BeanNotOfRequiredTypeException）
   *
   * @throws BeansException
   */
  public static <T> T getBean(String name, Class<T> requiredType) throws BeansException {
    T bean = applicationContext.getBean(name, requiredType);
    if (Objects.isNull(bean)) {
      if (!StringUtils.endsWith(name, "Impl")) {
        bean = applicationContext.getBean(name + "Impl", requiredType);
      }
    }
    return bean;
  }

  /** 如果BeanFactory包含一个与所给名称匹配的bean定义，则返回true */
  public static boolean containsBean(String name) {
    return applicationContext.containsBean(name);
  }

  /**
   * 判断以给定名字注册的bean定义是一个singleton还是一个prototype。
   * 如果与给定名字相应的bean定义没有被找到，将会抛出一个异常（NoSuchBeanDefinitionException）
   *
   * @throws NoSuchBeanDefinitionException
   */
  public static boolean isSingleton(String name) throws NoSuchBeanDefinitionException {
    return applicationContext.isSingleton(name);
  }

  /**
   * @throws NoSuchBeanDefinitionException
   */
  public static Class<?> getType(String name) throws NoSuchBeanDefinitionException {
    return applicationContext.getType(name);
  }

  /**
   * 如果给定的bean名字在bean定义中有别名，则返回这些别名
   *
   * @throws NoSuchBeanDefinitionException
   */
  public static String[] getAliases(String name) throws NoSuchBeanDefinitionException {
    return applicationContext.getAliases(name);
  }
}
