/**
 * Alipay.com Inc. Copyright (c) 2004-2021 All Rights Reserved.
 */
package com.alipay.test.acts.util;

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

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alipay.test.acts.constant.ActsYamlConstants;
import com.alipay.test.acts.db.enums.CSVColEnum;
import com.alipay.test.acts.enums.ActsActionEnum;
import com.alipay.test.acts.enums.YamlFieldEnum;
import com.alipay.test.acts.exception.ActsException;
import com.alipay.test.acts.util.comparison.TextRenderUtil;
import com.alipay.yaml.Yaml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.ResourceUtils;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.*;
import java.lang.reflect.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.*;

/**
 * @author pumo
 * @version $Id: ActsCommonUtil.java, v 0.1 2021年01月06日 1:54 PM pumo Exp $
 */
public class ActsCommonUtil {

    /**
     * logger
     */
    private static final Logger logger = LoggerFactory
            .getLogger(ActsCommonUtil.class);

    private static final String APP_NAME_LOW = "appname";
    private static final String APP_NAME_UP = "AppName";
    private static final String MODULE_NAME_LOW = "modulename";
    private static final String MODULE_NAME_UP = "ModuleName";
    private static final String METHOD_NAME_LOW = "methodname";
    private static final String METHOD_NAME_UP = "MethodName";
    private static final String ARGS_TYPE = "argsType";
    private static final String SERVICE_NAME_LOW = "serviceName";
    private static final String SERVICE_NAME_ALL_LOW = "servicename";
    private static final String SERVICE_NAME_UP = "ServiceName";
    private static final String SERVICE_REF = "serviceref";
    private static final String DEFAULT_PATH = "defaultPath";
    private static final String TEST_BASE_NAME = "TestBaseName";
    private static final String TEST_BASE_TEMPLATE = "${defaultPath}.test.base.${AppName}${ModuleName}TestBase";
    private static final String TEST_BASE_CLASS_TEMPLATE = "${AppName}${ModuleName}TestBase.java";
    private static final String PACKAGE_NAME = "packagename";
    private static final String PACKAGE_TEMPLATE = "${defaultPath}.test.${servicename}.${methodname}";
    private static final String BASE_PACKAGE_TEMPLATE = "${defaultPath}.test.base";
    private static final String SCRIPT_NAME_TEMPLATE = "${MethodName}Test.java";
    // 初始化时需要在文件内进行续写而非覆盖的文件名集合
    private static final String APPEND_FILE_COLLECTION = "sofa-log4j.properties";

    //public static void main(String[] args) throws Exception {
    //    initActs("pmtest");
    //}

    /**
     * 初始化ACTS框架必要文件
     * <p>
     * ！！！注意：若选择覆盖之前的初始化文件内容，请提前做好备份
     *
     * @param appName   应用名称
     * @param overwrite 是否覆写已有文件
     * @throws Exception
     */
    public static void initActs(String appName, String moduleName, boolean overwrite) throws Exception {
        // 需要在项目的test bundle下执行，获取的目录参考：/工程目录/应用名/app/test
        String basePath = System.getProperty("user.dir");
        // 获取应用默认编码
        String encoding = Charset.defaultCharset().displayName();
        // 加载需要写入的资源文件

        // Velocity上下文
        Map<String, Object> velocityContext = new HashMap<String, Object>();
        velocityContext.put(APP_NAME_LOW, appName);
        velocityContext.put(MODULE_NAME_LOW, moduleName);
        velocityContext.put(APP_NAME_UP, appName.substring(0, 1).toUpperCase() + appName.substring(1));
        velocityContext.put(MODULE_NAME_UP, moduleName.substring(0, 1).toUpperCase() + moduleName.substring(1));
        String defaultPath = BaseDataUtil.getGroupId() + "." + moduleName;
        velocityContext.put(DEFAULT_PATH, defaultPath);
        velocityContext.put(TEST_BASE_NAME, VelocityUtil.evaluateString(velocityContext, TEST_BASE_TEMPLATE));

        String testScript;
        try (InputStream inputStream = ResourceUtils.getURL("classpath:" + "templates/AppTestBase.vm").openStream()) {
            byte[] bytes = FileCopyUtils.copyToByteArray(inputStream);
            testScript = new String(bytes, StandardCharsets.UTF_8);
        }


        String pkgName = VelocityUtil.evaluateString(velocityContext, BASE_PACKAGE_TEMPLATE).toLowerCase();
        velocityContext.put(PACKAGE_NAME, pkgName);

        // merge customized context
        // methodFile
        String scriptName = VelocityUtil.evaluateString(velocityContext, TEST_BASE_CLASS_TEMPLATE);
        String methodFile = basePath + "/src/test/java/" + pkgName.replace(".", "/") + "/" + scriptName;

        // 判断是否需要跳过已存在的文件
        if (new File(methodFile).exists()) {
            System.out.println("[Skipped]Test script already exists at " + pkgName + "." + scriptName + " (" + scriptName + ":20)");
        }

        // create folder if not exist
        File folder = new File(methodFile).getParentFile();
        if (!folder.exists()) {
            folder.mkdirs();
        }

        String content = VelocityUtil.evaluateString(velocityContext, testScript);
        FileUtils.writeStringToFile(methodFile, content, encoding, false);

    }

