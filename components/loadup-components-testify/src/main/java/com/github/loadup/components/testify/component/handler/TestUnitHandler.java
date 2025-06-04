/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.testify.component.handler;

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

import com.github.loadup.commons.util.JsonUtil;
import com.github.loadup.components.testify.component.components.TestifyComponentUtil;
import com.github.loadup.components.testify.component.event.EventContextHolder;
import com.github.loadup.components.testify.enums.RunningContextEnum;
import com.github.loadup.components.testify.exception.TestifyException;
import com.github.loadup.components.testify.model.*;
import com.github.loadup.components.testify.runtime.*;
import com.github.loadup.components.testify.support.TestTemplate;
import com.github.loadup.components.testify.template.TestifyTestBase;
import com.github.loadup.components.testify.util.LogUtil;
import com.github.loadup.components.testify.utils.*;
import com.github.loadup.components.testify.utils.check.ObjectCompareUtil;
import java.lang.reflect.*;
import java.util.*;
import java.util.Map.Entry;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ClassUtils;
import org.testng.Assert;

/**
 * 处理各个测试数据
 *
 * @author tantian.wc
 *
 * Exp $
 */
public class TestUnitHandler {

    /**
     * 该list内的组件名不调用组件执行
     */
    private static List<String> ignoreCallStringList;

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    /**
     * itest 用例描述
     */
    private final String caseDesc = "NM_H_XXX_XXX";
    /**
     * 存储当前topic，eventcode 对比的对象
     */
    public Object storeExpEventObj = null;
    /**
     * 储存扫描过的对象，防止死循环
     */
    protected List<Object> scanList;
    /**
     * 运行时上下文
     */
    TestifyRuntimeContext testifyRuntimeContext;

    public TestUnitHandler() {
        this.testifyRuntimeContext = TestifyRuntimeContextThreadHold.getContext();
    }

    public TestUnitHandler(TestifyRuntimeContext testifyRuntimeContext) {

        this.testifyRuntimeContext = testifyRuntimeContext;
    }

    /**
     * 准备DB数据操作
     *
     * @param extMapInfo
     * @param groupIds
     */
    public void prepareDepData(Map<String, String> extMapInfo, String... groupIds) {
        testifyRuntimeContext.getDbDatasProcessor().updateDataSource(extMapInfo);
        try {

            // 数据准备
            if (null != testifyRuntimeContext.getPreparedDbData()
                    && 0 < testifyRuntimeContext.getPreparedDbData().size()) {

                DetailCollectUtils.appendAndLog("Preparing DB data:", logger);
                for (VirtualTable table : testifyRuntimeContext.getPreparedDbData()) {
                    if ((ArrayUtils.isEmpty(groupIds) && StringUtils.isEmpty(table.getNodeGroup()))
                            || ArrayUtils.contains(groupIds, table.getNodeGroup())) {
                        logger.info(table.getTableName());
                    }
                }
                replaceTableParam(testifyRuntimeContext.getPreparedDbData(), groupIds);
                testifyRuntimeContext
                        .getDbDatasProcessor()
                        .importDepDBDatas(testifyRuntimeContext.getPreparedDbData(), groupIds);

            } else {
                logger.info("None DB preparation");
            }
        } catch (Exception e) {
            throw new TestifyException(
                    "Unknown exception while preparing DB data. DB actual parameters:"
                            + testifyRuntimeContext.getPreparedDbData().toString(),
                    e);
        }
    }

