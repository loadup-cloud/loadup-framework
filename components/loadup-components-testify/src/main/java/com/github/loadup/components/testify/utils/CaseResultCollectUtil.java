/**

 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.github.loadup.components.testify.utils;

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

import com.github.loadup.components.testify.model.*;
import com.github.loadup.components.testify.runtime.TestifyRuntimeContext;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.github.loadup.components.testify.collector.sqlLog.SqlLogCollector;
import com.github.loadup.components.testify.component.db.DBDatasProcessor;
import com.github.loadup.components.testify.constant.TestifyPathConstants;
import com.github.loadup.components.testify.util.BaseDataUtil;
import com.github.loadup.components.testify.util.DeepCopyUtils;
import com.github.loadup.components.testify.util.FileUtil;
import com.github.loadup.components.testify.utils.config.ConfigrationFactory;

import org.springframework.util.CollectionUtils;

import java.io.File;
import java.util.*;

/**
 * 用例结果收集工具类
 *
 * @author chao.gao
 * 
 *
 * hongling.xiang Exp $
 */
public class CaseResultCollectUtil {

    /**
     * logger
     */
    private static final Logger logger = LoggerFactory
            .getLogger(CaseResultCollectUtil.class);

    /**
     * sql日志文件
     */
    private static final String SQL_LOG_PATH_NAME = "./logs/acts-sql.log";

    /**
     * 用例ID与用例数据映射关系
     */
    private static final Map<String, PrepareData> caseDatas = new HashMap<String, PrepareData>();

    /**
     * 保存原始请求PrepareData，避免用户重写beforeTest为PrepareData设置另外值
     *
     * @param caseId
     * @param prepareData
     */
    public static void holdOriginalRequest(String caseId, PrepareData prepareData) {

        if (null == prepareData || !isCollectCaseResultOpen()) {
            return;
        }

        // 保存当前用例执行数据
        caseDatas.put(caseId, DeepCopyUtils.deepCopy(prepareData));
    }

    /**
     * @param testifyRuntimeContext
     * @param events
     * @param clsLoader
     */
    public static void holdProcessData(TestifyRuntimeContext testifyRuntimeContext,
                                       Map<String, List<Object>> events,
                                       ClassLoader clsLoader) {

        String caseId = testifyRuntimeContext.getCaseId();
        Object actualResultObj = testifyRuntimeContext.getResultObj();
        //Object actualExceptionObj = actsRuntimeContext.getExceptionObj();

        // 不收集用例执行结果，则不保存中间数据
        if (!isCollectCaseResultOpen()) {
            return;
        }

        // 提取先前深拷贝的用例数据
        PrepareData newPrepareData = caseDatas.get(caseId);

        if (null == newPrepareData) {
            return;
        }

        try {
            // 设置预期结果
            if (null != actualResultObj) {
                VirtualResult expectResult = buildVirtualObject(actualResultObj, clsLoader);
                newPrepareData.setExpectResult(expectResult);
            }

            //if (null != actualExceptionObj) {
            //    VirtualException virtualException = new VirtualException();
            //    virtualException.setExpectException(new VirtualObject(actualExceptionObj));
            //    newPrepareData.setExpectException(virtualException);
            //}

            // 设置预期事件
            if (!CollectionUtils.isEmpty(events)) {
                newPrepareData.setExpectEventSet(buildExpEvents(events, clsLoader));
            }

            //设置组件反填结果
            if (isCollectComponentResultOpen()) {
                Map<String, Map<String, Object>> componentsResultMap = testifyRuntimeContext
                        .getComponentsResultMap();
                if (!CollectionUtils.isEmpty(componentsResultMap)) {
                    List<VirtualComponent> VirtualComponents = buildComponentResult(
                            componentsResultMap, newPrepareData, clsLoader);
                    newPrepareData.getVirtualComponentSet().setComponents(VirtualComponents);

                }
            }

            // 收集数据库操作表数据
            if (!CollectionUtils.isEmpty(testifyRuntimeContext.getBackFillSqlList())) {
                VirtualDataSet expectDataSet = new VirtualDataSet();
                expectDataSet.addTables(
                        collectDbDataBySql(testifyRuntimeContext.getDbDatasProcessor(), testifyRuntimeContext.getBackFillSqlList()));
                newPrepareData.setExpectDataSet(expectDataSet);
            } else {
                // 收集数据库操作表数据
                File logfile = new File(SQL_LOG_PATH_NAME);
                if (logfile.exists()) {
                    collectSqlLog(caseId, newPrepareData);
                }
            }

            caseDatas.put(caseId, newPrepareData);

        } catch (Throwable t) {
            logger
                    .warn("Collecting case result-unknown exception parsing SQL, caseId=" + caseId, t);
        }

    }


