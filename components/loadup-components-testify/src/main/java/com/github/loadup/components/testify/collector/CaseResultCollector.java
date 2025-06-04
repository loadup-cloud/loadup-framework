/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.testify.collector;

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

import com.github.loadup.components.testify.constant.TestifyConstants;
import com.github.loadup.components.testify.exception.TestifyException;
import com.github.loadup.components.testify.model.*;
import com.github.loadup.components.testify.util.BaseDataUtil;
import com.github.loadup.components.testify.util.FileUtils;
import com.github.loadup.components.testify.utils.file.CsvUtils;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

/**
 * 用例执行结果收集器
 *
 * <p>
 * 此类职责是收集用例执行结果，包含：
 * <li>调用服务的实际执行结果</li>
 * <li>所有发送消息的DTO对象</li>
 * <li>用例执行过程中sql操作（目前仅收集insert、update）日志</li>
 * <li>存放tair数据对象</li>
 * 结果收集完成后，组装一个新的PrepareData集合，用于结果反向结果
 * </p>
 *
 *
 *
 * hongling.xiang Exp $
 */
public class CaseResultCollector {

    /**
     * logger
     */
    private static final Logger logger = LoggerFactory.getLogger(CaseResultCollector.class);

    /**
     * sql日志文件
     */
    private static final String SQL_LOG_PATH_NAME = "./logs/acts-sql.log";

    /**
     * 用例结果收集
     *
     * <pre>
     *  注意：入参initCaseData集合中，为了方便，在用例执行过程中，
     *  已经为对象PrepareData已经设置事件对象及实际结果对象
     * </pre>
     *
     * @param caseFileFullName
     * @param caseDatas        初始用例数据集合对象
     */
    public static void saveCaseResult(String caseFileFullName, Map<String, PrepareData> caseDatas) {

        try {
            if (CollectionUtils.isEmpty(caseDatas) || StringUtils.isBlank(caseFileFullName)) {
                return;
            }

            // 保存收集的用例执行结果到临时文件
            doSaveCaseResult(caseFileFullName, caseDatas);

        } catch (Throwable e) {
            logger.warn("unknow exception while saving case result保存用例结果未知异常", e);
        } finally {
            // 后置动作：删除用于保存用例执行结果相关文件
            clearTemporaryData();
        }
    }

    /**
     * 生成caseObjs_actual.yaml用于保存当前执行后的实际结果
     *
     * @param caseFolders
     * @param caseDatas
     */
    public static void genActualYaml(List<File> caseFolders, Map<String, PrepareData> caseDatas) {
        try {
            for (File caseFolder : caseFolders) {
                String caseId = caseFolder.getName();
                File yamlFile = null;

                // retrieve case yaml
                for (File file : caseFolder.listFiles()) {
                    if (file.isFile() && file.getName().endsWith(".yaml")) {
                        yamlFile = file;
                        break;
                    }
                }

                if (null == yamlFile) {
                    logger.error("No valid yaml file found!");
                    break;
                }

                // 只返填RunOnly指定运行的用例
                if (!caseDatas.containsKey(caseId)) {
                    continue;
                }

                // retrieve case result
                Object res = caseDatas.get(caseId).getExpectResult().getResultObj();

                // retrieve case events
                VirtualEventSet eventSet = caseDatas.get(caseId).getExpectEventSet();

                // gen actual yaml
                BaseDataUtil.genActualYaml(res, eventSet, yamlFile);
            }
        } catch (Throwable e) {
            logger.warn("unknow exception while saving case result保存用例bak文件未知异常", e);
        } finally {
            // 后置动作：删除用于保存用例执行结果相关文件
            clearTemporaryData();
        }
    }

