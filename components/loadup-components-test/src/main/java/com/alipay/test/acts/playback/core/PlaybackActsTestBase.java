///**
// * Alipay.com Inc.
// * Copyright (c) 2004-2019 All Rights Reserved.
// */
//package com.alipay.test.acts.playback.core;
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
//import org.apache.commons.lang3.StringUtils;
//import com.alibaba.fastjson2.JSON;
//import com.alipay.ats.util.BundleBeansManager;
//import com.alipay.common.tracer.context.AbstractLogContext;
//import com.alipay.miu.facade.enums.QueryType;
//import com.alipay.miu.facade.model.CaseData;
//import com.alipay.miu.facade.model.CaseIndex;
//import com.alipay.sofa.runtime.test.SofaTestConstants;
//import com.alipay.sofa.runtime.test.testng.TestNGEclipseSofaTest;
//import com.alipay.test.acts.component.event.EventContextHolder;
//import com.alipay.test.acts.component.handler.TestUnitHandler;
//import com.alipay.test.acts.exception.ActsTestException;
//import com.alipay.test.acts.playback.CaseDataConver;
//import com.alipay.test.acts.playback.PlaybackOnlineDataService;
//import com.alipay.test.acts.playback.lothar.CaseContextHolder;
//import com.alipay.test.acts.playback.lothar.LotharCheckParser;
//import com.alipay.test.acts.playback.lothar.LotharCheckParser.IgnoreCallBack;
//import com.alipay.test.acts.playback.model.PlaybackPrepareData;
//import com.alipay.test.acts.playback.trservice.TrBeanAutowireInjector;
//import com.alipay.test.acts.playback.trservice.TrBeanReferenceRegister;
//import com.alipay.test.acts.playback.trservice.TrServiceRepository;
//import com.alipay.test.acts.playback.trservice.exception.BeanInvokeException;
//import com.alipay.test.acts.playback.trservice.exception.BeanNotFoundException;
//import com.alipay.test.acts.playback.trservice.model.TrBeanDescriptor;
//import com.alipay.test.acts.playback.trservice.model.TrBeanProxyReference;
//import com.alipay.test.acts.playback.util.ActsDummyContextUtil;
//import com.alipay.test.acts.playback.util.ArrayUtil;
//import com.alipay.test.acts.playback.util.ReflectUtil;
//import com.alipay.test.acts.runtime.ActsRuntimeContext;
//import com.alipay.test.acts.support.SofaRuntimeReference;
//import com.alipay.test.acts.utils.config.ConfigrationFactory;
//import com.alipay.test.acts.utils.config.PropertyConfig;
//import com.google.common.collect.Lists;
//
//import org.springframework.aop.framework.AopProxyUtils;
//import org.testng.annotations.AfterClass;
//import org.testng.annotations.BeforeClass;
//import org.testng.annotations.DataProvider;
//
//import java.lang.reflect.Field;
//import java.lang.reflect.Method;
//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.SQLException;
//import java.sql.Statement;
//import java.util.*;
//
///**
// * 全场景回放 ci 的测试基类
// *
// * <p>
// *     <li>1. lothar setup</li>
// *     <li>2. dataprovider 请求该应用的所有场景用例 id</li>
// *     <li>3. execute 组装完整用例，引入 tr 服务</li>
// *     <li>4. 以 tr 发起被测试服务，并透穿 tracer 信息</li>
// * </p>
// *
// * @author qingqin
// * @version $Id: PlayBackActsTestBase.java, v 0.1 2019年07月22日 下午10:29 qingqin Exp $
// */
//public abstract class PlaybackActsTestBase extends TestNGEclipseSofaTest {
//
//    /**
//     * 如果需要添加其他依赖，注意按照这种方式，先获取原来的配置，再补充
//     */
//    static {
//        String appendProps = "commons-pool-,commons-beanutils-,guava-,ats-common-,cmmons-io-,atd-app-server-,acts-common-util-,acts-common-dto,acts-core-,itest-common-lang-,miu-facade-,lothar-spring-";
//        appendSysProps(appendProps);
//    }
//
//    /**
//     * 子类新增 System Property 时，请使用该方式追加
//     * @param appendProps
//     */
//    public static void appendSysProps(String appendProps) {
//        String tmp = System.getProperty("test_artifacts", "");
//        if ("".equals(tmp) || tmp.endsWith(",")) {
//            appendProps = tmp + appendProps;
//        } else {
//            appendProps = tmp + "," + appendProps;
//        }
//        System.setProperty("test_artifacts", appendProps);
//    }
//
//    public ThreadLocal<TestUnitHandler> testUnitHandler = new ThreadLocal<TestUnitHandler>();
//
//    public ThreadLocal<ActsRuntimeContext> actsRuntimeContext = new ThreadLocal<ActsRuntimeContext>();
//
//    //public static Map<Long, String> mm = new ConcurrentHashMap<Long, String>();
//
//    public TrServiceRepository trServiceRepository;
//
//    /**
//     * 最大预热时间 3 分钟；参考了 sofa 容器的一般预热时间为 2-3 分钟
//     */
//    private static final long maxPreheadTime = 180000L;
//
//    /**
//     * 通过配置文件指定预热时间，单位 ms，默认 120000ms，最大值 maxPreheadTime，最小值 1000ms；
//     */
//    private static long preheadDuring;
//
//    private static final String DEFAULT_BUNDLE = SofaTestConstants.ON_THE_FLY_BUNDLE_NAME;
//
//    private boolean checkPass = false;
//
//    static {
//        String during = ConfigrationFactory.getConfigration().getPropertyValue("preheat_during", "120000");
//
//        try {
//            preheadDuring = Long.parseLong(during);
//        } catch (Exception e) {
//            preheadDuring = 0L;
//        }
//
//        preheadDuring = preheadDuring > maxPreheadTime ? maxPreheadTime : preheadDuring;
//    }
//
//    /**
//     * 统一时钟，屏蔽 ci 分组不均匀、多线程下不同实例的启动时间的偏差
//     */
//    private static final long start = System.currentTimeMillis();
//
//    /**
//     *
//     * @throws Exception 报错任何致命异常以阻止测试
//     */
//    @BeforeClass
//    protected void setUp() throws Exception {
//        /**
//         * 0.check app
//         */
//        checkApp();
//
//        /*
//         * 1.check lothar
//         */
//        checkLothar();
//
//        /*
//         * 2.setup acts
//         *
//         * 只适配 sofa4，因为 lothar 对于 sofa3 也不适配
//         */
//        adaptContext();
//
//        /*
//         * 检查数据服务
//         */
//        checkDataServer();
//
//        /*
//         * 设置获取用例链路
//         */
//        PlaybackOnlineDataService.setCaseSrc(getDevCase());
//
//        /*
//         * 处理系统预热
//         */
//        doPreheat();
//
//        //前序没有任何异常而退出，故改变检查状态为通过
//        checkPass = true;
//    }
//
//    protected void checkApp() throws Exception {
//        //应用检查检查，防止 12200 端口拒绝连接的报错，如果不需要此项检查，可以重写覆盖该方法
//        com.alipay.test.acts.playback.lothar.health.Scanner.appHealth();
//    }
//
//    protected boolean checkLothar() throws Exception {
//
//        //交由 lothar-spring 组件完成了，如果有其他补充动作，可以重写该方法
//        com.alipay.test.acts.playback.lothar.health.Scanner.scan();
//
//        return true;
//    }
//
//    /**
//     * 获取sofa上下文，放到 BundleBeansManager 里
//     *
//     */
//    protected void adaptContext() throws Exception {
//
//        BundleBeansManager.setSofaRuntimeContext(this.runtime);
//        SofaRuntimeReference.setCurrentSofaRuntime(this.runtime);
//
//        TrBeanReferenceRegister register = new TrBeanReferenceRegister(this.client);
//        TrBeanAutowireInjector injector = new TrBeanAutowireInjector(this.runtime, getBundleName());
//        trServiceRepository = new TrServiceRepository(register, injector, getClass());
//
//    }
//
//    protected void checkDataServer() throws Exception {
//
//        //这里不计入预热耗时中，强制等待 2000ms
//        try {
//            Thread.sleep(2000);
//        } finally {
//            logger.info("让子弹飞一会儿!");
//        }
//
//    }
//
//    /**
//     * 预处理耗时等待
//     * 包括 sofa 预热转发，以解决部分系统配置了预热转发出现 rpc 请求转发；
//     * 包括 lothar 规则预热等解决 mock 规则生效；
//     *
//     * 优化 ci 耗时；小于 1000 时默认不进行预热处理；
//     *
//     * @throws Exception 失眠啦
//     */
//    private void doPreheat() throws Exception {
//        if (preheadDuring < 1000) {
//            return;
//        }
//
//        while (true) {
//            Thread.sleep(1000);
//
//            long distance = System.currentTimeMillis() - start;
//            if (distance > preheadDuring) {
//                break;
//            }
//        }
//    }
//
//    /**
//     * 1.请求用例索引
//     * 2.组装用例迭代器
//     */
//    @DataProvider(name = "PlaybackCIProvider",parallel = true)
//    public Iterator<Object[]> getDataProvider(Method method) {
//
//        return getDataInput(false);
//    }
//
//    /**
//     * Linke识别用例bug，导致需要框架打补丁额外塞个caseId
//     * @param method
//     * @return
//     */
//    @DataProvider(name = "PlaybackCIProviderPatch",parallel = true)
//    public Iterator<Object[]> getDataProviderPatch(Method method) {
//        return getDataInput(true);
//    }
//
//    private Iterator<Object[]> getDataInput(boolean containCaseId) {
//        String isRun = ConfigrationFactory.getConfigration().getPropertyValue(
//                "is_run_fullscene", "true");
//
//        List<Object[]> prepareDataList = Lists.newArrayList();
//
//        if(!StringUtils.equals(isRun,"true") || !checkPass){
//            return prepareDataList.iterator();
//        }
//
//        //获取是否执行分组
//        String[] caseSplitInfo = getCaseSplitNum().split("#");
//        List<CaseIndex> caseDataIndexList;
//
//        //是否执行分页查询数据
//        if (caseSplitInfo.length == 2 && getCondtion() != QueryType.CASE) {
//            int groupTotal = Integer.valueOf(caseSplitInfo[0]);
//            int groupCurrent = Integer.valueOf(caseSplitInfo[1]);
//            caseDataIndexList = queryCaseRefsByGroupInfo(getCondtion(), getConditionValue(), groupTotal, groupCurrent);
//        } else {
//            caseDataIndexList = queryCaseRefs(getCondtion(), getConditionValue());
//        }
//
//        for (CaseIndex caseDataIndex : caseDataIndexList) {
//
//            caseDataIndex.setCaseDesc(caseDataIndex.getInterfaceName() + "#" + caseDataIndex.getMethodName());
//            Object[] args;
//            if (containCaseId) {
//                args = new Object[] {caseDataIndex, caseDataIndex.getCaseId()};
//            } else {
//                args = new Object[] {caseDataIndex};
//            }
//
//
//            prepareDataList.add(args);
//
//            /*
//             * 准备服务，有失败也不管，到运行时用例判错
//             */
//            TrBeanDescriptor descriptor = constructBeanDescriptor(caseDataIndex);
//            try {
//                if (caseDataIndex.getMethodName().contains("onUniformEvent")) {
//                    // 暂时用覆写bundleName来区分msg和rpc，因为msg不指定bundleName获取服务会失败
//                    trServiceRepository.injectTrService(descriptor);
//                }else {
//                    trServiceRepository.registerTrService(descriptor);
//                }
//
//            } catch (Exception e) {
//                logger.warn("can register bean:" + descriptor, e);
//            }
//
//        }
//
//        return prepareDataList.iterator();
//    }
//
//    /**
//     * 向服务器请求一批用例索引
//     *
//     * @param condition 查询条件
//     * @param query 条件值
//     * @return
//     */
//    protected List<CaseIndex> queryCaseRefsByGroupInfo(QueryType condition, String query, int totalGroup, int currentGroup) {
//
//        if (StringUtils.isEmpty(query)) {
//            return new ArrayList<CaseIndex>(0);
//        }
//        try {
//            return PlaybackOnlineDataService.queryOnlineCaseRefsByGroup(condition, query, totalGroup, currentGroup);
//        } catch (Exception ex) {
//            logger.error("failed to query playback data!", ex);
//            throw new BeanInvokeException("数据获取失败！", ex);
//        }
//    }
//
//    /**
//     * 向服务器请求一批用例索引
//     *
//     * @param condition 查询条件
//     * @param query 条件值
//     * @return
//     */
//    protected List<CaseIndex> queryCaseRefs(QueryType condition, String query) {
//
//        if (StringUtils.isEmpty(query)) {
//            return new ArrayList<CaseIndex>(0);
//        }
//        try {
//            return PlaybackOnlineDataService.queryOnlineCaseRefs(condition, query);  //线上录制数据获取
//        } catch (Exception ex) {
//            logger.error("failed to query playback data!", ex);
//            throw new BeanInvokeException("数据获取失败！", ex);
//        }
//    }
//
//    /**
//     * 由 case ref 构造出待观测 tr bean 的描述
//     *
//     * @param caseRef
//     * @return
//     */
//    final protected TrBeanDescriptor constructBeanDescriptor(CaseIndex caseRef) {
//
//        TrBeanDescriptor trBeanDescriptor = new TrBeanDescriptor();
//
//        Class interfaceType = ReflectUtil.loadClassByCurrentThreadClassLoader(caseRef.getInterfaceName());
//
//        trBeanDescriptor.setInterfaceType(interfaceType);
//        trBeanDescriptor.setInterfaceName(caseRef.getInterfaceName());
//        trBeanDescriptor.setUniqueId(caseRef.getUniqueId());
//
//        addBeanName(trBeanDescriptor);
//        setRpcServiceType(trBeanDescriptor);
//        return trBeanDescriptor;
//    }
//
//    /**
//     * 依据 interface 和 unique id 自主设置 bean name
//     *
//     * @param beanDescriptor
//     */
//    protected void addBeanName(TrBeanDescriptor beanDescriptor) {
//
//    }
//
//    /**
//     * 依据 interface 发布的协议服务类型来设置
//     * 如果是 tr 服务，beanDescriptor.setBindingCode(TrBeanDescriptor.TR);
//     * 如果是 ws 服务 beanDescriptor.setBindingCode(TrBeanDescriptor.WS);
//     *
//     * @param beanDescriptor
//     */
//    protected void setRpcServiceType(TrBeanDescriptor beanDescriptor) {
//
//    }
//
//    /**
//     * Condition#APP     对应 getAppName       生效
//     * Condition#API     对应 getSpecifiedApi  生效
//     * Condition#SCENE   对应 getSpecifiedApi  生效
//     *
//     * @return 用于检索回放用例集合的条件
//     */
//    abstract protected QueryType getCondtion();
//
//    /**
//     * 指定为 condition#app 指定的应用名称
//     *
//     * @return 应用名称，如 zintclcn
//     */
//    abstract protected String getAppName();
//    /**
//     * 集合中每个元素格式为 app#interface#method
//     *
//     * 如:zintclcn#com.alipay.fc.intclcn.common.service.facade.AssetPriceClcnFacade#calcAssetLatestPrice
//     *
//     * @return 接口列表
//     */
//    abstract protected List<String> getSpecifiedApis();
//
//    /**
//     * 集合中每个元素格式为 场景id
//     *
//     * 如:c857cbf656a5b0bc56b43ff9e7848ebe
//     *
//     * @return 用例列表
//     */
//    abstract protected List<String> getSpecifiedCases();
//
//    /**
//     * 获取用例执行拆分的分组，默认不执行拆分，这个地方主要用于单用例数据量太多时CI无法执行并发分发，执行效率较低
//     *
//     * 格式为:分组数#当前第几组(0开始)，例：5#0
//     * @return
//     */
//    protected String getCaseSplitNum(){
//        return "";
//    }
//
//    /**
//     * 用于获取测试对象的bundle，默认为测试类bundle
//     * @return
//     */
//    protected String getBundleName(){
//        return DEFAULT_BUNDLE;
//    }
//
//    /**
//     * 用于指定是否从线上数据链路获取用例，默认走线上链路
//     * @return
//     */
//    protected boolean getDataOnline() {
//        return true;
//    }
//
//    /**
//     * 用于区分线下录制用例的数据源，默认读取线上录制的用例数据
//     * @return
//     */
//    protected boolean getDevCase() {
//        return false;
//    }
//
//
//    private String getConditionValue() {
//        QueryType condition = getCondtion();
//
//        if (condition == QueryType.APP) {
//            return getAppName();
//        } else if (condition == QueryType.API) {
//            return JSON.toJSONString(getSpecifiedApis());
//        } else {
//            return JSON.toJSONString(getSpecifiedCases());
//        }
//    }
//
//    /**
//     * 执行测试
//     *
//     */
//    public void runTest(CaseIndex caseDataIndex) {
//
//        //DetailCollectUtils.appendAndLog(
//        //        "=============================Start excuting TestCase caseId:" + caseRef.getCaseNo() + " "
//        //                + caseRef.getCaseDes() + "=================", logger);
//        logger.info("=============================Start excuting TestCase caseId:" + caseDataIndex.getCaseId() + " "
//                + caseDataIndex.getCaseDesc() + "=================");
//
//        PlaybackPrepareData prepareData;
//        try {
//            //mm.put(Thread.currentThread().getId(), caseRef.getCaseNo());
//            /*
//             * 组装上下文，相比 bean 装载而言 数据组装太重了
//             */
//            initRuntimeContext(caseDataIndex);
//            /*
//             * 依据 caseref 查询、组装完整用例数据
//             */
//            prepareData = assemblePrepareData(caseDataIndex);
//        } catch (Exception e) {
//            throw new ActsTestException("Failed to run case:" + caseDataIndex.getCaseId(), e);
//        }
//
//        initRuntimeData(prepareData);
//        initTestUnitHandler();
//
//        try {
//
//            // 所有测试之前都要执行的
//            beforeActsTest(actsRuntimeContext.get());
//            process(actsRuntimeContext.get());
//
//        } finally {
//
//            // 所有测试之后都要执行的
//            afterActsTest(actsRuntimeContext.get());
//
//            // 线程变量清理
//            EventContextHolder.clear();
//
//            //清理掉缓存
//            actsRuntimeContext.remove();
//            testUnitHandler.remove();
//        }
//    }
//
//    /**
//     * 初始化 ACTS 上下文
//     *
//     * @param caseDataIndex
//     */
//    public void initRuntimeContext(CaseIndex caseDataIndex) throws Exception {
//
//        Object testedObj = getTestedObj(caseDataIndex);
//        Method testedMethod = findMethod(testedObj, caseDataIndex.getMethodName(), caseDataIndex.getInputParamsType());
//
//        actsRuntimeContext.set(new ActsRuntimeContext(caseDataIndex.getCaseId(), null,
//                new HashMap<String, Object>(), null, testedMethod, testedObj,
//                bundleContext));
//    }
//
//    /**
//     * 获取被测对象
//     *
//     * @return
//     */
//    protected Object getTestedObj(CaseIndex caseDataIndex) throws BeanNotFoundException {
//
//        //获得 tr bean 描述
//        TrBeanDescriptor descriptor = constructBeanDescriptor(caseDataIndex);
//
//        TrBeanProxyReference reference = trServiceRepository.injectTrService(descriptor);
//
//        return reference.getTarget();
//    }
//
//    /**
//     * 获取被测对象的被测方法
//     *
//     * @param methodName 被测试方法
//     * @param testedObj 被测试对象
//     * @return 目标方法
//     */
//    protected Method findMethod( Object testedObj, String methodName, String[] argsTypes) {
//
//        Class<?>[] clazzes;
//        try {
//            clazzes = AopProxyUtils.proxiedUserInterfaces(testedObj);
//        } catch (Exception e) {
//            clazzes = new Class<?>[] { testedObj.getClass() };
//        }
//
//        for (Class<?> clazz : clazzes) {
//            while (clazz != null && !clazz.equals(Object.class)) {
//                Method[] methods = clazz.getDeclaredMethods();
//                for (Method method : methods) {
//                    if (StringUtils.equals(method.getName(), methodName)) {
//                        if (matchTypes(argsTypes, method.getParameterTypes())) {
//                            return method;
//                        }
//                    }
//                }
//                clazz = clazz.getSuperclass();
//            }
//        }
//
//        // 接口中没有找到，则直接在对象里尝试寻找
//        Method[] publicMethods = testedObj.getClass().getMethods();
//        for (Method method : publicMethods) {
//            if (StringUtils.equals(method.getName(), methodName)) {
//                if(matchTypes(argsTypes, method.getParameterTypes())) {
//                    return method;
//                }
//            }
//        }
//        return null;
//    }
//
//    /**
//     * 重载方法识别，只判断个数
//     * @param argTypes
//     * @param paramTypes
//     * @return
//     */
//    protected boolean matchTypes(String[] argTypes, Class<?>[] paramTypes) {
//
//        if (ArrayUtils.isEmpty(argTypes)) {
//            return ArrayUtils.isEmpty(paramTypes);
//        }
//
//        if (ArrayUtils.isEmpty(paramTypes)) {
//            return false;
//        }
//
//        //类型个数
//        if (paramTypes.length != argTypes.length) {
//            return false;
//        }
//
//        //类型一致性判断
//
//        for(int i=0;i<argTypes.length;i++){
//            //因为录制数据类型不正确，只判断内部包
//            if(argTypes[i].contains("com.alipay")){
//                if(!argTypes[i].equals(paramTypes[i].getName())){
//                    return false;
//                }
//            }
//        }
//        return true;
//    }
//
//    /**
//     * 向服务器请求一个用例的完整数据
//     *
//     * @param caseDataIndex 用例索引
//     * @return 一个用例索引对应的完整用例数据
//     * @throws Exception 数据转换异常
//     */
//    protected PlaybackPrepareData assemblePrepareData(CaseIndex caseDataIndex) throws Exception {
//
//        CaseData caseData = queryCaseData(caseDataIndex);
//        if (null == caseData){
//            throw new RuntimeException("无查询数据！");
//        }
//
//
//        PlaybackPrepareData ppd = new PlaybackPrepareData(caseDataIndex.getCaseId(), caseDataIndex.getCaseDesc());
//
//        return CaseDataConver.conver(caseData, ppd);
//    }
//
//    private CaseData queryCaseData(CaseIndex caseDataIndex) {
//
//        //List<CaseIndex> caseRefs = new ArrayList<CaseIndex>(1);
//        //caseRefs.add(caseRef);
//
//        try {
//            return PlaybackOnlineDataService.queryOnlineCaseData(caseDataIndex); // 线上录制数据获取
//        }catch (Exception ex) {
//            throw new BeanInvokeException("数据获取失败！", ex);
//        }
//    }
//
//    /**
//     * 必须先使用 initRuntimeContext，在调用该方法
//     *
//     * @param prepareData 一个完整的组装好的用例数据
//     */
//    private void initRuntimeData(PlaybackPrepareData prepareData) {
//        actsRuntimeContext.get().setPrepareData(prepareData);
//    }
//
//    /**
//     * 初始化测试操作对象
//     */
//    public void initTestUnitHandler() {
//        this.testUnitHandler.set(new TestUnitHandler(actsRuntimeContext.get()));
//    }
//
//    public void process(ActsRuntimeContext actsRuntimeContext) {
//
//        try {
//            // 数据清理
//            clear(actsRuntimeContext);
//            // 数据准备
//            prepare(actsRuntimeContext);
//            // 测试执行
//            execute(actsRuntimeContext);
//            // 结果检查
//            check(actsRuntimeContext);
//
//            //记录结果
//            PlaybackOnlineDataService.updateCaseDataStatus(actsRuntimeContext.getCaseId(), AbstractLogContext.get().getTraceId(),
//                        true, "");
//
//        } catch (Exception e) {
//            logger.error("process 阶段异常", e);
//
//            //记录CASE失败原因进行分析
//
//            try {
//                PlaybackOnlineDataService.updateCaseDataStatus(actsRuntimeContext.getCaseId(), AbstractLogContext.get().getTraceId(),
//                                false, e.getMessage());
//            } catch (Exception ex) {
//            }
//
//            throw new ActsTestException(e);
//
//        } catch (Error e){
//
//            //记录CASE失败原因进行分析
//            try {
//                PlaybackOnlineDataService.updateCaseDataStatus(actsRuntimeContext.getCaseId(), AbstractLogContext.get().getTraceId(),
//                        false, e.getMessage());
//            } catch (Exception ex) {
//
//            }
//            //处理异常
//            Method method = actsRuntimeContext.getTestedMethod();
//            changeMessage(e, actsRuntimeContext.getCaseId(), method);
//            throw e;
//
//        } finally {
//            try {
//                clear(actsRuntimeContext);
//            } catch (Exception e) {
//                logger.error("clear 阶段异常", e);
//            }
//
//            logger.info(
//                    "=============================TestCase caseId:" + actsRuntimeContext.getCaseId() + " End=============================\r\n");
//
//        }
//
//    }
//
//    /**
//     * 为了不丢失原始的堆栈信息，对异常做特殊处理
//     *
//     * @param t        异常
//     * @param caseId   用例 id
//     * @param method   被测试方法
//     */
//    private void changeMessage(Throwable t, String caseId, Method method) {
//        StringBuilder msg = new StringBuilder();
//        msg.append("\r\n");
//        msg.append("caseId:");
//        msg.append(caseId);
//        msg.append("\r\n");
//        msg.append("interface:");
//        if (null == method) {
//            msg.append("null#null");
//        } else {
//            msg.append(method.getDeclaringClass());
//            msg.append("#");
//            msg.append(method.getName());
//        }
//        msg.append("\r\n");
//        msg.append("traceId:");
//        msg.append(AbstractLogContext.get().getTraceId());
//        msg.append("\r\n");
//        msg.append(t.getMessage());
//
//        try {
//            Field detailMessage = Throwable.class.getDeclaredField("detailMessage");
//            boolean access = detailMessage.isAccessible();
//            if (!access) {
//                detailMessage.setAccessible(true);
//            }
//            //替换
//            detailMessage.set(t, msg.toString());
//            //还原
//            if (!access) {
//                detailMessage.setAccessible(false);
//            }
//        } catch (Exception ex) {
//            return;
//        }
//    }
//
//    /**
//     *
//     * @param actsRuntimeContext
//     * @throws Exception
//     */
//    public void clear(ActsRuntimeContext actsRuntimeContext) throws Exception {
//        logger.info("=============================[acts clear begin]=============================\r\n");
//
//        ActsDummyContextUtil.clearDummyLogContext();
//        CaseContextHolder.remove(actsRuntimeContext.getCaseId());
//
//        logger.info("=============================[acts clear end]=============================\r\n");
//
//    }
//
//    /**
//     *
//     */
//    public void prepare(ActsRuntimeContext actsRuntimeContext) throws Exception {
//        logger.info("=============================[acts prepare begin]=============================\r\n");
//
//
//        ActsDummyContextUtil.createDummyLogContext();
//
//        PlaybackPrepareData ppd = (PlaybackPrepareData)actsRuntimeContext.getPrepareData();
//
//        ActsDummyContextUtil.addPenetrateAttribute("etetest", ppd.getCaseId());
//        if (getDataOnline()) {
//            // 用于lothar识别获取线上mock数据
//            ActsDummyContextUtil.addPenetrateAttribute("isOnline", "y");
//
//            if (getDevCase()) {
//                ActsDummyContextUtil.addPenetrateAttribute("case_env", "dev");
//            }
//        }
//        logger
//                .info("=============================[acts prepare end]=============================\r\n");
//    }
//
//    /**
//     *
//     * @param actsRuntimeContext acts 上下文
//     * @throws Exception 执行异常
//     */
//    public void execute(ActsRuntimeContext actsRuntimeContext) throws Exception {
//        logger.info("=============================[acts execute begin]=============================\r\n");
//        //调用方法
//        testUnitHandler.get().execute();
//        logger.info("=============================[acts execute end]=============================\r\n");
//    }
//
//    /**
//     *
//     */
//    public void check(ActsRuntimeContext actsRuntimeContext) throws Exception {
//        logger.info("=============================[acts check begin]=============================\r\n");
//
//        LotharCheckParser.print(CaseContextHolder.get(actsRuntimeContext.getCaseId()), ignore(actsRuntimeContext));
//        ignore(actsRuntimeContext).ignoreResultCheck();
//        testUnitHandler.get().checkException();
//        testUnitHandler.get().checkExpectResult();
//
//        logger.info("=============================[acts check end]=============================\r\n");
//
//    }
//
//    private IgnoreCallBack ignore (final ActsRuntimeContext actsRuntimeContext) {
//        return new IgnoreCallBack () {
//            @Override
//            public List<String> ignoreClasses() {
//                return setIgnoreClasses();
//            }
//
//            @Override
//            public List<String> ignoreMethods() {
//                return setIgnoreMethods();
//            }
//
//            /**
//             * 是否校验MOCK的相关内容，这个地方较为复杂，四层结构：类#方法#第几个参数#某个属性#是否校验
//             *
//             * @return
//             */
//            @Override
//            public List<String> ignoreCheck() {
//                return setIgnoreCheck();
//            }
//
//            @Override
//            public void ignoreResultCheck() {
//
//                List<String> ignoreInfo = setIgnoreResultCheckFileds();
//                if(!ignoreInfo.isEmpty()){
//                    for(String flagInfo : ignoreInfo){
//                        String objectName = flagInfo.split("#")[0];
//                        String filedName = flagInfo.split("#")[1];
//                        String flag = flagInfo.split("#")[2];
//                        Map<String, Map<String, String>> flagMap = actsRuntimeContext.getPrepareData().
//                                getExpectResult().getVirtualObject().getFlags();
//                        if(flagMap.containsKey(objectName)){
//                            flagMap.get(objectName).put(filedName,flag);
//                        }else{
//                            Map<String,String> fieldMap = new HashMap<String,String>();
//                            fieldMap.put(filedName,flag);
//                            flagMap.put(objectName,fieldMap);
//                        }
//
//                    }
//                }
//
//            }
//        };
//    }
//
//
//    /**
//     * 框架对于每个MOCK节点进行入参校验是否与线上录制时传入一致，不一致校验失败，这个地方可以过滤不进行校验的内容，格式为:
//     * 类名#方法名#第几个参数#哪个属性
//     * 过滤为层次结构可以只填写部分，#号分隔
//     * @return
//     */
//    public List<String> setIgnoreCheck() {
//        return null;
//    }
//
//    /**
//     * 框架对于每个MOCK节点进行入参校验是否与线上录制时传入一致，不一致校验失败，这个地方可以过滤哪些类不进行校验，直接填入类名
//     * @return
//     */
//    public List<String> setIgnoreClasses() {
//        List<String> ig = new ArrayList<String>();
//        ig.add("DateUtil");
//        return ig;
//    }
//
//    /**
//     * 框架对于每个MOCK节点进行入参校验是否与线上录制时传入一致，不一致校验失败，这个地方可以过滤哪些方法不进行校验，直接写入方法名
//     * @return
//     */
//    public List<String> setIgnoreMethods() {
//        return null;
//    }
//
//    /**
//     * 对返回结果某些字段设置不校验，格式:类(子类对象)#属性#是否校验(N,Y)
//     * @return
//     */
//    public List<String> setIgnoreResultCheckFileds(){return new ArrayList<String>(); }
//
//    /**
//     * 通用的beforeActsTest，可以在子类重写
//     *
//     * @param actsRuntimeContext
//     */
//    public void beforeActsTest(ActsRuntimeContext actsRuntimeContext) {
//
//    }
//
//    /**
//     * 通用的afterActsTest，可以在子类重写
//     *
//     * @param actsRuntimeContext
//     */
//    public void afterActsTest(ActsRuntimeContext actsRuntimeContext) {
//
//    }
//
//    /**
//     * TODO 回放用例结果存储，后续通过异步方式或者POST方式实现
//     * @param actsRuntimeContext
//     */
//    public static void recordTestResult(ActsRuntimeContext actsRuntimeContext,boolean isSucess,String errMsg){
//
//        String caseId = actsRuntimeContext.getCaseId();
//        String traceId = AbstractLogContext.get().getTraceId();
//        String jdbcUrl = "jdbc:mysql://11.166.119.126:3306/finatp?useUnicode=true&amp;characterEncoding=gbk";
//        String userName = "finatp";
//        String passWord = "ali88";
//        String sql = "update ete_online_scene_data set case_current_traceid='"+traceId+"',run_status='S',fail_cnt=0,case_run_detail='"+errMsg+"' where md5='"+caseId+"'";
//        if(!isSucess) {
//            sql = "update ete_online_scene_data set case_current_traceid='"+traceId+"',run_status='F',fail_cnt=ifnull(fail_cnt,0)+1,case_run_detail='"
//              + errMsg
//              + "' where md5='"
//              + caseId
//              + "'";
//        }
//        Connection conn = null;
//        Statement stamt = null;
//        try {
//            conn = DriverManager.getConnection(jdbcUrl, userName, passWord);
//            stamt = conn.createStatement();
//            stamt.execute(sql);
//            stamt.close();
//            conn.close();
//        } catch (Exception e) {
//
//        } finally {
//            if (stamt != null) {
//                try {
//                    stamt.close();
//                } catch (SQLException e) {
//
//                }
//            }
//            if (conn != null) {
//                try {
//                    conn.close();
//                } catch (Exception e) {
//
//                }
//            }
//        }
//
//    }
//
//    @AfterClass
//    public void triggerResultAnalysis() {
//        try {
//            if (getDevCase()) {
//                return;
//            }
//            // 这个暂时跳过执行，回放结果分析是跟名空团队共建的，待名空团队OK后打开
//            logger.info("=============================[trigger result analysis begin - 暂时跳过分析执行]=============================\r\n");
//            //PlaybackOnlineDataService.triggerPlaybackAnalysis(this.getAppName());
//            logger.info("=============================[trigger result analysis done]=============================\r\n");
//        } catch (Exception e) {
//            logger.error("failed to trigger result analysis!", e);
//        }
//    }
//
//    //=============================默认加载=================================
//
//    @Override
//    public String[] getConfigPattern() {
//
//        return PropertyConfig.getTestConfigPattern();
//    }
//
//    /**
//     * RPC model configuration file xml
//     * @see com.alipay.sofa.runtime.test.testng.TestNGEclipseSofaTest#getRpcServiceConfigurationLocations()
//     */
//    @Override
//    public String[] getRpcServiceConfigurationLocations() {
//        return new String[] { "servicetest/rpc-service-bean.xml" };
//    }
//
//    /**
//     * JVM model configuration file xml
//     * @see com.alipay.sofa.runtime.test.testng.TestNGEclipseSofaTest#getVirtualBundleConfigurationLocations()
//     */
//    @Override
//    public String[] getVirtualBundleConfigurationLocations() {
//        return new String[] { "servicetest/jvm-service-bean.xml", "META-INF/spring/lothar.xml" };
//    }
//
//}