    /**
     * 通过db直接查询出db数据
     *
     * @param sqlList
     * @return
     */
    public static List<VirtualTable> collectDbDataBySql(DBDatasProcessor processor, List<String> sqlList) {
        List<VirtualTable> virtualTableList = new ArrayList<VirtualTable>();
        if (CollectionUtils.isEmpty(sqlList)) {
            return new ArrayList<VirtualTable>();
        }

        for (String sql : sqlList) {
            //解析出sql中的tableName
            String tableName = StringUtils.substringBetween(StringUtils.toRootLowerCase(sql), "from", "where").trim();
            if (StringUtils.isEmpty(tableName)) {
                continue;
            }
            //解析出where条件后续对应字段默认打C
            String conditions = sql.substring(sql.toLowerCase().indexOf("where"));

            logger.info("ACTS:查询sql= " + sql);
            List<Map<String, Object>> tableList = processor.queryForList(tableName, sql);
            if (CollectionUtils.isEmpty(tableList)) {
                logger.info("ACTS:该表" + tableName + "没有查询到结果");
                continue;
            }

            VirtualTable virtualTable = new VirtualTable();
            virtualTable.setTableName(tableName);
            virtualTable.setTableData(tableList);
            Map<String, String> flags = new HashMap<String, String>();
            for (Map.Entry<String, Object> map : tableList.get(0).entrySet()) {
                if (conditions.toLowerCase().contains(map.getKey().toLowerCase())) {
                    flags.put(map.getKey(), "C");
                } else {
                    flags.put(map.getKey(), "Y");
                }
            }

            virtualTable.setFlags(flags);
            virtualTableList.add(virtualTable);
        }
        return virtualTableList;
    }

    /**
     * 获取反填对象的flag
     *
     * @param cl
     * @param actualResultObj
     * @return
     */
    public static Map<String, Map<String, String>> getObjectFlag(ClassLoader cl,
                                                                 Object actualResultObj) {

        String ObjectBaseName = actualResultObj.getClass().getSimpleName();
        File folder = FileUtil.getTestResourceFile(TestifyPathConstants.OBJECT_DATA_PATH);

        if (!folder.exists()) {
            return null;
        }
        // 兼容处理
        String dbModelFullPath = folder.getAbsolutePath();

        Map<String, Map<String, String>> flags = BaseDataUtil.getObjBaseFlags(cl, ObjectBaseName,
                null, dbModelFullPath, "GBK");

        return flags;
    }

    /**
     * 收集SQL日志，生成涉及表数据
     */
    public static void collectSqlLog(String caseId, PrepareData rootPrepareData) {
        PrepareData prepareData = rootPrepareData;

        // 解析生成用例对应的所有表
        Map<String, List<List<String>>> passedCaseSqlLog = SqlLogCollector.collectConcernedSqlLog(
                SQL_LOG_PATH_NAME, caseId);

        //此处做轮询，获取所有含caseId的sql,包含组件，格式为（主被测方法Id||组件Id）
        Map<String, List<List<String>>> singlePassedCaseSqlLog = new HashMap<String, List<List<String>>>();
        for (String key : passedCaseSqlLog.keySet()) {

            //筛选目标caseId对应数据，避免解析脏数据
            if (StringUtils.contains(key, caseId)) {
                singlePassedCaseSqlLog.put(key, passedCaseSqlLog.get(key));
            }
        }

        //循环反填包含组件的所有内容
        for (String key : singlePassedCaseSqlLog.keySet()) {

            List<List<String>> curCaseSqlLog = singlePassedCaseSqlLog.get(key);
            if (CollectionUtils.isEmpty(curCaseSqlLog)) {
                break;
            }

            // 解析当前用例sql日志获取表数据
            List<VirtualTable> caseVirtualTables = SqlLogCollector.parseSqlLog(curCaseSqlLog);

            //对于相同的表,进行合并
            caseVirtualTables = mergeAllSameTables(caseVirtualTables);

            if (CollectionUtils.isEmpty(caseVirtualTables)) {
                break;
            }

            // 填充PrepareData设置DB预期结果
            VirtualDataSet expectDataSet = new VirtualDataSet();
            expectDataSet.addTables(caseVirtualTables);

            //循环填充组件结果
            String[] keyStrs = key.split("\\|");
            for (int i = 0; i < keyStrs.length; i++) {
                if (i == (keyStrs.length - 1)) {
                    prepareData.setExpectDataSet(expectDataSet);
                } else {
                    prepareData = getComponentPreparedata(prepareData, keyStrs[i]);
                }
            }

        }

    }


