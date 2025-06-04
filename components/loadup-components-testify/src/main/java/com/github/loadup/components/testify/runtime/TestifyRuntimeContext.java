/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.testify.runtime;

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

import com.github.loadup.components.testify.component.db.DBDatasProcessor;
import com.github.loadup.components.testify.exception.TestifyException;
import com.github.loadup.components.testify.model.*;
import com.github.loadup.components.testify.template.TestifyTestBase;
import com.github.loadup.components.testify.util.VelocityUtil;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * Acts框架运行时上下文，可以设置数据，获取返回值
 *
 * @author tantian.wc
 * <p>
 * Exp $
 */
@Slf4j
public class TestifyRuntimeContext {

    /**
     * caseId
     */
    public String caseId;
    /**
     * 测试数据
     */
    public PrepareData prepareData;
    /**
     * 运行时的上下文
     */
    public Map<String, Object> componentContext;
    /**
     * 数据处理组件
     */
    public DBDatasProcessor dbDatasProcessor;
    /**
     * 被测方法
     */
    public Method testedMethod;
    /**
     * 被测对象
     */
    public Object testedObj;
    /**
     * 返回结果，execute阶段之后会生成
     */
    public Object resultObj;
    /**
     * 异常结果期望
     */
    public Object exceptionObj;

    /** 测试脚本的上下文 */
    /**
     * 组件上下文列表，总的组件
     */
    public List<TestifyTestBase> testifyComponents = new ArrayList<>();
    /**
     * 参数列表，可以通过$指定变量
     */
    public Map<String, Object> paramMap = new LinkedHashMap<String, Object>();
    /** mock列表 */
    //    public Map<VirtualMock, AtsSingleMock>  mocks                = new LinkedHashMap<VirtualMock,
    // AtsSingleMock>();

    /**
     * command组件分组存放List
     */
    public List<String> prepareCommandList = new ArrayList<String>();

    public List<String> clearCommandList = new ArrayList<String>();
    public List<String> checkCommandList = new ArrayList<String>();
    public List<String> defaultCommandList = new ArrayList<String>();

    /**
     * preparData组件分组存放List
     */
    public List<TestifyTestBase> BeforeClearPreList = new ArrayList<TestifyTestBase>();

    public List<TestifyTestBase> AfterClearPreList = new ArrayList<TestifyTestBase>();
    public List<TestifyTestBase> BeforeCheckPreList = new ArrayList<TestifyTestBase>();
    public List<TestifyTestBase> AfterCheckPreList = new ArrayList<TestifyTestBase>();
    public List<TestifyTestBase> BeforePreparePreList = new ArrayList<TestifyTestBase>();
    public List<TestifyTestBase> AfterPreparePreList = new ArrayList<TestifyTestBase>();

    // 定义一个MAP用来存放组件化运行结果
    public Map<String, Map<String, Object>> componentsResultMap = new LinkedHashMap<String, Map<String, Object>>();

    // 存放预跑反填的sqlList
    public List<String> backFillSqlList = new ArrayList<String>();

    public TestifyRuntimeContext() {}

    public TestifyRuntimeContext(
            String caseId,
            PrepareData prepareData,
            Map<String, Object> componentContext,
            DBDatasProcessor dbDatasProcessor,
            Method testedMethod,
            Object testedObj) {
        super();
        this.caseId = caseId;
        this.prepareData = prepareData;
        this.componentContext = componentContext;
        this.dbDatasProcessor = dbDatasProcessor;
        this.testedMethod = testedMethod;
        this.testedObj = testedObj;
    }

    /**
     * 获取第i个参数
     *
     * @param i
     * @return
     */
    public VirtualObject getArg(int i) {
        if (prepareData == null
                || prepareData.getArgs() == null
                || prepareData.getArgs().getVirtualObjects() == null) {
            return null;
        }

        int argNum = prepareData.getArgs().getVirtualObjects().size();
        if (argNum <= i) {
            throw new TestifyException("获取索引(" + i + ")超出入参个数(" + argNum + ")");
        }

        return prepareData.getArgs().getVirtualObjects().get(i);
    }

    /**
     * 获取第i个参数
     *
     * @param i
     * @return
     */
    public Object getArgValue(int i) {
        if (prepareData == null
                || prepareData.getArgs() == null
                || prepareData.getArgs().getInputArgs() == null) {
            return null;
        }

        int argNum = prepareData.getArgs().getInputArgs().size();
        if (argNum <= i) {
            throw new TestifyException("获取索引(" + i + ")超出入参个数(" + argNum + ")");
        }

        return prepareData.getArgs().getInputArgs().get(i);
    }

    /**
     * 获取指定的自定义入参
     *
     * @param virParsName
     * @return
     */
    public Object getUserDefParams(String virParsName) {
        if (prepareData == null
                || prepareData.getVirtualParams() == null
                || prepareData.getVirtualParams().getParams() == null) {
            return null;
        }

        VirtualObject virRet = (VirtualObject) prepareData.getVirtualParams().getByParaName(virParsName);
        if (null == virRet) {
            return null;
        }
        return virRet.getObject();
    }