    /**
     * 组件初始化
     */
    public void initComponents() {
        try {
            // 清理各阶段组件列表
            clearComponentsList();
            // 初始化组件
            if (testifyRuntimeContext.getPrepareData() instanceof PrepareData) {
                PrepareData prepareData = testifyRuntimeContext.getPrepareData();
                if (null != prepareData.getVirtualComponentSet()) {
                    for (VirtualComponent virtualComponent :
                            prepareData.getVirtualComponentSet().getComponents()) {
                        // 初始化command组件
                        if (virtualComponent.getPrepareData() == null) {
                            initCommandCmp(virtualComponent);
                        } else {
                            try {
                                Class<?> testClazz =
                                        getClass().getClassLoader().loadClass(virtualComponent.getComponentClass());
                                if (TestifyTestBase.class.isAssignableFrom(testClazz)) {
                                    TestifyTestBase testBase = (TestifyTestBase) testClazz.newInstance();
                                    //
                                    // testBase.injectBundleContext(actsRuntimeContext
                                    //                                            .getBundleContext());
                                    testBase.initRuntimeContext(
                                            virtualComponent.getComponentId(), virtualComponent.getPrepareData(), true);
                                    testBase.initTestUnitHandler();
                                    // 组件自定义参数配置
                                    componentPrepareUserPara(virtualComponent, testBase);

                                    // 组件化参数替换
                                    testBase.getTestifyRuntimeContext()
                                            .paramMap
                                            .putAll(testifyRuntimeContext.getParamMap());
                                    testifyRuntimeContext.paramMap.putAll(
                                            testBase.getTestifyRuntimeContext().getParamMap());
                                    // 放入默认分组，为了能够进行组件化的一些共有操作
                                    testifyRuntimeContext.getTestifyComponents().add(testBase);

                                    // 进行组件分类操作，当前支持组件化脚本在当前流程的清理，准备，check阶段执行
                                    ComponentsProcessor.getIndexForPreComp(
                                            testifyRuntimeContext, testBase, virtualComponent);
                                }
                            } catch (ClassNotFoundException e) {
                                throw new TestifyException(
                                        "cot find sub test class: " + virtualComponent.getComponentClass(), e);
                            }
                        }
                    }
                } else {
                    logger.info("no sub test");
                }
            }
        } catch (Exception e) {
            throw new TestifyException("unknown exception while run sub tests", e);
        }
    }

    /**
     * 组件执行逻辑，通过传入组件进行执行
     *
     * @param excuteCompList
     */
    public void executeComponents(List<TestifyTestBase> excuteCompList) {
        for (TestifyTestBase testifyComponents : excuteCompList) {
            // 打印日志
            DetailCollectUtils.appendAndLog(
                    "=============================开始执行组件caseId="
                            + testifyComponents.getTestifyRuntimeContext().getCaseId() + ":"
                            + testifyComponents
                                    .getTestifyRuntimeContext()
                                    .prepareData
                                    .getDescription()
                            + "=================",
                    logger);
            // 先将父类上下文放一次，因为可能上一个组件在最后做了put，该组件要使用就必须放进去
            testifyComponents.getTestifyRuntimeContext().paramMap.putAll(testifyRuntimeContext.getParamMap());
            // 组件的中的组件初始化及清理
            testifyComponents.initComponentsBeforeTest(testifyComponents.getTestifyRuntimeContext());

            // 组件的核心测试调用
            testifyComponents.beforeTestifyTest(testifyComponents.getTestifyRuntimeContext());
            testifyComponents.beforeTestifyTest(testifyComponents.getTestifyRuntimeContext().paramMap);
            testifyComponents.executeComponents(testifyComponents.getTestifyRuntimeContext());
            testifyComponents.prepare(testifyComponents.getTestifyRuntimeContext());
            testifyComponents.execute(testifyComponents.getTestifyRuntimeContext());
            testifyComponents.check(testifyComponents.getTestifyRuntimeContext());
            testifyComponents.afterTestifyTest(testifyComponents.getTestifyRuntimeContext());
            // 组件化参数替换，这个地方做这一步是为了将各个组件之间的结果对象打通，可以再其他组件中用上一个的结果值等，需要在各自组件中put进来
            testifyRuntimeContext.paramMap.putAll(
                    testifyComponents.getTestifyRuntimeContext().getParamMap());

            DetailCollectUtils.appendAndLog(
                    "=============================组件执行结束caseId="
                            + testifyComponents.getTestifyRuntimeContext().getCaseId()
                            + "=========================================",
                    logger);
        }
    }

    /**
     * 测试方法执行
     */
    public void execute() {
        try {
            List<Object> inputParams =
                    testifyRuntimeContext.getPrepareData().getArgs().getInputArgs();
            replaceAllParam(inputParams, testifyRuntimeContext.getParamMap());

            Object[] paramObjs = null;
            if (inputParams != null) {
                paramObjs = inputParams.toArray(new Object[0]);
            }

            if (testifyRuntimeContext.getTestedMethod() != null) {
                Object resultObj = null;
                try {
                    DetailCollectUtils.appendAndLog(
                            "Start to invoke method:"
                                    + testifyRuntimeContext.getTestedMethod().getName() + " parameters:",
                            logger);
                    if (inputParams == null) {
                        logger.info("null");
                    } else {
                        DetailCollectUtils.appendDetail(inputParams.toString());
                    }
                    Method testedMethod = testifyRuntimeContext.getTestedMethod();
                    // Object[] convertedParamObjs = convertParamObjs(testedMethod, paramObjs);
                    resultObj = testedMethod.invoke(testifyRuntimeContext.getTestedObj(), paramObjs);

                    try {
                        DetailCollectUtils.appendAndLog("Invocation result: " + ObjectUtil.toJson(resultObj), logger);
                    } catch (Exception e) {
                        DetailCollectUtils.appendAndLog("Invocation result: " + resultObj, logger);
                    }

                } catch (Exception e) {
                    Object exception = e.getCause() == null
                            ? (e.getMessage() == null ? e : new TestifyException(e.getMessage(), e))
                            : e.getCause();
                    DetailCollectUtils.appendAndLog("Exception Invocation: " + exception.toString(), logger);
                    testifyRuntimeContext.setExceptionObj(exception);
                }
                testifyRuntimeContext.setResultObj(resultObj);
                // 将结果及消息放入组件Map
                putResultToMap();
            } else {
                logger.info("Test method not found, interrupt invocation");
            }
        } catch (Exception e) {
            throw new TestifyException("unknown exception while invocation", e);
        }
    }

