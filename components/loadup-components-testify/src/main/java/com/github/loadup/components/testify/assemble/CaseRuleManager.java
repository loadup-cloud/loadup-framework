/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.testify.assemble;

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

import com.alibaba.fastjson2.JSON;
import com.github.loadup.components.testify.dal.daointerface.CaseRuleDAO;
import com.github.loadup.components.testify.dal.dataobject.CaseRuleDO;
import com.github.loadup.components.testify.dal.dataobject.OrgDbDO;
import com.github.loadup.components.testify.data.RuleDataFactory;
import com.github.loadup.components.testify.data.model.GenerateCondition;
import com.github.loadup.components.testify.object.manager.ObjectTypeManager;
import com.github.loadup.components.testify.util.DeepCopyUtils;
import com.github.loadup.components.testify.util.ReflectUtil;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

/**
 * 用例规则关联类
 *
 *
 *
 * @add by chao.gao
 */
public class CaseRuleManager {

    /**
     * 用例规则数据库访问对象
     */
    private final CaseRuleDAO caseRuleDAO = new CaseRuleDAO();

    /**
     * 根据配置的用例规则生成模型对象所有用例对象
     *
     * @param object   基本模型对象
     * @param modelObj 模型对象简称
     * @return
     */
    public Map<String, Object> generateAllCaseObject(
            Map<String, Object> relationMap, Object object, String modelObj, GenerateCondition condition) {

        String system = condition.getAppName();
        String methodName = condition.getMethodName();
        Map<String, Object> caseRuleObejcts = new HashMap<String, Object>();

        if (condition.isSpecAtom()) {
            Map<String, Object> caseObjs = generateCaseBySingleRule(relationMap, object, modelObj, "", condition);
            if (!CollectionUtils.isEmpty(caseObjs)) {
                caseRuleObejcts.putAll(caseObjs);
            }

        } else {

            // 加载模型对象指定的所有用例规则,状态必须为T
            String status = "T";
            List<CaseRuleDO> allCaseRules = caseRuleDAO.queryBySystemAndModelObj(system, methodName, modelObj, status);

            // 使用每一个用例规则生成多个用例
            for (CaseRuleDO caseRuleDO : allCaseRules) {
                Map<String, Object> caseObjs =
                        generateCaseBySingleRule(relationMap, object, modelObj, caseRuleDO.getCaseRule(), condition);
                if (!CollectionUtils.isEmpty(caseObjs)) {
                    caseRuleObejcts.putAll(caseObjs);
                    ;
                }
            }
        }

        return caseRuleObejcts;
    }