    /**
     * 设置第i个参数为obj
     *
     * @param i
     * @param obj
     * @return
     */
    public void setArg(int i, Object obj) {
        if (prepareData == null) {
            return;
        }
        if (prepareData.getArgs() == null || prepareData.getArgs().getVirtualObjects() == null) {
            prepareData.setArgs(VirtualArgs.getInstance());
            if (prepareData.getArgs().getVirtualObjects() == null) {
                prepareData.getArgs().setInputArgs(new ArrayList<Object>());
            }
            prepareData.getArgs().getVirtualObjects().add(new VirtualObject(obj));
        } else {
            prepareData.getArgs().getVirtualObjects().set(i, new VirtualObject(obj));
        }
    }

    /**
     * 设置指定的自定义入参
     *
     * @param virParsName
     * @param obj
     * @return
     */
    public void setUserDefParams(String virParsName, Object obj) {
        if (prepareData == null) {
            return;
        }
        if (prepareData.getArgs() == null || prepareData.getVirtualParams().getParams() == null) {
            prepareData.setVirtualParams(VirtualParams.getInstance());
            prepareData.getVirtualParams().addParam(virParsName, obj);
        } else {
            prepareData.getVirtualParams().addParam(virParsName, obj);
        }
    }

    /**
     * 获取异常的VirtualObject
     *
     * @return
     */
    public VirtualObject getException() {
        if (prepareData == null
                || prepareData.getExpectException() == null
                || prepareData.getExpectException().getExpectException() == null) {
            return null;
        }
        return prepareData.getExpectException().getVirtualObject();
    }

    /**
     * 设置异常
     *
     * @param e
     */
    public void setException(Throwable e) {
        if (prepareData == null) {
            return;
        }
        if (prepareData.getExpectException() == null
                || prepareData.getExpectException().getExpectException() == null) {
            prepareData.setExpectException(new VirtualException());
        }

        prepareData.getExpectException().getExpectException().setObject(e);
        prepareData
                .getExpectException()
                .getVirtualObject()
                .getFlagSetter(Throwable.class)
                .set("stackTrace", "N")
                .set("cause", "N");
        prepareData
                .getExpectException()
                .getVirtualObject()
                .getFlagSetter(e.getClass())
                .set("stackTrace", "N")
                .set("cause", "N");
    }

    /**
     * 获取当前期望结果对象
     *
     * @return
     */
    public Object getExpectResult() {
        if (prepareData == null || prepareData.getExpectResult() == null) {
            return null;
        }
        return prepareData.getExpectResult().getResultObj();
    }

    /**
     * 设置当前期望结果对象
     *
     * @return
     */
    public Boolean setExpectResult(Object objToSet) {
        if (prepareData == null) {
            return false;
        }

        VirtualResult virRt = prepareData.getExpectResult();
        if (virRt == null) {
            virRt = new VirtualResult();
        }

        if (null == virRt.getVirtualObject()) {
            VirtualObject virObj = new VirtualObject();
            virObj.setObject(objToSet);
        } else {
            virRt.getVirtualObject().setObject(objToSet);
        }
        prepareData.setExpectResult(virRt);

        return true;
    }

    /**
     * 更新表中的变量
     */
    public void refreshDataParam() {
        if (prepareData == null) {
            return;
        }
        if (prepareData.getDepDataSet() != null && prepareData.getDepDataSet().getVirtualTables() != null) {
            refreshAllTableParam(prepareData.getDepDataSet().getVirtualTables());
        }
        if (prepareData.getExpectDataSet() != null
                && prepareData.getExpectDataSet().getVirtualTables() != null) {
            refreshAllTableParam(prepareData.getExpectDataSet().getVirtualTables());
        }
    }

    public void refreshAllTableParam(List<VirtualTable> tables) {
        if (null == tables || 0 == tables.size()) {
            return;
        }
        for (VirtualTable table : tables) {
            refreshSingleTableParam(table);
        }
    }

    public void refreshSingleTableParam(VirtualTable table) {
        if (null == table) {
            return;
        }

        for (Map<String, Object> rowData : table.getTableData()) {
            for (String key : rowData.keySet()) {
                if (!String.valueOf(rowData.get(key)).contains("$")) {
                    // 无变量引用，直接跳过
                    continue;
                }
                String newVal = VelocityUtil.evaluateString(this.paramMap, String.valueOf(rowData.get(key)));
                rowData.put(key, newVal);
            }
        }
    }

    public PrepareData getPrepareData() {
        return prepareData;
    }

    public void setPrepareData(PrepareData prepareData) {
        this.prepareData = prepareData;
    }

    public Map<String, Object> getComponentContext() {
        return componentContext;
    }

    public void setComponentContext(Map<String, Object> componentContext) {
        this.componentContext = componentContext;
    }

    public DBDatasProcessor getDbDatasProcessor() {
        return dbDatasProcessor;
    }

    public void setDbDatasProcessor(DBDatasProcessor dbDatasProcessor) {
        this.dbDatasProcessor = dbDatasProcessor;
    }

