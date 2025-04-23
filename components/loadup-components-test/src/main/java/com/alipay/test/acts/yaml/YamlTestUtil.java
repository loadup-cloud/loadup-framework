/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.alipay.test.acts.yaml;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;

import org.apache.commons.lang3.StringUtils;
import com.alipay.test.acts.constant.ActsConfigConstants;
import com.alipay.test.acts.constant.ActsSpecialMapConstants;
import com.alipay.test.acts.context.ActsCaseContextHolder;
import com.alipay.test.acts.db.ActsDBUtil;
import com.alipay.test.acts.db.convertor.DataRowConvertor;
import com.alipay.test.acts.db.enums.DataBaseTypeEnum;
import com.alipay.test.acts.db.model.DBConnection;
import com.alipay.test.acts.log.ActsLogUtil;
import com.alipay.test.acts.msg.UniformEventUtil;
import com.alipay.test.acts.object.comparer.UnitComparer;
import com.alipay.test.acts.object.enums.UnitFlagEnum;
import com.alipay.test.acts.object.manager.ObjectCompareManager;
import com.alipay.test.acts.yaml.cpUnit.BaseCPUnit;
import com.alipay.test.acts.yaml.cpUnit.DataBaseCPUnit;
import com.alipay.test.acts.yaml.cpUnit.GroupDataBaseCPUnit;
import com.alipay.test.acts.yaml.cpUnit.ListObjectCPUnit;
import com.alipay.test.acts.yaml.cpUnit.MessageCPUnit;
import com.alipay.test.acts.yaml.cpUnit.ObjectCPUnit;
import com.alipay.test.acts.yaml.cpUnit.property.BaseUnitProperty;
import com.alipay.test.acts.yaml.cpUnit.property.ListObjectUnitProperty;
import com.alipay.test.acts.yaml.cpUnit.property.ObjectUnitProperty;
import com.alipay.test.acts.yaml.enums.CheckPointActionEnum;

/**
 * Yaml对象处理工具类
 * 
 * @author baishuo.lp
 * @version $Id: CPUnitProcessor.java, v 0.1 2015年8月17日 下午3:18:36 baishuo.lp Exp $
 */
public class YamlTestUtil {

    private static final Log logger              = LogFactory.getLog(YamlTestUtil.class);

    private static boolean   skipCompareDBLength = StringUtils
                                                     .equalsIgnoreCase("",
                                                         "true");

    private static boolean   isSingleYaml        = !StringUtils
                                                     .equalsIgnoreCase("",
                                                         "false");

    /**
     * 准备数据
     * 
     * @param unit
     */
    public static void prepareDB(BaseCPUnit unit) {

        //1. 加载预期数据
        DataBaseCPUnit dbUnit = (DataBaseCPUnit) unit;
        dbUnit.loadUniqueMap();

        //2. 待清理上下文添加列
        ActsCaseContextHolder.get().getPreCleanContent().add(dbUnit);

        //3. 预清理数据
        clean(dbUnit);

        //4. 如果特殊标记只删不插，直接返回
        boolean onlyDelete = parseBoolean(dbUnit.getSpecialMap().get(
            ActsSpecialMapConstants.ONLYDELETE));
        if (onlyDelete) {
            //标记ONLYDELETE的数据，仅在准备数据时预清理
            return;
        }

        //5. 连接数据库，生成并执行语句
        executeUpdate(dbUnit, CheckPointActionEnum.PREPARE);
    }