    /**
     * 解析资源文件的路径
     *
     * @param resource
     * @param initFolder
     * @return
     */
    private static String parseFilePath(Resource resource, String initFolder) {
        String desc = resource.getDescription();

        // 去除最后一个]字符，不信可debug看下desc具体结构file[path]
        return desc.substring(desc.indexOf(initFolder) + initFolder.length(), desc.length() - 1);
    }

    /**
     * 将文本文件中的内容读入到buffer中
     *
     * @param buffer buffer
     * @param is     输入流
     * @throws IOException 异常
     */
    private static void readToBuffer(StringBuffer buffer, InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String line = reader.readLine(); // 用来保存每行读取的内容，先读取第一行

        while (line != null) { // 如果 line 为空说明读完了
            buffer.append(line); // 将读到的内容添加到 buffer 中
            buffer.append("\n"); // 添加换行符
            line = reader.readLine(); // 读取下一行
        }

        reader.close();
        is.close();
    }

    private static void checkVersion() {
    }

    public static void genTestScript(Class interfaceClass, String appName, String moduleName) throws Exception {
        genTestScript(interfaceClass, null, appName,moduleName);
    }

    public static void genTestScript(Class interfaceClass, String mName, String appName,String moduleName) throws Exception {
        genTestScript(interfaceClass, mName, appName,moduleName, null, null, null);
    }

