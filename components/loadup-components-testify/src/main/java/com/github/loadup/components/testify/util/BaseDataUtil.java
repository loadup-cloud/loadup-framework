package com.github.loadup.components.testify.util;

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
import com.alibaba.fastjson2.JSONObject;
import com.github.loadup.components.testify.assemble.AssembleManager;
import com.github.loadup.components.testify.constant.TestifyConstants;
import com.github.loadup.components.testify.constant.TestifyPathConstants;
import com.github.loadup.components.testify.constant.TestifyYamlConstants;
import com.github.loadup.components.testify.data.model.GenerateCondition;
import com.github.loadup.components.testify.db.enums.CSVColEnum;
import com.github.loadup.components.testify.db.enums.DBFlagEnum;
import com.github.loadup.components.testify.enums.TestifyActionEnum;
import com.github.loadup.components.testify.enums.YamlFieldEnum;
import com.github.loadup.components.testify.exception.TestifyException;
import com.github.loadup.components.testify.exception.YamlFileException;
import com.github.loadup.components.testify.helper.CSVHelper;
import com.github.loadup.components.testify.model.*;
import com.github.loadup.components.testify.object.processor.ObjectProcessor;
import com.github.loadup.components.testify.object.result.Result;
import com.opencsv.CSVReader;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.springframework.util.CollectionUtils;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.nodes.*;
import org.yaml.snakeyaml.representer.Representer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.util.*;

@Slf4j
public class BaseDataUtil {
    private static String projectPath = "";

    /**
     * 将原一个yaml文件按case拆分成多个yaml文件并保存
     * 注意：工具执行完，确认新生成的按case拆分的yaml文件无误后，请手工删除原yaml文件
     *
     * @param testScriptSimpleName     对应测试脚本的名称如："GetArgInfoActsTest"
     * @param origYamlFileAbsolutePath 需要拆分的yaml文件的绝对路径
     * @param encoding                 编码如 "UTF-8" "GBK"
     */
    public static void saveYamlDataToCaseByCase(String testScriptSimpleName,
                                                String origYamlFileAbsolutePath, String encoding) {

        File yamlFile = new File(origYamlFileAbsolutePath);

        if (!yamlFile.exists()) {
            throw new RuntimeException("指定yaml文件不存在！");
        }

        Map<String, PrepareData> prepareDatas = loadFromYaml(yamlFile, Thread.currentThread()
                .getContextClassLoader());

        String yamlFolderPath = StringUtils.substringBeforeLast(origYamlFileAbsolutePath, "/") + "/"
                + testScriptSimpleName;
        File yamlFolder = new File(yamlFolderPath);

        storeToYamlByCase(prepareDatas, yamlFolder, encoding);

    }

    /**
     * 指定编码方式，每个case保存一个yaml文件
     *
     * @param prepareDatas
     * @param folder
     * @param encoding
     */
    public static void storeToYamlByCase(Map<String, PrepareData> prepareDatas, File folder,
                                         String encoding) {
        TreeMap<String, PrepareData> treeMap = new TreeMap<String, PrepareData>(
                new Comparator<String>() {
                    @Override
                    public int compare(String str1, String str2) {
                        return str1.compareTo(str2);
                    }
                });
        treeMap.putAll(prepareDatas);

        //清理yaml文件夹
        try {
            if (folder.exists()) {
                FileUtils.cleanDirectory(folder);
            }
        } catch (Exception e) {
            throw new YamlFileException("保存yaml的时候抛出异常", e);
        }

        for (String caseId : treeMap.keySet()) {

            TreeMap<String, PrepareData> tempMap = new TreeMap<String, PrepareData>();

            tempMap.put(caseId, treeMap.get(caseId));

            Yaml yaml = new Yaml(new MyRepresenter(new DumperOptions()));
            String str = yaml.dump(tempMap);
            try {

                File caseFile = new File(folder.getAbsolutePath() + "/" + caseId + ".yaml");
                FileUtils.writeStringToFile(caseFile, str, encoding);

            } catch (Exception e) {
                throw new YamlFileException("保存yaml的时候抛出异常", e);
            }
        }
    }

    /**
     * 指定编码方式保存yaml
     *
     * @param prepareDatas
     * @param file
     * @param encoding
     */
    public static void storeToYaml(Map<String, PrepareData> prepareDatas, File file, String encoding) {
        TreeMap<String, PrepareData> treeMap = new TreeMap<String, PrepareData>(
                new Comparator<String>() {
                    @Override
                    public int compare(String str1, String str2) {
                        return str1.compareTo(str2);
                    }
                });
        treeMap.putAll(prepareDatas);

        Yaml yaml = new Yaml(new MyRepresenter());
        String str = yaml.dump(treeMap);
        try {
            FileUtils.writeStringToFile(file, str, encoding);
        } catch (Exception e) {
            throw new YamlFileException("保存yaml的时候抛出异常", e);
        }
    }

    /**
     * 保存yaml
     *
     * @param prepareDatas
     * @param file
     */
    public static void storeToYaml(Map<String, PrepareData> prepareDatas, File file) {
        TreeMap<String, PrepareData> treeMap = new TreeMap<String, PrepareData>(
                new Comparator<String>() {
                    @Override
                    public int compare(String str1, String str2) {
                        return str1.compareTo(str2);
                    }
                });
        treeMap.putAll(prepareDatas);
        Yaml yaml = new Yaml(new MyRepresenter());
        String str = yaml.dump(treeMap);
        try {
            FileUtils.writeStringToFile(file, str);
        } catch (Exception e) {
            throw new YamlFileException("保存yaml的时候抛出异常", e);
        }
    }

    /**
     * 返填2.0新数据模式，包括出参和消息事件
     *
     * @param caseObjectSet
     * @param yamlFile
     */
    public static void backFillToYaml(CaseObjectSet caseObjectSet, File yamlFile) {
        // prepare yaml object
        Yaml yaml = getStdYaml();

        // prepare res yaml str
        String retObjTitle = caseObjectSet.isException() ?
                YamlFieldEnum.EXCEPTION.getCode() : YamlFieldEnum.RESULT.getCode();
        String res = retObjTitle + yaml.dump(caseObjectSet.getRetObj());
        // prepare msg yaml str
        String msg = YamlFieldEnum.EVENTS.getCode() + yaml.dump(caseObjectSet.getEvents());

        try {
            String yamlContent = FileUtils.readFileToString(yamlFile);
            // 考虑windows换行符为\r\n的情况
            yamlContent = yamlContent.replace("\r\n", "\n");

            String[] yamls = yamlContent.split(TestifyYamlConstants.YAML_SEPARATOR);

            if (yamls.length > YamlFieldEnum.RESULT.getPos()) {
                yamls[YamlFieldEnum.RESULT.getPos()] = res;
            }
            if (yamls.length > YamlFieldEnum.EVENTS.getPos()) {
                yamls[YamlFieldEnum.EVENTS.getPos()] = msg;
            }

            String backFillContent = String.join(TestifyYamlConstants.YAML_SEPARATOR, yamls);
            FileUtils.writeStringToFile(yamlFile, backFillContent);

        } catch (Exception e) {
            throw new YamlFileException("返填yaml的时候抛出异常", e);
        }
    }

    /**
     * 生成ACTS2.0新数据模式下的caseObjs_actual.yaml，用于保存执行后的实际数据
     *
     * @param resObj
     * @param events
     * @param yamlFile
     */
    public static void genActualYaml(Object resObj, VirtualEventSet events, File yamlFile) {
        if (yamlFile == null) {
            return;
        }

        // prepare yaml object
        Yaml yaml = getStdYaml();

        // prepare res yaml str
        String retObjTitle = YamlFieldEnum.RESULT.getCode();
        if (resObj != null && resObj.getClass().getName().toLowerCase().endsWith("exception")) {
            retObjTitle = YamlFieldEnum.EXCEPTION.getCode();
        }
        String res = retObjTitle + yaml.dump(resObj);

        // prepare msg yaml str
        String msg = YamlFieldEnum.EVENTS.getCode() + yaml.dump(BaseDataUtil.convertEventSetToList(events));

        try {
            // 获取原yaml文件数据
            String yamlContent = FileUtils.readFileToString(yamlFile);
            // 考虑windows换行符为\r\n的情况
            yamlContent = yamlContent.replace("\r\n", "\n");

            String[] yamls = yamlContent.split(TestifyYamlConstants.YAML_SEPARATOR);

            if (yamls.length > YamlFieldEnum.RESULT.getPos()) {
                yamls[YamlFieldEnum.RESULT.getPos()] = res;
            }
            if (yamls.length > YamlFieldEnum.EVENTS.getPos()) {
                yamls[YamlFieldEnum.EVENTS.getPos()] = msg;
            }

            // 组装实际结果内容
            String content = String.join(TestifyYamlConstants.YAML_SEPARATOR, yamls);

            // 写入文件
            File actualFile = new File(StringUtils.replace(yamlFile.getAbsolutePath(), ".yaml", "_actual.yaml"));
            FileUtils.writeStringToFile(actualFile, content);

        } catch (Exception e) {
            throw new YamlFileException("生成caseObjs_actual.yaml的时候抛出异常", e);
        }
    }