    /**
     * 普通数据校验
     * 
     * @param unit
     */
    public static void checkDB(BaseCPUnit unit) {
        //1. 加载预期数据
        DataBaseCPUnit dbUnit = (DataBaseCPUnit) unit;
        dbUnit.loadUniqueMap();

        //2. 待清理上下文添加列
        ActsCaseContextHolder.get().getPreCleanContent().add(dbUnit);

        //3. 连接数据库，生成并执行语句
        List<Map<String, Object>> rawResult = executeQuery(dbUnit);

        //4. 校验数据库数据
        if (parseBoolean(dbUnit.getSpecialMap().get(ActsSpecialMapConstants.NOTEXIST))) {
            //标记NOTEXIST的数据，仅校验其不存在于数据库
            if (rawResult.size() == 0) {
                ActsLogUtil.info(logger, dbUnit.getUnitName() + "已不存在'" + dbUnit.getDescription()
                                         + "'数据，符合预期");
            } else {
                ActsLogUtil.error(logger, dbUnit.getUnitName() + "仍然存在'" + dbUnit.getDescription()
                                          + "'数据，不符合预期");
            }
            return;
        } else if (rawResult.size() != 1) {
            ActsLogUtil.error(logger, dbUnit.getUnitName() + "检索出'" + dbUnit.getDescription()
                                      + "'数据为" + rawResult.size() + "条，不符合预期");
            return;
        } else {
            compareDBResult(dbUnit, rawResult.get(0), -1);
        }
    }

    /**
     * 组数据校验
     * 
     * @param unit
     */
    @SuppressWarnings("unchecked")
    public static void groupCheckDB(BaseCPUnit unit) {
        //1. 加载预期数据
        GroupDataBaseCPUnit cpUnit = (GroupDataBaseCPUnit) unit;
        List<String> conditionKeys =List.of(cpUnit.getConditionKeys());
        for (DataBaseCPUnit dbUnit : cpUnit.getDataList()) {
            dbUnit.setConditionKeys(conditionKeys);
            dbUnit.loadUniqueMap();
        }

        DataBaseCPUnit dbUnit = cpUnit.getDataList().get(0);
        dbUnit.getSpecialMap().put(ActsSpecialMapConstants.ORDERBY, cpUnit.getOrderBy());

        //2. 待清理上下文添加列
        ActsCaseContextHolder.get().getPreCleanContent().add(dbUnit);

        //3. 连接数据库，生成并执行语句
        List<Map<String, Object>> rawResult = executeQuery(dbUnit);

        if (rawResult.size() != cpUnit.getDataList().size()) {
            ActsLogUtil.error(logger, cpUnit.getUnitName() + "组数据校验获取数据条数不符合预期，期望值"
                                      + cpUnit.getDataList().size() + "条，实际值" + rawResult.size()
                                      + "条");
            return;
        }
        //4. 校验数据库数据
        for (int i = 0; i < cpUnit.getDataList().size(); i++) {
            compareDBResult(cpUnit.getDataList().get(i), rawResult.get(i), i + 1);
        }

    }

    /**
     * 准备对象
     * 
     * @param unit
     * @return
     */
    public static Object prepareObj(BaseCPUnit unit) {
        Object prepareObj = null;
        if (unit instanceof ObjectCPUnit) {
            ActsLogUtil.info(logger, "从" + unit.getTargetCSVPath() + "第" + unit.getDescription()
                                     + "列准备对象" + unit.getUnitName());
            ObjectUnitProperty property = ((ObjectCPUnit) unit).getProperty();
            prepareObj = property.genObject(YamlTestUtil.class.getClassLoader());
        } else if (unit instanceof ListObjectCPUnit) {
            ActsLogUtil.info(logger,
                "从" + unit.getTargetCSVPath() + "准备对象列表List<" + unit.getUnitName() + ">");
            ListObjectUnitProperty listProperty = new ListObjectUnitProperty(
                (ListObjectCPUnit) unit);
            prepareObj = listProperty.genObject(YamlTestUtil.class.getClassLoader());
        } else {
            ActsLogUtil.fail(logger, unit + "入参类型不合法");
        }
        ActsLogUtil.addProcessLog(prepareObj);
        return prepareObj;
    }

