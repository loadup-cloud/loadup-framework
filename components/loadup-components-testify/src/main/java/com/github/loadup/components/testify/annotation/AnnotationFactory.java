
package com.github.loadup.components.testify.annotation;

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

import com.github.loadup.components.testify.annotation.testify.*;
import com.github.loadup.components.testify.component.db.DBDatasProcessor;
import com.github.loadup.components.testify.template.TestifyTestBase;
import lombok.Getter;
import lombok.Setter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 */
@Getter
@Setter
public class AnnotationFactory {

    /**
     * 数据处理器
     */
    public DBDatasProcessor dbDatasProcessor;
    /**
     * 当前注册的注解方法
     */
    protected Map<String, List<TestifyMethod>> annotationMethods;
    /**
     *  builder工厂
     */
    protected TestifyMethodBuilder testifyMethodBuilder = new TestifyMethodBuilder();

    /**
     * 工厂构造函数
     *
     * @param annotationMethods
     * @param dbDatasProcessor
     */
    public AnnotationFactory(Map<String, List<TestifyMethod>> annotationMethods,
                             DBDatasProcessor dbDatasProcessor) {
        this.annotationMethods = annotationMethods;
        this.dbDatasProcessor = dbDatasProcessor;
    }

    /**
     * 扫描获取 @BeforeClean, @AfterClean, @BeforeCheck, @AfterCheck
     *
     * @BeforePrepare, @AfterPrepare, @BeforeTable, @AfterTable, @BeforeExecSql
     */
    public void initAnnotationMethod(Set<Method> allMethod, TestifyTestBase template) {

        // 防止跨用例集混用
        this.dbDatasProcessor.getBeforeExecSqlMethodList().clear();

        for (Method method : allMethod) {
            addTestifyMethod(method, AfterClean.class, annotationMethods, template);
            addTestifyMethod(method, BeforeClean.class, annotationMethods, template);
            addTestifyMethod(method, BeforeCheck.class, annotationMethods, template);
            addTestifyMethod(method, AfterCheck.class, annotationMethods, template);
            addTestifyMethod(method, BeforePrepare.class, annotationMethods, template);
            addTestifyMethod(method, AfterPrepare.class, annotationMethods, template);
            addTestifyMethod(method, Executor.class, annotationMethods, template);

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
     *
     * @param m
     * @param clsz
     */
    private void addTestifyMethod(Method m, Class<? extends Annotation> clsz,
                                  Map<String, List<TestifyMethod>> annoationMethods,
                                  TestifyTestBase template) {

        if (!annoationMethods.containsKey(clsz.getSimpleName())) {

            List<TestifyMethod> methodList = new LinkedList<TestifyMethod>();
            annoationMethods.put(clsz.getSimpleName(), methodList);
        }

        addTestifyMethod(annoationMethods.get(clsz.getSimpleName()), m, clsz, template);
    }

    /**
     * 添加注解方法
     *
     * @param methodList
     * @param m
     * @param clsz
     */
    private void addTestifyMethod(List<TestifyMethod> methodList, Method m,
                                  Class<? extends Annotation> clsz, TestifyTestBase template) {
        if (m.isAnnotationPresent(clsz)) {
            Annotation annotaion = m.getAnnotation(clsz);

            TestifyMethod testifyMethod = testifyMethodBuilder.buildTestifyMethod(m, template, clsz,
                    annotaion);
            int i = 0;
            for (i = 0; i < methodList.size(); i++) {
                if (methodList.get(i).getOrder() > testifyMethod.getOrder()) {
                    break;
                }
            }

            methodList.add(i, testifyMethod);
        }
    }





}