    /**
     * @param interfaceClass 接口类
     * @param mName          指定要生成的方法名，当填入null时会生成接口下面所有方法
     * @param appName        应用名
     * @param path           自定义路径，当填入null时会默认使用应用的groupId作为拼接路径
     * @param template       自定义模板，当填入null时会使用通用模板
     * @param context        自定义的模板上下文
     * @throws Exception
     */
    public static void genTestScript(Class interfaceClass, String mName, String appName,String moduleName,
                                     String path, String template, Map<String, Object> context) throws Exception {

        /**** 2. 初始化所需变量 ****/
        // 需要在项目的test bundle下执行，获取的目录参考：/工程目录/应用名/app/test
        String basePath = System.getProperty("user.dir");

        // 获取应用默认编码
        String encoding = Charset.defaultCharset().displayName();

        // service info
        String serviceRef = interfaceClass.getName();
        String serviceName = serviceRef.substring(serviceRef.lastIndexOf(".") + 1);

        // Velocity上下文
        Map<String, Object> velocityContext = new HashMap<String, Object>();
        velocityContext.put(APP_NAME_LOW, appName);
        velocityContext.put(MODULE_NAME_LOW, moduleName);
        velocityContext.put(APP_NAME_UP, appName.substring(0, 1).toUpperCase() + appName.substring(1));
        velocityContext.put(MODULE_NAME_UP, moduleName.substring(0, 1).toUpperCase() + moduleName.substring(1));

        velocityContext.put(SERVICE_NAME_LOW, serviceName.substring(0, 1).toLowerCase() + serviceName.substring(1));
        velocityContext.put(SERVICE_NAME_ALL_LOW, serviceName.toLowerCase());
        velocityContext.put(SERVICE_NAME_UP, serviceName);
        velocityContext.put(SERVICE_REF, serviceRef);

        String defaultPath = BaseDataUtil.getGroupId() + "." + moduleName;
        if (!serviceRef.contains(defaultPath)) {
            defaultPath = serviceRef.substring(0, serviceRef.indexOf(".", serviceRef.indexOf(appName)));
        }
        if (path != null) {
            defaultPath = path;
        }
        velocityContext.put(DEFAULT_PATH, defaultPath);
        velocityContext.put(TEST_BASE_NAME, VelocityUtil.evaluateString(velocityContext, TEST_BASE_TEMPLATE));

        // 获取测试脚本内容
        // 原始代码：
        // String testScript = org.apache.commons.io.FileUtils.readFileToString(new File("MethodNameActsTest.vm")); //(String) HttpUtil.doGet(SCRIPT_URL, "UTF-8");
        // 修改为通过classpath加载：
        String testScript;
        try (InputStream inputStream = ResourceUtils.getURL("classpath:" + "templates/MethodNameTest.vm").openStream()) {
            byte[] bytes = FileCopyUtils.copyToByteArray(inputStream);
            testScript = new String(bytes, StandardCharsets.UTF_8);
        }
        if (template != null) {
            testScript = template;
        }


        /**** 3. 提取所有被测方法，准备依次生成测试脚本 ****/
        List<Method> methodList = new ArrayList<Method>();
        methodList.addAll(Arrays.asList(interfaceClass.getDeclaredMethods()));

        try {
            // 指定方法名时会遍历父类方法
            if (null != mName) {
                Class tmpCls = interfaceClass.getSuperclass();
                while (null != tmpCls) {
                    methodList.addAll(Arrays.asList(tmpCls.getDeclaredMethods()));
                    tmpCls = tmpCls.getSuperclass();
                }
            }
        } catch (Exception e) {
            System.out.println("[Exception]遍历父类方法时报错，先跳过不影响正常执行\n" + e.getMessage());
        }


        if (methodList.size() < 1) {
            throw new ActsException("方法数为0，无法生成对应用例！");
        }

        int genCount = 0;
        for (Method method : methodList) {
            if (!Modifier.isPublic(method.getModifiers()) && !Modifier.isProtected(method.getModifiers())) {
                // 仅支持public和protected级别的方法，其余方法跳过先
                continue;
            }
            // filter methodName
            String methodName = method.getName();
            if (null != mName && !methodName.equals(mName)) {
                continue;
            }

            // method argsType
            ArrayList args = new ArrayList<String>();
            for (Type t : method.getGenericParameterTypes()) {
                args.add(t.getTypeName());
            }
            String argsType = String.join(",", args);

            // update velocity context variables
            velocityContext.put(METHOD_NAME_LOW, methodName);
            velocityContext.put(METHOD_NAME_UP, methodName.substring(0, 1).toUpperCase() + methodName.substring(1));
            velocityContext.put(ARGS_TYPE, argsType);
            String pkgName = VelocityUtil.evaluateString(velocityContext, PACKAGE_TEMPLATE).toLowerCase();
            velocityContext.put(PACKAGE_NAME, pkgName);

            // merge customized context
            if (context != null) {
                velocityContext.putAll(context);
            }

            // methodFile
            String scriptName = VelocityUtil.evaluateString(velocityContext, SCRIPT_NAME_TEMPLATE);
            String methodFile = basePath + "/src/test/java/" + pkgName.replace(".", "/") + "/" + scriptName;

            // 判断是否需要跳过已存在的文件
            if (new File(methodFile).exists()) {
                System.out.println("[Skipped]Test script already exists at " + pkgName + "." + scriptName + " (" + scriptName + ":20)");
                continue;
            }

            // create folder if not exist
            File folder = new File(methodFile).getParentFile();
            if (!folder.exists()) {
                folder.mkdirs();
            }

            String content = VelocityUtil.evaluateString(velocityContext, testScript);
            FileUtils.writeStringToFile(methodFile, content, encoding, false);

            // 生成用例文件夹内容
            String caseName = "case01";
            String yamlStr = genScaffoldYaml(method);

            BaseDataUtil.genCaseFolderStucture(folder, caseName, yamlStr, null, null);

            // 埋点：统计使用情况
            BaseDataUtil.reportUsageStatus(appName, methodName, ActsActionEnum.GEN_TEST_SCRIPT, caseName, null);

            genCount++;

            System.out.println("[Success]Test script has been generated at " + pkgName + "." + scriptName + " (" + scriptName + ":20)");
        }

        if (genCount == 0) {
            System.out.println("本次未生成任何方法测试脚本，请确认接口类方法定义或指定的方法名是否正确！");
        }
    }

