/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.alipay.test.acts.assemble;

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

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.CollectionUtils;

import org.apache.commons.lang3.StringUtils;
import com.alipay.test.acts.callback.GenerateCacheDataCallBack;
import com.alipay.test.acts.dal.dataobject.OrgDbDO;
import com.alipay.test.acts.data.RuleDataFactory;
import com.alipay.test.acts.data.model.GenerateCondition;
import com.alipay.test.acts.datarule.RULE;
import com.alipay.test.acts.datarule.RULE.ReferenceHandler;
import com.alipay.test.acts.model.PrepareData;
import com.alipay.test.acts.model.VirtualObject;
import com.alipay.test.acts.model.VirtualTable;
import com.alipay.test.acts.relation.ObjectRelationManager;
import com.alipay.test.acts.util.DeepCopyUtils;
import com.alipay.test.acts.util.LocalCacheUtil;
import com.alipay.test.acts.util.ReflectUtil;

/**
 * 对象组装类
 * 
 * @author batuo.zxm
 * @add by chao.gao
 * @version $Id: AssembleUtil.java, v 0.1 2015年9月30日 下午4:05:33 batuo.zxm Exp $
 */
public class AssembleManager {

    private final ObjectRelationManager objectRelationManager    = new ObjectRelationManager();

    private boolean                     assembleNullComplexField = true;

    private final CaseRuleManager       caseRuleManager          = new CaseRuleManager();

    /**
     * Getter method for property <tt>assembleNullComplexField</tt>.
     * 
     * @return property value of assembleNullComplexField
     */
    public boolean isAssembleNullComplexField() {
        return assembleNullComplexField;
    }

    /**
     * Setter method for property <tt>assembleNullComplexField</tt>.
     * 
     * @param assembleNullComplexField
     *            value to be assigned to property assembleNullComplexField
     */
    public void setAssembleNullComplexField(boolean assembleNullComplexField) {
        this.assembleNullComplexField = assembleNullComplexField;
    }

    /**
     * 根据模型组装对象
     * 
     * @param object
     *            待组装对象
     * @param model
     *            对象映射到的模型
     * @throws ClassNotFoundException
     * 
     */
    public Map<String, Object> assembleCaseAllObj(GenerateCondition condition, Object object,
                                                  String model, boolean isReGenOrgFieldValue) {

        String system = condition.getAppName();
        String methodName = condition.getMethodName();

        try {
            // 构建此模型对象关联关系
            Map<String, Object> relationMap = objectRelationManager.loadObjectRelation(system,
                model);

            // 生成基本的对象
            internalAssembleByModel(object, relationMap, isReGenOrgFieldValue, system);

            // 基于基本对象，根据用例规则生成多个用例对象
            return caseRuleManager.generateAllCaseObject(relationMap, object, model, condition);
        } catch (Exception e) {
            throw new RuntimeException("assemble failed:" + e.getMessage(), e);
        }
    }

    /**
     * 根据模型组装对象
     * 
     * @param object
     *            待组装对象
     * @param model
     *            对象映射到的模型
     * @throws ClassNotFoundException
     */
    public void assembleSingleCaseObj(String system, Object object, String model,
                                      boolean isReGenOrgFieldValue) {

        try {
            // 构建此模型对象关联关系
            Map<String, Object> relationMap = objectRelationManager.loadObjectRelation(system,
                model);

            // 生成基本的对象
            internalAssembleByModel(object, relationMap, isReGenOrgFieldValue, system);
        } catch (Exception e) {
            throw new RuntimeException("assemble failed:" + e.getMessage(), e);
        }
    }

