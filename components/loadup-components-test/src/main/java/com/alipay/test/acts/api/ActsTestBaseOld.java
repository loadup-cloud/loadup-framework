///**
// * Alipay.com Inc.
// * Copyright (c) 2004-2015 All Rights Reserved.
// */
//package com.alipay.test.acts.api;
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
//import java.io.File;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Map.Entry;
//
//
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//import org.testng.Assert;
//import org.testng.annotations.AfterClass;
//import org.testng.annotations.AfterMethod;
//import org.testng.annotations.BeforeMethod;
//import org.testng.annotations.Listeners;
//
//import com.alipay.test.acts.constant.ActsYamlConstants;
//import com.alipay.test.acts.context.ActsCaseContextHolder;
//import com.alipay.test.acts.context.ActsSuiteContextHolder;
//import com.alipay.test.acts.driver.AtsDataDriver;
//import com.alipay.test.acts.driver.listener.TestNGBeforeTestConfig;
//import com.alipay.test.acts.log.ActsLogUtil;
//import com.alipay.test.acts.mock.AtsMockUtil;
//import com.alipay.test.acts.msg.UniformEventUtil;
//import com.alipay.test.acts.yaml.YamlCheckPoint;
//import com.alipay.test.acts.yaml.YamlTestCase;
//import com.alipay.test.acts.yaml.YamlTestUtil;
//import com.alipay.test.acts.yaml.cpUnit.BaseCPUnit;
//import com.alipay.test.acts.yaml.cpUnit.DataBaseCPUnit;
//import com.alipay.test.acts.yaml.cpUnit.ListDataBaseCPUnit;
//import com.alipay.test.acts.yaml.enums.CPUnitTypeEnum;
//
///**
// * ??轰??Yaml???浠剁??妗???跺?虹被
// *
// * @author baishuo.lp
// * @version $Id: ActsTestBase.java, v 0.1 2015骞?7???13??? 涓????12:02:39 baishuo.lp Exp $
// */
//@Listeners(value = TestNGBeforeTestConfig.class)
//public class ActsTestBaseOld extends AtsDataDriver {
//
//    private static final Log log = LogFactory.getLog(ActsTestBaseOld.class);
//
//    static {
//        initTestArtifact("acts-common-,reflections-,guava-,javassist-,cloning-");
//    }
//
//    /**
//     * mini_bundle:just load bundles from "mini_bundle"
//     *
//     * @author mokong
//     */
//
//    /**
//     * 浠?榛?璁?CP??瑰??澶???版??
//     *
//     * @return ???澶?瀵硅薄Map
//     */
//    protected Map<String, Object> prepare() {
//        return prepare("insert");
//    }
//
//    /**
//     * ??轰??CP??瑰??绉版?ュ??澶???版??
//     *
//     * @param checkPointName
//     * @return ???澶?瀵硅薄Map
//     */
//    protected Map<String, Object> prepare(String checkPointName) {
//        Map<String, Object> result = new HashMap<String, Object>();
//
//        // 1. ???澶?COMMON??哄????版??
//        if (!ActsCaseContextHolder.get().isNeedLoadCommonSection()) {
//            ActsCaseContextHolder.get().setNeedLoadCommonSection(true);
//            result.putAll(prepare(ActsYamlConstants.COMMONKEY, ActsYamlConstants.COMMONKEY));
//        }
//
//        // 2. ???澶???ㄤ????版??
//        String caseId = ActsCaseContextHolder.get().getCaseId();
//        result.putAll(prepare(caseId, checkPointName));
//        return result;
//    }
//
//    /**
//     * 浠?榛?璁?CP??规?￠????版??
//     *
//     * @param args
//     *            寰???￠??瀵硅薄Map
//     */
//    protected void check(Map<String, Object> args) {
//        check("check", args);
//    }
//
//    /**
//     * 浠?榛?璁?CP??规?￠????版??
//     *
//     * @param args
//     *            寰???￠??瀵硅薄搴????
//     */
//    protected void check(Object... args) {
//        check("check", getObjMap(args));
//    }
//
//    /**
//     * 浠????瀹?CP??规?￠????版??
//     *
//     * @param checkPointName
//     * @param args
//     *            寰???￠??瀵硅薄搴????
//     */
//    protected void check(String checkPointName, Object... args) {
//        check(checkPointName, getObjMap(args));
//    }
//
//    /**
//     * 浠????瀹?CP??规?￠????版??
//     *
//     * @param checkPointName
//     * @param args
//     *            寰???￠??瀵硅薄Map
//     */
//    protected void check(String checkPointName, Map<String, Object> args) {
//        String caseId = ActsCaseContextHolder.get().getCaseId();
//
//        YamlTestCase testCase;
//        if (YamlTestUtil.isSingleYaml()) {
//            testCase = ActsSuiteContextHolder.get().getYamlTestData().getTestCase(caseId);
//        } else {
//            testCase = ActsCaseContextHolder.get().getYamlTestData().getTestCase(caseId);
//        }
//        if (testCase != null) {
//            YamlCheckPoint checkPoint = testCase.getCheckPoint(checkPointName);
//            if (checkPoint != null) {
//                // 棰?娓????涓?涓????涓????璇???????
//                ActsCaseContextHolder.get().getProcessErrorLog().clear();
//
//                ActsLogUtil.addProcessLog("\n==============??版????￠????ュ??============\n");
//                for (Entry<String, BaseCPUnit> entry : checkPoint.getCheckPointUnitMap().entrySet()) {
//                    BaseCPUnit unit = entry.getValue();
//                    if (args != null
//                        && (unit == null || unit.getUnitType() == CPUnitTypeEnum.OBJECT)) {
//                        Object obj = args.get(entry.getKey());
//                        ActsLogUtil.addProcessLog("寮?濮???￠??瀵硅薄:" + entry.getKey());
//                        ActsLogUtil.addProcessLog(obj);
//                        if (unit == null && obj == null) {
//                            continue;
//                        } else if (unit == null || obj == null) {
//                            // 蹇?椤绘?惧?ㄨ?????姣?杈?锛????涓?processor?????芥?夸????伴?????
//                            ActsLogUtil.error(logger, entry.getKey()
//                                                      + ((unit == null) ? "棰???????" : "寰?姣?杈?瀵硅薄")
//                                                      + "涓虹┖");
//                        } else {
//                            YamlTestUtil.checkObj(unit, obj);
//                        }
//                        args.remove(entry.getKey());
//                    } else {
//                        switch (unit.getUnitType()) {
//                            case DATABASE:
//                                if (unit instanceof DataBaseCPUnit) {
//                                    YamlTestUtil.checkDB(unit);
//                                } else if (unit instanceof ListDataBaseCPUnit) {
//                                    for (DataBaseCPUnit dbUnit : ((ListDataBaseCPUnit) unit)
//                                        .getDataList()) {
//                                        YamlTestUtil.checkDB(dbUnit);
//                                    }
//                                } else {
//                                    Assert.fail(unit + "?????ュ??甯?");
//                                }
//                                break;
//                            case GROUP:
//                                YamlTestUtil.groupCheckDB(unit);
//                                break;
//                            case MESSAGE:
//                                YamlTestUtil.checkMsg(unit);
//                                break;
//                            default:
//                                ActsLogUtil.fail(logger,
//                                    "???澶???版??涓???????褰????绫诲??" + unit.getUnitType());
//                                break;
//                        }
//                    }
//                }
//                if (!args.isEmpty()) {
//                    ActsLogUtil.error(log, "??ュ??涓?瀛???ㄦ??姣?杈?瀵硅薄");
//                }
//            } else {
//                ActsLogUtil.warn(log, "褰??????ㄤ??娌℃??瀵瑰??CP???" + checkPointName);
//            }
//        } else {
//            ActsLogUtil.warn(log, "褰??????ㄤ?????Yaml涓?娌℃??瀵瑰????哄??");
//        }
//        ActsCaseContextHolder.clearUniqueMap();
//        if (ActsCaseContextHolder.get().getProcessErrorLog().size() > 0) {
//            List<String> errorLogs = ActsCaseContextHolder.get().getProcessErrorLog();
//            String errorLog = "";
//            for (String err : errorLogs) {
//                errorLog += File.separator + err;
//            }
//            Assert.fail(checkPointName + ":" + errorLog);
//        }
//    }
//
//    /**
//     * 娓??????版??
//     */
//    protected void clean() {
//        ActsLogUtil.addProcessLog("\n==============??版??娓??????ュ??============\n");
//        List<DataBaseCPUnit> unitList = ActsCaseContextHolder.get().getPreCleanContent();
//        for (DataBaseCPUnit unit : unitList) {
//            YamlTestUtil.clean(unit);
//        }
//    }
//
//    /**
//     * ??????error淇℃??锛?浠?Assert.fail?????洪??璇???????
//     *
//     * @param message
//     * @param e
//     */
//    protected void fail(String message, Throwable e) {
//        ActsLogUtil.fail(log, message, e);
//    }
//
//    /**
//     * ??轰??topic???code??峰??褰????event???琛? 涓???????瀵圭?规??event??煎????ユ????????琛?杩?琛?姣?杈?锛?甯歌?????浣跨??checkMsg??ュ???????￠??
//     *
//     * @param eventTopic
//     * @param eventCode
//     * @return 娑????瀵硅薄???琛?
//     */
//    protected List<UniformEvent> getMsgList(String eventTopic, String eventCode) {
//        return UniformEventUtil.getMsgs(eventTopic, eventCode);
//    }
//
//    /**
//     * 寤舵??
//     *
//     * @param t
//     *            姣?绉????
//     */
//    protected void sleep(long t) {
//
//        try {
//            Thread.sleep(t);
//        } catch (InterruptedException e) {
//            ActsLogUtil.error(log, "", e);
//        }
//    }
//
//    /**
//     * ??ㄤ?????缃???ц??锛???ㄤ?????濮??????ㄤ??涓?涓???????娓????娑????
//     *
//     * @param objs
//     */
//    @BeforeMethod
//    protected void beforeMethod(Object[] objs) {
//        ActsLogUtil.initLogContext(objs);
////        UniformEventUtil.uniformEventList.clear();
//    }
//
//    /**
//     * ??ㄤ?????缃??????ㄦ?ц??锛???????娴?璇???ュ??锛?娓????娑???????Mock
//     */
//    @AfterMethod
//    protected void afterMethod() {
////        UniformEventUtil.uniformEventList.clear();
//        AtsMockUtil.releaseAllMock();
//        if (!YamlTestUtil.isSingleYaml())
//            ActsLogUtil.dumpBaseline();
//        ActsLogUtil.clearLogContext();
//    }
//
//    /**
//     * ?????????缃??????ㄦ?ц??锛??????????Baseline锛?骞舵???????????涓?涓????
//     */
//    @AfterClass
//    protected void afterClass() {
//        if (YamlTestUtil.isSingleYaml())
//            ActsLogUtil.dumpBaseline();
//        ActsSuiteContextHolder.clear();
//    }
//
//    /**
//     * ??轰????ㄤ??ID???CP??瑰??绉拌??琛????澶???版??锛?浠?渚??????ㄨ?????
//     *
//     * @param caseId
//     * @param checkPointName
//     * @return ???澶?瀵硅薄Map
//     */
//    private Map<String, Object> prepare(String caseId, String checkPointName) {
//        YamlTestCase testCase;
//        if (YamlTestUtil.isSingleYaml()) {
//            testCase = ActsSuiteContextHolder.get().getYamlTestData().getTestCase(caseId);
//        } else {
//            testCase = ActsCaseContextHolder.get().getYamlTestData().getTestCase(caseId);
//        }
//        Map<String, Object> result = new HashMap<String, Object>();
//        if (testCase != null) {
//            YamlCheckPoint checkPoint = testCase.getCheckPoint(checkPointName);
//            if (checkPoint != null) {
//                // 棰?娓????涓?涓????涓????璇???????
//                ActsCaseContextHolder.get().getProcessErrorLog().clear();
//
//                ActsLogUtil.addProcessLog("\n==============??版?????澶???ュ??============\n");
//                for (Entry<String, BaseCPUnit> entry : checkPoint.getCheckPointUnitMap().entrySet()) {
//                    if (entry.getValue() == null) {
//                        result.put(entry.getKey(), null);
//                        continue;
//                    }
//                    BaseCPUnit cpUnit = entry.getValue();
//                    switch (cpUnit.getUnitType()) {
//                        case DATABASE:
//                            if (cpUnit instanceof DataBaseCPUnit) {
//                                YamlTestUtil.prepareDB(cpUnit);
//                            } else if (cpUnit instanceof ListDataBaseCPUnit) {
//                                for (DataBaseCPUnit dbUnit : ((ListDataBaseCPUnit) cpUnit)
//                                    .getDataList()) {
//                                    YamlTestUtil.prepareDB(dbUnit);
//                                }
//                            } else {
//                                Assert.fail(cpUnit + "?????ュ??甯?");
//                            }
//                            break;
//                        case OBJECT:
//                            result.put(cpUnit.getUnitName(), YamlTestUtil.prepareObj(cpUnit));
//                            break;
//                        default:
//                            ActsLogUtil.fail(logger,
//                                "???澶???版??涓???????褰????绫诲??" + cpUnit.getUnitType());
//                            break;
//                    }
//                }
//            } else {
//                ActsLogUtil.warn(log, "褰??????ㄤ??娌℃??瀵瑰??CP???" + checkPointName);
//            }
//        } else {
//            ActsLogUtil.warn(log, "褰??????ㄤ?????Yaml涓?娌℃??瀵瑰????哄??");
//        }
//        ActsCaseContextHolder.clearUniqueMap();
//        if (ActsCaseContextHolder.get().getProcessErrorLog().size() > 0) {
//            Assert.fail(checkPointName + "??版?????澶?澶辫触:"
//                        + ActsCaseContextHolder.get().getProcessErrorLog());
//        }
//        return result;
//    }
//
//    /**
//     * ??轰??瀵硅薄搴????锛???????瀵硅薄Map
//     *
//     * @param objs
//     * @return
//     */
//    @SuppressWarnings("rawtypes")
//    private Map<String, Object> getObjMap(Object... objs) {
//        Map<String, Object> objMap = new HashMap<String, Object>();
//        if (objs != null) {
//            for (Object obj : objs) {
//                Assert.assertNotNull("浣跨?ㄦ?扮????ュ????讹??瀵硅薄涓???戒负绌?", obj);
//                if (obj instanceof List) {
//                    Assert.assertTrue("浣跨?ㄦ?扮????ュ????讹??list涓???戒负绌?", ((List) obj).size() > 0);
//                    objMap.put(((List) obj).get(0).getClass().getSimpleName(), obj);
//                } else {
//                    objMap.put(obj.getClass().getSimpleName(), obj);
//                }
//            }
//        }
//        return objMap;
//    }
//
//    public static String getAppPath() {
//        String testBundlePath = System.getProperty("user.dir");
//
//        String[] spiltPaths = StringUtils.splitByWholeSeparator(testBundlePath, "test");
//        String appPath = spiltPaths[0];
//
//        return appPath;
//    }
//
//    public static void main(String[] args) {
//
//        ActsTestBaseOld actsTestBase = new ActsTestBaseOld();
//        String appPath = actsTestBase.getAppPath();
//
//        System.out.print(appPath);
//    }
//}
