/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.testify.template;

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

import com.beust.jcommander.internal.Maps;
import com.github.loadup.components.testify.annotation.*;
import com.github.loadup.components.testify.annotation.testify.*;
import com.github.loadup.components.testify.collector.CaseResultCollector;
import com.github.loadup.components.testify.component.components.TestifyComponentUtil;
import com.github.loadup.components.testify.component.db.DBDatasProcessor;
import com.github.loadup.components.testify.component.event.EventContextHolder;
import com.github.loadup.components.testify.component.handler.TestUnitHandler;
import com.github.loadup.components.testify.db.enums.DBFlagEnum;
import com.github.loadup.components.testify.driver.TestifyConfiguration;
import com.github.loadup.components.testify.enums.TestifyActionEnum;
import com.github.loadup.components.testify.exception.TestifyException;
import com.github.loadup.components.testify.model.*;
import com.github.loadup.components.testify.runtime.TestifyRuntimeContext;
import com.github.loadup.components.testify.runtime.TestifyRuntimeContextThreadHold;
import com.github.loadup.components.testify.support.TestTemplate;
import com.github.loadup.components.testify.util.*;
import com.github.loadup.components.testify.utils.*;
import com.github.loadup.components.testify.utils.check.ObjectCompareUtil;
import com.github.loadup.components.testify.utils.config.ConfigrationFactory;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.management.RuntimeErrorException;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.util.CollectionUtils;
import org.testng.Assert;
import org.testng.annotations.*;

/**
 * Acts test base
 *
 * @author yuanren.syr
 *
 */
@Getter
@Setter
public class TestifyTestBase extends AbstractTestNGSpringContextTests {

    /**
     * 收集日志用的
     */
    protected static final String[] SUFFIX = new String[] {"NormalTest", "FuncExceptionTest"};

    /* sql日志 **/
    private static final Logger sqlLogger = LoggerFactory.getLogger("acts-sql-logger");

    /**
     * 数据处理
     */
    public static DBDatasProcessor dbDatasProcessor;

    /**
     * Deprecated
     */
    public static boolean invokModel;

    private final String SCAFFOLD_CASE = "SCAFFOLD_CASE";
    public TestUnitHandler testUnitHandler;

    /**
     * 被测方法名
     */
    public String testedMethodName;

    /**
     * 被测方法
     */
    public Method TestifyTestMethod;

    public String testDataFilePath;
    public String testDataFolderPath;

    /**
     * 2.0数据模式用例文件夹
     */
    public List<File> caseFolders = new ArrayList<File>();

    public TestifyRuntimeContext testifyRuntimeContext;

    /**
     * annotationMethods map
     */
    public Map<String, List<TestifyMethod>> annoationMethods = new HashMap<String, List<TestifyMethod>>();

    protected TestTemplate testTemplate;

    /**
     * 数据准备的模板
     */
    protected PrepareTemplate prepareTemplate = new PrepareTemplateImpl();

    /**
     * caseId和数据准备的Map
     */
    protected Map<String, PrepareData> prepareDatas = new HashMap<String, PrepareData>();

    /**
     * 是否每次执行都刷新
     */
    protected Boolean autoRefresh = false;

    public static boolean isInvokModel() {
        return invokModel;
    }

    public static void setInvokModel(boolean invokModel) {
        TestifyTestBase.invokModel = invokModel;
    }

    /**
     * 测试用例执行前操作
     *
     * @throws Exception
     */
    @BeforeClass
    protected void setUp() throws Exception {

        try {
            // set the runtime field for bean get support
            // sofa 4
            String runtimeName = "runtime";
            String runtimeMethod = "setSofaRuntimeContext";
            adaptContext(runtimeName, runtimeMethod);
            // sofa 3
            String contextName = "bundleContext";
            String contextMethod = "setBundleContext";
            adaptContext(contextName, contextMethod);

            // 数据源初始化
            if (dbDatasProcessor == null) {
                dbDatasProcessor = new DBDatasProcessor();
            }
            dbDatasProcessor.initDataSource();
            String autoRefresh = ConfigrationFactory.getConfigration().getPropertyValue("auto_refresh");
            if (!org.apache.commons.lang3.StringUtils.isBlank(autoRefresh)) {
                this.autoRefresh = Boolean.valueOf(autoRefresh);
            }

            // 获取自定义flag方法，并内置到比较类
            List<Method> flagMethods = AnnotationUtils.findMethods(this.getClass(), Flag.class);
            Map<String, Method> flagMethodsMap = new HashMap<>();
            for (Method method : flagMethods) {
                flagMethodsMap.put(method.getAnnotation(Flag.class).value(), method);
            }
            ObjectCompareUtil.setFlagMethodsHolder(flagMethodsMap, this);

            // 注册当前定义的所有参数化组件
            //            String componentPackage =
            // this.getClass().getPackage().getName().split(".acts.")[0]
            //                    + ".acts.component";
            //            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            //            ActsComponentUtil.init(componentPackage, loader);

            // 初始化ACTS注解参数
            //            ActsAnnotationFactory annotation = new
            // ActsAnnotationFactory(annoationMethods,
            //                    dbDatasProcessor);
            //            Set<Method> allMethod = ClassHelper.getAvailableMethods(this.getClass());
            //            annotation.initAnnotationMethod(allMethod, this);

        } catch (Exception e) {
            logger.error("Exception raised during setup process", e);
            throw new RuntimeException(e);
        } catch (Error err) {
            logger.error("Error raised during setup process", err);
            throw err;
        }
    }