    /**
     * 组装单个preparedata
     * 
     * @param prepareData
     */
    public void assembleSingleData(String system, PrepareData newPrepareData) {

        /* 组装所有入参所有的用例对象 */
        if (newPrepareData.getArgs() != null && newPrepareData.getArgs().inputArgs != null) {
            for (VirtualObject argObj : newPrepareData.getArgs().inputArgs) {
                if (argObj.object == null)
                    continue;
                assembleSingleCaseObj(system, argObj.object, argObj.object.getClass()
                    .getSimpleName(), true);
            }
        }

        /* 组装出参 */
        if (newPrepareData.getExpectResult() != null
            && newPrepareData.getExpectResult().getResult() != null) {
            assembleSingleCaseObj(system, newPrepareData.getExpectResult().getResult(),
                newPrepareData.getExpectResult().getResult().getClass().getSimpleName(), false);
        }

        /* 组装前置表数据 */
        if (newPrepareData.getDepDataSet() != null
            && newPrepareData.getDepDataSet().getVirtualTables() != null) {
            for (VirtualTable virtualTable : newPrepareData.getDepDataSet().getVirtualTables()) {
                assembleVirtualTable(system, virtualTable);
            }
        }

        /* 组装预期表数据 */
        if (newPrepareData.getExpectDataSet() != null
            && newPrepareData.getExpectDataSet().getVirtualTables() != null) {
            for (VirtualTable virtualTable : newPrepareData.getExpectDataSet().getVirtualTables()) {
                assembleVirtualTable(system, virtualTable);
            }
        }
    }

    /**
     * 
     * @param condition
     *            插件传递下来的信息对象
     * @param prepareData
     *            一个数据对象，取自第一个用例或者初始化的第一个用例
     * @return
     */

    public Map<String, PrepareData> assemblyAllData(GenerateCondition condition,
                                                    PrepareData prepareData) {

        String system = condition.getAppName();
        String methodName = condition.getMethodName();
        Map<String, PrepareData> prepareDataSet = new HashMap<String, PrepareData>();

        /* 组装所有入参所有的用例对象 */
        Map<String, Map<String, Object>> argsCaseObjs = new HashMap<String, Map<String, Object>>();
        if (prepareData.getArgs() != null && prepareData.getArgs().inputArgs != null) {
            for (VirtualObject argObj : prepareData.getArgs().inputArgs) {
                // 先判断是否为空，避免页面入参没有加载模板的情况
                if (argObj.object == null)
                    continue;
                Map<String, Object> caseRuleObejcts = new HashMap<String, Object>();
                caseRuleObejcts = assembleCaseAllObj(condition, argObj.object, argObj.object
                    .getClass().getSimpleName(), true);

                argsCaseObjs.put(argObj.object.getClass().getSimpleName(), caseRuleObejcts);

            }
        }

        // 存在为空可能
        if (CollectionUtils.isEmpty(argsCaseObjs)) {
            return prepareDataSet;
        }

        String maxKey = getMakKey(argsCaseObjs);
        // 以某一个对象生成的最多用例规则个数生成用例个数
        for (int i = 0; i < argsCaseObjs.get(maxKey).size(); i++) {

            StringBuffer caseId = new StringBuffer();

            // 需要深拷贝
            PrepareData newPrepareData = DeepCopyUtils.deepCopy(prepareData);

            for (VirtualObject argObj : newPrepareData.getArgs().inputArgs) {

                if (argObj == null || argObj.getObject() == null) {
                    continue;
                }
                Object key = argObj.getObject().getClass().getSimpleName();

                if (!argsCaseObjs.keySet().contains(key)) {
                    continue;
                }

                Map<String, Object> caseObjs = argsCaseObjs.get(argObj.getObject().getClass()
                    .getSimpleName());
                // 此处判断针对多个入参的接口，但是仅有部分接口参数有用例规则的情况
                if (!CollectionUtils.isEmpty(caseObjs)) {
                    List<Object> keys = Arrays.asList(caseObjs.keySet().toArray());
                    int randomIndex = (int) Math.random() * (keys.size() - 1);
                    if (StringUtils.equalsIgnoreCase(maxKey, argObj.getObject().getClass()
                        .getSimpleName())) {
                        randomIndex = i;
                    }
                    String caseObjKey = (String) keys.get(randomIndex);
                    argObj.object = caseObjs.get(caseObjKey);
                    caseId.append(caseObjKey).append("_caseID_");
                }

            }

            /* 组装出参 */
            if (newPrepareData.getExpectResult() != null
                && newPrepareData.getExpectResult().getResult() != null) {
                assembleSingleCaseObj(system, newPrepareData.getExpectResult().getResult(),
                    newPrepareData.getExpectResult().getResult().getClass().getSimpleName(), false);
            }

            /* 组装前置表数据 */
            if (newPrepareData.getDepDataSet() != null
                && newPrepareData.getDepDataSet().getVirtualTables() != null) {
                for (VirtualTable virtualTable : newPrepareData.getDepDataSet().getVirtualTables()) {
                    assembleVirtualTable(system, virtualTable);
                }
            }

            /* 组装预期表数据 */
            if (newPrepareData.getExpectDataSet() != null
                && newPrepareData.getExpectDataSet().getVirtualTables() != null) {
                for (VirtualTable virtualTable : newPrepareData.getExpectDataSet()
                    .getVirtualTables()) {
                    assembleVirtualTable(system, virtualTable);
                }
            }

            prepareDataSet.put(
            // genCaseNo(caseId.toString().substring(0,
            // caseId.toString().length() - 1), i + 1),
                "NewCase" + caseId.toString(), newPrepareData);
        }

        return prepareDataSet;
    }