    private Object[] convertParamObjs(Method testedMethod, Object[] paramObjs) throws Exception {
        Class<?>[] parameterTypes = testedMethod.getParameterTypes();
        List<Object> convertedParamObjs = new ArrayList<>();
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> parameterType = parameterTypes[i];
            Set<Entry<String, Map>> entrySet = ((Map) paramObjs[i]).entrySet();
            Object collect = entrySet.stream()
                    .filter(entry -> entry.getKey().equals(parameterType.getName()))
                    .map(entry -> {
                        Class<?> aClass = null;
                        try {
                            aClass = ClassUtils.forName(
                                    parameterType.getName(), this.getClass().getClassLoader());
                        } catch (ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                        Object o1 = JsonUtil.parseObject(entry.getValue(), aClass);
                        return o1;
                    })
                    .findFirst()
                    .get();
            convertedParamObjs.add(collect);
        }
        return convertedParamObjs.toArray(new Object[0]);
    }

    /**
     * 异常结果校验
     */
    public void checkException() {

        // 异常对比
        if (testifyRuntimeContext.getPrepareData().getExpectException() != null
                && testifyRuntimeContext.getPrepareData().getExpectException().getExpectException() != null
                && testifyRuntimeContext
                                .getPrepareData()
                                .getExpectException()
                                .getExpectException()
                                .getObject()
                        != null) {
            if (testifyRuntimeContext.getExceptionObj() != null) {
                logger.info("Checking Exception");
                Object expectedExp = testifyRuntimeContext.getExceptionObj();
                VirtualException ve = testifyRuntimeContext.getPrepareData().getExpectException();
                Object actualExp = ve.getExpectExceptionObject();
                ObjectCompareUtil.compare(
                        expectedExp, actualExp, ve.getVirtualObject().flags, testifyRuntimeContext.paramMap);

            } else {
                throw new AssertionError("None Exception raised during invocation, but expects one.");
            }
        } else {
            if (testifyRuntimeContext.getExceptionObj() != null) {
                throw new TestifyException("unknown exception raised during invocation", (Exception)
                        testifyRuntimeContext.getExceptionObj());
            }
            logger.info("None exception to check");
        }
    }

    /**
     * DB结果校验
     *
     * @param extMapInfo
     * @param groupIds
     */
    public void checkExpectDbData(HashMap<String, String> extMapInfo, String... groupIds) {
        try {

            ComponentsTestifyRuntimeContextThreadHold.setContext(this.testifyRuntimeContext);
            testifyRuntimeContext.getDbDatasProcessor().updateDataSource(extMapInfo);
            // 做DB期望值比对
            if (null != testifyRuntimeContext.getPrepareData().getExpectDataSet()
                    && testifyRuntimeContext.getPrepareData().getExpectDataSet().getVirtualTables() != null) {
                logger.info("Checking DB, tables checked");
                for (VirtualTable virtualTable : testifyRuntimeContext
                        .getPrepareData()
                        .getExpectDataSet()
                        .getVirtualTables()) {
                    if ((ArrayUtils.isEmpty(groupIds) && StringUtils.isEmpty(virtualTable.getNodeGroup()))
                            || ArrayUtils.contains(groupIds, virtualTable.getNodeGroup())) {
                        logger.info(virtualTable.getTableName());
                    }
                }
                replaceTableParam(testifyRuntimeContext
                        .getPrepareData()
                        .getExpectDataSet()
                        .getVirtualTables());
                testifyRuntimeContext
                        .getDbDatasProcessor()
                        .compare2DBDatas(
                                testifyRuntimeContext
                                        .getPrepareData()
                                        .getExpectDataSet()
                                        .getVirtualTables(),
                                groupIds);
            } else {
                logger.info("None DB expectation");
            }
        } catch (Exception e) {
            throw new TestifyException("unknown exception while checking DB", e);
        }
    }