    /**
     * 返回嵌套的组件preparedata
     *
     * @param key
     * @return
     */
    public static PrepareData getComponentPreparedata(PrepareData prepareData, String key) {
        List<VirtualComponent> components = new LinkedList<VirtualComponent>();
        components = prepareData.getVirtualComponentSet().getComponents();
        for (VirtualComponent virtualComponent : components) {
            if (virtualComponent.getComponentId().equals(key)) {
                return virtualComponent.getPrepareData();

            }
        }
        //
        return null;
    }

    /***
     * 合并相同的表
     *
     * @param caseVirtualTables
     * @return
     */
    public static List<VirtualTable> mergeAllSameTables(List<VirtualTable> caseVirtualTables) {
        List<VirtualTable> result = new ArrayList<VirtualTable>();

        Set<String> distinctTableNames = getDistinctTableNames(caseVirtualTables);

        for (String tableName : distinctTableNames) {
            List<VirtualTable> sameTables = filterByTableName(caseVirtualTables, tableName);
            VirtualTable vt = combineSameTables(sameTables);
            result.add(vt);
        }
        return result;
    }

    /***
     * 合并一张相同的表
     *
     * @param caseVirtualTables
     * @return
     */
    private static VirtualTable combineSameTables(List<VirtualTable> caseVirtualTables) {
        VirtualTable result = new VirtualTable();
        if (caseVirtualTables != null && caseVirtualTables.size() > 0) {
            result = DeepCopyUtils.deepCopy(caseVirtualTables.get(0));
        }
        //清空表的内容,然后再加
        result.getTableData().clear();
        for (VirtualTable vt : caseVirtualTables) {
            result.getTableData().addAll(vt.getTableData());
        }

        return result;
    }

    /***
     * 获取唯一的表名集合
     *
     * @param caseVirtualTables
     * @return
     */
    private static Set<String> getDistinctTableNames(List<VirtualTable> caseVirtualTables) {
        Set<String> distinctTableNames = new HashSet<String>();
        for (VirtualTable vt : caseVirtualTables) {
            distinctTableNames.add(vt.getTableName());
        }
        return distinctTableNames;

    }

    /***
     * 根据表名取出相同的表集合
     *
     * @param caseVirtualTables
     * @param tableName
     * @return
     */
    private static List<VirtualTable> filterByTableName(List<VirtualTable> caseVirtualTables,
                                                        String tableName) {
        List<VirtualTable> result = new ArrayList<VirtualTable>();
        for (VirtualTable vt : caseVirtualTables) {
            if (StringUtils.equalsIgnoreCase(vt.getTableName(), tableName)) {
                result.add(vt);
            }
        }

        return DeepCopyUtils.deepCopy(result);

    }

    /**
     * 构建预期结果对象
     *
     * @param actualResultObj
     * @param clsLoader
     * @return
     */
    private static VirtualResult buildVirtualObject(Object actualResultObj, ClassLoader clsLoader) {

        VirtualObject virtualObject = new VirtualObject(actualResultObj, actualResultObj.getClass()
                .getSimpleName());
        virtualObject.setDescription(actualResultObj.getClass().getSimpleName());
        //获取模板中的flag
        //        Map<String, Map<String, String>> flags = getObjectFlag(clsLoader, actualResultObj);
        //        virtualObject.setFlags(flags);

        VirtualResult virtualResult = new VirtualResult();
        virtualResult.setResult(virtualObject);

        return virtualResult;
    }