    /**
     * 生成用例编号
     * 
     * @param caseKey
     * @return
     */
    private String genCaseNo(String caseKey, int caseIndex) {

        StringBuilder caseNo = new StringBuilder("NewCase");
        caseNo.append("[");
        // caseKey = StringUtils.replace(caseKey, "choose", "R");
        String[] tokens = caseKey.split(",");
        for (String token : tokens) {
            if (StringUtils.contains(token, ".")) {
                caseNo.append(token.substring(StringUtils.lastIndexOf(token, ".") + 1));
            } else {
                caseNo.append(token);
            }
            caseNo.append(",");
        }
        return caseNo.toString().substring(0, caseNo.lastIndexOf(",")) + "]";
    }

    private String getMakKey(Map<String, Map<String, Object>> argsCaseObjs) {
        String maxKey = StringUtils.EMPTY;
        for (String argKey : argsCaseObjs.keySet()) {

            if (StringUtils.isBlank(maxKey)) {
                maxKey = argKey;
                continue;
            }

            if (argsCaseObjs.get(argKey) == null) {
                continue;
            }

            if (argsCaseObjs.get(argKey).size() > argsCaseObjs.get(maxKey).size()) {
                maxKey = argKey;
            }
        }
        return maxKey;
    }

    private void assembleVirtualTable(String system, VirtualTable virtualTable) {
        if (virtualTable.getTableName() == null) {
            /* 表名即是模型名，不可为空 */
            return;
        }

        if (virtualTable.getTableData().size() == 0) {
            /* 应该有模板数据，否则跳过 */
            return;
        }

        Map<String, Object> modelInstance = assembleByModel(system, virtualTable.getTableName());
        for (Map<String, Object> row : virtualTable.getTableData()) {
            mergeMap(modelInstance, row);
        }
    }

    private void mergeMap(Map<String, Object> baseMap, Map<String, Object> targetMap) {
        for (String key : baseMap.keySet()) {
            if (targetMap.containsKey(key)) {
                Object ruleObject = baseMap.get(key);
                if (ruleObject == null) {
                    continue;
                } else {
                    targetMap.put(key, ruleObject);
                }
            }
        }
    }

    /**
     * 根据模型，返回一个平级结构的Map，不递归处理嵌套模型
     * 
     * @param model
     * @return
     */
    public Map<String, Object> assembleByModel(String system, String model) {
        Map<String, Object> relationMap = objectRelationManager.loadObjectRelation(system, model);
        try {
            return internalAssembleByModel(relationMap, system);
        } catch (Exception e) {
            throw new RuntimeException("assemble failed:" + e.getMessage(), e);
        }
    }