    /**
     * 获取测试数据
     * <ol>
     * <li>Prepare Datas</li>
     * <li>Mock Interfaces</li>
     * </ol>
     */
    public void prepareTestData() {

        List<File> caseFolders = scanCaseFolders(getTestPath());

        // 并存的情况下，优先执行2.0用例
        if (caseFolders.isEmpty()) {
            logger.warn("未扫描到对应用例数据！");
            return;
        } else {
            // refresh caseFolders
            this.caseFolders.clear();
            this.caseFolders.addAll(caseFolders);

            prepareDatas = BaseDataUtil.loadFromCaseFolders(
                    caseFolders, this.getClass().getClassLoader());
        }

        // 加载yaml后，遍历组件，根据组件useOrigData判断是否用组件源数据替换当前组件数据
        for (String key : prepareDatas.keySet()) {
            if (null != prepareDatas.get(key).getVirtualComponentSet()
                    && null != prepareDatas.get(key).getVirtualComponentSet().getComponents()
                    && !prepareDatas
                            .get(key)
                            .getVirtualComponentSet()
                            .getComponents()
                            .isEmpty()) {

                for (int i = 0;
                        i
                                < prepareDatas
                                        .get(key)
                                        .getVirtualComponentSet()
                                        .getComponents()
                                        .size();
                        i++) {

                    VirtualComponent comp = prepareDatas
                            .get(key)
                            .getVirtualComponentSet()
                            .getComponents()
                            .get(i);

                    if (comp.isUseOrigData()) {
                        // 用引用case的数据替代当前组件数据
                        PrepareData origCompData = searchPrepareData(comp.getComponentClass(), comp.getComponentId());

                        prepareDatas
                                .get(key)
                                .getVirtualComponentSet()
                                .getComponents()
                                .get(i)
                                .setPrepareData(origCompData);
                    }
                }
            }
        }
    }

    /**
     * 预跑反填复写方法，如果那些表需要预跑反填需要把自己需要查询的sql进行设置,改方法全系统通用只需要配置一份，系统所有的用例都能使用预跑反填功能
     *
     * @param
     * @return
     */
    public List<String> backFillSqlList() {
        List<String> sqlList = new ArrayList<String>();
        // 这里把需要查询的sql放入List，需要你去补充，以下是一个样例
        // 可以通过变量替换方式任意从入参和结果中获取参数，入参的获取方式为：$args.get(0).getOutOrderNo()
        // 结果的获取方式为：$result.getMasterOrderNo()
        /*
           sqlList.add("select * from fas_order where order_no = '$result.getOrderNo()';");
           sqlList.add("select * from fas_note where order_no = '$result.getOrderNo()';");
        */
        return sqlList;
    }

    /**
     * 调用准备模板生成数据准备文件
     */
    protected void prepareData() {
        try {
            Class<?> prepareClass = getClass();
            ((PrepareTemplateImpl) prepareTemplate)
                    .setDataAccessConfigManager(dbDatasProcessor.getDataAccessConfigManager());

            // 读取编码准备的base方法，包含各个用例共用的基础数据
            // 测试类继承自一个文件名如：XxxxXxxxActsPrepare，包含@TestBase和@TestCase注解
            List<Method> baseMethods = AnnotationUtils.findMethods(prepareClass, TestBase.class);
            // 读取TestCase方法，包含一些差异化准备数据
            List<Method> methods = MethodUtils.filterMethod(
                    AnnotationUtils.findMethods(prepareClass, TestCase.class),
                    new Class<?>[] {PrepareData.class},
                    void.class);

            for (Method m : methods) {
                // 加载base方法内部准备的数据
                PrepareData basePrepareData = null;
                if (baseMethods != null && baseMethods.size() == 1) {
                    Method baseMethod = baseMethods.get(0);
                    baseMethod.setAccessible(true);
                    basePrepareData = (PrepareData) baseMethod.invoke(this);
                } else {
                    basePrepareData = new PrepareData();
                }
                // 加载并更新单个case的差异化数据
                m.invoke(this, basePrepareData);

                // 针对有yaml数据准备的情况，再对每个case进行数据更新，autoRefresh必然设值是true
                PrepareData prepareData = basePrepareData;
                String caseId = null;
                // 这还用判断吗？这个m不就是从TestCase注解找出来的吗
                if (m.isAnnotationPresent(TestCase.class)) {
                    for (String key : prepareDatas.keySet()) {
                        if (StringUtils.equals(
                                prepareDatas.get(key).getDescription(),
                                m.getAnnotation(TestCase.class).desc())) {
                            prepareData = prepareDatas.get(key);
                            caseId = key;
                            break;
                        }
                    }
                } else {
                    prepareData = prepareDatas.get(m.getName());
                }

                // merge prepareData 代码覆盖yaml ####有问题，不管里面有没有实质对象都会覆盖
                if (basePrepareData.getArgs() != null) {
                    prepareData.setArgs(basePrepareData.getArgs());
                }
                if (basePrepareData.getDepDataSet() != null) {
                    prepareData.setDepDataSet(basePrepareData.getDepDataSet());
                }
                if (basePrepareData.getExpectDataSet() != null) {
                    prepareData.setExpectDataSet(basePrepareData.getExpectDataSet());
                }
                if (basePrepareData.getExpectEventSet() != null) {
                    prepareData.setExpectEventSet(basePrepareData.getExpectEventSet());
                }
                if (basePrepareData.getExpectException() != null) {
                    prepareData.setExpectException(basePrepareData.getExpectException());
                }
                if (basePrepareData.getExpectResult() != null) {
                    prepareData.setExpectResult(basePrepareData.getExpectResult());
                }
                //                if (basePrepareData.getVirtualMockSet() != null) {
                //
                // prepareData.setVirtualMockSet(basePrepareData.getVirtualMockSet());
                //                }
                if (caseId == null) {
                    caseId = m.getName();
                }
                prepareData.setDescription(m.getAnnotation(TestCase.class).desc());
                prepareDatas.put(caseId, prepareData);
            }

            URL url = this.getClass().getResource(getDataFilePath());

            // 编码准备的数据保存成yaml

            boolean isSplitYaml = false;
            if (StringUtils.isNotBlank(TestifyConfiguration.getProperty("split_yaml_by_case"))) {
                isSplitYaml = Boolean.valueOf(TestifyConfiguration.getProperty("split_yaml_by_case"))
                        .booleanValue();
            }
            if (!isSplitYaml) {
                // 1、所有case存储为一个yaml的情况
                File file = null;
                if (null == url) {
                    file = new File(getDataFilePath());
                }
                if (file.exists()) {
                    FileUtils.forceDelete(file);
                }
                storeToYaml(prepareDatas, file);

            } else {
                // 2、一个case一个yaml的情况
                File folder = null;
                if (null == url) {
                    folder = new File(getDataFolderPath());
                }
                if (folder.exists() && folder.isDirectory()) {
                    FileUtils.cleanDirectory(folder);
                }
                storeToYamlFolder(prepareDatas, folder);
            }
        } catch (Exception e) {
            logger.error("", e);
        }
    }

