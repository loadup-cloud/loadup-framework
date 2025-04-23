package com.alipay.test.acts.component.handler;

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

import com.alipay.acts.utils.check.ObjectCompareUtil;
import com.alipay.test.acts.component.components.ActsComponentUtil;
import com.alipay.test.acts.component.event.EventContextHolder;
import com.alipay.test.acts.enums.RunningContextEnum;
import com.alipay.test.acts.exception.ActsTestException;
import com.alipay.test.acts.hbase.HBaseContext;
import com.alipay.test.acts.hbase.HBaseDO;
import com.alipay.test.acts.model.*;
import com.alipay.test.acts.runtime.ActsRuntimeContext;
import com.alipay.test.acts.runtime.ActsRuntimeContextThreadHold;
import com.alipay.test.acts.runtime.ComponentsActsRuntimeContextThreadHold;
import com.alipay.test.acts.support.TestTemplate;
import com.alipay.test.acts.template.ActsTestBase;
import com.alipay.test.acts.util.VelocityUtil;
import com.alipay.test.acts.utils.CaseResultCollectUtil;
import com.alipay.test.acts.utils.ComponentsProcessor;
import com.alipay.test.acts.utils.DetailCollectUtils;
import com.alipay.test.acts.utils.ObjectUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.Map.Entry;

/**
 * 处理各个测试数据
 *
 * @author tantian.wc
 * @version $Id: TestUnitHandler.java, v 0.1 2015年10月21日 下午7:43:31 tantian.wc
 * Exp $
 */
public class TestUnitHandler {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 储存扫描过的对象，防止死循环
     */
    protected List<Object> scanList;

    /**
     * itest 用例描述
     */
    private final String caseDesc = "NM_H_XXX_XXX";
    /**
     * 运行时上下文
     */
    ActsRuntimeContext actsRuntimeContext;

    /**
     * 存储当前topic，eventcode 对比的对象
     */
    public Object storeExpEventObj = null;

    /**
     * 该list内的组件名不调用组件执行
     */
    private static List<String> ignoreCallStringList;

    public TestUnitHandler() {
        this.actsRuntimeContext = ActsRuntimeContextThreadHold.getContext();
    }

    public TestUnitHandler(ActsRuntimeContext actsRuntimeContext) {

        this.actsRuntimeContext = actsRuntimeContext;
    }

    /**
     * 准备DB数据操作
     *
     * @param extMapInfo
     * @param groupIds
     */
    public void prepareDepData(Map<String, String> extMapInfo, String... groupIds) {
        actsRuntimeContext.getDbDatasProcessor().updateDataSource(extMapInfo);
        try {

            // 数据准备
            if (null != actsRuntimeContext.getPreparedDbData()
                    && 0 < actsRuntimeContext.getPreparedDbData().size()) {

                DetailCollectUtils.appendAndLog("Preparing DB data:", logger);
                for (VirtualTable table : actsRuntimeContext.getPreparedDbData()) {
                    if ((ArrayUtils.isEmpty(groupIds) && StringUtils.isEmpty(table.getNodeGroup()))
                            || ArrayUtils.contains(groupIds, table.getNodeGroup())) {
                        logger.info(table.getTableName());
                    }
                }
                replaceTableParam(actsRuntimeContext.getPreparedDbData(), groupIds);
                actsRuntimeContext.getDbDatasProcessor().importDepDBDatas(
                        actsRuntimeContext.getPreparedDbData(), groupIds);

            } else {
                logger.info("None DB preparation");
            }
        } catch (Exception e) {
            throw new ActsTestException(
                    "Unknown exception while preparing DB data. DB actual parameters:" + actsRuntimeContext.getPreparedDbData().toString(),
                    e);
        }
    }