    /**
     * 结果对象校验
     */
    public void checkExpectResult() {
        try {
            // 做结果期望值比对
            if (testifyRuntimeContext.getPrepareData().getExpectResult() != null
                    && testifyRuntimeContext
                                    .getPrepareData()
                                    .getExpectResult()
                                    .getResult()
                                    .getObject()
                            != null) {
                DetailCollectUtils.appendAndLog("Checking invocation result:", logger);
                VirtualObject expect =
                        testifyRuntimeContext.getPrepareData().getExpectResult().getVirtualObject();
                Object actual = testifyRuntimeContext.getResultObj();
                try {

                    logger.info("\nexpect:" + ObjectUtil.toJson(expect.getObject()) + "\nactual:"
                            + ObjectUtil.toJson(actual));
                } catch (Exception e) {
                    logger.error("\nexpect:" + expect + "\nactual:" + actual);
                    LogUtil.printColoredError(
                            logger, "\nexpect:" + expect + "\nactual:" + actual + "," + e.getMessage());
                }
                ObjectCompareUtil.compare(
                        actual, expect.getObject(), expect.getFlags(), testifyRuntimeContext.paramMap);
            } else {
                logger.info("None result expectation");
            }
        } catch (Exception e) {
            throw new TestifyException("unknown exception while checking invocation result", e);
        }
    }

    /**
     * 消息校验操作
     *
     * @param groupIds
     */
    public void checkExpectEvent(String... groupIds) {
        try {
            // 消息对比
            logger.info("Checking Events");
            if (!testifyRuntimeContext
                    .getPrepareData()
                    .getExpectEventSet()
                    .getVirtualEventObjects()
                    .isEmpty()) {
                if ("jvm".equalsIgnoreCase("TestUtils.getSofaConfig(test_xmode)")) {
                    Map<String, List<Object>> uEventList = EventContextHolder.getBizEvent();
                    for (VirtualEventObject virtualEventObject : testifyRuntimeContext
                            .getPrepareData()
                            .getExpectEventSet()
                            .getVirtualEventObjects()) {
                        if ((ArrayUtils.isEmpty(groupIds) && StringUtils.isEmpty(virtualEventObject.getNodeGroup()))
                                || ArrayUtils.contains(groupIds, virtualEventObject.getNodeGroup())) {
                            Map<String, List<Object>> events = EventContextHolder.getBizEvent();
                            DetailCollectUtils.appendAndLog("实际拦截消息列表" + ObjectUtil.toJson(events), logger);
                            String expEventCode = virtualEventObject.getEventCode();
                            String expTopic = virtualEventObject.getTopicId();
                            Object expPayLoad =
                                    virtualEventObject.getEventObject().getObject();
                            String flag = virtualEventObject.getIsExist();
                            String key = expEventCode + "|" + ((expTopic == null) ? "" : expTopic);

                            // 变量替换
                            replaceAllParam(expPayLoad, testifyRuntimeContext.getParamMap());

                            if (StringUtils.equals(flag, "N")) {
                                if (StringUtils.isBlank(expEventCode)) {
                                    Assert.assertTrue(
                                            events == null || events.isEmpty(), "Event detected, but none expectation");
                                } else if (events.get(key) != null) {
                                    Assert.assertTrue(
                                            false,
                                            "Unexpected event found, with " + "topicId: "
                                                    + virtualEventObject.getTopicId() + " eventCode"
                                                    + virtualEventObject.getEventCode() + " ");
                                }
                            } else if (StringUtils.equals(flag, "Y")) {
                                List<Object> payLoads = events.get(key);
                                boolean found = false;
                                if (payLoads == null || payLoads.isEmpty()) {
                                    Assert.assertTrue(
                                            false,
                                            "Specified event not found, with topic:"
                                                    + virtualEventObject.getTopicId() + " eventCode:"
                                                    + virtualEventObject.getEventCode() + "");
                                } else {
                                    for (Object obj : payLoads) {
                                        if (ObjectCompareUtil.matchObj(
                                                obj,
                                                expPayLoad,
                                                virtualEventObject
                                                        .getEventObject()
                                                        .getFlags(),
                                                testifyRuntimeContext.getParamMap())) {
                                            found = true;
                                            break;
                                        }
                                    }
                                    storeExpEventObj = expPayLoad;
                                }
                                if (!found) {
                                    Assert.assertTrue(
                                            false,
                                            "cannot find event matching the expected payload"
                                                    + "topicId: " + virtualEventObject.getTopicId()
                                                    + " eventCode" + virtualEventObject.getEventCode()
                                                    + "\n实际消息列表为 :" + ObjectUtil.toJson(payLoads)
                                                    + "\n期望消息为 :" + ObjectUtil.toJson(storeExpEventObj)
                                                    + "\n" + "错误信息为："
                                                    + ObjectCompareUtil.getReportStr()
                                                            .toString()
                                                    + "\n");

                                    // 打进日志统计中，方便直接排查
                                    DetailCollectUtils.appendAndLogColoredError(
                                            "消息对比失败，失败信息为:" + "topicId: "
                                                    + virtualEventObject.getTopicId() + " eventCode"
                                                    + virtualEventObject.getEventCode() + "\n实际消息列表为 :"
                                                    + ObjectUtil.toJson(payLoads) + "\n期望消息为 :"
                                                    + ObjectUtil.toJson(storeExpEventObj) + "\n"
                                                    + "错误信息为："
                                                    + ObjectCompareUtil.getReportStr()
                                                            .toString()
                                                    + "\n",
                                            logger);
                                }
                            }
                        }
                    }
                } else {
                    logger.info("Skip event check in rpc mode");
                }
            } else {
                logger.info("None event expectation");
            }
        } catch (Exception e) {
            throw new TestifyException("unknow exception raised while cheking events", e);
        }
    }

