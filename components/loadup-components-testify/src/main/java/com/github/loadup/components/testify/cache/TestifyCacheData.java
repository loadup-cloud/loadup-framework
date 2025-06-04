/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.testify.cache;

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

import com.github.loadup.components.testify.constant.TestifyPathConstants;
import com.github.loadup.components.testify.constant.TestifyYamlConstants;
import com.github.loadup.components.testify.msg.model.MessageModel;
import com.github.loadup.components.testify.object.comparer.UnitComparer;
import com.github.loadup.components.testify.object.generator.CustomGenerator;
import com.github.loadup.components.testify.util.FileUtil;
import com.github.loadup.components.testify.yaml.enums.CPUnitTypeEnum;
import java.io.File;
import java.util.*;
import java.util.Map.Entry;
import org.springframework.util.CollectionUtils;

/**
 * 缓存字段
 *
 */
public class TestifyCacheData {

    /**
     * 自定义数据库表名集合
     */
    private static final Set<String> dbDataSet = new HashSet<String>();

    /**
     * 自定义对象类名集合
     */
    private static final Set<String> objectDataSet = new HashSet<String>();

    /**
     * 消息unit名称集合，需要在基类手动预加载
     */
    private static final Map<String, MessageModel> messageDataMap = new HashMap<String, MessageModel>();

    /**
     * 自定义字段生成器集合
     */
    private static Map<String, CustomGenerator> customGenerators = new HashMap<String, CustomGenerator>();

    /**
     * 自定义字段比较器集合
     */
    private static Map<String, UnitComparer> customComparers = new HashMap<String, UnitComparer>();

    static {
        loadDataSet();
    }

    /**
     * 预加载现存数据库、对象名称集合
     */
    @SuppressWarnings("unchecked")
    public static void loadDataSet() {

        // 搜集表名
        collectDbTableName();

        File folder = FileUtil.getTestResourceFile(TestifyPathConstants.OBJECT_DATA_PATH);
        if (folder.isDirectory()) {
            String[] files = folder.list();
            for (String fileName : files) {
                objectDataSet.add(fileName);
            }
        }

        File msgFile = FileUtil.getTestResourceFile(TestifyPathConstants.MSGCONFIG_PATH);
        LinkedHashMap<?, ?> msgConfigData = FileUtil.readYaml(msgFile);
        if (CollectionUtils.isEmpty(msgConfigData)) {
            return;
        }
        for (Entry<?, ?> entry : msgConfigData.entrySet()) {
            MessageModel model = new MessageModel();
            model.setMessageKey((String) entry.getKey());
            Map<String, Object> value = (Map<String, Object>) entry.getValue();
            String topic = (String) value.get("topic");
            String eventCode = (String) value.get("eventCode");
            String messageClass = (String) value.get("className");
            model.setEventTopic(topic);
            model.setEventCode(eventCode);
            model.setMessageClass(messageClass);
            model.setMessageKey((String) entry.getKey());
            if (!messageDataMap.containsKey(model.getMessageKey())) {
                putMessageModel(model);
            }
        }
    }

    /**
     * 搜集自定义数据表名称集合
     */
    private static void collectDbTableName() {
        File folder = FileUtil.getTestResourceFile(TestifyPathConstants.DB_DATA_PATH);
        if (folder.isDirectory()) {
            String[] files = folder.list();
            for (String fileName : files) {
                String tableName = fileName.split(".csv")[0];
                dbDataSet.add(tableName);
            }
        }
    }

    /**
     * 获取用户自定义数据库模板对应的所有表名
     *
     * @return
     */
    public static Set<String> getDbTableNameSet() {

        if (0 == dbDataSet.size()) {
            collectDbTableName();
        }
        return dbDataSet;
    }

    /**
     * 基于unit名称获取数据类型
     *
     * @param unitName
     * @return
     */
    public static CPUnitTypeEnum getCPUnitType(String unitName) {
        if (dbDataSet.contains(unitName)) {
            return CPUnitTypeEnum.DATABASE;
        } else if (objectDataSet.contains(unitName)) {
            return CPUnitTypeEnum.OBJECT;
        } else if (messageDataMap.containsKey(unitName)) {
            return CPUnitTypeEnum.MESSAGE;
        } else if (unitName.startsWith(TestifyYamlConstants.GROUPKEY)) {
            return CPUnitTypeEnum.GROUP;
        } else return null;
    }

    /**
     * 添加消息字段
     *
     * @param messageKey
     */
    public static void putMessageModel(MessageModel model) {
        messageDataMap.put(model.getMessageKey(), model);
    }

    /**
     * 基于字段获取消息模型
     *
     * @param messageKey
     * @return
     */
    public static MessageModel getMessageModel(String messageKey) {
        return messageDataMap.get(messageKey);
    }

    /**
     * 添加自定义生成器
     *
     * @param customCode
     * @param generator
     */
    public static void addCustomGenerator(String customCode, CustomGenerator generator) {
        customGenerators.put(customCode, generator);
    }

    /**
     * 获取自定义生成器
     *
     * @param customCode
     * @return
     */
    public static CustomGenerator getCustomGenerator(String customCode) {
        return customGenerators.get(customCode);
    }

    /**
     * 添加自定义比较器
     *
     * @param customCode
     * @param generator
     */
    public static void addCustomComparer(String customCode, UnitComparer comparer) {
        customComparers.put(customCode, comparer);
    }

    /**
     * 获取自定义生成器
     *
     * @param customCode
     * @return
     */
    public static UnitComparer getCustomComparer(String customCode) {
        return customComparers.get(customCode);
    }
}
