/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.testify.transfer;

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

import com.github.loadup.components.testify.collector.sqlLog.SqlLogParseFactory;
import com.github.loadup.components.testify.exception.YamlFileException;
import com.github.loadup.components.testify.model.PrepareData;
import com.github.loadup.components.testify.model.VirtualDataSet;
import com.github.loadup.components.testify.model.VirtualTable;
import com.github.loadup.components.testify.util.BaseDataUtil;
import com.github.loadup.components.testify.util.DeepCopyUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

/**
 *
 *
 */
public class TransferApi {
    static final String endRegex = "执行用例.*结束";
    static final String startRegex = "开始执行用例";
    private static final Logger Logger = LoggerFactory.getLogger(TransferApi.class);

    /***
     * 对外开放的迁移接口.
     *
     * @param argsYamlPath 拦截器拦截的yaml文件位置
     * @param tableFilePath 日志存储的文件位置
     * @param yamlPath 最终用例对应的yaml位置
     * @param caseIdPrefix caseId的前缀
     * @return
     */
    public static boolean transfer(String argsYamlPath, String tableFilePath, String yamlPath, String caseIdPrefix) {

        boolean success = true;
        try {
            File argsYaml = new File(argsYamlPath);
            Map<String, PrepareData> args =
                    BaseDataUtil.loadFromYaml(argsYaml, TransferApi.class.getClassLoader(), "GBK");

            Logger.info("拦截到的args结果:" + args);
            Map<String, PrepareData> tables = parseInsertLog(tableFilePath);

            Map<String, PrepareData> result = mergeArgsAndTables(args, tables);

            result = replaceAllCaseId(result, caseIdPrefix);

            File file = new File(yamlPath);
            storeToYaml(result, file);
        } catch (Exception e) {
            e.printStackTrace();
            success = false;
        } finally {

        }
        return success;
    }

    /***
     * 重命名所有的CaseId为标准.如果原有的caseId符合规范,则不修改.
     *
     * @param result
     * @param caseIdPrefix
     * @return
     */
    private static Map<String, PrepareData> replaceAllCaseId(Map<String, PrepareData> result, String caseIdPrefix) {

        int caseStart = 1;

        Map<String, PrepareData> replaceedResult = new HashMap<String, PrepareData>();
        for (Entry<String, PrepareData> caseEntry : result.entrySet()) {

            String newCaseId;
            if (caseEntry.getKey().contains(CaseConstants.caseContainsStr)) {
                newCaseId = caseEntry.getKey();
            } else {
                newCaseId = caseIdPrefix + "_" + caseStart;
            }

            replaceedResult.put(newCaseId, caseEntry.getValue());

            caseStart++;
        }

        Logger.info("合并后的结构:" + replaceedResult);
        return replaceedResult;
    }

    /***
     * 存储到yaml中
     *
     * @param prepareDatas
     * @param file
     */
    private static void storeToYaml(Map<String, PrepareData> prepareDatas, File file) {
        DumperOptions opt = new DumperOptions();
        Yaml yaml = new Yaml(new BaseDataUtil.MyRepresenter(opt));
        String str = yaml.dump(prepareDatas);
        try {
            FileUtils.writeStringToFile(file, str);
        } catch (Exception e) {
            throw new YamlFileException("读写文件出现异常", e);
        }
    }

    /***
     * 合并请求的prepareData和数据的prepareData
     *
     * @param args
     * @param tables
     * @return
     */
    private static Map<String, PrepareData> mergeArgsAndTables(
            Map<String, PrepareData> args, Map<String, PrepareData> tables) {

        for (String caseId : args.keySet()) {

            // talbes 可能为空
            if (tables.isEmpty()) {
                args.get(caseId).setDescription(caseId);
            } else {
                VirtualDataSet vdt = tables.get(caseId).getDepDataSet();
                VirtualDataSet copyed = DeepCopyUtils.deepCopy(vdt);
                args.get(caseId).setDescription(caseId);
                args.get(caseId).setDepDataSet(copyed);
            }
        }
        return args;
    }