    /**
     * 2.0数据模式 用例结果收集
     *
     * @param caseFolders
     * @param caseDatas
     * @param overwrite
     * @param hasDBData
     */
    public static void saveCaseResult(
            List<File> caseFolders, Map<String, PrepareData> caseDatas, boolean overwrite, boolean hasDBData) {

        try {
            for (File caseFolder : caseFolders) {

                String caseId = caseFolder.getName();
                File yamlFile = null;

                // retrieve case yaml
                for (File file : caseFolder.listFiles()) {
                    if (file.isFile() && file.getName().endsWith(".yaml")) {
                        yamlFile = file;
                        break;
                    }
                }

                if (null == yamlFile) {
                    logger.error("[AutoFill]No valid yaml file found!");
                    break;
                }

                // 只返填RunOnly指定运行的用例
                if (!caseDatas.containsKey(caseId)) {
                    continue;
                }

                // retrieve case result
                Object res = caseDatas.get(caseId).getExpectResult().getResultObj();

                // retrieve case events
                VirtualEventSet eventSet = caseDatas.get(caseId).getExpectEventSet();

                // save data
                if ((null != res || 0 != eventSet.getVirtualEventObjects().size()) && yamlFile != null) {
                    doSaveCaseResult(yamlFile, res, eventSet, overwrite);
                }

                if (!hasDBData) {
                    continue;
                }

                // 确保checkDB文件夹存在
                new File(caseFolder, TestifyConstants.CHECK_DB_FOLDER).mkdirs();

                try {
                    // 对db进行预跑反填
                    for (File file : caseFolder.listFiles()) {
                        if (file.getName().contains(TestifyConstants.CHECK_DB_FOLDER)) {
                            // 对于db数据进行预跑反填
                            VirtualDataSet virtualDataSet =
                                    caseDatas.get(caseId).getExpectDataSet();
                            for (VirtualTable virtualTable : virtualDataSet.getVirtualTables()) {
                                String filePath = file.getPath() + "/" + virtualTable.getTableName() + ".csv";
                                // 如果该校验文件已经存在
                                if (new File(filePath).exists()) {
                                    // 如果是需要复写则再生成预跑反填结果
                                    if (overwrite) {
                                        CsvUtils.genCsvFromObjTable(virtualTable, filePath);
                                    }
                                } else {
                                    // 如果没有该文件则直接预跑反填完成
                                    CsvUtils.genCsvFromObjTable(virtualTable, filePath);
                                    logger.info("ACTS:" + virtualTable + "预跑反填db结果成功，路径=" + filePath);
                                }
                            }
                        }
                    }
                } catch (TestifyException e) {
                    // db返填异常不中断流程
                    logger.error("以下用例db返填失败（若已存在db数据不会进行覆盖，强制返填需要删除对应csv文件后再进行返填）", e);
                }
            }
        } catch (Throwable e) {
            logger.warn("unknow exception while saving case result保存用例结果未知异常", e);
        } finally {
            // 后置动作：删除用于保存用例执行结果相关文件
            clearTemporaryData();
        }
    }

    /**
     * 清除过程中数据
     */
    private static void clearTemporaryData() {

        File sqlLogFile = new File(SQL_LOG_PATH_NAME);
        if (sqlLogFile.exists()) {
            boolean r = sqlLogFile.delete();

            if (!r) {
                // 尝试再删除一次
                try {
                    FileUtils.forceDeleteOnExit(sqlLogFile);
                } catch (Exception e) {
                    logger.error("", e);
                }
            }
        }
    }

    /**
     * 保存用例结果到临时文件
     *
     * @throws IOException
     */
    private static void doSaveCaseResult(String caseFileFullName, Map<String, PrepareData> caseDatas)
            throws IOException {

        // 用例结果文件名为原文件名后最近"_res"
        File caseResFile = new File(StringUtils.replace(caseFileFullName, ".yaml", "_res.yaml"));

        if (!caseResFile.exists()) {
            // 创建一个新文件
            caseResFile.createNewFile();
        }

        // 保存用例结果到临时文件(自动覆盖原有内容)
        BaseDataUtil.storeToYaml(caseDatas, caseResFile);
    }

    /**
     * 2.0新数据模式返填结果对象, 强制返填/原对象为null而新对象有值时返填
     * 该函数主要判断是否需要返填，返填操作交由工具类执行
     *
     * @param yamlFile
     * @param resObj
     * @param events
     * @param overwrite
     */
    private static void doSaveCaseResult(File yamlFile, Object resObj, VirtualEventSet events, boolean overwrite) {
        // read file content
        CaseObjectSet caseObjectSet =
                BaseDataUtil.loadListFromYaml(yamlFile, Thread.currentThread().getContextClassLoader());
        boolean isUpdated = false;

        if (caseObjectSet.isEmpty()) {
            logger.error("No any objects read from " + yamlFile.getName() + ", check");
        }

        if (overwrite || (null == caseObjectSet.getRetObj() && null != resObj)) {
            caseObjectSet.setRetObj(resObj);
            isUpdated = true;
        }

        if (overwrite
                || (null == caseObjectSet.getEvents()
                        && 0 != events.getVirtualEventObjects().size())) {
            caseObjectSet.setEvents(BaseDataUtil.convertEventSetToList(events));
            isUpdated = true;
        }

        // write back file
        if (isUpdated) {
            logger.info(yamlFile.getPath() + ": has been updated.");
            BaseDataUtil.backFillToYaml(caseObjectSet, yamlFile);

            if (!overwrite) {
                logger.warn("Please check auto filled case result or events in your case yaml file.");
            }
        }
    }
}