    /**
     * 清理准备数据
     *
     * @param extMapInfo
     * @param groupIds
     */
    public void clearDepData(Map<String, String> extMapInfo, String... groupIds) {
        testifyRuntimeContext.getDbDatasProcessor().updateDataSource(extMapInfo);
        try {
            // 清理导入的数据
            if (Objects.nonNull(testifyRuntimeContext.getPrepareData().getDepDataSet())) {
                DetailCollectUtils.appendAndLog(
                        "====================Cleaning up DB data preparations=============================", logger);
                replaceTableParam(
                        testifyRuntimeContext.getPrepareData().getDepDataSet().getVirtualTables(), groupIds);
                testifyRuntimeContext
                        .getDbDatasProcessor()
                        .cleanDBDatas(
                                testifyRuntimeContext
                                        .getPrepareData()
                                        .getDepDataSet()
                                        .getVirtualTables(),
                                groupIds);

            } else {
                logger.info("None DB preparation to clean");
            }
        } catch (Exception e) {
            throw new TestifyException("Unknown exception while cleaning DB preparations", e);
        }
    }

    /**
     * 清理期望数据
     *
     * @param extMapInfo
     * @param groupIds
     */
    public void clearExpectDBData(Map<String, String> extMapInfo, String... groupIds) {
        testifyRuntimeContext.getDbDatasProcessor().updateDataSource(extMapInfo);
        try {
            if (null != testifyRuntimeContext.getPrepareData().getExpectDataSet()) {
                logger.info("Cleaning up DB expectation data");
                replaceTableParam(
                        testifyRuntimeContext
                                .getPrepareData()
                                .getExpectDataSet()
                                .getVirtualTables(),
                        groupIds);
                testifyRuntimeContext
                        .getDbDatasProcessor()
                        .cleanDBDatas(
                                testifyRuntimeContext
                                        .getPrepareData()
                                        .getExpectDataSet()
                                        .getVirtualTables(),
                                groupIds);

            } else {
                logger.info("None DB expectation to clean up");
            }
        } catch (Exception e) {
            throw new TestifyException("unknown exception raised while cleaning DB expectations", e);
        }
    }

    /**
     * 组件清理操作，操作所有的组件
     */
    public void clearComponents() {
        for (TestifyTestBase c : testifyRuntimeContext.getTestifyComponents()) {
            c.clear(c.getTestifyRuntimeContext());
        }
    }

    /**
     * 组件清理阶段执行
     *
     * @param testTemplate
     */
    public void executeClearComponent(TestTemplate testTemplate, List<TestifyTestBase> excuteCompList) {
        // 执行清理command组件
        for (String cmd : testifyRuntimeContext.clearCommandList) {
            testTemplate.executeByCcil(caseDesc, cmd, "", "", "");
        }
        // 执行清理阶段的prepareData对象组件
        executeComponents(excuteCompList);
    }