    /**
     * 从yaml文件读取测试用例
     *
     * @param folder      yaml所在的package
     * @param classLoader
     * @param encoding
     * @return
     */
    public static Map<String, PrepareData> loadFromYamlByCase(File folder, ClassLoader classLoader,
                                                              String encoding) {

        Map<String, PrepareData> caseMap = new HashMap<String, PrepareData>();
        if (folder.exists()) {
            try {
                File[] files = folder.listFiles();

                for (File file : files) {
                    if (file.getAbsolutePath().endsWith("yaml")) {
                        String str = FileUtils.readFileToString(file, encoding);

                        caseMap.putAll((Map<String, PrepareData>) getStdYaml(classLoader).load(str));
                    }
                }

            } catch (Exception e) {
                String sberr = e.getMessage();
                if (StringUtils.contains(sberr, " Cannot create property=")) {
                    String filedName = StringUtils.substringBetween(sberr,
                            "Unable to find property '", "' on class");
                    if (StringUtils.isBlank(filedName)) {
                        if (StringUtils.contains(sberr, "Class not found")) {
                            String clsName = StringUtils.substringBetween(sberr, "Class not found:",
                                    ";");
                            throw new YamlFileException("类" + clsName + "加载失败，建议重新mvn后导入工程!");
                        }
                    } else {
                        throw new YamlFileException("该类定义中未发现以下字段：" + StringUtils.trim(filedName)
                                + "，请确认该字段getter/setter方法或该类的本地jar包已更新至最新状态");
                    }

                }
                throw new YamlFileException("读取yaml的时候抛出异常", e);
            }
        }

        return caseMap;

    }

    /**
     * 从yaml文件读取测试用例
     *
     * @param file
     * @param classLoader
     * @param encoding
     * @return
     */
    public static Map<String, PrepareData> loadFromYaml(File file, ClassLoader classLoader,
                                                        String encoding) {
        if (file.exists()) {
            try {
                String str = FileUtils.readFileToString(file, encoding);

                return (Map<String, PrepareData>) getStdYaml(classLoader).load(str);

            } catch (Exception e) {
                String sberr = e.getMessage();
                if (StringUtils.contains(sberr, " Cannot create property=")) {
                    String filedName = StringUtils.substringBetween(sberr,
                            "Unable to find property '", "' on class");
                    if (StringUtils.isBlank(filedName)) {
                        if (StringUtils.contains(sberr, "Class not found")) {
                            String clsName = StringUtils.substringBetween(sberr, "Class not found:",
                                    ";");
                            throw new YamlFileException("类" + clsName + "加载失败，建议重新mvn后导入工程!");
                        }
                    } else {
                        throw new YamlFileException("该类定义中未发现以下字段：" + StringUtils.trim(filedName)
                                + "，请确认该字段getter/setter方法或该类的本地jar包已更新至最新状态");
                    }

                }
                throw new YamlFileException("读取yaml的时候抛出异常", e);
            }
        }
        return null;
    }

    /**
     * 从yaml读取测试用例
     *
     * @param file
     * @param classLoader
     * @return
     */
    public static Map<String, PrepareData> loadFromYaml(File file, ClassLoader classLoader) {
        if (file.exists()) {
            try {
                String str = FileUtils.readFileToString(file);

                return (Map<String, PrepareData>) getStdYaml(classLoader).load(str);
            } catch (Exception e) {
                String sberr = e.getMessage();
                if (StringUtils.contains(sberr, " Cannot create property=")) {
                    String filedName = StringUtils.substringBetween(sberr,
                            "Unable to find property '", "' on class");
                    if (StringUtils.isBlank(filedName)) {
                        if (StringUtils.contains(sberr, "Class not found")) {
                            String clsName = StringUtils.substringBetween(sberr, "Class not found:",
                                    ";");
                            throw new YamlFileException("类" + clsName + "加载失败，建议重新mvn后导入工程!");
                        }
                    } else {
                        throw new YamlFileException("该类定义中未发现以下字段：" + StringUtils.trim(filedName)
                                + "，请确认该字段getter/setter方法或该类的本地jar包已更新至最新状态");
                    }

                }
                throw new YamlFileException("读取yaml的时候抛出异常", e);
            }
        }
        return null;
    }

    /**
     * 从yaml的字符串读取测试用例
     *
     * @param str
     * @param classLoader
     * @return
     */
    public static Map<String, PrepareData> loadFromYaml(String str, ClassLoader classLoader) {

        try {
            return (Map<String, PrepareData>) getStdYaml(classLoader).load(str);

        } catch (Exception e) {
            String sberr = e.getMessage();
            if (StringUtils.contains(sberr, " Cannot create property=")) {
                String filedName = StringUtils.substringBetween(sberr, "Unable to find property '",
                        "' on class");
                if (StringUtils.isBlank(filedName)) {
                    if (StringUtils.contains(sberr, "Class not found")) {
                        String clsName = StringUtils
                                .substringBetween(sberr, "Class not found:", ";");
                        throw new YamlFileException("类" + clsName + "加载失败，建议重新mvn后导入工程!");
                    }
                } else {
                    throw new YamlFileException("该类定义中未发现以下字段：" + StringUtils.trim(filedName)
                            + "，请确认该字段getter/setter方法或该类的本地jar包已更新至最新状态");
                }

            }
            throw new YamlFileException("读取yaml的时候抛出异常", e);
        }

    }

    /**************************** ACTS 2.0 Utils ****************************/

    public static Yaml getStdYaml() {
        return getStdYaml(Thread.currentThread().getContextClassLoader());
    }

    public static Yaml getStdYaml(ClassLoader classLoader) {
        // 设置yaml文本按flow模式输出
        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.FLOW);
        // 设置美化格式
        dumperOptions.setPrettyFlow(true);
        // 设置本地时区
        dumperOptions.setTimeZone(TimeZone.getDefault());

        MyRepresenter myRepresenter = new MyRepresenter();
        myRepresenter.addClassTag(JSONObject.class, new Tag(JSONObject.class));
        myRepresenter.addClassTag(Long.class, new Tag(Long.class));