    private static String genScaffoldYaml(Method testedMethod) throws Exception {
        List<Object> inputs = new ArrayList<Object>();
        for (Type type : testedMethod.getGenericParameterTypes()) {
            // 实例化对象
            try {
                // fieldName用于报错信息内定位具体字段
                Object arg = autoGenObj(type, testedMethod.getName() + "#Argument" + inputs.size());
                inputs.add(arg);

            } catch (Exception e) {
                System.out.println("【ERROR】自动生成" + type.getTypeName() + "对象失败, => " + e.toString()
                        + ", 需要手动通过 ActsCommonUtil.genObjToYamlStr 方法构建入参对象并塞入YAML对应的入参位置");
                inputs.add(null);
            } catch (Error err) {
                System.out.println("【ERROR】自动生成" + type.getTypeName() + "对象失败, => " + err.toString()
                        + ", 需要手动通过 ActsCommonUtil.genObjToYamlStr 方法构建入参对象并塞入YAML对应的入参位置");
                inputs.add(null);
            }
        }

        String initYamlStr = YamlFieldEnum.ARGS.getCode() + genArgumentsToYamlStr(inputs);
        initYamlStr += ActsYamlConstants.YAML_SEPARATOR + YamlFieldEnum.FLAGS.getCode() + YamlFieldEnum.FLAGS.getInitVal();
        initYamlStr += ActsYamlConstants.YAML_SEPARATOR + YamlFieldEnum.RESULT.getCode() + YamlFieldEnum.RESULT.getInitVal();
        initYamlStr += ActsYamlConstants.YAML_SEPARATOR + YamlFieldEnum.EVENTS.getCode() + YamlFieldEnum.EVENTS.getInitVal();
        initYamlStr += ActsYamlConstants.YAML_SEPARATOR + YamlFieldEnum.PARAMS.getCode() + YamlFieldEnum.PARAMS.getInitVal();
        initYamlStr += ActsYamlConstants.YAML_SEPARATOR + YamlFieldEnum.MOCKS.getCode() + YamlFieldEnum.MOCKS.getInitVal();
        //initYamlStr += ActsYamlConstants.YAML_SEPARATOR + YamlFieldEnum.COMPOS.getCode() + YamlFieldEnum.COMPOS.getInitVal();
        return initYamlStr;
    }