    /**
     * 组件准备阶段执行
     *
     * @param testTemplate
     */
    public void excutePreCmdComponent(TestTemplate testTemplate) {
        // 执行准备command组件
        for (String cmd : testifyRuntimeContext.prepareCommandList) {
            //            testTemplate.executeByCcil(caseDesc, cmd, "", "", "");
        }
    }

    /**
     * 组件check阶段执行
     *
     * @param testTemplate
     */
    public void excuteCheckComponent(TestTemplate testTemplate, List<TestifyTestBase> excuteCompList) {
        // 执行check command组件
        for (String cmd : testifyRuntimeContext.checkCommandList) {
            testTemplate.executeByCcil(caseDesc, cmd, "", "", "");
        }
        // 执行check阶段的prepareData对象组件
        executeComponents(excuteCompList);
    }

    /**
     * 执行默认阶段组件
     *
     * @param testTemplate
     */
    public void excutedefaultCmdComponent(TestTemplate testTemplate) {
        // 执行默认 command组件
        for (String cmd : testifyRuntimeContext.defaultCommandList) {
            testTemplate.executeByCcil(caseDesc, cmd, "", "", "");
        }
    }

    public void replaceByFields(Object obj, Map<String, Object> varParaMap) {
        try {
            if (hasReplaced(obj)) {
                return;
            }
            Class<?> objType = obj.getClass();
            if (ObjectCompareUtil.isComparable(objType)) {
                if (obj instanceof String) {
                    if (((String) obj).startsWith("$")) {
                        String key = ((String) obj).replace("$", "");
                        if (varParaMap != null && varParaMap.get(key) != null) {
                            if (!(varParaMap.get(key) instanceof String)) {
                                return;
                            }
                            Field[] fieldsOfString = String.class.getDeclaredFields();
                            for (Field field : fieldsOfString) {
                                field.setAccessible(true);
                                field.set(obj, field.get(varParaMap.get(key)));
                            }
                        }
                    } else if (((String) obj).startsWith("@")) {
                        // 解析组件中的变量及组件
                        String str = (String) obj;
                        String callString = str;
                        if (StringUtils.contains(str, "$")) {
                            String query = StringUtils.substringAfter(str, "?");
                            callString = StringUtils.substringBefore(str, "?") + "?";
                            if (StringUtils.isNotBlank(query)) {
                                String[] pairs = StringUtils.split(query, "&");
                                for (String pair : pairs) {
                                    if (StringUtils.isBlank(pair)) {
                                        continue;
                                    }
                                    Object value = StringUtils.substringAfter(pair, "=");
                                    replaceByFields(value, varParaMap);
                                    callString =
                                            callString + StringUtils.substringBefore(pair, "=") + "=" + value + "&";
                                }
                                callString = StringUtils.substring(callString, 0, callString.length() - 1);
                            }
                        }
                        // 执行组件化参数
                        // 忽略列表的组件名不执行
                        if (!ignoreCallStringList.contains(callString)) {
                            String rs = (String) TestifyComponentUtil.run(callString);

                            logger.info("发现组件调用：" + callString + " 执行结果: " + rs);

                            Field[] fieldsOfString = String.class.getDeclaredFields();
                            for (Field field : fieldsOfString) {
                                field.setAccessible(true);
                                field.set(obj, field.get(rs));
                            }
                        }
                    }
                }
                return;
            } else if (objType.isArray()) {

                Object[] objArray = (Object[]) obj;
                if (objArray.length == 0) {
                    return;
                }
                for (int i = 0; i < objArray.length; i++) {
                    replaceByFields(objArray[i], varParaMap);
                }
                return;
            } else if (obj instanceof Map) {
                Map<Object, Object> objMap = (Map) obj;
                if (objMap.size() == 0) {
                    return;
                }
                for (Entry<Object, Object> entry : objMap.entrySet()) {
                    Object targetVal = entry.getValue();
                    replaceByFields(targetVal, varParaMap);
                }
            } else if (obj instanceof List) {
                List objList = (List) obj;

                if (objList.size() == 0) {
                    return;
                }
                for (int j = 0; j < objList.size(); j++) {

                    replaceByFields(objList.get(j), varParaMap);
                }
            } else {

                List<Field> fields = new ArrayList<Field>();

                for (Class<?> c = objType; c != null; c = c.getSuperclass()) {
                    for (Field field : c.getDeclaredFields()) {
                        int modifiers = field.getModifiers();
                        if (!Modifier.isStatic(modifiers)
                                && !Modifier.isTransient(modifiers)
                                && !fields.contains(field)) {
                            fields.add(field);
                        }
                    }
                }
                for (Field field : fields) {
                    try {
                        field.setAccessible(true);
                        Object objTarget = field.get(obj);
                        replaceByFields(objTarget, varParaMap);

                    } catch (IllegalArgumentException e) {
                        return;
                    } catch (IllegalAccessException e) {
                        return;
                    } catch (Exception e) {
                        return;
                    }
                }
            }
        } catch (Exception e) {
            return;
        }
    }