        Yaml yaml = new Yaml(new SelectiveConstructor(classLoader), myRepresenter, dumperOptions);
        return yaml;
    }

    public static void genCaseFolderStucture(File dir, String caseName, String yamlStr,
                                             List<VirtualTable> prepareTables, List<VirtualTable> checkTables) throws Exception {
        // generate case folder
        File caseFolder = new File(dir, caseName);
        caseFolder.mkdir();

        // generate case yaml
        File caseYaml = new File(caseFolder, TestifyConstants.CASE_YAML_NAME);
        caseYaml.createNewFile();
        FileUtils.writeStringToFile(caseYaml.getPath(), yamlStr, Charset.defaultCharset().displayName(), false);

        // generate prepare db data
        File prepareFolder = new File(caseFolder, TestifyConstants.PREPARE_DB_FOLDER);
        prepareFolder.mkdir();
        if (prepareTables != null) {
            for (VirtualTable prepareTable : prepareTables) {
                writeTableData(prepareTable, prepareFolder);
            }
        }

        // generate check db data
        File checkFolder = new File(caseFolder, TestifyConstants.CHECK_DB_FOLDER);
        checkFolder.mkdir();
        if (checkTables != null) {
            for (VirtualTable checkTable : checkTables) {
                writeTableData(checkTable, checkFolder);
            }
        }

    }

    public static void writeTableData(VirtualTable table, File folder) {
        if (table == null || table.getTableData() == null || table.getTableData().size() < 1) {
            log.error("[ConvertDataError]Table is empty.");
            return;
        }

        String tableName = table.getTableName();
        List<Map<String, Object>> rows = table.getTableData();
        Set<String> keys = rows.get(0).keySet();


        List<String[]> dbmodel = new ArrayList<String[]>();

        List<String> headers = new ArrayList<String>(Arrays.asList(CSVColEnum.COLUMN.getCode(), CSVColEnum.TYPE.getCode(),
                CSVColEnum.COMMENT.getCode(), CSVColEnum.PRIMARY.getCode(),
                CSVColEnum.NULLABLE.getCode(), CSVColEnum.FLAG.getCode()));
        for (int i = 0; i < rows.size(); i++) {
            headers.add(CSVColEnum.VALUE.getCode());
        }
        dbmodel.add(headers.toArray(new String[headers.size()]));

        for (String key : keys) {
            List<String> fieldInfo = new ArrayList<String>(Arrays.asList(key, "",
                    "", "", "", table.getFlagByFieldNameIgnoreCase(key)));
            for (int i = 0; i < rows.size(); i++) {
                Object val = rows.get(i).get(key);
                if (val == null) {
                    fieldInfo.add(null);
                } else {
                    fieldInfo.add(val.toString());
                }
            }

            dbmodel.add(fieldInfo.toArray(new String[fieldInfo.size()]));
        }

        DbTableModelUtil.writeToCsv(new File(folder, tableName + ".csv"), dbmodel, Charset.defaultCharset().displayName());
    }




    /**
     * A从所有用例目录读取用例集数据
     *
     * @param caseFolders
     * @param classLoader
     * @return
     */
    public static Map<String, PrepareData> loadFromCaseFolders(List<File> caseFolders, ClassLoader classLoader) {
        Map<String, PrepareData> caseMap = new HashMap<String, PrepareData>();
        for (File caseFolder : caseFolders) {
            PrepareData prepareData = loadFromCaseFolder(caseFolder, classLoader);
            if (null != prepareData) {
                caseMap.put(caseFolder.getName(), prepareData);
            } else {
                log.warn(caseFolder.getName() + " cannot read case data!");
            }
        }
        return caseMap;
    }

    public static PrepareData loadFromCaseFolder(File caseFolder, ClassLoader classLoader) {
        PrepareData prepareData = new PrepareData();
        File yamlFile = null;
        File prepareDBFolder = null;
        File checkDBFolder = null;

        // retrieve target files
        for (File file : caseFolder.listFiles()) {
            if (file.isFile() && (file.getName().endsWith(".yaml") || file.getName().endsWith(".yml"))) {
                if (file.getName().endsWith("_actual.yaml") || file.getName().endsWith("_actual.yml")) {
                    // 此处跳过备用比较文件，防止误读当用例数据
                    continue;
                }
                yamlFile = file;
            } else if (file.isDirectory() && file.list().length > 0) {
                if (file.getName().equals(TestifyConstants.PREPARE_DB_FOLDER)) {
                    prepareDBFolder = file;
                } else if (file.getName().equals(TestifyConstants.CHECK_DB_FOLDER)) {
                    checkDBFolder = file;
                }
            }
        }


        if (null == yamlFile) {
            return null;
        }

        CaseObjectSet caseObjectSet = loadListFromYaml(yamlFile, classLoader);

        loadCaseObjectSet2PrepareData(caseObjectSet, prepareData);

        // load caseId
        String caseId = caseFolder.getName();
        try {
            String yamlContent = FileUtils.readFileToString(yamlFile);
            String desc = StringUtils.substringBetween(yamlContent, "Case Desc:", "\n").trim();
            if (!desc.equals("用例描述填这行")) {
                caseId = desc;
            }
        } catch (Exception e) {
            log.warn(e.getMessage());
        }
        prepareData.setDescription(caseId);


        //load db data
        if (null != prepareDBFolder) {
            prepareData.setDepDataSet(readDBDataFromCsv(prepareDBFolder));
        }

        if (null != checkDBFolder) {
            prepareData.setExpectDataSet(readDBDataFromCsv(checkDBFolder));
        }


        return prepareData;
    }

    private static void loadCaseObjectSet2PrepareData(CaseObjectSet caseObjectSet, PrepareData prepareData) {
        // load args
        prepareData.setArgs(new VirtualArgs(caseObjectSet.getArgs()));

        // load result/exception
        if (caseObjectSet.isException()) {
            VirtualException ve = new VirtualException(caseObjectSet.getRetObj());
            prepareData.setExpectException(ve);
        } else {
            VirtualResult virtualResult = new VirtualResult(caseObjectSet.getRetObj());
            prepareData.setExpectResult(virtualResult);
        }

        // load events
        VirtualEventSet virtualEventSet = parseEventSetFromList(caseObjectSet.getEvents());
        prepareData.setExpectEventSet(virtualEventSet);

        // load user-defined params
        prepareData.setVirtualParams(new VirtualParams(caseObjectSet.getParams()));

        // load virtual mocks
//        VirtualMockSet virtualMockSet = new VirtualMockSet();
//        virtualMockSet.setVirtualMockList(caseObjectSet.getVirtualMocks());
//        prepareData.setVirtualMockSet(virtualMockSet);

        // load virtual components
        VirtualComponentSet virtualComponentSet = parseComponentSetFromList(caseObjectSet.getComponents());
        prepareData.setVirtualComponentSet(virtualComponentSet);

        // set flags
        if (null != caseObjectSet.getFlags()) {
            setFlags(prepareData, caseObjectSet.getFlags());
        }

    }

    /**
     * 对预期校验项（结果、异常、消息）的字段设置flag
     *
     * @param prepareData
     * @param flags
     */
    public static void setFlags(PrepareData prepareData, Map<String, Map<String, String>> flags) {

        // set result flag
        if (prepareData.getExpectResult() != null
                && prepareData.getExpectResult().getVirtualObject() != null) {
            mergeFlagMap(flags, prepareData.getExpectResult().getVirtualObject().getFlags());
        }

        // set exception flag
        if (prepareData.getExpectException() != null
                && prepareData.getExpectException().getVirtualObject() != null) {
            mergeFlagMap(flags, prepareData.getExpectException().getVirtualObject().getFlags());
        }

        // set event flag
        if (prepareData.getExpectEventSet() != null) {
            for (VirtualEventObject obj :
                    prepareData.getExpectEventSet().getVirtualEventObjects()) {
                mergeFlagMap(flags, obj.getEventObject().getFlags());
            }
        }

    }

    /**
     * merge srcFlags into destFlags
     *
     * @param srcFlags
     * @param destFlags
     */
    private static void mergeFlagMap(Map<String, Map<String, String>> srcFlags,
                                     Map<String, Map<String, String>> destFlags) {
        if (null == srcFlags || null == destFlags) {
            return;
        }
        for (String objName : srcFlags.keySet()) {
            if (destFlags.containsKey(objName)) {
                for (String key : srcFlags.get(objName).keySet()) {
                    destFlags.get(objName).putIfAbsent(key, srcFlags.get(objName).get(key));
                }
            } else {
                destFlags.put(objName, srcFlags.get(objName));
            }
        }
    }

    /**
     * 将2.0数据模式中的消息存储类型：List<Map<String, Object>> 转换成VirtualEventSet
     *
     * @param events
     * @return
     */
    private static VirtualEventSet parseEventSetFromList(List<Map<String, Object>> events) {
        VirtualEventSet virtualEventSet = new VirtualEventSet();
        if (CollectionUtils.isEmpty(events)) {
            return virtualEventSet;
        }

        for (int i = 0; i < events.size(); i++) {
            Map event = events.get(i);
            VirtualEventObject obj = new VirtualEventObject();
            obj.setEventCode((String) event.get("eventCode"));
            obj.setTopicId((String) event.get("topicId"));
            obj.setFlag((String) event.get("isExist"));
            obj.setEventObject(new VirtualObject(event.get("eventObject")));
            virtualEventSet.addEventObject(obj);
        }

        return virtualEventSet;
    }

    /**
     * 将2.0数据模式中的组件化类型：List<Map<String, Object>> 转换成VirtualComponentSet
     *
     * @param compos
     * @return
     */
    private static VirtualComponentSet parseComponentSetFromList(List<Map<String, Object>> compos) {
        VirtualComponentSet virtualComponentSet = new VirtualComponentSet();
        if (CollectionUtils.isEmpty(compos)) {
            return virtualComponentSet;
        }

        for (int i = 0; i < compos.size(); i++) {
            Map compo = compos.get(i);
            VirtualComponent obj = new VirtualComponent();
            obj.setUseOrigData((Boolean) compo.get("useOrigData"));
            obj.setComponentId((String) compo.get("caseId"));
            obj.setComponentClass((String) compo.get("componentClass"));
            virtualComponentSet.addVirtualComponent(obj);
        }

        return virtualComponentSet;
    }


    /**
     * 将VirtualEventSet转换成2.0数据模式中的消息存储类型：List<Map<String, Object>>
     *
     * @param virtualEventSet
     * @return
     */
    public static List<Map<String, Object>> convertEventSetToList(VirtualEventSet virtualEventSet) {
        if (virtualEventSet == null) {
            return null;
        }

        List<VirtualEventObject> eventObjs = virtualEventSet.getVirtualEventObjects();

        if (eventObjs.size() < 1) {
            return null;
        }

        List events = new ArrayList<Map<String, Object>>();

        for (int i = 0; i < eventObjs.size(); i++) {
            VirtualEventObject eventObj = eventObjs.get(i);
            Map event = new HashMap<String, Object>();
            event.put("eventCode", eventObj.getEventCode());
            event.put("topicId", eventObj.getTopicId());
            event.put("isExist", eventObj.getIsExist());
            event.put("eventObject", eventObj.getEventObject().getObject());

            events.add(event);
        }

        return events;
    }

    /**
     * ACTS2.0新数据模式的yaml读取用例，仅包含原始入参出参
     *
     * @param yamlFile
     * @param classLoader
     * @return
     */
    public static CaseObjectSet loadListFromYaml(File yamlFile, ClassLoader classLoader) {

        try {
            Yaml yaml = new Yaml(new SelectiveConstructor(classLoader),
                    new MyRepresenter());
            Iterator<Object> yamlContent = yaml.loadAll(ParamFilter.filterJson(FileUtils.readFileToString(yamlFile))).iterator();

            CaseObjectSet caseObjectSet = new CaseObjectSet(IteratorUtils.toList(yamlContent));

            return caseObjectSet;
        } catch (Exception e) {
            String sberr = e.getMessage();
            if (StringUtils.contains(sberr, " Cannot create property=")) {
                String filedName = StringUtils.substringBetween(sberr, "Unable to find property '",
                        "' on class");
                if (StringUtils.isBlank(filedName)) {
                    if (StringUtils.contains(sberr, "Class not found")) {
                        String clsName = StringUtils
                                .substringBetween(sberr, "Class not found:", ";");
                        throw new YamlFileException("类" + clsName + "加载失败，建议重新mvn后导入工程!");
                    }
                } else {
                    throw new YamlFileException("该类定义中未发现以下字段：" + StringUtils.trim(filedName)
                            + "，请确认该字段getter/setter方法或该类的本地jar包已更新至最新状态");
                }

            }
            throw new YamlFileException("读取yaml的时候抛出异常", e);
        }
    }

    /**
     * 2.0新数据模式，从csv文件读取db数据，若包含M标签则将同一表拆分为多份VirtualTable进行存储
     *
     * @param csvFolder
     * @return
     */
    public static VirtualDataSet readDBDataFromCsv(File csvFolder) {
        VirtualDataSet virtualDataSet = new VirtualDataSet();
        for (File csvFile : csvFolder.listFiles()) {
            if (csvFile.isDirectory() || !csvFile.getName().endsWith(".csv")) {
                log.warn(csvFile.getPath() + " is not a qualified db file.");
                continue;
            }

            String tableName = csvFile.getName().replace(".csv", "");

            // process csv data
            List dbdata = CSVHelper.readFromCsv(csvFile);

            String[] firstRow = (String[]) dbdata.get(0);
            List<String> titles = new ArrayList<String>();

            //对标题行进行特殊处理，防止格式化影响（但数据无法消除格式化影响，最好是引导用户不要去格式化csv文件内容）
            for (String title : firstRow) {
                titles.add(title.replaceAll("\"", "").trim());
            }

            int colNameIndex = titles.indexOf(CSVColEnum.COLUMN.getCode());
            int flagIndex = titles.indexOf(CSVColEnum.FLAG.getCode());
            int valIndex = titles.indexOf(CSVColEnum.VALUE.getCode());

            List<Map<String, Object>> tableData = new ArrayList<>();
            Map<String, String> flags = new HashMap<>();

            // 考虑有多个value的case（多行数据）
            int valCount = 0;
            for (String title : titles) {
                if (title.equals(CSVColEnum.VALUE.getCode())) {
                    valCount++;
                    tableData.add(new HashMap<String, Object>());
                }
            }

            // 从第二行开始，第一行是标题
            for (int i = 1; i < dbdata.size(); i++) {
                String[] colData = (String[]) dbdata.get(i);
                String colName = colData[colNameIndex].replaceAll("\"", "").trim();
                String flag = colData[flagIndex].replaceAll("\"", "").trim();
                flags.put(colName, flag);

                // 多行数据的情况，默认value后面都是value
                for (int j = 0; j < valCount; j++) {
                    tableData.get(j).put(colName, ParamFilter.filterJson(colData[valIndex + j]));
                }

            }

            // 添加单表数据到DB数据集，如果包含M标签，则每一条数据用一个VirtualTable单独进行存储
            if (flags.containsValue(DBFlagEnum.M.name())) {
                for (Map<String, Object> rowData : tableData) {
                    Map<String, String> newFlags = new HashMap<>();
                    // 每行数据单独存一个VirtualTable，当遇到M flag则更新至当前行的flag并更新rowData里面的值
                    for (String field : flags.keySet()) {
                        String currentFlag = flags.get(field);
                        if (currentFlag.equals(DBFlagEnum.M.name())) {
                            // 解析出真实的flag
                            String val = (String) rowData.get(field);
                            String[] sl = val.split("\\|", 2);
                            if (sl.length == 2) {
                                // 重组实际flag和值
                                currentFlag = sl[0];
                                val = sl[1];
                            } else if (sl.length == 1) {
                                // 仅存在flag, 一般是N flag的场景，N|
                                currentFlag = sl[0];
                                val = null;
                            } else {
                                throw new TestifyException(
                                        "[M_Flag]字段对应值解析失败，请保证M flag的字段内容格式为 {flag}|{value}，如Y|actual_cotent");
                            }
                            // 更新值
                            rowData.put(field, val);
                        }
                        newFlags.put(field, currentFlag);
                    }

                    VirtualTable virtualTable = new VirtualTable();
                    virtualTable.setTableName(tableName);
                    virtualTable.setFlags(newFlags);
                    virtualTable.addRow(rowData);

                    virtualDataSet.addVirtualTable(virtualTable);
                }
            } else {
                // 塞单个VirtualTable就行
                VirtualTable virtualTable = new VirtualTable();
                virtualTable.setTableName(tableName);
                virtualTable.setFlags(flags);
                virtualTable.setTableData(tableData);

                virtualDataSet.addVirtualTable(virtualTable);
            }
        }

        return virtualDataSet;
    }

    /**
     * 填充用例结果
     *
     * @param caseFilePath  原始用例文件全路径
     * @param originalCases 原始用例集合
     * @param resultCases   结果用例集合
     */
    public static void fillCaseResult(String caseFilePath, String encoding,
                                      Map<String, PrepareData> originalCases,
                                      Map<String, PrepareData> resultCases) {

        //结果用例替换原有用例
        for (String caseId : originalCases.keySet()) {
            if (resultCases.containsKey(caseId)) {
                originalCases.put(caseId, resultCases.get(caseId));
            }
        }

        //重写原始用例文件
        // storeToYaml(originalCases, new File(caseFilePath), encoding);
    }

    /**
     * 填充用例结果
     *
     * @param caseFilePath   原始用例文件全路径
     * @param originalCases  原始用例集合
     * @param resultCases    结果用例集合
     * @param needFillTables 需要反填的表
     */
    public static void fillCaseResult(String caseFilePath, String encoding,
                                      Map<String, PrepareData> originalCases,
                                      Map<String, PrepareData> resultCases,
                                      Map<String, Set<String>> needFillTables) {

        //先做一个操作,把 resultMap 中不需要填充的还原,先保留原始的表

        //结果用例替换原有用例
        for (String caseId : originalCases.keySet()) {
            if (resultCases.containsKey(caseId)) {
                //先做一个操作,把 resultMap 中不需要填充的还原,这是原始的,用户自己填写的.
                List<VirtualTable> virtualTables = originalCases.get(caseId).getExpectDataSet()
                        .getVirtualTables();
                PrepareData resultPrepareData = resultCases.get(caseId);

                //然后删掉不在needFillTables的表

                List<VirtualTable> resultExceptTable = resultPrepareData.getExpectDataSet()
                        .getVirtualTables();

                if (resultExceptTable != null && !resultExceptTable.isEmpty()) {
                    Iterator<VirtualTable> iter = resultExceptTable.iterator();
                    while (iter.hasNext()) {
                        if (!needFillTables.get(caseId).contains(iter.next().getTableName())) {
                            iter.remove();
                        }
                    }
                }

                originalCases.put(caseId, resultPrepareData);

                originalCases.get(caseId).getExpectDataSet().addTables(virtualTables);
            }
        }

        //重写原始用例文件
        // storeToYaml(originalCases, new File(caseFilePath), encoding);
    }

    public static Map<String, PrepareData> refreshBase(Map<String, PrepareData> tmp,
                                                       ClassLoader classLoader, String rootPath,
                                                       String encode) {
        for (Map.Entry<String, PrepareData> entry : tmp.entrySet()) {
            PrepareData prepareData = entry.getValue();
            for (VirtualObject virtualObject : prepareData.getArgs().getVirtualObjects()) {
                refreshObjBase(virtualObject, classLoader, rootPath);
            }
            refreshObjBase(prepareData.getExpectException().getVirtualObject(), classLoader,
                    rootPath);
            refreshObjBase(prepareData.getExpectResult().getVirtualObject(), classLoader, rootPath);
            for (VirtualTable virtualTable : prepareData.getDepDataSet().getVirtualTables()) {
                refreshDBBase(virtualTable, rootPath, encode);
            }
            for (VirtualTable virtualTable : prepareData.getExpectDataSet().getVirtualTables()) {
                refreshDBBase(virtualTable, rootPath, encode);
            }
        }
        return tmp;
    }

    private static Object refreshObjBase(VirtualObject virtualObject, ClassLoader classLoader,
                                         String rootPath) {
        String baseName = virtualObject.objBaseName;
        String desc = virtualObject.getObjBaseDesc();
        Object object = virtualObject.getObject();
        if (StringUtils.isBlank(baseName) || StringUtils.isBlank(desc) || object == null) {
            return object;
        }

        Object baseObj = getObjectFromBase(classLoader, baseName, desc, rootPath);

        if (baseObj == null) {
            return object;
        }
        Class<?> clazz = object.getClass();
        if (!clazz.isInstance(baseObj)) {
            return object;
        }
        Field[] fields = object.getClass().getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                if (field.get(baseObj) != null && field.get(object) == null) {
                    field.set(object, field.get(baseObj));
                }

            } catch (Exception e) {
                throw new TestifyException("反射获取对象失败", e);
            }
        }
        return object;
    }

    /**
     * 将指定yaml文件、文件夹中，指定类中某个不需要校验的字段批量设置为N
     *
     * @param file               文件，yaml数据文件，或者包含yaml数据文件的文件夹
     * @param classLoader        类加载器
     * @param classQualifiedName 待修改Flag的类的全称
     * @param fieldName          待修改Flag的属性名
     */
    public static void refreshAllObjFlagN(File file, ClassLoader classLoader,
                                          String classQualifiedName, String fieldName) {
        if (file.exists()) {
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                for (File f : files) {
                    refreshAllObjFlagN(f, classLoader, classQualifiedName, fieldName);
                }
            } else {

                if (!file.getName().endsWith(".yaml")) {
                    return;
                }
                Map<String, PrepareData> tmp = loadFromYaml(file, classLoader, null);

                for (Map.Entry<String, PrepareData> entry : tmp.entrySet()) {
                    PrepareData prepareData = entry.getValue();
                    for (VirtualObject virtualObject : prepareData.getArgs().getVirtualObjects()) {
                        refreshObjFlagN(virtualObject, classQualifiedName, fieldName);
                    }
                    refreshObjFlagN(prepareData.getExpectException().getVirtualObject(),
                            classQualifiedName, fieldName);

                    refreshObjFlagN(prepareData.getExpectResult().getVirtualObject(),
                            classQualifiedName, fieldName);

                }

                storeToYaml(tmp, file);

            }

        }

    }

    private static void refreshObjFlagN(VirtualObject virtualObject, String classQualifiedName,
                                        String fieldName) {
        Map<String, Map<String, String>> currentFlag = virtualObject.getFlags();

        if (null != currentFlag.get(classQualifiedName)) {
            currentFlag.get(classQualifiedName).put(fieldName, "N");
        } else {
            Map<String, String> classFieldFlagMap = new HashMap<String, String>();
            classFieldFlagMap.put(fieldName, "N");
            currentFlag.put(classQualifiedName, classFieldFlagMap);
        }

    }

    private static VirtualTable refreshDBBase(VirtualTable virtualTable, String rootPath,
                                              String encode) {
        if (StringUtils.isBlank(virtualTable.getTableName())
                || StringUtils.isBlank(virtualTable.getTableBaseDesc())) {
            return virtualTable;
        }
        VirtualTable baseObjVirtualTable = getVirtualTableFromBase(virtualTable.getTableName(),
                virtualTable.getTableBaseDesc(), rootPath, encode);
        Map<String, Object> baseRow = baseObjVirtualTable.getTableData().get(0);

        //base里面多的要新增
        for (String key : baseRow.keySet()) {
            for (Map<String, Object> row : virtualTable.getTableData()) {
                if (row.containsKey(key)) {
                    break;
                } else {
                    row.put(key, baseRow.get(key));
                }
            }
        }
        //base里面没有的要remove掉
        for (Map<String, Object> row : virtualTable.getTableData()) {
            for (String key : row.keySet()) {
                if (baseRow.containsKey(key)) {
                    break;
                } else {
                    row.remove(key);
                }
            }
        }

        return virtualTable;
    }

    /**
     * 这是给插件用的．加载所有的模板文件
     *
     * @param rootPath resource目录
     * @param subPath  objBase还是dataBase
     * @return
     */
    public static List<String> loadBase(String rootPath, String subPath) {

        List<String> baseList = new ArrayList<String>();

        String baseDBPath = rootPath + subPath;
        File rootFile = new File(baseDBPath);
        if (!rootFile.isDirectory()) {
            return baseList;
        }
        File[] files = rootFile.listFiles();

        for (File file : files) {
            if (file.isDirectory() && file.listFiles() != null && file.listFiles().length == 0) {
                continue;
            }
            if (file.getName().contains("svn") || file.getName().contains("git")
                    || file.getName().contains("DS_Store")) {
                continue;
            }
            baseList.add(file.getName().replace(".csv", ""));
        }
        return baseList;
    }

    public static List<String> loadDesc(String csvPath) {

        List<String> descList = new ArrayList<String>();
        @SuppressWarnings("rawtypes")
        List tableList = CSVHelper.readFromCsv(csvPath);
        if (tableList == null || tableList.size() == 0) {
            return null;
        }
        String[] labels = (String[]) tableList.get(0);
        for (String label : labels) {
            if (CSVColEnum.getByCode(label) == null) {
                descList.add(label);
            }
        }
        return descList;
    }

    /**
     * 从base加载对象
     *
     * @param type           需要被包装成的对象
     * @param classLoader    需要传入一个加载了目标对象的classloader
     * @param ObjectBaseName base的文件名
     * @param desc           base里面的desc名
     * @param rootPath       base文件上层resource目录
     * @return
     * @throws Exception
     */
    public static Object getVirtualObjectFromBase(Class<?> type, ClassLoader classLoader,
                                                  String ObjectBaseName, String desc,
                                                  String rootPath) throws Exception {
        if (StringUtils.isBlank(rootPath)) {
            throw new TestifyException("csvPath为空，构建" + ObjectBaseName + "失败");
        }

        ObjectProcessor processor;

        //带范型的情况，模版文件夹带有范型的名字

        if (type.equals(VirtualEventObject.class)) {

            processor = new ObjectProcessor(classLoader, rootPath
                    + TestifyPathConstants.OBJECT_DATA_PATH
                    + ObjectBaseName + "/" + ObjectBaseName
                    + ".csv", desc);
            Object obj = processor.genObject();
            VirtualEventObject virtualEventObject = new VirtualEventObject();
            VirtualObject vir = new VirtualObject(obj);
            vir.setFlags(processor.getFlags());
            virtualEventObject.setEventObject(vir);
            return virtualEventObject;
        } else if (type.equals(VirtualObject.class)) {
            processor = new ObjectProcessor(classLoader, rootPath
                    + TestifyPathConstants.OBJECT_DATA_PATH
                    + ObjectBaseName + "/" + ObjectBaseName
                    + ".csv", desc);
            Object obj = processor.genObject();

            VirtualObject virtualObject = new VirtualObject(obj, obj.getClass().getCanonicalName());
            virtualObject.setFlags(processor.getFlags());
            return virtualObject;
        } else {
            processor = new ObjectProcessor(classLoader, rootPath
                    + TestifyPathConstants.OBJECT_DATA_PATH
                    + ObjectBaseName + "/" + ObjectBaseName
                    + ".csv", desc);
            Object obj = processor.genObject();
            return obj;
        }

    }

    /**
     * 从base加载对象，带编码
     *
     * @param classLoader    需要传入一个加载了目标对象的classloader
     * @param ObjectBaseName base的文件名
     * @param desc           base里面的desc名
     * @param rootPath       base文件上层resource目录
     * @return
     */
    @SuppressWarnings("unchecked")
    public static Object getObjectFromBase(ClassLoader classLoader, String ObjectBaseName,
                                           String desc, String rootPath, String encoding) {
        if (StringUtils.isBlank(rootPath)) {
            throw new TestifyException("csvPath为空，构建" + ObjectBaseName + "失败");
        }
        Object obj = null;
        try {

            String csvPath = rootPath + TestifyPathConstants.OBJECT_DATA_PATH + ObjectBaseName + "/"
                    + ObjectBaseName + ".csv";
            ObjectProcessor processor = new ObjectProcessor(classLoader, csvPath, desc, encoding);

            //VirtualMap的情况单独处理，加载所有key－value
            if (processor.getClassName().equals("com.github.loadup.components.test.model.VirtualMap")) {

                Map<Object, Object> map = new HashMap<Object, Object>();
                List<String> descList = loadDesc(csvPath);

                for (String descTemp : descList) {
                    ObjectProcessor processorTemp = new ObjectProcessor(classLoader, csvPath,
                            descTemp, encoding);
                    Map<Object, Object> descMap = (Map<Object, Object>) processorTemp.genObject();
                    map.putAll(descMap);
                }

                obj = map;
            } else {
                obj = processor.genObject();
            }

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return obj;
    }

    /**
     * 从base加载对象，带编码
     *
     * @param classLoader    需要传入一个加载了目标对象的classloader
     * @param ObjectBaseName base的文件名
     * @param desc           base里面的desc名
     * @param rootPath       base文件上层resource目录
     * @return
     */
    public static Map<String, Map<String, String>> getObjBaseFlags(ClassLoader classLoader,
                                                                   String ObjectBaseName,
                                                                   String desc, String rootPath,
                                                                   String encoding) {

        if (StringUtils.isBlank(rootPath)) {
            throw new TestifyException("csvPath为空，构建" + ObjectBaseName + "失败");
        }
        Object obj = null;
        Map<String, Map<String, String>> flagMap = new LinkedHashMap<String, Map<String, String>>();
        try {

            String csvPath = rootPath + TestifyPathConstants.OBJECT_DATA_PATH + ObjectBaseName + "/"
                    + ObjectBaseName + ".csv";
            ObjectProcessor processor = new ObjectProcessor(classLoader, csvPath, desc, encoding);

            //执行下模板执行，里面有获取flag变量
            processor.genObject();

            flagMap = processor.getFlags();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return flagMap;

    }

    /**
     * 从base加载对象,不带编码
     *
     * @param classLoader    需要传入一个加载了目标对象的classloader
     * @param ObjectBaseName base的文件名
     * @param desc           base里面的desc名
     * @param rootPath       base文件上层resource目录
     * @return
     */
    @SuppressWarnings("unchecked")
    public static Object getObjectFromBase(ClassLoader classLoader, String ObjectBaseName,
                                           String desc, String rootPath) {
        if (StringUtils.isBlank(rootPath)) {
            throw new TestifyException("csvPath为空，构建" + ObjectBaseName + "失败");
        }
        Object obj = null;
        try {

            String csvPath = rootPath + TestifyPathConstants.OBJECT_DATA_PATH + ObjectBaseName + "/"
                    + ObjectBaseName + ".csv";
            ObjectProcessor processor = new ObjectProcessor(classLoader, csvPath, desc);

            //VirtualMap的情况单独处理，加载所有key－value
            if (processor.getClassName().equals("com.github.loadup.components.test.model.VirtualMap")) {

                Map<Object, Object> map = new HashMap<Object, Object>();
                List<String> descList = loadDesc(csvPath);

                for (String descTemp : descList) {
                    ObjectProcessor processorTemp = new ObjectProcessor(classLoader, csvPath,
                            descTemp);
                    Map<Object, Object> descMap = (Map<Object, Object>) processorTemp.genObject();
                    map.putAll(descMap);
                }

                obj = map;
            } else {
                obj = processor.genObject();
            }

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return obj;
    }

    /**
     * 从base加载表对象
     *
     * @param dataBaseName base文件名
     * @param desc         base里面的desc名
     * @param rootPath
     * @param encode       编码方式
     * @return base文件上层resource目录
     */
    public static VirtualTable getVirtualTableFromBase(String dataBaseName, String desc,
                                                       String rootPath, String encode) {
        VirtualTable virtualTable = new VirtualTable(dataBaseName, dataBaseName);
        String targetCSVPath = rootPath + TestifyPathConstants.DB_DATA_PATH + dataBaseName + ".csv";

        //List tableList = CSVHelper.readFromCsv(targetCSVPath);
        List<?> tableList = CSVHelper.readFromCsv(new File(targetCSVPath), encode);
        if (tableList == null || tableList.size() == 0) {
            return null;
        }
        String[] labels = (String[]) tableList.get(0);
        int colNameCol = 0, commentCol = 0, typeCol = 0, ruleCol = 0, flagCol = 0, indexCol = -1;
        for (int i = 0; i < labels.length; i++) {
            String label = labels[i].toLowerCase().trim();
            if (StringUtils.equalsIgnoreCase(label, desc)) {
                indexCol = i;
            } else {
                CSVColEnum labelEnum = CSVColEnum.getByCode(label);
                if (labelEnum != null) {
                    switch (CSVColEnum.getByCode(label)) {
                        case COLUMN:
                            colNameCol = i;

                            break;
                        case COMMENT:
                            commentCol = i;

                            break;
                        case FLAG:
                            flagCol = i;

                            break;
                        case TYPE:
                            typeCol = i;

                            break;

                        case RULE:
                            ruleCol = i;

                            break;
                        default:
                            throw new TestifyException("csv文件格式有误");
                    }
                }
            }
        }
        Map<String, String> flags = new LinkedHashMap<String, String>();
        Map<String, Object> tableRowData = new LinkedHashMap<String, Object>();
        for (int i = 1; i < tableList.size(); i++) {
            String[] row = (String[]) tableList.get(i);
            String columnName = row[colNameCol].trim();
            String flagCode = row[flagCol].trim();
            if (indexCol == -1) {
                tableRowData.put(columnName, "");
            } else {
                String value = row[indexCol].trim();
                tableRowData.put(columnName, value);
            }
            flags.put(columnName, flagCode);
        }

        virtualTable.addRow(tableRowData);
        virtualTable.setFlags(flags);
        return virtualTable;

    }

    /**
     * /**
     *
     * @param prepareData 入参为默认初始化模板类型
     * @return 返回一组PrepareData
     */
    public static Map<String, PrepareData> generateAllPrepareData(GenerateCondition condition,
                                                                  PrepareData prepareData,
                                                                  ClassLoader clsLoader) {

        // TODO: 分为几步完成
        // 1. 从数据库加载填充默认模板PrepareData对象
        // loadPrepareDataModel(prepareData);

        //获取当前系统的classloader
        Thread.currentThread().setContextClassLoader(clsLoader);

        // 2. 根据DB中的用例规则生成全量数据
        AssembleManager assembleManager = new AssembleManager();
        Map<String, PrepareData> tmp = new HashMap<String, PrepareData>();
        tmp = assembleManager.assemblyAllData(condition, prepareData);

        // 3. 根据关联关系修正结果
        //modifyParedata(tmp);

        return tmp;
    }

    /**
     * 组装preparedata
     *
     * @param prepareData
     */
    public static PrepareData generatePrepareData(String system, PrepareData prepareData,
                                                  ClassLoader clsLoader) {
        // TODO: 分为几步完成
        // 1. 加载模板数据，设定随机生成值标示 TODO://

        //获取当前系统的classloader
        Thread.currentThread().setContextClassLoader(clsLoader);

        // 2. 随机生成数据修正
        AssembleManager assembleManager = new AssembleManager();
        assembleManager.assembleSingleData(system, prepareData);

        // 3. 根据关联关系修正结果 TODO://

        return prepareData;
    }

    /**
     * 根据common-facade层的api package 生成api中每一个服务的入参、出参对象的数据模版
     *
     * @param csvModelRootPath     测试bundle下resources/model/objModel/ 绝对目录
     * @param commonFacadeRootPath common－facade层的绝对路径
     */
    public static Result generateServicesObjModel(String csvModelRootPath,
                                                  String commonFacadeRootPath, String apiPackage) {
        Result result = new Result();

        try {
            // 1.获取包下的 .java文件
            String targetApiPath = commonFacadeRootPath + "/src/main/java/"
                    + apiPackage.replace(".", "/");
            File pakagefile = new File(targetApiPath);

            File[] files = pakagefile.listFiles();
            String comment = "";
            for (File file : files) {
                if (null == file || !file.exists() || file.isDirectory()) {
                    continue;
                }
                String className = StringUtils.substringBefore(file.getName(), ".");
                if (StringUtils.isBlank(className)) {
                    continue;
                }
                //构造类加载器
                try {

                    String genModelMsg = ReflectUtil.genModelForCls(csvModelRootPath, ReflectUtil
                                    .getClassForName(apiPackage + "." + className).getClass().getClassLoader(),
                            apiPackage + "." + className);
                    comment = comment + genModelMsg;
                    log.info("服务" + className + "生成对象模版成功!");
                } catch (Exception e) {
                    String errorMsg = "服务" + className + "生成对象模版失败！错误原因： " + e + "\n";
                    log.info("服务" + className + "生成对象模版失败！");
                    comment = comment + errorMsg;
                    continue;
                }
            }
            result.setComment(comment);
            result.setSuccess(true);
        } catch (Exception e) {
            result.setComment("生成模版发生未知异常！" + e);
            result.setSuccess(false);
        }
        return result;
    }

    /**
     * 根据common-facade层的api package 生成api中每一个服务的入参、出参对象的数据模版
     *
     * @param classloader
     * @param csvModelRootPath     测试bundle下resources/model/objModel/ 绝对目录
     * @param commonFacadeRootPath common－facade层的绝对路径
     */
    public static Result generateServicesObjModel(ClassLoader classloader, String csvModelRootPath,
                                                  String commonFacadeRootPath, String apiPackage) {
        Result result = new Result();

        try {

            // 1.获取包下的 .java文件
            String targetApiPath = commonFacadeRootPath + "/src/main/java/"
                    + apiPackage.replace(".", "/");
            File pakagefile = new File(targetApiPath);

            File[] files = pakagefile.listFiles();
            String comment = "";
            for (File file : files) {
                if (null == file || !file.exists() || file.isDirectory()) {
                    continue;
                }
                String className = StringUtils.substringBefore(file.getName(), ".");
                if (StringUtils.isBlank(className)) {
                    continue;
                }
                try {
                    String genModelMsg = ReflectUtil.genModelForCls(csvModelRootPath, classloader,
                            apiPackage + "." + className);
                    comment = comment + genModelMsg;
                    log.info("服务" + className + "生成对象模版完毕！");
                } catch (Throwable e) {
                    String errorMsg = "服务" + className + "生成对象模版失败！错误原因： " + e + "\n";
                    log.info(errorMsg);
                    comment = comment + errorMsg;
                    continue;
                }
            }
            result.setComment(comment);
            result.setSuccess(true);
        } catch (Exception e) {
            result.setComment("生成模版发生未知异常！" + e);
            result.setSuccess(false);
        }
        return result;
    }

    public static Result generateServiceObjModel(String csvModelRootPath, ClassLoader classloader,
                                                 String classFullName) {

        Result result = new Result();
        try {
            // 根据java文件获取入参、出参对象，并对复杂对象生成模版
            String genModelMsg = ReflectUtil.genModelForCls(csvModelRootPath, classloader,
                    classFullName);
            result.setComment(genModelMsg);
            result.setSuccess(true);
        } catch (Exception e) {
            result.setComment("生成模版发生未知异常！" + e);
            result.setSuccess(false);
        }
        return result;
    }

    /**
     * 按照类全名生成数据模版，用于应用内的类
     *
     * @param fullClassName
     */
    public static void generateSingleObjModel(String csvModelRootPath, String fullClassName) {
        ReflectUtil.genModelForCls(csvModelRootPath, fullClassName);
    }

    /**
     * 按照类加载器、类全名生成数据模版，用于应用外、依赖jar中的类
     *
     * @param csvModelRootPath
     * @param clsLoader
     * @param fullClassName
     */
    public static Result generateSingleObjModel(String csvModelRootPath, ClassLoader clsLoader,
                                                String fullClassName) {
        Result result = new Result();
        try {
            result.setSuccess(true);
            String resMsg = ReflectUtil.genModelForCls(csvModelRootPath, clsLoader, fullClassName);
            result.setComment(resMsg);
        } catch (Exception e) {
            result.setComment("生成模版发生未知异常！" + e);
            result.setSuccess(false);
        }
        return result;
    }

    /**
     * 按照类加载器、类全名生成数据模版，用于应用外、依赖jar中的类
     *
     * @param csvModelRootPath
     * @param clsLoader
     * @param fullClassName
     * @param isResultOnly
     */
    public static Result generateSingleObjModel(String csvModelRootPath, ClassLoader clsLoader,
                                                String fullClassName, String methodName,
                                                boolean isResultOnly) {
        Result result = new Result();
        try {
            result.setSuccess(true);
            String resMsg = ReflectUtil.genModelForSpeciMethod(csvModelRootPath, clsLoader,
                    fullClassName, methodName, isResultOnly);
            result.setComment(resMsg);
        } catch (Exception e) {
            result.setComment("生成模版发生未知异常！" + e);
            result.setSuccess(false);
        }
        return result;
    }

    /**
     * 根据对象生成模版
     *
     * @param actual
     * @param csvModelRootPath
     * @return
     */
    public static Result genObjCSVFileWithData(Object actual, String csvModelRootPath) {
        Result result = new Result();
        result.setSuccess(true);

        File mockModel = FileUtil.getTestResourceFileByRootPath(StringUtils.substringBeforeLast(
                csvModelRootPath, "/"));
        if (!mockModel.exists()) {
            mockModel.mkdir();
        }

        try {
            CSVHelper.genObjCSVFileWithData(actual, csvModelRootPath);

            //String resMsg = ReflectUtil.genModelForCls(csvModelRootPath, clsLoader, fullClassName);

        } catch (Throwable e) {
            result.setSuccess(false);
            result.setComment("根据对象生成模版CSV文件失败！异常信息：" + e + "\n");
        }

        return result;
    }

    /**
     * 生成单个表模版
     *
     * @param csvModelRootPath testbundle的resource目录
     * @param conn
     * @param table
     * @param dbType
     * @return
     */
    public static boolean genDBCSVFile(String csvModelRootPath, Connection conn, String table,
                                       String dataSql, String dbType, String encode) {
        try {
            DbTableModelUtil.genDBCSVFile(csvModelRootPath, conn, table, dataSql, dbType, encode);
            log.info(table + "生成数据模版成功！");
            return true;
        } catch (Throwable e) {
            log.info(table + "生成数据模版失败！");
            return false;
        }
    }

    /**
     * 从DO生成单个表模版
     *
     * @param csvModelRootPath testbundle的resource目录
     * @param clsLoader
     * @param fullClassName
     * @param encode
     * @return
     */
    public static boolean genDOCSVFile(String csvModelRootPath, ClassLoader clsLoader,
                                       String fullClassName, String encode) {
        try {
            DbTableModelUtil.genDOCSVFile(csvModelRootPath, clsLoader,
                    fullClassName, encode);
            log.info(fullClassName + "生成数据模版成功！");
            return true;
        } catch (Throwable e) {
            log.info(fullClassName + "生成数据模版失败！");
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public static void refreshYaml(List<File> yamlFiles, ClassLoader clsLoader, String encode) {
        Yaml yaml = new Yaml();
        if (!yamlFiles.isEmpty()) {
            projectPath = getBasePath(yamlFiles.get(0));
        }
        for (File yamlFile : yamlFiles) {

            if (yamlFile.exists()) {
                try {
                    //String str = FileUtils.readFileToString(yamlFile, encode);
                    Map<String, PrepareData> prepareDataMap = loadFromYaml(yamlFile, clsLoader,
                            encode);

                    for (PrepareData prepareData : prepareDataMap.values()) {
                        refreshDataSet(prepareData.getDepDataSet());
                        refreshDataSet(prepareData.getExpectDataSet());
                    }
                    storeToYaml(prepareDataMap, yamlFile, encode);

                } catch (Exception e) {
                    throw new YamlFileException("读取yaml的时候抛出异常，yaml：" + yamlFile.getAbsolutePath(),
                            e);
                } finally {

                }
            }
        }

    }

    private static String getBasePath(File file) {
        String filePath = file.getAbsolutePath();
        String separator = File.separator;
        int index = filePath.indexOf("src" + separator + "test" + separator + "java" + separator
                + "com" + separator + "");
        return filePath.substring(0, index);
    }

    private static void refreshDataSet(VirtualDataSet dataSet) throws Exception {
        for (VirtualTable table : dataSet.getVirtualTables()) {
            refreshVirtualTable(table);
        }
    }

    private static void refreshVirtualTable(VirtualTable table) throws Exception {
        List<String> actualFields = readActualModel(table.getTableBaseDesc());
        Map<String, String> actualFlags = new HashMap<String, String>();
        int lines = table.getTableData().size();
        List<Map<String, Object>> actualData = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < lines; i++) {
            actualData.add(new HashMap<String, Object>());
        }
        for (String field : actualFields) {

            String flag = "Y";
            if (table.getFlags() != null) {
                //兼容属性名大小写
                String flagL = table.getFlags().get(field.toLowerCase());
                String flagU = table.getFlags().get(field.toUpperCase());

                flag = StringUtils.isBlank(flagL) ? flagU : flagL;

            }

            actualFlags.put(field, flag);

            //将原yaml的值还原
            for (int i = 0; i < lines; i++) {

                //兼容属性名大小写
                Object valueL = table.getRow(i).get(field.toLowerCase());
                Object valueU = table.getRow(i).get(field.toUpperCase());
                Object value = valueL == null ? valueU : valueL;

                actualData.get(i).put(field, value);
            }
        }
        table.setFlags(actualFlags);
        table.setTableData(actualData);
    }

    private static List<String> readActualModel(String modelName) throws Exception {
        List<String> fields = new ArrayList<String>();
        String csvModelRootPath = projectPath + "src/test/resources/model/dbModel/";
        String modelPath = csvModelRootPath + modelName + ".csv";
        File file = new File(modelPath);
        FileReader fReader;
        fReader = new FileReader(file);
        CSVReader csvReader = new CSVReader(fReader);
        csvReader.readNext();
        List<String[]> modelFields = csvReader.readAll();
        for (String[] fieldInfo : modelFields) {
            fields.add(fieldInfo[0]);
        }
        csvReader.close();

        return fields;
    }

    public static String runCMD(String cmd) throws Exception {
        Process targetProcess = Runtime.getRuntime().exec(cmd);

        InputStreamReader isr = new InputStreamReader(targetProcess.getInputStream());
        BufferedReader brd = new BufferedReader(isr);

        StringBuilder sb = new StringBuilder();
        for (String line = brd.readLine(); line != null; line = brd.readLine()) {
            sb.append(line);
        }

        return sb.toString();
    }

    public static void reportUsageStatus(String appName, String methodName, TestifyActionEnum action,
                                         String caseId, String result) {
        try {

        } catch (Exception e) {
            log.error("reportUsageStatus失败", e);
        }
    }

    /**
     * 仅限在其他应用中作为依赖包调用时获取当前包的版本号
     *
     * @return
     */
    public static String getCurVer() {
        String prefix = "util-";
        String suffix = ".jar";
        return "1.0.0";
    }

    public static String getGroupId() throws Exception {
        String rootPath = System.getProperty("user.dir");
        MavenXpp3Reader reader = new MavenXpp3Reader();
        String myPom = rootPath + File.separator + "pom.xml";
        Model model = reader.read(new FileReader(myPom));
        String groupId = model.getGroupId() != null ? model.getGroupId() : model.getParent().getGroupId();
        return groupId;
    }

    public static boolean isCI() {
        // ci环境为linux系统且不属于cloudIDE服务器
        return System.getProperty("os.name").contains("Linux") && null == System.getenv("CLOUDIDE_ENV");
    }

    /**
     * 重置测试脚本文件内的AutoFill注解overwrite值为false
     *
     * @param testFolder
     * @throws Exception
     */
    public static void resetOverwriteVal(File testFolder) throws Exception {
        for (File f : testFolder.listFiles()) {
            // 定位到测试脚本文件
            if (f.getName().endsWith("ActsTest.java")) {
                StringBuffer sb = new StringBuffer();
                BufferedReader br = new BufferedReader(new FileReader(f));
                // 遍历行
                for (String line; (line = br.readLine()) != null; sb.append(System.getProperty("line.separator"))) {
                    if (line.trim().startsWith("@AutoFill")) {
                        // 定位到需要替换的行，替换overwrite的值为false
                        sb.append(line.substring(0, line.indexOf("overwrite")) + StringUtils.replace(line.substring(line.indexOf("overwrite")), "true", "false", 1));
                    } else {
                        sb.append(line);
                    }
                }
                // 删除多余的换行
                sb.deleteCharAt(sb.length() - 1);
                // 回写至文件
                FileUtils.writeStringToFile(f, sb.toString());
            }
        }
    }

    /**
     * 特殊类型的序列化逻辑
     *
     * @author tantian.wc
     *
     */
    public static class MyRepresenter extends Representer {
        public MyRepresenter() {
            super(new DumperOptions());
        }

        public MyRepresenter(DumperOptions options) {
            super(options);
        }

        @Override
        protected NodeTuple representJavaBeanProperty(Object javaBean, Property property,
                                                      Object propertyValue, Tag customTag) {
            if (property.getType().equals(Currency.class)) {

                //特殊处理
                Node node = null;
                if (StringUtils.isBlank(String.valueOf(propertyValue))
                        || String.valueOf(propertyValue).equals("null")) {
                    node = representScalar(Tag.STR, "null");
                } else {
                    node = representScalar(Tag.STR, ((Currency) propertyValue).getCurrencyCode());
                }
                return new NodeTuple(representScalar(Tag.STR, property.getName()), node);
            } else if (property.getType().equals(StackTraceElement[].class)) {
                //Return null to skip theproperty
                return null;
            } else {
                super.getPropertyUtils().setSkipMissingProperties(true);
                return super
                        .representJavaBeanProperty(javaBean, property, propertyValue, customTag);
            }
        }
    }

    /**
     * 特殊类型的反序列化逻辑
     *
     * @author tantian.wc
     */
    public static class SelectiveConstructor extends Constructor {
        private static LoaderOptions loaderOptions = new LoaderOptions();

        static {
            loaderOptions.setAllowRecursiveKeys(true);
            loaderOptions.setTagInspector(tag -> true);
        }

        private ClassLoader loader = CustomClassLoaderConstructor.class.getClassLoader();

        public SelectiveConstructor() {
            super(loaderOptions);
            // define a custom way to create a mapping node
            yamlClassConstructors.put(NodeId.scalar, new MyScalarConstruct());
            yamlClassConstructors.put(NodeId.mapping, new MyMappingContruct());
        }

        public SelectiveConstructor(ClassLoader cLoader) {
            this(Object.class, cLoader);
            yamlClassConstructors.put(NodeId.scalar, new MyScalarConstruct());
            yamlClassConstructors.put(NodeId.mapping, new MyMappingContruct());

        }

        public SelectiveConstructor(Class<? extends Object> theRoot, ClassLoader theLoader) {
            super(theRoot, loaderOptions);
            if (theLoader == null) {
                throw new TestifyException("Loader must be provided");
            }
            this.loader = theLoader;
        }

        @Override
        protected Class<?> getClassForName(String name) throws ClassNotFoundException {
            return Class.forName(name, false, loader);
        }

        class MyScalarConstruct extends ConstructScalar {
            @Override
            public Object construct(Node nnode) {
                Class<?> type = nnode.getType();
                if (type.equals(Currency.class)) {
                    if (StringUtils.isBlank(((ScalarNode) nnode).getValue())) {
                        return null;
                    }
                    return Currency.getInstance(((ScalarNode) nnode).getValue());
                } else if (type.equals(BigDecimal.class)) {
                    if (StringUtils.isBlank(((ScalarNode) nnode).getValue())) {
                        return null;
                    }
                    return new BigDecimal(((ScalarNode) nnode).getValue());
                } else {
                    return super.construct(nnode);
                }
            }
        }

        class MyMappingContruct extends ConstructMapping {
            @Override
            public Object construct(Node nnode) {

                Object obj = super.construct(nnode);

                if (nnode.getType().equals(JSONObject.class)) {
                    return JSON.toJSON(obj);
                }

                return obj;
            }
        }

    }


}