    // 最后需要加到工具类里
    private static Object autoGenObj(Type type, String fieldName) throws Exception {
        Type tmpType = type;
        Object obj = null;

        // 参数化泛型处理
//        if (tmpType instanceof ParameterizedTypeImpl) {
//            Class rawType = ((ParameterizedTypeImpl) tmpType).getRawType();
//            Type[] args = ((ParameterizedTypeImpl) tmpType).getActualTypeArguments();
//
//            //System.out.println("泛型处理：" + rawType.getName());
//
//            if (rawType == List.class) {
//
//                obj = new ArrayList();
//                ((ArrayList) obj).add(autoGenObj(args[0], fieldName + "#" + args[0].getTypeName()));
//
//            } else if (rawType == Map.class) {
//                obj = new HashMap();
//                Object key = autoGenObj(args[0], fieldName + "#" + args[0].getTypeName());
//                Object val = autoGenObj(args[1], fieldName + "#" + args[1].getTypeName());
//                ((HashMap) obj).put(key, val);
//
//            } else if (rawType == Set.class) {
//                obj = new HashSet();
//                ((HashSet) obj).add(autoGenObj(args[0], fieldName + "#" + args[0].getTypeName()));
//
//            } else {
//                System.out.println("-------------unable to handle it: " + rawType.getName() + "----------------");
//            }
//        }


        // 基础数据类型处理
        if (tmpType instanceof Class && ((Class) tmpType).isPrimitive()) {
            if (tmpType == int.class || tmpType == short.class || tmpType == long.class) {
                obj = 0;
            } else if (tmpType == float.class || tmpType == double.class) {
                obj = 0.0;
            } else if (tmpType == byte.class) {
                obj = (byte) 0;
            } else if (tmpType == char.class) {
                obj = 'c';
            } else if (tmpType == boolean.class) {
                obj = true;
            }
        }

        if (tmpType instanceof Class && ((Class) tmpType).isArray()) {
            Class clz = ((Class) tmpType).getComponentType();
            obj = Array.newInstance(clz, 1);
            Array.set(obj, 0, autoGenObj(clz, clz.getTypeName()));
            return obj;

        }

        if (tmpType instanceof Class && ((Class) tmpType).isEnum()) {
            Object[] enumList = (Object[]) ((Class) tmpType).getDeclaredMethod("values").invoke(tmpType);
            if (enumList.length > 0) {
                obj = enumList[0];
            }
        }

        if (tmpType instanceof Class && null == obj) {

            // 复杂类型支持配置默认对象
            String simpleName = ((Class) type).getSimpleName();
            File yamlFile = new File("src/test/resources/defaultObjs/" + simpleName + ".yaml");
            if (yamlFile.exists() && yamlFile.isFile()) {
                Object defObj = new Yaml().load(ParamFilter.filterJson(FileUtils.readFileToString(yamlFile)));
                if (((Class) type).getName().equals(defObj.getClass().getName())) {
                    return defObj;
                }
            }

            try {
                if (tmpType == String.class) {
                    obj = "1";
                } else {
                    obj = ((Class) tmpType).newInstance();
                }

            } catch (InstantiationException e) {
                // 缺失无参构造器，无法通过newInstance进行实例化
                // 尝试其他有参构造器
                Constructor[] cons = ((Class) tmpType).getConstructors();
                if (cons.length > 0) {
                    for (Constructor con : cons) {
                        // 解析构造器的入参并进行调用
                        Object[] args = new Object[con.getGenericParameterTypes().length];
                        int i = 0;
                        for (Type t : con.getGenericParameterTypes()) {
                            args[i] = autoGenObj(t, fieldName + "#" + t.getTypeName());
                            i++;
                        }
                        try {
                            obj = con.newInstance(args);
                        } catch (Exception anyE) {

                        }

                    }
                } else {
                    // 无任何构造器，无法进行实例化
                    System.out.println("该对象【" + e.getMessage() + "】无任何构造器，无法自动进行实例化，"
                            + "请使用genObjYaml工具类方法手动生成Yaml字符串！坐标：" + fieldName);
                }
            }

            //遍历设值
            Map<String, Field> fields = CSVApisUtil.findTargetClsFields((Class) tmpType);
            if (null != fields) {
                for (String key : fields.keySet()) {
                    Field field = fields.get(key);
                    field.setAccessible(true);
                    field.set(obj, autoGenObj(field.getGenericType(), fieldName + "#" + key));
                }
            }
        }
        return obj;
    }

    public static String genFlags(Class clazz, List<String> fields) {
        Map<String, String> fieldFlags = new HashMap<String, String>();

        if (null != fields) {
            for (String field : fields) {
                fieldFlags.put(field, "N");
            }
        }

        String res = clazz.getName() + ": " + genArgumentsToYamlStr(fieldFlags);

        setClipboardString(res);

        return res;
    }