    /**
     * Acts的DataProvider
     *
     * @param method
     * @return
     * @throws IOException
     */
    @DataProvider(name = "TestifyDataProvider")
    public Iterator<Object[]> getDataProvider(Method method) throws IOException {
        try {
            TestifyTestMethod = method;
            testedMethodName = method.getName();
            prepareTestData();
            if (CollectionUtils.isEmpty(prepareDatas)) {
                return null;
            }

            String errMsg = dbCheckFlagC(prepareDatas);
            if (!errMsg.isEmpty()) {
                throw new TestifyException(errMsg);
            }

            List<Object[]> prepareDataList = Lists.newArrayList();
            String rexKeyName = "test_only";
            if (System.getProperty("os.arch").equals("aarch64")) {
                // 执行arm机器打标用例
                rexKeyName = "arm_test_only";
            }
            String rexStr = ConfigrationFactory.getConfigration().getPropertyValue(rexKeyName);
            if (StringUtils.isBlank(rexStr)) {
                rexStr = ".*";
            } else {
                rexStr = rexStr + ".*";
            }
            logger.info("Run cases matching regex: [" + rexStr + "]");
            Pattern pattern = Pattern.compile(rexStr);
            // 排序
            TreeMap<String, PrepareData> treeMap = new TreeMap<String, PrepareData>(new Comparator<String>() {
                @Override
                public int compare(String str1, String str2) {
                    return str1.compareTo(str2);
                }
            });
            treeMap.putAll(prepareDatas);
            for (String caseId : treeMap.keySet()) {
                if (prepareDatas.get(caseId).getDescription() != null) {
                    Matcher matcher = pattern.matcher(prepareDatas.get(caseId).getDescription());
                    if (!matcher.find()) {
                        logger.info("["
                                + prepareDatas.get(caseId).getDescription()
                                + "] does not match ["
                                + rexStr
                                + "], skip it");
                        continue;
                    }
                }

                // runOnly本地调试过滤用例
                if (TestifyTestMethod.isAnnotationPresent(RunOnly.class) && !BaseDataUtil.isCI()) {
                    String[] caseList =
                            TestifyTestMethod.getAnnotation(RunOnly.class).caseList();
                    boolean match = false;
                    for (String pStr : caseList) {
                        if (Pattern.matches(pStr, caseId)) {
                            match = true;
                            break;
                        }
                    }
                    if (!match) {
                        continue;
                    }
                }

                String desc = prepareDatas.get(caseId).getDescription();
                desc = (desc == null) ? "" : desc;
                Object[] args = new Object[] {caseId, desc, prepareDatas.get(caseId)};
                prepareDataList.add(args);
            }

            if (prepareDataList.size() == 0) {
                logger.warn("[NoValidCaseFound] 本次执行未找到有效用例.");
            }

            return prepareDataList.iterator();
        } catch (Exception e) {
            logger.error("Prepare Data Exception!", e);
            throw new RuntimeException(e);
        } catch (Error error) {
            logger.error("Prepare Data Error!", error);
            throw new RuntimeErrorException(error);
        }
    }

    /**
     * 强校验DB数据是否设置C flag用作清理数据
     *
     * @param prepareDatas
     * @return
     */
    private String dbCheckFlagC(Map<String, PrepareData> prepareDatas) {
        StringBuffer sb = new StringBuffer();

        for (String caseId : prepareDatas.keySet()) {
            PrepareData prepareData = prepareDatas.get(caseId);

            if (null != prepareData.getDepDataSet()) {
                for (VirtualTable preTable : prepareData.getDepDataSet().getVirtualTables()) {
                    if (!preTable.getFlags().values().contains(DBFlagEnum.C.name())) {
                        sb.append("\n["
                                + caseId
                                + "]"
                                + preTable.getTableName()
                                + " from PrepareDBData requires at least one C flag to"
                                + " clean up data.");
                    }
                }
            }

            if (null != prepareData.getExpectDataSet()) {
                for (VirtualTable expTable : prepareData.getExpectDataSet().getVirtualTables()) {
                    if (!expTable.getFlags().values().contains(DBFlagEnum.C.name())
                            && !expTable.getFlags().values().contains(DBFlagEnum.CN.name())) {
                        sb.append("\n["
                                + caseId
                                + "]"
                                + expTable.getTableName()
                                + " from CheckDBData requires at least one C or CN flag to"
                                + " clean up data.");
                    }
                }
            }
        }

        return sb.toString();
    }