    /**
     * mock 准备操作
     *
     * @param groupIds
     */
    public void prepareMocks(String... groupIds) {
        try {
//            // 外部服务mock
//            if (null != actsRuntimeContext.getPrepareData().getVirtualMockSet()
//                    && actsRuntimeContext.getPrepareData().getVirtualMockSet().getVirtualMockList() != null) {
//                // RPC模式不mock
//                if ("jvm".equalsIgnoreCase("TestUtils.getSofaConfig(test_xmode)")) {
//                    logger.info("Start to mock");
//                    for (VirtualMock virtualMock : actsRuntimeContext.getPrepareData()
//                            .getVirtualMockSet().getVirtualMockList()) {
//                        replaceAllParam(virtualMock.getMockResult(),
//                                actsRuntimeContext.getParamMap());
//                        if ((ArrayUtils.isEmpty(groupIds) && StringUtils.isEmpty(virtualMock
//                                .getNodeGroup()))
//                                || ArrayUtils.contains(groupIds, virtualMock.getNodeGroup())) {
//                            DetailCollectUtils.appendAndLog(
//                                    "start mock[" + virtualMock.getContainer() + "] beanName:[ "
//                                            + virtualMock.getBeanName() + "] methodName:["
//                                            + virtualMock.getMethodName() + "]", logger);
////                            actsRuntimeContext.mocks.put(virtualMock, MockUtil.doMock(virtualMock, null));
//                        }
//                    }
//                } else {
//                    logger.info("running in rpc mode, no need to mock");
//                }
//            } else {
//                logger.info("None mock target");
//            }
        } catch (Exception e) {
            throw new ActsTestException("unknown exception while mock prepration", e);
        }
    }

    /**
     * 组件初始化
     */
    public void initComponents() {
        try {
            //清理各阶段组件列表
            clearComponentsList();
            // 初始化组件
            if (actsRuntimeContext.getPrepareData() instanceof PrepareData) {
                PrepareData prepareData = actsRuntimeContext.getPrepareData();
                if (null != prepareData.getVirtualComponentSet()) {
                    for (VirtualComponent virtualComponent : prepareData.getVirtualComponentSet()
                            .getComponents()) {
                        // 初始化command组件
                        if (virtualComponent.getPrepareData() == null) {
                            initCommandCmp(virtualComponent);
                        } else {
                            try {
                                Class<?> actsTestClazz = getClass().getClassLoader().loadClass(
                                        virtualComponent.getComponentClass());
                                if (ActsTestBase.class.isAssignableFrom(actsTestClazz)) {
                                    ActsTestBase actsTest = (ActsTestBase) actsTestClazz
                                            .newInstance();
//                                    actsTest.injectBundleContext(actsRuntimeContext
//                                            .getBundleContext());
                                    actsTest.initRuntimeContext(virtualComponent.getComponentId(),
                                            virtualComponent.getPrepareData(), true);
                                    actsTest.initTestUnitHandler();
                                    // 组件自定义参数配置
                                    componentPrepareUserPara(virtualComponent, actsTest);

                                    // 组件化参数替换
                                    actsTest.getActsRuntimeContext().paramMap
                                            .putAll(actsRuntimeContext.getParamMap());
                                    actsRuntimeContext.paramMap.putAll(actsTest
                                            .getActsRuntimeContext().getParamMap());
                                    //放入默认分组，为了能够进行组件化的一些共有操作
                                    actsRuntimeContext.getActsComponents().add(actsTest);

                                    //进行组件分类操作，当前支持组件化脚本在当前流程的清理，准备，check阶段执行
                                    ComponentsProcessor.getIndexForPreComp(actsRuntimeContext,
                                            actsTest, virtualComponent);
                                }
                            } catch (ClassNotFoundException e) {
                                throw new ActsTestException("cot find sub test class: "
                                        + virtualComponent.getComponentClass(),
                                        e);
                            }
                        }
                    }
                } else {
                    logger.info("no sub test");
                }
            }
        } catch (Exception e) {
            throw new ActsTestException("unknown exception while run sub tests", e);

        }
    }