    /**
     * 根据模型，返回一个平级结构的Map，不递归处理嵌套模型
     * 
     * @param modelRelationMap
     * @return
     */
    private Map<String, Object> internalAssembleByModel(Map<String, Object> modelRelationMap,
                                                        final String system) {
        Map<String, Object> assembleObject = new HashMap<String, Object>();
        for (Map.Entry<String, Object> entry : modelRelationMap.entrySet()) {
            String fieldName = entry.getKey();
            if (entry.getValue() instanceof Map) {
                /* 不处理嵌套模型 */
                continue;
            } else if (entry.getValue() instanceof OrgDbDO) {
                OrgDbDO dbDO = (OrgDbDO) entry.getValue();
                String rule = dbDO.getSourceRule();
                Object raw = new RuleDataFactory().getDataByRule(system, null, rule);
                if (raw != null) {
                    assembleObject.put(entry.getKey(), raw);
                }

            } else {
                throw new RuntimeException("unknown model object");
            }
        }
        return assembleObject;
    }

    @SuppressWarnings("unchecked")
    private void internalAssembleByModel(Object object, Map<String, Object> modelRelationMap,
                                         Boolean isReGenOrgFieldValue, final String system)
                                                                                           throws ClassNotFoundException,
                                                                                           InstantiationException,
                                                                                           IllegalAccessException {

        for (Map.Entry<String, Object> entry : modelRelationMap.entrySet()) {
            String fieldName = entry.getKey();
            Field field = ReflectUtil.getField(object, fieldName);
            if (field == null) {
                continue;
            }

            if (entry.getValue() instanceof Map) {

                Map<String, Object> map = (Map<String, Object>) entry.getValue();
                Object fieldValue = ReflectUtil.getFieldValue(object, fieldName);

                if (field.getType().isAssignableFrom(List.class)) {
                    /* 判断是不是List */
                    /* TODO:判断是不是Map */
                    ParameterizedType parameterizedType = (ParameterizedType) field
                        .getGenericType();
                    Class<?> clazz = (Class<?>) parameterizedType.getActualTypeArguments()[0];
                    List<Object> list = new ArrayList<Object>();
                    if (!ReflectUtil.canInstantiate(clazz)) {
                        Object insideItem = ReflectUtil.instantiateClass(clazz);
                        internalAssembleByModel(insideItem, map, isReGenOrgFieldValue, system);
                        list.add(insideItem);
                        ReflectUtil.setFieldValue(object, fieldName, list);
                    }

                } else if (fieldValue == null && assembleNullComplexField) {
                    /* 若属性对象为空，初始化之 */
                    Class<?> clazz = field.getType();
                    Object initValue = null;
                    if (!ReflectUtil.canInstantiate(clazz)) {
                        initValue = ReflectUtil.instantiateClass(clazz);
                        ReflectUtil.setFieldValue(object, fieldName, initValue);
                        internalAssembleByModel(initValue, map, isReGenOrgFieldValue, system);
                    }

                } else {
                    internalAssembleByModel(fieldValue, map, isReGenOrgFieldValue, system);
                }

            } else if (entry.getValue() instanceof OrgDbDO) {
                OrgDbDO dbDO = (OrgDbDO) entry.getValue();
                final String rule = dbDO.getSourceRule();
                Object raw = null;
                try {
                    if (!isReGenOrgFieldValue) {
                        // 使用本地缓存原子属性值
                        raw = LocalCacheUtil.getObject(
                            dbDO.getSystem() + "_" + dbDO.getSourceData(),
                            new GenerateCacheDataCallBack() {
                                @Override
                                public Object getCacheValue(String key) {
                                    return new RuleDataFactory().getDataByRule(system, null, rule);
                                }
                            });
                    } else {
                        // 重新生成值
                        raw = new RuleDataFactory().getDataByRule(system, null, rule);
                    }

                    if (field.getType().isAssignableFrom(List.class)) {
                        /* 是List */
                        ParameterizedType parameterizedType = (ParameterizedType) field
                            .getGenericType();
                        Class<?> clazz = (Class<?>) parameterizedType.getActualTypeArguments()[0];
                        List<Object> list = new ArrayList<Object>();
                        Object value = ReflectUtil.valueByCorrectType(object, clazz, raw);
                        list.add(value);
                        ReflectUtil.setFieldValue(object, fieldName, list);
                    } else {
                        Object value = ReflectUtil.valueByCorrectType(object, field, raw);
                        ReflectUtil.setFieldValue(object, fieldName, value);
                    }
                } catch (Exception ex) {
                }
            } else {
                throw new RuntimeException("unknown model object");
            }
        }
    }

}