    /**
     * 获取被测对象
     *
     * @return
     */
    protected Object getTestedObj() {
        List<Field> fields = AnnotationUtils.findFields(getClass(), TestBean.class);
        if (fields == null || fields.size() != 1) {
            logger.error("没有找到被测类,请检查被测bean是否添加了@TestBean注解.如果没有发布服务的bean,请添加XAutoWire注解");
            throw new RuntimeException("没有找到被测类,请检查被测bean是否添加了@TestBean注解.如果没有发布服务的bean,请添加XAutoWire注解");
        }
        Object testedObj;
        try {
            fields.get(0).setAccessible(true);
            testedObj = fields.get(0).get(this);
        } catch (Exception e) {
            logger.error("当前测试bean获取出现异常，且确保测试bean已注入，排查详见FAQ");
            throw new RuntimeException();
        }

        if (testedObj == null) {
            logger.error("没有找到被测类,请检查被测bean注入,如bean名是否正确.");
            throw new RuntimeException("没有找到被测类,请检查被测bean注入,如bean名是否正确.");
        }
        return testedObj;
    }

    /**
     * 获取被测接口
     *
     * @return
     */
    protected String getTestedInterface() {
        List<Field> fields = AnnotationUtils.findFields(getClass(), TestBean.class);
        if (fields == null || fields.size() != 1) {
            return null;
        }

        return fields.get(0).getType().getSimpleName();
    }

    /**
     * 获取数据驱动文件地址
     *
     * @return
     */
    protected String getDataFilePath() {
        List<Field> fields = AnnotationUtils.findFields(getClass(), TestBean.class);
        if (fields == null || fields.size() != 1) {
            throw new TestifyException(
                    "Specified method not found, check wether service has started, or add XAutoWire" + " annotations");
        }
        try {
            fields.get(0).setAccessible(true);
        } catch (Exception e) {
            return null;
        }
        String fileName = this.getClass().getSimpleName() + "." + testedMethodName + ".yaml";
        // 相对路径
        String relativePath = getTestPath() + "/";
        String fileFullName = relativePath + fileName;
        fileFullName = FileUtil.getFilePathByResource(this.getClass().getClassLoader(), fileFullName);
        this.testDataFilePath = fileFullName;
        return fileFullName;
    }

    /**
     * 获取数据驱动文件夹地址，用于每个case一个yaml文件的场景
     *
     * @return
     */
    protected String getDataFolderPath() {
        List<Field> fields = AnnotationUtils.findFields(getClass(), TestBean.class);
        if (fields == null || fields.size() != 1) {
            throw new TestifyException(
                    "Specified method not found, check wether service has started, or add XAutoWire" + " annotations");
        }
        try {
            fields.get(0).setAccessible(true);
        } catch (Exception e) {
            return null;
        }
        String folderName = this.getClass().getSimpleName();
        // 相对路径
        String relativePath = getTestPath() + "/";
        String folderFullName = relativePath + folderName;
        folderFullName = FileUtil.getFilePathByResource(this.getClass().getClassLoader(), folderFullName);
        this.testDataFolderPath = folderFullName;
        return folderFullName;
    }

    /**
     * 获取新模式的用例文件夹
     *
     * @return
     */
    protected List<File> scanCaseFolders(String testPath) {
        List<File> caseFolders = new ArrayList<File>();
        File testFolder = new File(testPath);

        for (File file : testFolder.listFiles()) {
            if (file.isDirectory() && file.getName().contains("case")) {
                caseFolders.add(file);
            }
        }

        return caseFolders;
    }

    protected String getTestPath() {
        String testPath =
                "src/test/java/" + this.getClass().getPackage().getName().replace(".", "/");
        return FileUtil.getFilePathByResource(this.getClass().getClassLoader(), testPath);
    }

    /**
     * 通用的beforeActsTest，可以在子类重写
     *
     * @param testifyRuntimeContext
     */
    public void beforeTestifyTest(TestifyRuntimeContext testifyRuntimeContext) {

        for (Method m : this.getClass().getDeclaredMethods()) {
            // 遍历执行本次用例的对应准备方法
            if (m.isAnnotationPresent(PrepareCase.class)
                    && Pattern.matches(m.getAnnotation(PrepareCase.class).value(), testifyRuntimeContext.getCaseId())) {
                try {
                    m.invoke(this, testifyRuntimeContext);
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.error("【PrepareCase】执行准备方法时报错：", e);
                    throw new TestifyException(e);
                }
            }
        }
    }

    /**
     * 通用的beforeActsTest，可以在子类重写
     *
     * @param paramMap
     */
    public void beforeTestifyTest(Map<String, Object> paramMap) {}