    public Method getTestedMethod() {
        return testedMethod;
    }

    public void setTestedMethod(Method testedMethod) {
        this.testedMethod = testedMethod;
    }

    public Object getTestedObj() {
        return testedObj;
    }

    public void setTestedObj(Object testedObj) {
        this.testedObj = testedObj;
    }

    public Object getResultObj() {
        return resultObj;
    }

    public void setResultObj(Object resultObj) {
        this.resultObj = resultObj;
    }

    public Object getExceptionObj() {
        return exceptionObj;
    }

    public void setExceptionObj(Object exceptionObj) {
        this.exceptionObj = exceptionObj;
    }

    public Map<String, Object> getParamMap() {
        return paramMap;
    }

    public void setParamMap(Map<String, Object> paramMap) {
        this.paramMap = paramMap;
    }

    public void addOneParam(String paraName, Object paraObj) {
        paramMap.put(paraName, paraObj);
    }

    public Object getParamByName(String paraName) {
        if (null == paraName) {
            return null;
        }

        return paramMap.get(paraName);
    }

    @SuppressWarnings("unchecked")
    public <T> T getParamByNameWithGeneric(String paraName) {
        if (null == paraName) {
            return null;
        }
        return (T) paramMap.get(paraName);
    }

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    public List<TestifyTestBase> getTestifyComponents() {
        return testifyComponents;
    }

    public void setTestifyComponents(List<TestifyTestBase> testifyComponents) {
        this.testifyComponents = testifyComponents;
    }

    /**
     * @return property value of componentsResultMap
     */
    public Map<String, Map<String, Object>> getComponentsResultMap() {
        return componentsResultMap;
    }

    /**
     * @param componentsResultMap value to be assigned to property componentsResultMap
     */
    public void setComponentsResultMap(Map<String, Map<String, Object>> componentsResultMap) {
        this.componentsResultMap = componentsResultMap;
    }

    /**
     * 获取所有入参
     */
    public List<Object> getInputParams() {
        if (null == prepareData || null == prepareData.getArgs()) {
            return null;
        }
        List<Object> retObjList = new ArrayList<Object>();
        for (VirtualObject tempVObj : prepareData.getArgs().getVirtualObjects()) {
            retObjList.add(tempVObj.getObject());
        }
        return retObjList;
    }

    /**
     * 按位置获取入参
     */
    public Object getInputParamByPos(int i) {
        if (null == prepareData.getArgs() || null == prepareData.getArgs().getVirtualObjects()) {
            return null;
        }
        if (i >= prepareData.getArgs().inputArgs.size()) {
            return null;
        }
        return prepareData.getArgs().getVirtualObjects().get(i).getObject();
    }

    /**
     * 新增用例入参
     */
    public void addInputParam(Object obj) {
        if (null == prepareData.getArgs()) {
            return;
        }
        VirtualObject vObj = new VirtualObject();
        vObj.setObject(obj);
        prepareData.getArgs().addArg(vObj);
    }

    /**
     * 按位置快速获取组件中的参数
     */
    public List<Object> getVirtualComponentInputParasByPos(int i) {
        if (null == prepareData
                || null == prepareData.getVirtualComponentSet()
                || null == prepareData.getVirtualComponentSet().getComponents()) {
            return null;
        }
        if (i >= prepareData.getVirtualComponentSet().getComponents().size()) {
            return null;
        }
        PrepareData prepareDataComponent =
                prepareData.getVirtualComponentSet().getComponents().get(i).getPrepareData();
        if (null == prepareDataComponent || null == prepareDataComponent.getArgs()) {
            return null;
        }
        List<Object> retObjList = new ArrayList<Object>();
        for (Object tempObj : prepareDataComponent.getArgs().getVirtualObjects()) {
            retObjList.add(tempObj);
        }
        return retObjList;
    }

    /**
     * 按位置快速获取组件中的结果
     */
    public Object getVirtualComponentExpectResultByPos(int i) {
        if (null == prepareData
                || null == prepareData.getVirtualComponentSet()
                || null == prepareData.getVirtualComponentSet().getComponents()) {
            return null;
        }
        if (i >= prepareData.getVirtualComponentSet().getComponents().size()) {
            return null;
        }
        PrepareData prepareDataComponent =
                prepareData.getVirtualComponentSet().getComponents().get(i).getPrepareData();
        if (null == prepareDataComponent || null == prepareDataComponent.getExpectResult()) {
            return null;
        }
        return prepareDataComponent.getExpectResult().getResultObj();
    }

    /**
     * 获取db准备数据
     */
    public List<VirtualTable> getPreparedDbData() {
        if (null == prepareData.getDepDataSet()
                || null == prepareData.getDepDataSet().getVirtualTables()) {
            return null;
        }
        return prepareData.getDepDataSet().getVirtualTables();
    }

    public List<String> getBackFillSqlList() {
        return backFillSqlList;
    }

    public void setBackFillSqlList(List<String> backFillSqlList) {
        this.backFillSqlList = backFillSqlList;
    }
}