    /**
     * 对象校验
     * 
     * @param unit
     * @param object
     */
    @SuppressWarnings("rawtypes")
    public static void checkObj(BaseCPUnit unit, Object object) {
//        Assert.assertNotNull("差异化数据不能为空（前置会处理）", unit);
//        Assert.assertNotNull("待比较对象不能为空（前置会处理）", object);
        if (unit instanceof ObjectCPUnit) {
            Assert.assertNotNull("生成对象路径或列标识为空且未被上层拦截", unit.getDescription());
            ObjectUnitProperty property = ((ObjectCPUnit) unit).getProperty();
            property.compare(object);
        } else if (unit instanceof ListObjectCPUnit) {
//            Assert.assertTrue("待校验对象必须为列表类型", object instanceof List);
            ListObjectCPUnit listUnit = (ListObjectCPUnit) unit;
            List listObj = (List) object;
            if (listObj.size() != listUnit.getAttributeList().size()) {
                ActsLogUtil.error(logger,
                    unit.getUnitName() + "列对象长度不同，期望值:" + listUnit.getAttributeList().size()
                            + "，实际值:" + listObj.size());
            }
            for (int i = 0; i < listObj.size(); i++) {
                checkObj(listUnit.getAttributeList().get(i), listObj.get(i));
            }
        } else {
            ActsLogUtil.fail(logger, "入参格式不正确");
        }
    }

    /**
     * 消息校验
     * 
     * @param unit
     */
//    public static void checkMsg(BaseCPUnit unit) {
//        MessageCPUnit msgUnit = (MessageCPUnit) unit;
//        ActsLogUtil.addProcessLog("开始进行消息校验：[Topic=" + msgUnit.getEventTopic() + ",eventCode="
//                                  + msgUnit.getEventCode() + "]");
//        List<UniformEvent> eventList = UniformEventUtil.getMsgs(msgUnit.getEventTopic(),
//            msgUnit.getEventCode());
//        ActsLogUtil.addProcessLog(eventList);
//        ListObjectCPUnit listUnit = new ListObjectCPUnit(msgUnit);
//        if (StringUtils.equals(msgUnit.getUnitName(), "UniformEvent")) {
//            checkObj(listUnit, eventList);
//        } else {
//            List<Object> payloads = new ArrayList<Object>();
//            for (UniformEvent event : eventList) {
//                payloads.add(event.getEventPayload());
//            }
//            checkObj(listUnit, payloads);
//        }
//    }

    /**
     * 数据清理
     * 
     * @param dataRow
     */
    public static void clean(DataBaseCPUnit unit) {
        //1. 连接数据库，生成并执行语句
        executeUpdate(unit, CheckPointActionEnum.CLEAN);
    }

    /**
     * 基于dataRow，执行检索数据
     * 
     * @param dataRow
     * @return
     */
    private static List<Map<String, Object>> executeQuery(DataBaseCPUnit unit) {
        //1. 基于表名获取dbConfigKey
        String dbConfigKey = ActsDBUtil.getDBConfigKey(unit.getUnitName(), null);

        //2. 初始化数据库连接，获取数据库类型
        DBConnection conn = ActsDBUtil.initConnection(dbConfigKey);
        DataBaseTypeEnum dbType = conn.getDbType();

        //3. 生成数据库执行语句
        String sql = DataRowConvertor.rowToSqL(unit, CheckPointActionEnum.CHECK, dbType);

        //4. 设定虚拟分库分表（如果有）
        setVirtualTddlRule(unit);

        //5. 执行语句
        List<Map<String, Object>> rawResult = conn.executeQuery(sql);
        ActsLogUtil.addProcessLog(rawResult);

        //6. 清理虚拟分库分表
        clearVirtualTddlRule();
        return rawResult;
    }

    /**
     * 基于dataRow指定action，执行更新数据
     * 
     * @param dataRow
     * @param action
     */
    private static void executeUpdate(DataBaseCPUnit unit, CheckPointActionEnum action) {
//        Assert.assertTrue("操作类型只能是清理或准备", action == CheckPointActionEnum.CLEAN
//                                          || action == CheckPointActionEnum.PREPARE);

        //1. 基于表名获取dbConfigKey
        String dbConfigKey = ActsDBUtil.getDBConfigKey(unit.getUnitName(), null);

        //2. 初始化数据库连接，获取数据库类型
        DBConnection conn = ActsDBUtil.initConnection(dbConfigKey);
        DataBaseTypeEnum dbType = conn.getDbType();

        //3. 生成数据库执行语句
        String sql = DataRowConvertor.rowToSqL(unit, action, dbType);

        //4. 设定虚拟分库分表（如果有）
        setVirtualTddlRule(unit);

        //5. 执行语句
        int i = conn.executeUpdate(sql);

        //6. 清理虚拟分库分表
        clearVirtualTddlRule();
        if (i != -1) {
            ActsLogUtil.info(logger, "影响了" + i + "行");
        }
    }