    /**
     * 通用的afterActsTest，可以在子类重写
     *
     * @param context
     */
    public void afterTestifyTest(TestifyRuntimeContext context) {

        for (Method m : this.getClass().getDeclaredMethods()) {
            // 遍历执行本次用例的对应准备方法
            if (m.isAnnotationPresent(CheckCase.class)
                    && Pattern.matches(m.getAnnotation(CheckCase.class).value(), context.getCaseId())) {
                try {
                    m.invoke(this, context);
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.error("【CheckCase】执行校验方法时报错：", e);
                    throw new TestifyException(e);
                }
            }
        }
    }

    /**
     * 对预期校验项（结果、异常、消息）的某些字段设置不校验，格式:类(子类对象)#属性#是否校验(N,Y)
     *
     * @return
     */
    public List<String> setIgnoreCheckFileds() {
        return new ArrayList<String>();
    }

    /**
     * 初始化ACTS上下文
     *
     * @param caseId
     * @param prepareData
     * @param isComponent
     */
    public void initRuntimeContext(String caseId, PrepareData prepareData, boolean isComponent) {
        Object testedObj = getTestedObj();
        Method testedMethod = this.findMethod(testedMethodName, testedObj, isComponent);
        // 允许访问私有方法
        testedMethod.setAccessible(true);
        testifyRuntimeContext = new TestifyRuntimeContext(
                caseId,
                prepareData,
                new HashMap<String, Object>(),
                TestifyTestBase.dbDatasProcessor,
                testedMethod,
                testedObj);
    }

    /**
     * 初始化测试操作对象
     */
    public void initTestUnitHandler() {
        this.testUnitHandler = new TestUnitHandler(testifyRuntimeContext);
    }

    /**
     * 执行测试
     *
     * @param caseId
     * @param prepareData
     */
    public void runTest(String caseId, PrepareData prepareData) {

        if (SCAFFOLD_CASE == caseId) {
            return;
        }

        DetailCollectUtils.appendAndLog(
                "=============================Start excuting TestCase caseId:"
                        + caseId
                        + " "
                        + prepareData.getDescription()
                        + "=================",
                logger);

        // 组装上下文
        initRuntimeContext(caseId, prepareData, false);
        initTestUnitHandler();
        TestifyRuntimeContextThreadHold.setContext(testifyRuntimeContext);
        testifyRuntimeContext.componentContext.put("caseFileFullName", testDataFilePath);
        CaseResultCollectUtil.holdOriginalRequest(caseId, prepareData);
        DetailCollectUtils.appendDetail(ObjectUtil.toJson(prepareData));

        try {
            if (sqlLogger.isInfoEnabled()) {
                sqlLogger.info("acts_caseId=" + caseId);
            }
            // 所有测试之前都要执行的
            initComponentsBeforeTest(testifyRuntimeContext);
            beforeTestifyTest(testifyRuntimeContext);
            beforeTestifyTest(testifyRuntimeContext.paramMap);
            process(testifyRuntimeContext);

        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            DetailCollectUtils.appendDetail("runTest阶段异常原因:" + sw.toString());
            logger.error("runTest阶段异常", e);
            throw new TestifyException(e.getCause());

        } catch (Error err) {
            logger.error("runTest阶段错误", err);
            throw err;

        } finally {

            // 所有测试之后都要执行的
            afterTestifyTest(testifyRuntimeContext);

            // 线程变量清理
            EventContextHolder.clear();
        }
    }

    /**
     * 用例执行结果收集
     */
    @AfterClass
    protected void collectCaseResult() {
        try {

            // 清空组件
            TestifyComponentUtil.clear();

            // 用例文件全路径
            if (TestifyRuntimeContextThreadHold.getContext() == null) {

                // context init error
                logger.warn("actsRuntimeContext is null, the case doesn't run properly, please check"
                        + " for other exceptions which might stop the case running.");
                return;
            }
            String caseFilePath =
                    (String) Optional.fromNullable(TestifyRuntimeContextThreadHold.getContext().componentContext)
                            .or(Maps.<String, Object>newHashMap())
                            .get("caseFileFullName");

            if (StringUtils.isBlank(caseFilePath)) {
                logger.warn("Result file path is empty!");
            }

            // 关键日志记录文件
            DetailCollectUtils.saveBuffer(caseFilePath);

            // 不收集用例执行结果
            if (!CaseResultCollectUtil.isCollectCaseResultOpen()) {
                return;
            }

            // 搜集用例结果（仅限1.0的用例模式）
            if (prepareDatas.size() > 0 && caseFolders.isEmpty()) {
                CaseResultCollector.saveCaseResult(caseFilePath, CaseResultCollectUtil.getAllCaseDatas());
            }

            // 新数据模式返填用例结果
            if (TestifyTestMethod.isAnnotationPresent(AutoFill.class) && caseFolders.size() > 0) {
                boolean overwrite =
                        TestifyTestMethod.getAnnotation(AutoFill.class).overwrite();
                if (overwrite) {
                    BaseDataUtil.resetOverwriteVal(new File(getTestPath()));
                }

                CaseResultCollector.saveCaseResult(
                        caseFolders,
                        CaseResultCollectUtil.getAllCaseDatas(),
                        overwrite,
                        !CollectionUtils.isEmpty(testifyRuntimeContext.getBackFillSqlList()));
            }

        } catch (Exception e) {
            logger.warn("AfterClass excution failure", e);
        }
    }