    /**
     * 组件执行逻辑，通过传入组件进行执行
     *
     * @param excuteCompList
     */
    public void executeComponents(List<ActsTestBase> excuteCompList) {
        for (ActsTestBase actsComponents : excuteCompList) {
            //打印日志
            DetailCollectUtils.appendAndLog(
                    "=============================开始执行组件caseId="
                            + actsComponents.getActsRuntimeContext().getCaseId() + ":"
                            + actsComponents.getActsRuntimeContext().prepareData.getDescription()
                            + "=================", logger);
            // 先将父类上下文放一次，因为可能上一个组件在最后做了put，该组件要使用就必须放进去
            actsComponents.getActsRuntimeContext().paramMap
                    .putAll(actsRuntimeContext.getParamMap());
            //组件的中的组件初始化及清理
            actsComponents.initComponentsBeforeTest(actsComponents.getActsRuntimeContext());

            //组件的核心测试调用
            actsComponents.beforeActsTest(actsComponents.getActsRuntimeContext());
            actsComponents.beforeActsTest(actsComponents.getActsRuntimeContext().paramMap);
            actsComponents.executeComponents(actsComponents.getActsRuntimeContext());
            actsComponents.prepare(actsComponents.getActsRuntimeContext());
            actsComponents.execute(actsComponents.getActsRuntimeContext());
            actsComponents.check(actsComponents.getActsRuntimeContext());
            actsComponents.afterActsTest(actsComponents.getActsRuntimeContext());
            // 组件化参数替换，这个地方做这一步是为了将各个组件之间的结果对象打通，可以再其他组件中用上一个的结果值等，需要在各自组件中put进来
            actsRuntimeContext.paramMap
                    .putAll(actsComponents.getActsRuntimeContext().getParamMap());

            DetailCollectUtils.appendAndLog("=============================组件执行结束caseId="
                    + actsComponents.getActsRuntimeContext().getCaseId()
                    + "=========================================", logger);
        }
    }

    /**
     * 测试方法执行
     */
    public void execute() {
        try {
            List<Object> inputParams = actsRuntimeContext.getPrepareData().getArgs().getInputArgs();
            replaceAllParam(inputParams, actsRuntimeContext.getParamMap());

            Object[] paramObjs = null;
            if (inputParams != null) {
                paramObjs = inputParams.toArray(new Object[0]);
            }

            if (actsRuntimeContext.getTestedMethod() != null) {
                Object resultObj = null;
                try {
                    DetailCollectUtils.appendAndLog("Start to invoke method:"
                            + actsRuntimeContext.getTestedMethod()
                            .getName() + " parameters:", logger);
                    if (inputParams == null) {
                        logger.info("null");
                    } else {
                        DetailCollectUtils.appendDetail(inputParams.toString());
                    }
                    resultObj = actsRuntimeContext.getTestedMethod().invoke(
                            actsRuntimeContext.getTestedObj(), paramObjs);

                    try {
                        DetailCollectUtils.appendAndLog(
                                "Invocation result: " + ObjectUtil.toJson(resultObj), logger);
                    } catch (Exception e) {
                        DetailCollectUtils.appendAndLog("Invocation result: " + resultObj, logger);
                    }

                } catch (Exception e) {
                    Object exception = e.getCause() == null ? (e.getMessage() == null ? e : new ActsTestException(e.getMessage(), e)) : e.getCause();
                    DetailCollectUtils.appendAndLog("Exception Invocation: " + exception.toString(), logger);
                    actsRuntimeContext.setExceptionObj(exception);
                }
                actsRuntimeContext.setResultObj(resultObj);
                //将结果及消息放入组件Map
                putResultToMap();
            } else {
                logger.info("Test method not found, interrupt invocation");
            }
        } catch (Exception e) {
            throw new ActsTestException("unknown exception while invocation", e);
        }

    }