    public void replaceTableParam(List<VirtualTable> virtualTables, String... groupIds) {
        //        for (VirtualTable virtualTable : virtualTables) {
        //            if ((ArrayUtils.isEmpty(groupIds) && StringUtils.isEmpty(virtualTable.getNodeGroup()))
        //                    || ArrayUtils.contains(groupIds, virtualTable.getNodeGroup())) {
        //                for (Map<String, Object> row : virtualTable.getTableData()) {
        //                    for (String key : row.keySet()) {
        //                        if (String.valueOf(row.get(key)).contains("$")) {
        //                            row.put(key, VelocityUtil.evaluateString(actsRuntimeContext.getParamMap(),
        // String.valueOf(row.get
        //                            (key))));
        //                        } else if (String.valueOf(row.get(key)).startsWith("@")) {
        //                            //解析组件中的变量及组件
        //                            String str = String.valueOf(row.get(key));
        //                            String callString = str;
        //                            if (StringUtils.contains(str, "$")) {
        //                                String query = StringUtils.substringAfter(str, "?");
        //                                callString = StringUtils.substringBefore(str, "?") + "?";
        //                                if (StringUtils.isNotBlank(query)) {
        //                                    String[] pairs = StringUtils.split(query, "&");
        //                                    for (String pair : pairs) {
        //                                        if (StringUtils.isBlank(pair)) {
        //                                            continue;
        //                                        }
        //                                        Object value = StringUtils.substringAfter(pair, "=");
        //                                        replaceByFields(value, actsRuntimeContext.getParamMap());
        //                                        callString = callString
        //                                                + StringUtils.substringBefore(pair, "=") + "="
        //                                                + value + "&";
        //
        //                                    }
        //                                    callString = StringUtils.substring(callString, 0,
        //                                            callString.length() - 1);
        //
        //                                }
        //
        //                            }
        //                            //执行组件化参数
        //                            //忽略列表的组件名不执行
        //                            if (!ignoreCallStringList.contains(callString)) {
        //                                String rs = (String) ActsComponentUtil.run(callString);
        //
        //                                logger.info("发现组件调用：" + callString + " 执行结果: " + rs);
        //
        //                                row.put(key, rs);
        //
        //                            }
        //                        }
        //                    }
        //                }
        //            }
        //        }
    }

    /**
     * 判断对象是否被做过变量替换
     *
     * @param target
     * @return
     */
    public boolean hasReplaced(Object target) {
        for (Object object : scanList) {
            // 这里要用==判断引用是否相同
            if (target == object) {
                return true;
            }
        }
        scanList.add(target);
        return false;
    }

    public void replaceAllParam(Object obj, Map<String, Object> varParaMap) {

        List<Object> newObj = new ArrayList<Object>();
        if (obj instanceof List) {
            for (Object o : (List<?>) obj) {
                String className = o.getClass().getName();
                // SOFABoot3.x为javax.servlet包，SOFABoot4.x为jakarta.servlet包
                // 比对类名字符串同时兼容掉
                if (!className.endsWith("http.HttpServletRequest")
                        && !className.endsWith("http.HttpSession")
                        && !className.endsWith("http.HttpServletResponse")) {
                    newObj.add(o);
                }
            }
        } else {
            newObj.add(obj);
        }
        scanList = new ArrayList<Object>();
        replaceByFields(newObj, varParaMap);
    }

    public TestifyRuntimeContext getTestifyRuntimeContext() {
        return testifyRuntimeContext;
    }

    public void setTestifyRuntimeContext(TestifyRuntimeContext testifyRuntimeContext) {
        this.testifyRuntimeContext = testifyRuntimeContext;
    }