    /**
     * 可以重新这个类自定义编排
     *
     * @param testifyRuntimeContext
     */
    public void process(TestifyRuntimeContext testifyRuntimeContext) {

        try {
            // 数据清理
            clear(testifyRuntimeContext);
            // 组件初始化及执行
            executeComponents(testifyRuntimeContext);
            // 数据准备
            prepare(testifyRuntimeContext);
            // 测试执行
            execute(testifyRuntimeContext);
            // 结果检查
            check(testifyRuntimeContext);

            BaseDataUtil.reportUsageStatus(
                    null, testedMethodName, TestifyActionEnum.RUN_TEST, testifyRuntimeContext.getCaseId(), "true");
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            DetailCollectUtils.appendDetail("process阶段异常原因:" + sw.toString());
            logger.error("process阶段异常", e);
            throw new TestifyException(e.getCause());

        } catch (AssertionError err) {
            // 埋点统计用例结果
            BaseDataUtil.reportUsageStatus(
                    null, testedMethodName, TestifyActionEnum.RUN_TEST, testifyRuntimeContext.getCaseId(), "false");
            throw err;

        } catch (Error err) {
            logger.error("process阶段错误", err);
            throw err;

        } finally {

            try {
                if (sqlLogger.isInfoEnabled()) {
                    sqlLogger.info("Finish acts_caseId=" + testifyRuntimeContext.getCaseId());
                }
                // 设置db预跑反填的查询sql
                if (TestifyTestMethod.isAnnotationPresent(AutoFill.class)) {
                    testifyRuntimeContext.setBackFillSqlList(
                            VelocityUtil.evaluateStringList(testifyRuntimeContext.paramMap, backFillSqlList()));
                    String[] sqlList =
                            TestifyTestMethod.getAnnotation(AutoFill.class).sqlList();
                    if (sqlList.length != 0) {
                        testifyRuntimeContext.setBackFillSqlList(VelocityUtil.evaluateStringList(
                                testifyRuntimeContext.paramMap,
                                Arrays.asList(TestifyTestMethod.getAnnotation(AutoFill.class)
                                        .sqlList())));
                    }
                }

                ClassLoader clsLoader = this.getClass().getClassLoader();
                // 保存用例执行过程数据
                CaseResultCollectUtil.holdProcessData(
                        testifyRuntimeContext, EventContextHolder.getBizEvent(), clsLoader);

                // 多阶段接口执行
                try {
                    invokeTestifyMethods(Executor.class, testifyRuntimeContext);
                } finally {
                    // 清理
                    clear(testifyRuntimeContext);
                }

            } catch (TestifyException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 仅获取测试脚本对应的yaml文件地址
     *
     * @return
     */
    protected String getYamlPath() {
        String fileName = this.getClass().getSimpleName() + "." + testedMethodName + ".yaml";
        // 相对路径
        String relativePath =
                "src/test/java/" + this.getClass().getPackage().getName().replace(".", "/") + "/";
        String fileFullName = relativePath + fileName;
        this.testDataFilePath = fileFullName;
        return fileFullName;
    }

    /**
     * 初始化执行组件及自定义参数
     *
     * @param testifyRuntimeContext
     */
    public void initComponentsBeforeTest(TestifyRuntimeContext testifyRuntimeContext) {

        // 先初始化组件
        testUnitHandler.initComponents();
        // 在组件执行之前先进行一把组件的清理动作，避免脏数据干扰
        testUnitHandler.clearComponents();
        // 自定义入参替换，将自定义参数放入parameter中
        testUnitHandler.prepareUserPara();
    }

    /**
     * 可重写自定义组件executeComponents阶段
     *
     * @param testifyRuntimeContext
     * @throws TestifyException
     */
    public void executeComponents(TestifyRuntimeContext testifyRuntimeContext) throws TestifyException {

        // 调用cci组件组件
        testUnitHandler.excutedefaultCmdComponent(testTemplate);
    }

    /**
     * 可重写自定义prepare阶段
     *
     * @param testifyRuntimeContext
     * @throws TestifyException
     */
    public void prepare(TestifyRuntimeContext testifyRuntimeContext) throws TestifyException {

        logger.info("=============================[test prepare" + " begin]=============================\r\n");
        // 注解执行前准备
        invokeTestifyMethods(BeforePrepare.class, testifyRuntimeContext);
        // 数据准备阶段之前组件执行
        testUnitHandler.executeComponents(testifyRuntimeContext.BeforePreparePreList);
        // 数据准备
        testUnitHandler.prepareDepData(null);
        // Hbase数据准备
        //        testUnitHandler.prepareHbaseData();
        // 执行ccil准备组件
        //        testUnitHandler.excutePreCmdComponent(testTemplate);
        // 执行准备阶段之后的组件
        testUnitHandler.executeComponents(testifyRuntimeContext.AfterPreparePreList);
        // 注解执行准备后执行
        invokeTestifyMethods(AfterPrepare.class, testifyRuntimeContext);
        logger.info("=============================[test prepare end]=============================\r\n");
    }

    /**
     * 可重写自定义执行阶段
     *
     * @param testifyRuntimeContext
     * @throws TestifyException
     */
    public void execute(TestifyRuntimeContext testifyRuntimeContext) throws TestifyException {
        logger.info("=============================[acts execute" + " begin]=============================\r\n");
        // 调用方法
        testUnitHandler.execute();
        logger.info("=============================[acts execute end]=============================\r\n");
    }

    /**
     * 可重写自定义check阶段
     *
     * @param testifyRuntimeContext
     * @throws TestifyException
     */
    public void check(TestifyRuntimeContext testifyRuntimeContext) throws TestifyException {

        logger.info("=============================[acts check begin]=============================\r\n");
        // 注解执行check之前
        invokeTestifyMethods(BeforeCheck.class, testifyRuntimeContext);
        // 执行检查阶段之前的组件
        testUnitHandler.executeComponents(testifyRuntimeContext.BeforeCheckPreList);
        // 检查结果
        ignoreCheckFields(testifyRuntimeContext);
        testUnitHandler.checkException();
        testUnitHandler.checkExpectDbData(null);
        testUnitHandler.checkExpectEvent();
        testUnitHandler.checkExpectResult();
        // 执行检查阶段之后的组件
        // testUnitHandler.excuteCheckComponent(testTemplate, testifyRuntimeContext.AfterCheckPreList);
        // 注解执行check之后
        invokeTestifyMethods(AfterCheck.class, testifyRuntimeContext);

        // 若配置了线程变量的校验，则在调用完方法之后校验是否清理了指定的线程变量
        ThreadLocalsCheckUtil.validateConfThreadLocalNotExist(this.getClass().getName());

        logger.info("=============================[acts check end]=============================\r\n");
    }

    /**
     * 可重写自定义clear阶段
     *
     * @param testifyRuntimeContext
     * @throws TestifyException
     */
    public void clear(TestifyRuntimeContext testifyRuntimeContext) throws TestifyException {
        // 执行清理阶段之前的组件
        logger.info("=============================[test clear begin]=============================\r\n");
        invokeTestifyMethods(BeforeClean.class, testifyRuntimeContext);
        testUnitHandler.executeComponents(testifyRuntimeContext.BeforeClearPreList);
        testUnitHandler.clearDepData(null);
        testUnitHandler.clearExpectDBData(null);

        // 执行清理阶段之后的组件
        testUnitHandler.executeClearComponent(testTemplate, testifyRuntimeContext.AfterClearPreList);

        invokeTestifyMethods(AfterClean.class, testifyRuntimeContext);
        logger.info("=============================[acts clear end]=============================\r\n");
    }

    /**
     * 获取被测对象的被测方法
     *
     * @param methodName
     * @param testedObj
     * @return
     */
    protected Method findMethod(String methodName, Object testedObj, boolean isComponent) {

        if (isComponent) {
            Method[] scriptMethods = this.getClass().getDeclaredMethods();
            for (Method method : scriptMethods) {
                if (method.isAnnotationPresent(Test.class)) {
                    methodName = method.getName();
                }
            }
        }
        Class<?>[] clazzes;
        try {
            clazzes = AopProxyUtils.proxiedUserInterfaces(testedObj);
        } catch (Exception e) {
            clazzes = new Class<?>[] {testedObj.getClass()};
        }
        if (clazzes != null) {
            for (Class<?> clazz : clazzes) {
                while (clazz != null && !clazz.equals(Object.class)) {
                    Method[] methods = clazz.getDeclaredMethods();
                    if (methods != null) {
                        for (Method method : methods) {
                            if (method != null && StringUtils.equalsIgnoreCase(method.getName(), methodName)) {
                                return method;
                            }
                        }
                    }
                    clazz = clazz.getSuperclass();
                }
            }
        }
        // 接口中没有找到，则直接在对象里尝试寻找
        Method[] publicMethods = testedObj.getClass().getMethods();
        for (Method method : publicMethods) {
            if (StringUtils.equalsIgnoreCase(method.getName(), methodName)) {
                return method;
            }
        }
        return null;
    }

    /**
     * 把数据存入yaml
     *
     * @param prepareDatas
     * @param file
     */
    public void storeToYaml(Map<String, PrepareData> prepareDatas, File file) {
        BaseDataUtil.storeToYaml(prepareDatas, file);
    }

    /**
     * 把数据存入yaml文件夹，适用于一个case一个yaml文件的情况
     *
     * @param prepareDatas
     * @param file
     */
    public void storeToYamlFolder(Map<String, PrepareData> prepareDatas, File folder) {
        BaseDataUtil.storeToYamlByCase(prepareDatas, folder, null);
    }

    /**
     * 从yaml文件读取
     *
     * @param file
     * @param classLoader
     * @return
     */
    public Map<String, PrepareData> loadFromYaml(File file, ClassLoader classLoader) {
        if (file.exists()) {
            String str;
            try {
                str = FileUtils.readFileToString(file);
            } catch (Exception e) {
                Assert.assertTrue(false, "Failed to read yaml,  Exception:");
                e.printStackTrace();
                return null;
            }
            return BaseDataUtil.loadFromYaml(str, classLoader);
        }
        return null;
    }

    /**
     * 从yaml文件夹读取，适用于一个case一个yaml文件的情况
     *
     * @param folder
     * @param classLoader
     * @return
     */
    public Map<String, PrepareData> loadFromYamlFolder(File folder, ClassLoader classLoader) {
        if (folder.exists() && folder.isDirectory()) {
            return BaseDataUtil.loadFromYamlByCase(folder, classLoader, null);
        }
        return null;
    }

    /**
     * 查询指定测试脚本、指定case的yaml数据
     *
     * @param componentClass
     * @param caseId
     * @return
     */
    private PrepareData searchPrepareData(String componentClass, String caseId) {

        Map<String, PrepareData> args = new HashMap<String, PrepareData>();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();

        String testClass = componentClass.replace(".", "/");
        int i = testClass.lastIndexOf("/");
        String testClassDir = FileUtil.getFilePathByResource(
                this.getClass().getClassLoader(), "src/test/java/" + testClass.substring(0, i));

        try {
            List<File> caseFolders = scanCaseFolders(testClassDir);

            // 并存的情况下，优先执行2.0用例
            if (caseFolders.size() > 0) {

                args = BaseDataUtil.loadFromCaseFolders(
                        caseFolders, this.getClass().getClassLoader());

            } else {

                File yamlFile = getYamlFile(testClassDir);
                if (null != yamlFile && yamlFile.exists()) {

                    // 按所有case存储为一个yaml的情况
                    args = BaseDataUtil.loadFromYaml(yamlFile, loader, null);

                } else {

                    // 每个case一个yaml的情况
                    File yamlFolder = new File(FileUtil.getFilePathByResource(
                            this.getClass().getClassLoader(), "src/test/java/" + testClass + "/"));
                    args = BaseDataUtil.loadFromYamlByCase(yamlFolder, loader, null);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!args.isEmpty()) {
            return args.get(caseId);
        }

        return null;
    }

    /**
     * 查询指定目录下的yaml数据文件
     *
     * @param ymalFileDir
     * @return
     */
    private File getYamlFile(String ymalFileDir) {
        File f = new File(ymalFileDir);
        File s[] = f.listFiles();
        for (int i = 0; i < s.length; i++) {
            if (StringUtils.contains(s[i].getName(), "yaml") && !StringUtils.contains(s[i].getName(), "res")) {
                return s[i];
            }
        }
        return null;
    }

    /**
     *
     *
     * @param dbDatasProcessor value to be assigned to property dbDatasProcessor
     */
    public void setDbDatasProcessor(DBDatasProcessor dbDatasProcessor) {
        this.dbDatasProcessor = dbDatasProcessor;
    }

    /**
     * 获取sofa上下文，放到BundleBeansManager里
     *
     * @param name
     * @param methodName
     */
    public void adaptContext(String name, String methodName) {
        try {
            //            Field field = TestNGOsgiTest.class
            //                    .getDeclaredField(name);
            //            Method method = BundleBeansManager.class.getDeclaredMethod(methodName,
            // field.getType());
            //            method.invoke(this, field.get(this));
            //            if (field.get(this).toString().contains("SofaRuntimeContext")) {
            //                SofaRuntimeReference.setCurrentSofaRuntime((SofaRuntimeContext)
            // field.get(this));
            //            }
        } catch (Exception e) {
        }
    }

    /**
     * @param clsz
     * @param testifyRuntimeContext
     */
    public void invokeTestifyMethods(Class<? extends Annotation> clsz, TestifyRuntimeContext testifyRuntimeContext) {
        List<TestifyMethod> list = this.annoationMethods.get(clsz.getSimpleName());

        if (list == null) {
            return;
        }

        for (TestifyMethod method : list) {
            method.invoke(testifyRuntimeContext);
        }
    }

    public PrepareTemplate getPrepareTemplate() {
        return prepareTemplate;
    }

    public void setPrepareTemplate(PrepareTemplate prepareTemplate) {
        this.prepareTemplate = prepareTemplate;
    }

    /**
     * 对预期校验项（结果、异常、消息）的字段设置flag
     *
     * @param testifyRuntimeContext
     */
    protected void ignoreCheckFields(TestifyRuntimeContext testifyRuntimeContext) {
        List<String> ignoreInfo = setIgnoreCheckFileds();
        if (ignoreInfo.isEmpty()) {
            return;
        }

        Map<String, Map<String, String>> flagMap = new HashMap<String, Map<String, String>>();
        for (String flagInfo : ignoreInfo) {
            if (flagInfo.split("#").length != 3) {
                logger.warn("Illegal ignore result field: " + flagInfo);
                continue;
            }
            String objectName = flagInfo.split("#")[0];
            String filedName = flagInfo.split("#")[1];
            String flag = flagInfo.split("#")[2];

            if (flagMap.containsKey(objectName)) {
                flagMap.get(objectName).put(filedName, flag);
            } else {
                Map<String, String> fieldMap = new HashMap<String, String>();
                fieldMap.put(filedName, flag);
                flagMap.put(objectName, fieldMap);
            }
        }

        BaseDataUtil.setFlags(testifyRuntimeContext.getPrepareData(), flagMap);
    }

    //    /**
    //     * 重载方法定位，通过入参及参数类型选择正确方法
    //     * @param method
    //     * @param prepareData
    //     * @return
    //     */
    //    public boolean isThisMethod(Method method,PrepareData prepareData){
    //        int count = method.getParameterCount();
    //        VirtualArgs args = prepareData.getArgs();
    //        if(count == 0 && args == null){
    //            return true;
    //        }
    //        List<String> argsList= args.getArgTypes();
    //        Collections.replaceAll(argsList,"java.lang.Integer","int");
    //        List<String> parametersList = new ArrayList<>();
    //        Parameter[] parameters = method.getParameters();
    //        Arrays.stream(parameters).forEach(x -> {
    //            parametersList.add(x.getType().getName());
    //        });
    //        if((parametersList.size() == argsList.size()) && argsList.containsAll(parametersList)
    // && parametersList.containsAll(argsList)){
    //            return true;
    //        }
    //        return false;
    //    }

    /**
     *
     */
    public String[] getReplaceXmls() {

        String[] replaceStrs = new String[] {};

        TestifyConfiguration.loadProperties();

        if (MiniUtils.isReplaceXmls()) {
            replaceStrs = MiniUtils.loadReplaceXmls();
        }

        return replaceStrs;
    }
}