    /**
     * 构造预期事件
     *
     * @param events
     * @return
     */
    public static VirtualEventSet buildExpEvents(Map<String, List<Object>> events,
                                                 ClassLoader clsLoader) {

        VirtualEventSet virtualEventSet = new VirtualEventSet();
        for (String key : events.keySet()) {
            String[] keys = key.split("\\|");
            List<Object> payloads = events.get(key);
            for (Object payload : payloads) {
                virtualEventSet.addEventObject(payload, keys[0], (keys.length == 2) ? keys[1] : "");

            }
        }

        //给每个消息体赋值flag
        //        for (VirtualEventObject virtualEventObject : virtualEventSet.getVirtualEventObjects()) {
        //
        //            VirtualObject eventObject = virtualEventObject.getEventObject();
        //            Map<String, Map<String, String>> flags = getObjectFlag(clsLoader, eventObject);
        //            eventObject.setFlags(flags);
        //            virtualEventObject.setEventObject(eventObject);
        //        }

        return virtualEventSet;
    }

    /**
     * 设置组件反填结果
     *
     * @param componentsResultMap
     * @param prepareData
     * @param clsLoader
     */
    private static List<VirtualComponent> buildComponentResult(Map<String, Map<String, Object>> componentsResultMap,
                                                               PrepareData prepareData,
                                                               ClassLoader clsLoader) {

        List<VirtualComponent> components = new LinkedList<VirtualComponent>();
        components = prepareData.getVirtualComponentSet().getComponents();
        //遍历perpare中的组件和运行组件结果做匹配
        for (VirtualComponent virtualComponent : components) {
            String ComponentId = virtualComponent.getComponentId();
            for (String key : componentsResultMap.keySet()) {
                if (key == ComponentId) {
                    Map<String, Object> componentResult = componentsResultMap.get(key);
                    for (String resultKey : componentResult.keySet()) {
                        if (resultKey.equals("virtualResult")) {
                            //匹配上做组件运行结果填充
                            VirtualResult expectResult = buildVirtualObject(
                                    componentResult.get(resultKey), clsLoader);
                            virtualComponent.getPrepareData().setExpectResult(expectResult);
                        }
                        if (key.equals("virtualEventSet")) {
                            //匹配上组件消息结果填充
                            virtualComponent.getPrepareData().setExpectEventSet(
                                    (VirtualEventSet) componentResult.get(resultKey));
                        }

                    }
                }
            }

        }

        return components;

    }

    /**
     * 获取所有的用例数据
     *
     * @return
     */
    public static Map<String, PrepareData> getAllCaseDatas() {
        return caseDatas;
    }

    /**
     * 收集用例结果开关是否打开
     *
     * @return
     */
    public static boolean isCollectCaseResultOpen() {

        String isCollectCaseResult = ConfigrationFactory.getConfigration().getPropertyValue(
                "collect_case_result");

        // 默认搜集
        if (StringUtils.isBlank(isCollectCaseResult)) {
            return false;
        }

        return StringUtils.equalsIgnoreCase(isCollectCaseResult.trim(), Boolean.TRUE.toString());
    }

    /**
     * 判断是否生成ACTS 2.0的caseObjs_actual.yaml
     *
     * @return
     */
    public static boolean isGenActualYaml() {

        String gen_actual_yaml = ConfigrationFactory.getConfigration().getPropertyValue(
                "gen_actual_yaml");

        // 默认搜集
        if (StringUtils.isBlank(gen_actual_yaml)) {
            return false;
        }

        return StringUtils.equalsIgnoreCase(gen_actual_yaml.trim(), Boolean.TRUE.toString());

    }

    /**
     * 用例组件结果开关是否打开：决定是否收集组件的结果
     *
     * @return
     */
    public static boolean isCollectComponentResultOpen() {

        String isCollectComponentResult = ConfigrationFactory.getConfigration().getPropertyValue(
                "collect_case_component_result");

        // 默认collect_case_component_result为空时不进行组件反填
        if (StringUtils.isBlank(isCollectComponentResult)) {
            return true;
        }

        return StringUtils
                .equalsIgnoreCase(isCollectComponentResult.trim(), Boolean.TRUE.toString());
    }

}