    public static String genObjToYamlStr(Object obj) {

        String yamlStr = BaseDataUtil.getStdYaml().dump(obj);

        // 自动置剪辑版
        try {
            setClipboardString(yamlStr);
        } catch (Exception e) {
            System.out.println("yaml字符串如下[可直接复制]：");
            System.out.println(yamlStr);
        } catch (Error err) {
            System.out.println("yaml字符串如下[可直接复制]：");
            System.out.println(yamlStr);
        }

        return yamlStr;
    }

    private static String genArgumentsToYamlStr(Object obj) {
        return BaseDataUtil.getStdYaml().dump(obj);
    }

    private static void setClipboardString(String text) {
        // 封装文本内容
        Transferable trans = new StringSelection(text);
        // 把文本内容设置到系统剪贴板
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(trans, null);
    }

    public static void genDBTableCols(List<String> guids) throws Exception {
        for (String guid : guids) {
            genDBTableCols(guid);
        }
    }

    /**
     * 使用iDB API生成db模板
     * 同时兼容guid和arn
     *
     * @param arn
     * @throws Exception
     */
    public static void genDBTableCols(String arn) throws Exception {
        List<String[]> dbmodel = null;
        // 当前arn命名规则以acs开头
        if (arn.startsWith("acs:")) {
            dbmodel = genDBTableColsByArn(arn);

            //再尝试guid获取表结构
            if (dbmodel == null || dbmodel.size() == 0) {
                dbmodel = genDBTableColsByGuid(arn);
            }

        } else {
            // 尝试老的guid获取
            dbmodel = genDBTableColsByGuid(arn);

            //再尝试arn获取表结构
            if (dbmodel == null || dbmodel.size() == 0) {
                dbmodel = genDBTableColsByArn(arn);
            }
        }

        // 写入文件
        if (dbmodel != null && dbmodel.size() > 1) {
            String tableName = arn.substring(arn.lastIndexOf(".") + 1);
            // write to csv
            //生成db模型默认包目录
            String folder = System.getProperty("user.dir") + "/src/test/resources/model/";
            new File(folder).mkdirs();
            String filePath = folder + tableName + ".csv";
            dbTableModelUtil.writeToCsv(new File(filePath), dbmodel, Charset.defaultCharset().displayName());
        } else {
            throw new Exception("当前guid/arn信息：" + arn + "获取不到对应表结构，GUID方式将逐步下线，请使用ARN唯一标识生成表结构数据，详情：https://yuque.antfin.com/acts/user_guide/manual#cni1D");
        }
    }

    /**
     * @description:通过arn生成csv文件
     * @author: xxy
     * @date: 2023/5/4 15:43
     * @param: [arn]
     * @return: java.util.List<java.lang.String [ ]>
     **/
    public static List<String[]> genDBTableColsByArn(String arn) throws Exception {
        //注意是不带域名的，同时uri是不带请求参数的！！！！！
        // 但是在路径参数如  /api/meta/topology/IDB_L_test   是需要当成路径一起鉴权的
        String api = "/api/openOmc/rds/v1/listColumnMetaByTableArn";
        String host = "http://oceanbasecoremeta.test.alipay.net";
//        String host = "https://oceanbasecoremeta.alipay.com";
        // timeout默认15秒
//        OmcClientParams clientParams = new OmcClientParams(host, ACCESS_ID, new String(Base64Util.decode(new String(Base64Util.decode(ACCESS_KEY)))));
//        OmcHttpClient client = new OmcHttpClient(clientParams);
//        Map<String, Object> params = new HashMap<>();
//        params.put("tableArn", arn);
        // 基础模式
        JSONObject res = new JSONObject();//client.get(api, params);
        List<String[]> dbmodel = new ArrayList<String[]>();
        if (Boolean.valueOf(String.valueOf(res.get("success")))) {
            // dbmodel headers
            String[] headers = {CSVColEnum.COLUMN.getCode(), CSVColEnum.TYPE.getCode(),
                    CSVColEnum.COMMENT.getCode(), CSVColEnum.PRIMARY.getCode(),
                    CSVColEnum.NULLABLE.getCode(), CSVColEnum.FLAG.getCode(),
                    CSVColEnum.VALUE.getCode()};
            dbmodel.add(headers);
            JSONArray cols = (JSONArray) res.get("data");

            for (int i = 0; i < cols.size(); i++) {
                JSONObject col = cols.getJSONObject(i);

                String[] dataRow = new String[headers.length];

                for (int j = 0; j < headers.length; j++) {
                    if (headers[j].equals("flag")) {
                        dataRow[j] = "Y";
                    } else if (headers[j].equals("value")) {
                        dataRow[j] = "";
                    } else if (headers[j].equals("dataType")) {
                        dataRow[j] = col.getString("columnType");
                    } else if (headers[j].equals("primaryKey")) {
                        dataRow[j] = col.getString(headers[j]).equals("1") ? "true" : "false";
                    } else {
                        dataRow[j] = col.getString(headers[j]);
                    }
                }

                dbmodel.add(dataRow);
            }
        } else {
            return null;
        }
        return dbmodel;
    }