    /**
     * 异常结果校验
     */
    public void checkException() {

        // 异常对比
        if (actsRuntimeContext.getPrepareData().getExpectException() != null
                && actsRuntimeContext.getPrepareData().getExpectException().getExpectException() != null
                && actsRuntimeContext.getPrepareData().getExpectException().getExpectException()
                .getObject() != null) {
            if (actsRuntimeContext.getExceptionObj() != null) {
                logger.info("Checking Exception");
                Object expectedExp = actsRuntimeContext.getExceptionObj();
                VirtualException ve = actsRuntimeContext.getPrepareData().getExpectException();
                Object actualExp = ve.getExpectExceptionObject();
                ObjectCompareUtil.compare(expectedExp, actualExp, ve.getVirtualObject().flags,
                        actsRuntimeContext.paramMap);

            } else {
                throw new AssertionError("None Exception raised during invocation, but expects one.");

            }
        } else {
            if (actsRuntimeContext.getExceptionObj() != null) {
                throw new ActsTestException("unknown exception raised during invocation",
                        (Exception) actsRuntimeContext.getExceptionObj());
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

            ComponentsActsRuntimeContextThreadHold.setContext(this.actsRuntimeContext);
            actsRuntimeContext.getDbDatasProcessor().updateDataSource(extMapInfo);
            // 做DB期望值比对
            if (null != actsRuntimeContext.getPrepareData().getExpectDataSet()
                    && actsRuntimeContext.getPrepareData().getExpectDataSet().getVirtualTables() != null) {
                logger.info("Checking DB, tables checked");
                for (VirtualTable virtualTable : actsRuntimeContext.getPrepareData()
                        .getExpectDataSet().getVirtualTables()) {
                    if ((ArrayUtils.isEmpty(groupIds) && StringUtils.isEmpty(virtualTable
                            .getNodeGroup()))
                            || ArrayUtils.contains(groupIds, virtualTable.getNodeGroup())) {
                        logger.info(virtualTable.getTableName());
                    }
                }
                replaceTableParam(actsRuntimeContext.getPrepareData().getExpectDataSet()
                        .getVirtualTables());
                actsRuntimeContext.getDbDatasProcessor().compare2DBDatas(
                        actsRuntimeContext.getPrepareData().getExpectDataSet().getVirtualTables(),
                        groupIds);
            } else {
                logger.info("None DB expectation");
            }
        } catch (Exception e) {
            throw new ActsTestException("unknown exception while checking DB", e);
        }
    }

    /**
     * 结果对象校验
     */
    public void checkExpectResult() {
        try {
            // 做结果期望值比对
            if (actsRuntimeContext.getPrepareData().getExpectResult() != null
                    && actsRuntimeContext.getPrepareData().getExpectResult().getResult().getObject() != null) {
                DetailCollectUtils.appendAndLog("Checking invocation result:", logger);
                VirtualObject expect = actsRuntimeContext.getPrepareData().getExpectResult()
                        .getVirtualObject();
                Object actual = actsRuntimeContext.getResultObj();
                try {

                    logger.info("\nexpect:" + ObjectUtil.toJson(expect.getObject()) + "\nactual:"
                            + ObjectUtil.toJson(actual));
                } catch (Exception e) {
                    logger.error("\nexpect:" + expect + "\nactual:" + actual);
                    //                    LogUtil.printColoredError(logger, "\nexpect:" + expect + "\nactual:" + actual,
                    //                        e);
                }
                ObjectCompareUtil.compare(actual, expect.getObject(), expect.getFlags(),
                        actsRuntimeContext.paramMap);
            } else {
                logger.info("None result expectation");
            }
        } catch (Exception e) {
            throw new ActsTestException("unknown exception while checking invocation result", e);
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
            if (!actsRuntimeContext.getPrepareData().getExpectEventSet().getVirtualEventObjects()
                    .isEmpty()) {
                if ("jvm".equalsIgnoreCase("TestUtils.getSofaConfig(test_xmode)")) {
                    Map<String, List<Object>> uEventList = EventContextHolder.getBizEvent();
                    for (VirtualEventObject virtualEventObject : actsRuntimeContext
                            .getPrepareData().getExpectEventSet().getVirtualEventObjects()) {
                        if ((ArrayUtils.isEmpty(groupIds) && StringUtils.isEmpty(virtualEventObject
                                .getNodeGroup()))
                                || ArrayUtils.contains(groupIds, virtualEventObject.getNodeGroup())) {
                            Map<String, List<Object>> events = EventContextHolder.getBizEvent();
                            DetailCollectUtils.appendAndLog("实际拦截消息列表" + ObjectUtil.toJson(events),
                                    logger);
                            String expEventCode = virtualEventObject.getEventCode();
                            String expTopic = virtualEventObject.getTopicId();
                            Object expPayLoad = virtualEventObject.getEventObject().getObject();
                            String flag = virtualEventObject.getIsExist();
                            String key = expEventCode + "|" + ((expTopic == null) ? "" : expTopic);

                            //变量替换
                            replaceAllParam(expPayLoad, actsRuntimeContext.getParamMap());

                            if (StringUtils.equals(flag, "N")) {
                                if (StringUtils.isBlank(expEventCode)) {
                                    Assert.assertTrue(events == null || events.isEmpty(),
                                            "Event detected, but none expectation");
                                } else if (events.get(key) != null) {
                                    Assert.assertTrue(false,
                                            "Unexpected event found, with " + "topicId: "
                                                    + virtualEventObject.getTopicId() + " eventCode"
                                                    + virtualEventObject.getEventCode() + " ");
                                }
                            } else if (StringUtils.equals(flag, "Y")) {
                                List<Object> payLoads = events.get(key);
                                boolean found = false;
                                if (payLoads == null || payLoads.isEmpty()) {
                                    Assert.assertTrue(false,
                                            "Specified event not found, with topic:"
                                                    + virtualEventObject.getTopicId() + " eventCode:"
                                                    + virtualEventObject.getEventCode() + "");
                                } else {
                                    for (Object obj : payLoads) {
                                        if (ObjectCompareUtil.matchObj(obj, expPayLoad,
                                                virtualEventObject.getEventObject().getFlags(),
                                                actsRuntimeContext.getParamMap())) {
                                            found = true;
                                            break;
                                        }
                                    }
                                    storeExpEventObj = expPayLoad;
                                }
                                if (!found) {
                                    Assert.assertTrue(false,
                                            "cannot find event matching the expected payload"
                                                    + "topicId: " + virtualEventObject.getTopicId()
                                                    + " eventCode" + virtualEventObject.getEventCode()
                                                    + "\n实际消息列表为 :" + ObjectUtil.toJson(payLoads)
                                                    + "\n期望消息为 :" + ObjectUtil.toJson(storeExpEventObj)
                                                    + "\n" + "错误信息为："
                                                    + ObjectCompareUtil.getReportStr().toString()
                                                    + "\n");

                                    //打进日志统计中，方便直接排查
                                    DetailCollectUtils.appendAndLogColoredError(
                                            "消息对比失败，失败信息为:" + "topicId: "
                                                    + virtualEventObject.getTopicId() + " eventCode"
                                                    + virtualEventObject.getEventCode() + "\n实际消息列表为 :"
                                                    + ObjectUtil.toJson(payLoads) + "\n期望消息为 :"
                                                    + ObjectUtil.toJson(storeExpEventObj) + "\n"
                                                    + "错误信息为："
                                                    + ObjectCompareUtil.getReportStr().toString()
                                                    + "\n", logger);
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
            throw new ActsTestException("unknow exception raised while cheking events", e);
        }

    }

    /**
     * 清理准备数据
     *
     * @param extMapInfo
     * @param groupIds
     */
    public void clearDepData(Map<String, String> extMapInfo, String... groupIds) {
        actsRuntimeContext.getDbDatasProcessor().updateDataSource(extMapInfo);
        try {
            // 清理导入的数据
            if (Objects.nonNull(actsRuntimeContext.getPrepareData().getDepDataSet())) {
                DetailCollectUtils.appendAndLog("====================Cleaning up DB data preparations=============================", logger);
                replaceTableParam(actsRuntimeContext.getPrepareData().getDepDataSet()
                        .getVirtualTables(), groupIds);
                actsRuntimeContext.getDbDatasProcessor().cleanDBDatas(
                        actsRuntimeContext.getPrepareData().getDepDataSet().getVirtualTables(),
                        groupIds);

            } else {
                logger.info("None DB preparation to clean");
            }
        } catch (Exception e) {
            throw new ActsTestException("Unknown exception while cleaning DB preparations", e);
        }
    }

    /**
     * 清理期望数据
     *
     * @param extMapInfo
     * @param groupIds
     */
    public void clearExpectDBData(Map<String, String> extMapInfo, String... groupIds) {
        actsRuntimeContext.getDbDatasProcessor().updateDataSource(extMapInfo);
        try {
            if (null != actsRuntimeContext.getPrepareData().getExpectDataSet()) {
                logger.info("Cleaning up DB expectation data");
                replaceTableParam(actsRuntimeContext.getPrepareData().getExpectDataSet()
                        .getVirtualTables(), groupIds);
                actsRuntimeContext.getDbDatasProcessor().cleanDBDatas(
                        actsRuntimeContext.getPrepareData().getExpectDataSet().getVirtualTables(),
                        groupIds);

            } else {
                logger.info("None DB expectation to clean up");
            }
        } catch (Exception e) {
            throw new ActsTestException("unknown exception raised while cleaning DB expectations",
                    e);
        }
    }

    /**
     * 组件清理操作，操作所有的组件
     */
    public void clearComponents() {
        for (ActsTestBase actsComponents : actsRuntimeContext.getActsComponents()) {
            actsComponents.clear(actsComponents.getActsRuntimeContext());
        }
    }

    /**
     * mock 清理操作
     *
     * @param groupIds
     */
    public void clearMocks(String... groupIds) {
        try {
            // 清除mock
//            if (null != actsRuntimeContext.getPrepareData().getVirtualMockSet()) {
//                DetailCollectUtils.appendAndLog("---------Cleaning up mock-------------", logger);
//                for (VirtualMock virtualMock : actsRuntimeContext.mocks.keySet()) {
//                    if (ArrayUtils.isEmpty(groupIds)
//                            || ArrayUtils.contains(groupIds, virtualMock.getNodeGroup())) {
//                        actsRuntimeContext.mocks.get(virtualMock).removeMock();
//                    }
//                }

//            } else {
//                logger.info("None mock to clean up");
//            }
        } catch (Exception e) {
            throw new ActsTestException("Unknown exception raised while cleaning mock", e);
        }
    }

    /**
     * 组件清理阶段执行
     *
     * @param testTemplate
     */
    public void excuteClearComponent(TestTemplate testTemplate, List<ActsTestBase> excuteCompList) {
        // 执行清理command组件
        for (String cmd : actsRuntimeContext.clearCommandList) {
            testTemplate.executeByCcil(caseDesc, cmd, "", "", "");
        }
        //执行清理阶段的prepareData对象组件
        executeComponents(excuteCompList);

    }

    /**
     * 组件准备阶段执行
     *
     * @param testTemplate
     */
    public void excutePreCmdComponent(TestTemplate testTemplate) {
        // 执行准备command组件
        for (String cmd : actsRuntimeContext.prepareCommandList) {
//            testTemplate.executeByCcil(caseDesc, cmd, "", "", "");
        }

    }

    /**
     * 组件check阶段执行
     *
     * @param testTemplate
     */
    public void excuteCheckComponent(TestTemplate testTemplate, List<ActsTestBase> excuteCompList) {
        // 执行check command组件
        for (String cmd : actsRuntimeContext.checkCommandList) {
            testTemplate.executeByCcil(caseDesc, cmd, "", "", "");
        }
        //执行check阶段的prepareData对象组件
        executeComponents(excuteCompList);

    }

    /**
     * 执行默认阶段组件
     *
     * @param testTemplate
     */
    public void excutedefaultCmdComponent(TestTemplate testTemplate) {
        // 执行默认 command组件
        for (String cmd : actsRuntimeContext.defaultCommandList) {
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
                        //解析组件中的变量及组件
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
                                    callString = callString
                                            + StringUtils.substringBefore(pair, "=") + "="
                                            + value + "&";

                                }
                                callString = StringUtils.substring(callString, 0,
                                        callString.length() - 1);

                            }

                        }
                        //执行组件化参数
                        //忽略列表的组件名不执行
                        if (!ignoreCallStringList.contains(callString)) {
                            String rs = (String) ActsComponentUtil.run(callString);

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
                        if (!Modifier.isStatic(modifiers) && !Modifier.isTransient(modifiers)
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
//                            row.put(key, VelocityUtil.evaluateString(actsRuntimeContext.getParamMap(), String.valueOf(row.get(key))));
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

    public ActsRuntimeContext getActsRuntimeContext() {
        return actsRuntimeContext;

    }

    public void setActsRuntimeContext(ActsRuntimeContext actsRuntimeContext) {
        this.actsRuntimeContext = actsRuntimeContext;
    }

    /**
     * 自定义入参替换
     */
    public void prepareUserPara() {
        // 自定义入参的变量替换
        if (actsRuntimeContext.getPrepareData().getVirtualParams() != null) {
            Map<String, VirtualObject> userParams = actsRuntimeContext.getPrepareData()
                    .getVirtualParams().getParams();

            if (userParams == null) {
                userParams = new HashMap();
            }

            replaceAllParam(userParams, actsRuntimeContext.getParamMap());
            for (Entry<String, VirtualObject> entry : userParams.entrySet()) {
                Object val = entry.getValue() == null ? null : entry.getValue().getObject();
                actsRuntimeContext.paramMap.put(entry.getKey(), val);

            }

            actsRuntimeContext.getPrepareData().getVirtualParams().setParams(userParams);
        }
    }

    /**
     * 组件自定义入参替换
     */
    public void componentPrepareUserPara(VirtualComponent component, ActsTestBase actsTest) {
        // 自定义入参的变量替换
        if (component.getPrepareData().getVirtualParams() != null) {
            Map<String, VirtualObject> userParams = component.getPrepareData().getVirtualParams()
                    .getParams();
            replaceAllParam(userParams, actsTest.getActsRuntimeContext().getParamMap());

            for (Entry<String, VirtualObject> entry : userParams.entrySet()) {
                actsTest.getActsRuntimeContext().paramMap.put(entry.getKey(), entry.getValue()
                        .getObject());

            }

            actsTest.getActsRuntimeContext().getPrepareData().getVirtualParams()
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
            actsRuntimeContext.defaultCommandList.add(component.getCompExpression());
        } else if (StringUtils.equals(cmdCmpExcuteIndex, "数据清理阶段")) {
            actsRuntimeContext.clearCommandList.add(component.getCompExpression());
        } else if (StringUtils.equals(cmdCmpExcuteIndex, "数据准备阶段")) {
            actsRuntimeContext.prepareCommandList.add(component.getCompExpression());
        } else if (StringUtils.equals(cmdCmpExcuteIndex, "数据check阶段")) {
            actsRuntimeContext.checkCommandList.add(component.getCompExpression());

        }
    }

    /**
     * 准备HBASE数据
     */
    public void prepareHbaseData() {
        try {

            VirtualObject param = actsRuntimeContext.getPrepareData().getVirtualParams()
                    .getParams().get("HBaseContext");

            HBaseContext hbaseContext = (HBaseContext) param.getObject();
            for (HBaseDO hbaseDO : hbaseContext.getHbasePrepareList()) {
                if (hbaseDO.getTimestamp() == null) {
                    //如果没有设置时间戳，默认为当前时间
                    Date nowTime = new Date();
                    long timestamp = nowTime.getTime();
                    hbaseDO.setTimestamp(timestamp);
                }
//                Put put = new Put(Bytes.toBytes(hbaseDO.getRowKey()));
//                put.add(Bytes.toBytes(hbaseDO.getFamily()), Bytes.toBytes(hbaseDO.getQualifier()),
//                        hbaseDO.getTimestamp(), Bytes.toBytes(hbaseDO.getData()));
//                HBaseClient.insert(hbaseDO.getTableName(), put);

            }

        } catch (Exception e) {
            logger.warn("hbase prepare failed or no hbase context");
        }
    }

    /**
     * check HBASE 数据
     */
    public void checkExpectHbaseData() {

        //获取校验结果
        VirtualObject param = actsRuntimeContext.getPrepareData().getVirtualParams().getParams()
                .get("HBaseContext");
        //为空证明非HBASE，调过
        if (param == null) {
            return;
        }
        HBaseContext hbaseContext = (HBaseContext) param.getObject();
//        for (HBaseDO hbaseDO : hbaseContext.getHbaseCheckList()) {
//            Get get = new Get(Bytes.toBytes(hbaseDO.getRowKey()));
//            Result result = HBaseClient.query(hbaseDO.getTableName(), get);
//            String value = Bytes.toString(result.getValue(Bytes.toBytes(hbaseDO.getFamily()),
//                    Bytes.toBytes(hbaseDO.getQualifier())));
//            if (StringUtils.equals(hbaseDO.getFlag(), "R")) {
//                Pattern pattern = Pattern.compile(hbaseDO.getData());
//                Matcher matcher = pattern.matcher(value);
//                Assert.assertTrue(matcher.matches(), "Hbase值校验失败:" + hbaseDO.summary());
//            } else {
//                Assert.assertEquals(value, hbaseDO.getData(), "Hbase值校验失败:" + hbaseDO.summary());
//            }
//
//        }
    }

    /**
     * 清除组件列表
     */
    public void clearComponentsList() {
        this.actsRuntimeContext.actsComponents.clear();
        this.actsRuntimeContext.AfterCheckPreList.clear();
        this.actsRuntimeContext.AfterClearPreList.clear();
        this.actsRuntimeContext.AfterPreparePreList.clear();
        this.actsRuntimeContext.BeforeCheckPreList.clear();
        this.actsRuntimeContext.BeforeClearPreList.clear();
        this.actsRuntimeContext.BeforePreparePreList.clear();
    }

    /**
     * 将结果对象放入Map
     */
    private void putResultToMap() {

        VirtualResult res = new VirtualResult(this.actsRuntimeContext.getResultObj());
        VirtualEventSet event = CaseResultCollectUtil.buildExpEvents(
                EventContextHolder.getBizEvent(), this.getClass().getClassLoader());
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put(RunningContextEnum.VIRTUAL_RESULT.getCode(), res);
        resultMap.put(RunningContextEnum.VIRTUAL_EVENT.getCode(), event);
        resultMap.put(RunningContextEnum.RESULT.getCode(), this.actsRuntimeContext.getResultObj());
        resultMap.put(RunningContextEnum.EXPECT_RESULT.getCode(), this.actsRuntimeContext.getExpectResult());
        //把入参中的实际入参摘取出来
        List<VirtualObject> inputArgs = this.actsRuntimeContext.prepareData.getArgs().inputArgs;
        List<Object> args = new ArrayList<Object>();
        if (null != inputArgs) {
            for (VirtualObject virtualObject : inputArgs) {
                args.add(virtualObject.getObject());
            }
        }
        resultMap.put(RunningContextEnum.ARGS.getCode(), args);
        this.actsRuntimeContext.getComponentsResultMap().put(
                this.getActsRuntimeContext().getCaseId(), resultMap);
        this.actsRuntimeContext.paramMap.putAll(resultMap);


    }

    //忽略组件调用的组件名构造
    public void ignoreCallingList(List<String> list) {
        if (null == ignoreCallStringList || ignoreCallStringList.size() == 0) {
            ignoreCallStringList = new ArrayList<>();
        }
        ignoreCallStringList.addAll(list);
    }

}