    /***
     * 解析日志文件
     *
     * @param logFilePath
     * @return
     */
    private static Map<String, PrepareData> parseInsertLog(String logFilePath) {

        Map<String, PrepareData> map = new HashMap<String, PrepareData>();

        // 1.拆分log日志,解析到一个Map<String,List<String>> 列表,key是caseId,List<String>是所有当前case相关的列

        Map<String, List<String>> contentMap = splitLogContent(logFilePath);

        // 2.开始过滤List<String> ,过滤后的结果为List<String>,里面都是insert语句,顺便清洗掉

        contentMap = filterInvalidInfo(contentMap, null);

        // 3.开始解析insert语句
        Map<String, List<VirtualTable>> tables = new HashMap<String, List<VirtualTable>>();

        tables = parseInsertSqls(contentMap);

        // 4.开始填充到Map<String, PrepareData>中
        fillPrepareMap(tables, map);

        Logger.info("解析到的table结果:" + map);
        return map;
    }

    /***
     * 填充结果map
     *
     * @param tables
     * @param map
     */
    private static void fillPrepareMap(Map<String, List<VirtualTable>> tables, Map<String, PrepareData> map) {

        for (String caseId : tables.keySet()) {

            PrepareData value = new PrepareData();
            VirtualDataSet depDataSet = new VirtualDataSet();
            depDataSet.addTables(tables.get(caseId));
            value.setDepDataSet(depDataSet);
            map.put(caseId, value);
        }
    }

    /***
     * 解析insert内容
     *
     * @param contentMap
     * @return
     */
    private static Map<String, List<VirtualTable>> parseInsertSqls(Map<String, List<String>> contentMap) {
        Map<String, List<VirtualTable>> tables = new HashMap<String, List<VirtualTable>>();

        for (String caseId : contentMap.keySet()) {
            List<VirtualTable> value = new ArrayList<VirtualTable>();
            List<String> insertSqls = contentMap.get(caseId);
            for (String insertSql : insertSqls) {

                VirtualTable vt;
                vt = parseSingleInsertSql(insertSql);
                value.add(vt);
            }

            // 替换所有的flag标志位
            replaceFlag(value);

            tables.put(caseId, value);
        }
        return tables;
    }

    // 替换flag
    @SuppressWarnings({"unchecked"})
    private static void replaceFlag(List<VirtualTable> vtbs) {

        Method m = null;
        try {
            m = SqlLogParseFactory.class.getDeclaredMethod("fetchFieldFlagsFromDbModel", new Class<?>[] {String.class});
        } catch (Exception e) {
            //            Logger.error(e);
        }

        for (VirtualTable vtb : vtbs) {
            String tbName = vtb.getTableName();

            Map<String, String> fieldsFlag;
            try {
                if (m != null) {
                    m.setAccessible(true);
                    fieldsFlag = (Map<String, String>) m.invoke(SqlLogParseFactory.class, tbName);
                    vtb.setFlags(fieldsFlag);
                }
            } catch (Exception e) {
                //                Logger.error(e);
            }
        }
    }