    /**
     * 使用用例规则对对象部分字段再次赋值
     *
     * @param object
     * @param caseRule
     * @return
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> generateCaseBySingleRule(
            Map<String, Object> relationMap,
            Object object,
            String modelObj,
            String caseRule,
            GenerateCondition condition) {

        // 单个用例规则生成的所有用例对象
        Map<String, Object> allCaseObj = new HashMap<String, Object>();
        String system = condition.getAppName();

        // 解析caseRule中涉及的原子对象属性
        //        //TODO:目前仅支持choose(x,xx,xx)格式解析
        //        String ruleContent = StringUtils.substring(caseRule, caseRule.indexOf("(") + 1,
        //            caseRule.indexOf(")"));
        //        String[] fields = StringUtils.split(ruleContent, ",");
        //
        //        Map<String, String> ruleFieldOrgFieldMap = new HashMap<String, String>();
        //        //需过滤掉第一个规则字段，其余的才为真正字段属性
        //        for (int i = 1; i < fields.length; i++) {
        //            findOrgFiled(relationMap, fields[i], ruleFieldOrgFieldMap, fields[i]);
        //        }

        // 用例规则字段替换为原子字段，重新组装成底层能够识别的用例规则
        //        String newCaseRule = caseRule;
        //        for (String ruleField : ruleFieldOrgFieldMap.keySet()) {
        //            newCaseRule = StringUtils.replace(newCaseRule, ruleField,
        //                ruleFieldOrgFieldMap.get(ruleField));
        //        }

        // 根据用例规则生成多对应字段组合值
        List<Map<String, String>> fieldValues = null;
        if (!condition.isSpecAtom()) {
            String rowData = new RuleDataFactory().getDataByOriginRule(system, null, caseRule);
            fieldValues = (List<Map<String, String>>) JSON.parse(rowData);
        } else {
            fieldValues = condition.getRuleMaps(modelObj);
        }
        // 这个list是类似[{"filed2":"aaa2","filed1":"aaa1"},{"filed2":"bbb2","filed1":"bbb1"},{"filed2":"aaa2","filed1":"aaa1"}] 这样的
        if (CollectionUtils.isEmpty(fieldValues)) {
            return null;
        }

        // 遍历所有的字段用例组合
        for (int i = 0; i < fieldValues.size(); i++) {

            // 需要深拷贝
            Object newObject = DeepCopyUtils.deepCopy(object);
            Map<String, String> fieldValusMap = fieldValues.get(i);

            String objectKey = caseRule;

            for (String ruleFieldKey : fieldValusMap.keySet()) {
                deassign(newObject, ruleFieldKey, fieldValusMap.get(ruleFieldKey));

                if (!condition.isSpecAtom()) {
                    objectKey = StringUtils.replace(
                            objectKey, ruleFieldKey, ruleFieldKey + ":" + fieldValusMap.get(ruleFieldKey));
                } else if (condition.isSpecAtom()) {
                    objectKey = objectKey + "-" + ruleFieldKey + ":" + fieldValusMap.get(ruleFieldKey);
                }
            }

            allCaseObj.put(objectKey, newObject);
        }

        return allCaseObj;
    }

    /**
     * 查询获取规则字段对应的原子对象属性字段
     *
     * @param relationMap
     * @param ruleField
     * @param ruleFieldOrgFieldMap
     * @param ruleFieldKey
     */
    @SuppressWarnings("unchecked")
    private void findOrgFiled(
            Map<String, Object> relationMap,
            String ruleField,
            Map<String, String> ruleFieldOrgFieldMap,
            String ruleFieldKey) {

        String[] fieldPathArray = StringUtils.split(ruleField, ".");

        // 目标字段属性
        if (fieldPathArray.length == 2) {
            OrgDbDO orgDb = (OrgDbDO) relationMap.get(fieldPathArray[1]);
            ruleFieldOrgFieldMap.put(ruleFieldKey, orgDb.getSourceData());
            return;
        }

        // 递归查找目标子对象属性
        String subFieldPath = StringUtils.substring(ruleField, ruleField.indexOf(".") + 1);

        findOrgFiled(
                (Map<String, Object>) relationMap.get(fieldPathArray[1]),
                subFieldPath,
                ruleFieldOrgFieldMap,
                ruleFieldKey);
    }

    /**
     * 对象字段重新赋值
     *
     * @param object
     * @param ruleField 属性全路径
     */
    private void deassign(Object object, String ruleField, String newValue) {

        String[] fieldPathArray = StringUtils.split(ruleField, ".");

        // 目标字段属性赋值
        if (fieldPathArray.length == 2) {

            ObjectTypeManager objectTypeManager = new ObjectTypeManager();

            if (objectTypeManager.isCollectionType(object.getClass())) {

                @SuppressWarnings("unchecked")
                List<Object> parentList = (List<Object>) object;

                Object oneObject = parentList.get(0);
                String[] arrs = StringUtils.split(newValue, ",");

                for (int i = 0; i < arrs.length; i++) {
                    if (i == 0) {
                        Field field = ReflectUtil.getField(oneObject, fieldPathArray[1]);
                        field.setAccessible(true);
                        ReflectUtil.setFieldValue(oneObject, fieldPathArray[1], arrs[i]);
                    } else {
                        Object cloned = DeepCopyUtils.deepCopy(oneObject);
                        Field field = ReflectUtil.getField(cloned, fieldPathArray[1]);
                        field.setAccessible(true);
                        ReflectUtil.setFieldValue(cloned, fieldPathArray[1], arrs[i]);
                        parentList.add(cloned);
                    }
                }

            } else {
                // 对于简单的List对象,目前是不支持的
                ReflectUtil.setFieldValue(object, fieldPathArray[1], newValue);
            }
        } else {
            // 递归查找目标子对象属性
            String subFieldPath = StringUtils.substring(ruleField, ruleField.indexOf(".") + 1);

            deassign(ReflectUtil.getFieldValue(object, fieldPathArray[1]), subFieldPath, newValue);
        }
    }
}