    /**
     * 自定义入参替换
     */
    public void prepareUserPara() {
        // 自定义入参的变量替换
        if (testifyRuntimeContext.getPrepareData().getVirtualParams() != null) {
            Map<String, VirtualObject> userParams =
                    testifyRuntimeContext.getPrepareData().getVirtualParams().getParams();

            if (userParams == null) {
                userParams = new HashMap();
            }

            replaceAllParam(userParams, testifyRuntimeContext.getParamMap());
            for (Entry<String, VirtualObject> entry : userParams.entrySet()) {
                Object val = entry.getValue() == null ? null : entry.getValue().getObject();
                testifyRuntimeContext.paramMap.put(entry.getKey(), val);
            }

            testifyRuntimeContext.getPrepareData().getVirtualParams().setParams(userParams);
        }
    }

    /**
     * 组件自定义入参替换
     */
    public void componentPrepareUserPara(VirtualComponent component, TestifyTestBase testBase) {
        // 自定义入参的变量替换
        if (component.getPrepareData().getVirtualParams() != null) {
            Map<String, VirtualObject> userParams =
                    component.getPrepareData().getVirtualParams().getParams();
            replaceAllParam(userParams, testBase.getTestifyRuntimeContext().getParamMap());

            for (Entry<String, VirtualObject> entry : userParams.entrySet()) {
                testBase.getTestifyRuntimeContext()
                        .paramMap
                        .put(entry.getKey(), entry.getValue().getObject());
            }

            testBase.getTestifyRuntimeContext()
                    .getPrepareData()
                    .getVirtualParams()
                    .setParams(userParams);
        }
    }

    /**
     * 初始化command组件
     *
     * @param component
     */
    public void initCommandCmp(VirtualComponent component) {

        String cmdCmpExcuteIndex = component.getCompExcuteIndex();
        if (StringUtils.equals(cmdCmpExcuteIndex, "无")) {
            testifyRuntimeContext.defaultCommandList.add(component.getCompExpression());
        } else if (StringUtils.equals(cmdCmpExcuteIndex, "数据清理阶段")) {
            testifyRuntimeContext.clearCommandList.add(component.getCompExpression());
        } else if (StringUtils.equals(cmdCmpExcuteIndex, "数据准备阶段")) {
            testifyRuntimeContext.prepareCommandList.add(component.getCompExpression());
        } else if (StringUtils.equals(cmdCmpExcuteIndex, "数据check阶段")) {
            testifyRuntimeContext.checkCommandList.add(component.getCompExpression());
        }
    }

    /**
     * 清除组件列表
     */
    public void clearComponentsList() {
        this.testifyRuntimeContext.testifyComponents.clear();
        this.testifyRuntimeContext.AfterCheckPreList.clear();
        this.testifyRuntimeContext.AfterClearPreList.clear();
        this.testifyRuntimeContext.AfterPreparePreList.clear();
        this.testifyRuntimeContext.BeforeCheckPreList.clear();
        this.testifyRuntimeContext.BeforeClearPreList.clear();
        this.testifyRuntimeContext.BeforePreparePreList.clear();
    }

    /**
     * 将结果对象放入Map
     */
    private void putResultToMap() {

        VirtualResult res = new VirtualResult(this.testifyRuntimeContext.getResultObj());
        VirtualEventSet event = CaseResultCollectUtil.buildExpEvents(
                EventContextHolder.getBizEvent(), this.getClass().getClassLoader());
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put(RunningContextEnum.VIRTUAL_RESULT.getCode(), res);
        resultMap.put(RunningContextEnum.VIRTUAL_EVENT.getCode(), event);
        resultMap.put(RunningContextEnum.RESULT.getCode(), this.testifyRuntimeContext.getResultObj());
        resultMap.put(RunningContextEnum.EXPECT_RESULT.getCode(), this.testifyRuntimeContext.getExpectResult());
        // 把入参中的实际入参摘取出来
        List<VirtualObject> inputArgs = this.testifyRuntimeContext.prepareData.getArgs().inputArgs;
        List<Object> args = new ArrayList<Object>();
        if (null != inputArgs) {
            for (VirtualObject virtualObject : inputArgs) {
                args.add(virtualObject.getObject());
            }
        }
        resultMap.put(RunningContextEnum.ARGS.getCode(), args);
        this.testifyRuntimeContext
                .getComponentsResultMap()
                .put(this.getTestifyRuntimeContext().getCaseId(), resultMap);
        this.testifyRuntimeContext.paramMap.putAll(resultMap);
    }

    // 忽略组件调用的组件名构造
    public void ignoreCallingList(List<String> list) {
        if (null == ignoreCallStringList || ignoreCallStringList.size() == 0) {
            ignoreCallStringList = new ArrayList<>();
        }
        ignoreCallStringList.addAll(list);
    }
}