    public static void main(String[] args) throws Exception {
        genDBTableColsByArn("acs:alipay-daily:rds::alipay:logic-table:341521.agdsccalipay.agdscc_jiebei_credit_v2");
    }

    /**
     * @description:通过guid生成csv文件
     * @author: xxy
     * @date: 2023/5/4 15:48
     * @param: [guid]
     * @return: java.util.List<java.lang.String [ ]>
     **/
    public static List<String[]> genDBTableColsByGuid(String guid) throws Exception {
        String api = "/api/meta/table/columns/" + guid;
        String host = "https://oceanbasecoremeta.alipay.com";
//        OmcClientParams clientParams = new OmcClientParams(host, ACCESS_ID, new String(Base64Util.decode(new String(Base64Util.decode(ACCESS_KEY)))));
//        OmcHttpClient client = new OmcHttpClient(clientParams);
        JSONObject res = new JSONObject();//client.get(api, null);
        List<String[]> dbmodel = new ArrayList<String[]>();
        if (Boolean.valueOf(String.valueOf(res.get("success")))) {
            // dbmodel headers
            String[] headers = {CSVColEnum.COLUMN.getCode(), CSVColEnum.TYPE.getCode(),
                    CSVColEnum.COMMENT.getCode(), CSVColEnum.PRIMARY.getCode(),
                    CSVColEnum.NULLABLE.getCode(), CSVColEnum.FLAG.getCode(),
                    CSVColEnum.VALUE.getCode()};
            dbmodel.add(headers);
            JSONArray cols = (JSONArray) res.get("root");

            for (int i = 0; i < cols.size(); i++) {
                JSONObject col = cols.getJSONObject(i);

                String[] dataRow = new String[headers.length];

                for (int j = 0; j < headers.length; j++) {
                    if (headers[j].equals("flag")) {
                        dataRow[j] = "Y";
                    } else if (headers[j].equals("value")) {
                        dataRow[j] = "";
                    } else {
                        dataRow[j] = col.getString(headers[j]);
                    }
                }

                dbmodel.add(dataRow);
            }
        } else {
            return null;
        }

        TextRenderUtil.print(TextRenderUtil.RED_BOLD, "iDB即将下线，新版ODC平台请使用ARN唯一标识生成表结构数据，详情：https://yuque.antfin.com/acts/user_guide/manual#cni1D");
        return dbmodel;
    }

    /**
     * 创建默认值文件路径及生成文件内容
     *
     * @param obj
     * @return
     * @throws Exception
     */
    public static String genDefValueYaml(Object obj) throws Exception {
        String yamlStr = BaseDataUtil.getStdYaml().dump(obj);
        String folder = System.getProperty("user.dir") + "/src/test/resources/defaultObjs/";
        String filePath = folder + obj.getClass().getSimpleName() + ".yaml";

        File defaultObjs = new File(folder);
        if (!defaultObjs.exists() || !defaultObjs.isDirectory()) {
            defaultObjs.mkdirs();
        }


        FileUtils.writeStringToFile(filePath, yamlStr, Charset.defaultCharset().displayName(), false);
        return yamlStr;
    }
}