    /***
     * 解析单条insert的sql
     *
     * @param insertSql
     * @return
     */
    private static VirtualTable parseSingleInsertSql(String insertSql) {

        String tableName = parseTableName(insertSql);
        VirtualTable vt = new VirtualTable();
        Map<String, Object> row = new HashMap<String, Object>();
        String tableNameFields = "";
        if (StringUtils.contains(insertSql, " into ") && StringUtils.contains(insertSql, " values")) {

            tableNameFields = insertSql
                    .substring(insertSql.indexOf(" into ") + 6, insertSql.indexOf(" values"))
                    .trim();
        } else if (StringUtils.contains(insertSql, " INTO ") && StringUtils.contains(insertSql, " VALUES")) {
            tableNameFields = insertSql
                    .substring(insertSql.indexOf(" INTO ") + 6, insertSql.indexOf(" VALUES"))
                    .trim();
        }
        String tableFields = tableNameFields
                .substring(tableNameFields.indexOf("(") + 1, tableNameFields.indexOf(")"))
                .trim();

        // 字段值
        String tableValues = "";
        if (StringUtils.contains(insertSql, " values")) {

            tableValues = StringUtils.substring(insertSql, insertSql.indexOf(" values") + 7)
                    .trim();
        } else if (StringUtils.contains(insertSql, " VALUES")) {
            tableValues = StringUtils.substring(insertSql, insertSql.indexOf(" VALUES") + 7)
                    .trim();
        }

        String[] tableFieldsArray = tableFields.split(",");
        // -2是有);两个符号
        String[] tableValuesArray =
                tableValues.substring(1, tableValues.length() - 1).split(",");

        // 然后对于map需要特殊处理

        String[] parsedArray = new String[tableFieldsArray.length];
        int indexResult = 0;
        for (int i = 0; i < tableValuesArray.length; i++) {

            if (StringUtils.startsWith(tableValuesArray[i], " '{")) {

                String newLine = "";
                while (!StringUtils.endsWith(tableValuesArray[i], "}'")) {
                    newLine += tableValuesArray[i];
                    i++;
                }

                newLine += tableValuesArray[i];
                parsedArray[indexResult] = newLine;
            } else {
                parsedArray[indexResult] = tableValuesArray[i];
            }

            indexResult++;
        }
        tableValuesArray = parsedArray;

        for (int index = 0; index < tableFieldsArray.length; index++) {

            // 过滤掉首位单引号
            String field = tableFieldsArray[index];
            field = StringUtils.trim(field);
            String value = tableValuesArray[index];
            value = StringUtils.trim(value);
            if (StringUtils.startsWith(field, "'")) {
                field = field.substring(field.indexOf("'") + 1, field.lastIndexOf("'"));
            }
            if (StringUtils.startsWith(value, "'")) {
                value = value.substring(value.indexOf("'") + 1, value.lastIndexOf("'"));
            }
            row.put(field, value);
        }
        vt.addRow(row);
        vt.setTableName(tableName);
        return vt;
    }

    /***
     * 过滤非insert语句
     *
     * @param contentMap
     * @param object
     * @return
     */
    private static Map<String, List<String>> filterInvalidInfo(Map<String, List<String>> contentMap, Object object) {

        final String startStr = "执行SQL语句: ";
        final String endStr = " 表名";
        final String keyword = "insert";
        final String keyword2 = "INSERT";
        for (String caseId : contentMap.keySet()) {
            List<String> oneCaseInsertSql = contentMap.get(caseId);
            List<String> newInsertSql = new ArrayList<String>();
            for (String str : oneCaseInsertSql) {
                if (StringUtils.contains(str, startStr)
                        && (StringUtils.contains(str, keyword) || StringUtils.contains(str, keyword2))) {
                    int start = str.indexOf(startStr) + startStr.length();
                    int end = str.indexOf(endStr);
                    String newSql = str.substring(start, end);
                    newInsertSql.add(newSql);
                }
            }

            contentMap.put(caseId, newInsertSql);
        }
        return contentMap;
    }

    private static String parseTableName(String sql) {

        sql = StringUtils.lowerCase(sql);
        String tableNameFields =
                sql.substring(sql.indexOf(" into") + 5, sql.indexOf(" values")).trim();
        return tableNameFields.substring(0, tableNameFields.indexOf("(")).trim();
    }

    /***
     * 分拆log内容
     *
     * @param logFilePath
     * @return
     */
    private static Map<String, List<String>> splitLogContent(String logFilePath) {

        Map<String, List<String>> map = new HashMap<String, List<String>>();
        BufferedReader logReader = null;
        try {
            logReader = new BufferedReader(new FileReader(logFilePath));

            String curLine = StringUtils.EMPTY;
            String caseId = StringUtils.EMPTY;
            while ((curLine = logReader.readLine()) != null) {

                if (curLine.contains(startRegex)) {
                    caseId = StringUtils.substring(curLine, curLine.indexOf(startRegex) + startRegex.length() + 1);
                    List<String> list = new ArrayList<String>();
                    map.put(caseId, list);
                } else if (curLine.matches(endRegex)) {
                    caseId = StringUtils.EMPTY;
                } else if (StringUtils.isNotEmpty(caseId)) {
                    map.get(caseId).add(curLine);
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != logReader) {
                    logReader.close();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return map;
    }
}
