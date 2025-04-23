/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2016 All Rights Reserved.
 */
package com.alipay.test.acts.annotation;

/*-
 * #%L
 * loadup-components-test
 * %%
 * Copyright (C) 2022 - 2025 loadup_cloud
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import com.alipay.test.acts.annotation.acts.*;
import com.alipay.test.acts.component.db.DBDatasProcessor;
import com.alipay.test.acts.template.ActsTestBase;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 
 * @author tianzhu.wtzh
 * @version $Id: ActsAnnotationFactory.java, v 0.1 2016年5月12日 上午11:09:18 tianzhu.wtzh Exp $
 */
public class ActsAnnotationFactory {

    /** 当前注册的注解方法*/
    protected Map<String, List<IActsMethod>> annoationMethods;

    /**数据处理器*/
    public DBDatasProcessor                  dbDatasProcessor;

    /** IActsMethod builder工厂 */
    protected ActsMethodBuilder              actsMethodBuilder = new ActsMethodBuilder();

    /**
     * 工厂构造函数
     * @param annoationMethods
     * @param dbDatasProcessor
     */
    public ActsAnnotationFactory(Map<String, List<IActsMethod>> annoationMethods,
                                 DBDatasProcessor dbDatasProcessor) {
        this.annoationMethods = annoationMethods;
        this.dbDatasProcessor = dbDatasProcessor;
    }

    /**
     * 扫描获取 @BeforeClean, @AfterClean, @BeforeCheck, @AfterCheck
     * @BeforePrepare, @AfterPrepare, @BeforeTable, @AfterTable, @BeforeExecSql
     */
    public void initAnnotationMethod(Set<Method> allMethod, ActsTestBase template) {

        // 防止跨用例集混用
        this.dbDatasProcessor.getBeforeExecSqlMethodList().clear();

        for (Method method : allMethod) {
            addActsMethod(method, AfterClean.class, annoationMethods, template);
            addActsMethod(method, BeforeClean.class, annoationMethods, template);
            addActsMethod(method, BeforeCheck.class, annoationMethods, template);
            addActsMethod(method, AfterCheck.class, annoationMethods, template);
            addActsMethod(method, BeforePrepare.class, annoationMethods, template);
            addActsMethod(method, AfterPrepare.class, annoationMethods, template);
            addActsMethod(method, Executor.class, annoationMethods, template);

            //@BeforeTable, @AfterTable 属于特殊类的标签
            //如果包含如下方法，需要对它进行支持
            if (method.isAnnotationPresent(BeforeTable.class)) {

                this.dbDatasProcessor
                    .getBeforeVTableExecuteMethodList().add(
                    new IVTableGroupCmdMethodImpl(template, method));
            }

            if (method.isAnnotationPresent(AfterTable.class)) {

               this.dbDatasProcessor
                    .getAfterVTableExecuteMethodList().add(
                    new IVTableGroupCmdMethodImpl(template, method));
            }

            if (method.isAnnotationPresent(BeforeExecSql.class)) {

                this.dbDatasProcessor
                        .getBeforeExecSqlMethodList().add(
                        new IVTableGroupCmdMethodImpl(template, method));
            }

        }

    }

    /**
     * 添加方法
     * @param m
     * @param clsz
     */
    private void addActsMethod(Method m, Class<? extends Annotation> clsz,
                               Map<String, List<IActsMethod>> annoationMethods,
                               ActsTestBase template) {

        if (!annoationMethods.containsKey(clsz.getSimpleName())) {

            List<IActsMethod> methodList = new LinkedList<IActsMethod>();
            annoationMethods.put(clsz.getSimpleName(), methodList);
        }

        addActsMethod(annoationMethods.get(clsz.getSimpleName()), m, clsz, template);
    }

    /**
     * 添加注解方法
     * @param methodList
     * @param m
     * @param clsz
     */
    private void addActsMethod(List<IActsMethod> methodList, Method m,
                               Class<? extends Annotation> clsz, ActsTestBase template) {
        if (m.isAnnotationPresent(clsz)) {
            Annotation annotaion = m.getAnnotation(clsz);

            IActsMethod iActsMethod = actsMethodBuilder.buildActsMethod(m, template, clsz,
                annotaion);
            int i = 0;
            for (i = 0; i < methodList.size(); i++) {
                if (methodList.get(i).getOrder() > iActsMethod.getOrder()) {
                    break;
                }
            }

            methodList.add(i, iActsMethod);
        }
    }

    /**
     * Getter method for property <tt>annoationMethods</tt>.
     * 
     * @return property value of annoationMethods
     */
    public Map<String, List<IActsMethod>> getAnnoationMethods() {
        return annoationMethods;
    }

    /**
     * Setter method for property <tt>annoationMethods</tt>.
     * 
     * @param annoationMethods value to be assigned to property annoationMethods
     */
    public void setAnnoationMethods(Map<String, List<IActsMethod>> annoationMethods) {
        this.annoationMethods = annoationMethods;
    }

    /**
     * Getter method for property <tt>dbDatasProcessor</tt>.
     * 
     * @return property value of dbDatasProcessor
     */
    public DBDatasProcessor getDbDatasProcessor() {
        return dbDatasProcessor;
    }

    /**
     * Setter method for property <tt>dbDatasProcessor</tt>.
     * 
     * @param dbDatasProcessor value to be assigned to property dbDatasProcessor
     */
    public void setDbDatasProcessor(DBDatasProcessor dbDatasProcessor) {
        this.dbDatasProcessor = dbDatasProcessor;
    }

    /**
     * Getter method for property <tt>actsMethodBuilder</tt>.
     * 
     * @return property value of actsMethodBuilder
     */
    public ActsMethodBuilder getActsMethodBuilder() {
        return actsMethodBuilder;
    }

    /**
     * Setter method for property <tt>actsMethodBuilder</tt>.
     * 
     * @param actsMethodBuilder value to be assigned to property actsMethodBuilder
     */
    public void setActsMethodBuilder(ActsMethodBuilder actsMethodBuilder) {
        this.actsMethodBuilder = actsMethodBuilder;
    }

}