    /**
     * 数据库比较
     * 
     * @param dataRow
     * @param data
     */
    private static void compareDBResult(DataBaseCPUnit unit, Map<String, Object> data, int groupId) {

        if (ActsCaseContextHolder.get().isNeedCompareTableLength()) {
            int actSize = data.size();
            int expSize = unit.getModifyMap().size();
            if (skipCompareDBLength) {
                ActsLogUtil.warn(logger, unit.getUnitName() + "表长度期望值:" + expSize + ",实际值:"
                                         + actSize);
            } else if (actSize != expSize) {
                ActsLogUtil.error(logger, unit.getUnitName() + "表长度期望值:" + expSize + ",实际值:"
                                          + actSize);
                return;
            }
        }
        for (Entry<String, Object> entry : data.entrySet()) {
            String columnName = entry.getKey();
            Object value = entry.getValue();
            BaseUnitProperty property = unit.getModifyMap().get(columnName);
            UnitComparer comparer = ObjectCompareManager.getComparerManager().get(
                UnitFlagEnum.getByCode(property.getFlagCode()));
            boolean cpResult = comparer.compare(property.getExpectValue(), value,
                property.getFlagCode());
            if (!cpResult) {
                if (groupId == -1) {
                    ActsLogUtil.error(logger,
                        "检查表:" + unit.getUnitName() + "@" + unit.getDescription() + "---"
                                + property.getKeyName() + " " + property.getDbColumnComment()
                                + "---" + " 期望值=" + property.getExpectValue() + " 实际值=" + value);
                } else {
                    ActsLogUtil.error(
                        logger,
                        "第" + groupId + "行组数据检查表:" + unit.getUnitName() + "@"
                                + unit.getDescription() + "---" + property.getKeyName() + " "
                                + property.getDbColumnComment() + "---" + " 期望值="
                                + property.getExpectValue() + " 实际值=" + value);
                }
                property.setCompareSuccess(false);
                property.setActualValue(value);
            }
        }
    }

    /**
     * 整理specialMap中获取的boolean值
     * 
     * @param data
     * @return
     */
    private static boolean parseBoolean(Object data) {
        if (data == null) {
            return false;
        } else if (data instanceof String) {
            return Boolean.valueOf((Boolean) data);
        } else if (data instanceof Boolean) {
            return (Boolean) data;
        } else {
            return false;
        }
    }

    /**
     * 设置虚拟分库分表位
     * 
     * @param tableName
     * @param splitId
     */
    private static void setVirtualTddlRule(DataBaseCPUnit unit) {
        String splitKey = (String) unit.getSpecialMap().get(ActsSpecialMapConstants.SPLITKEY);
        String splitValue = (String) unit.getSpecialMap().get(ActsSpecialMapConstants.SPLITVALUE);

        if (StringUtils.isBlank(splitValue)) {
            return;
        }

        if (splitKey == null)
            splitKey = "split_id";
//
//        SimpleCondition simpleCondition = new SimpleCondition();
//        simpleCondition.setVirtualTableName(unit.getUnitName());
//        simpleCondition.put(splitKey, splitValue);
//        ActsLogUtil.info(logger, "设置虚拟分库分表位[" + splitKey + "=" + splitValue + "]");
//        ThreadLocalMap.put(ThreadLocalString.ROUTE_CONDITION, simpleCondition);
    }

    /**
     * 清理虚拟分库分表位修改的线程变量
     * 
     * @param tableName
     * @param splitId
     */
    private static void clearVirtualTddlRule() {
//        ThreadLocalMap.put(ThreadLocalString.ROUTE_CONDITION, null);
    }

    /**
     * Getter method for property <tt>isSingleYaml</tt>.
     * 
     * @return property value of isSingleYaml
     */
    public static boolean isSingleYaml() {
        return isSingleYaml;
    }

}
