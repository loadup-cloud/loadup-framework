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

import com.github.loadup.components.testify.db.enums.CSVColEnum;
import com.github.loadup.components.testify.log.TestifyLogUtil;
import com.github.loadup.components.testify.object.manager.ObjectTypeManager;
import com.google.common.collect.ImmutableList;
import com.google.common.reflect.Invokable;
import com.google.common.reflect.Parameter;
import com.google.common.reflect.TypeToken;
import com.opencsv.CSVWriter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.azeckoski.reflectutils.ClassData;
import org.azeckoski.reflectutils.ClassFields;
import org.azeckoski.reflectutils.ReflectUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.*;
import java.util.*;
import java.util.Map.Entry;

/**
 * 提供给外部的类或者接口填写CSV的工具类
 *
 * @author 马洪良
 *
 */
@Slf4j
public class CSVApisUtil {
    private static final ObjectTypeManager objectTypeManager = new ObjectTypeManager();
    private static final String LIST_CONTENT_TEMPLATE = "1";                    //多个元素的情况："1;2;3"
    private static final String COMPLEX_LIST_CONTENT_TEMPLATE = "FILE@1";               //多个元素的情况："FILE@1;2"
    private static final String COMPLEX_TYPE_CONTENT_TEMPLATE = "FILE@1";
    private static final String FILE_WORDS = "FILE";
    private final static Set<String> simpleClassType = new HashSet<String>();

    static {
        simpleClassType.add("boolean");
        simpleClassType.add("java.lang.Integer");
        simpleClassType.add("java.lang.Float");
        simpleClassType.add("java.lang.Double");
        simpleClassType.add("java.lang.Long");
        simpleClassType.add("java.lang.Short");
        simpleClassType.add("java.lang.Byte");
        simpleClassType.add("java.lang.Boolean");
        simpleClassType.add("java.lang.Character");
        simpleClassType.add("java.util.Properties");
        simpleClassType.add("java.lang.String");
        simpleClassType.add("java.util.Date");
        simpleClassType.add("java.util.Currency");
        simpleClassType.add("java.math.BigDecimal");
        simpleClassType.add("java.io.Serializable");
        simpleClassType.add("java.lang.Object");
        simpleClassType.add("java.lang.Void");
        //作为测试临时打开
        //        simpleClassType.add("java.util.List");
        //        simpleClassType.add("java.util.ArrayList");
        //        simpleClassType.add("java.util.Map");
        //        simpleClassType.add("java.util.HashMap");
        //        simpleClassType.add("ARRAY");
    }

    /**
     * 对外的方法，提供解析一个Class
     *
     * @param clsName     类名
     * @param classLoade  clsLoad
     * @param genRootPath csv根目录
     * @throws ClassNotFoundException
     */
    public static Set<String> paraClassToCscFile(String clsName, ClassLoader classLoade,
                                                 String genRootPath) throws ClassNotFoundException {
        Set<String> sbfWarnError = new HashSet<String>();
        Class<?> specifyClass = classLoade.loadClass(clsName);
        if (specifyClass.isInterface()) {
            processInterface(specifyClass, genRootPath, sbfWarnError);
        } else {
            doProcess(specifyClass, null, new HashSet<String>(),
                    mkCsvFolderForCls(specifyClass, null, genRootPath), sbfWarnError);
        }

        return sbfWarnError;
    }

    /**
     * 对外的方法，提供解析一个Class指定的方法的入参、返回结果
     *
     * @param clsName     类名
     * @param classLoade  clsLoad
     * @param genRootPath csv根目录
     * @throws ClassNotFoundException
     */
    public static Set<String> paraClassSpeciMethodToCscFile(String clsName, ClassLoader classLoade,
                                                            String genRootPath, String methodName,
                                                            boolean isResultOnly)
            throws ClassNotFoundException {
        Set<String> sbfWarnError = new HashSet<String>();
        Class<?> specifyClass = classLoade.loadClass(clsName);
        processInterfaceMethod(specifyClass, genRootPath, sbfWarnError, methodName, isResultOnly);

        return sbfWarnError;

    }

    /**
     * 根据类创建CSV文件
     *
     * @param clsMain
     * @param clsType
     * @param genRootPath
     * @return 返回创建的csv文件
     */
    public static String mkCsvFolderForCls(Class<?> clsMain, Type clsType, String genRootPath) {
        if (isWrapClass(clsMain)) {
            log.warn("当前对象时基础类型的对象，建立CSV不支持");
            return null;
        }
        if (clsMain.isArray() && isWrapClass(clsMain.getComponentType())) {
            log.warn("数组的基本对象，建立CSV不支持");
            return null;
        }

        Class<?> clsAdd = clsMain.isArray() ? clsMain.getComponentType() : clsMain;

        String csvRootPath;
        String csvFolder;
        File objModel = FileUtil.getTestResourceFileByRootPath(genRootPath);
        if (!objModel.exists())
            objModel.mkdir();

        if (clsType == null) {
            csvFolder = genRootPath + clsAdd.getSimpleName();
            csvRootPath = csvFolder + "/" + clsAdd.getSimpleName() + ".csv";
        } else {
            if (clsType instanceof ParameterizedType) {
                Class<?> clsSub = getParameRawCls(clsType);
                Type typeInner = HandMutiParameType(clsType, 0);
                Class<?> typeSub = getClass(typeInner, 0);
                //带范型的情况，csv命名带范型具体名称
                csvFolder = genRootPath + clsAdd.getSimpleName() + "_" + clsSub.getSimpleName()
                        + "_" + typeSub.getSimpleName();
                csvRootPath = csvFolder + "/" + clsAdd.getSimpleName() + "_"
                        + clsSub.getSimpleName() + "_" + typeSub.getSimpleName() + ".csv";
            } else {
                Class<?> clsSub = getClass(clsType, 0);
                //带范型的情况，csv命名带范型具体名称
                csvFolder = genRootPath + clsAdd.getSimpleName() + "_" + clsSub.getSimpleName();
                csvRootPath = csvFolder + "/" + clsAdd.getSimpleName() + "_"
                        + clsSub.getSimpleName() + ".csv";
            }

        }
        File file = FileUtil.getTestResourceFileByRootPath(csvFolder);
        if (!file.exists())
            file.mkdir();

        return csvRootPath;
    }

    /**
     * 根据class生成内部的csv文而建
     *
     * @param objClass
     * @param csvPath
     * @return
     */
    private static String getCsvFileName(Class<?> objClass, String csvPath) {

        if (isWrapClass(objClass)) {
            log.warn("当前对象时基础类型的对象，建立CSV不支持");
            return null;
        }
        String[] paths = csvPath.split("/");
        ArrayUtils.reverse(paths);

        String className = objClass.getSimpleName() + ".csv";

        if (!StringUtils.equals(className, paths[0])) {
            csvPath = StringUtils.replace(csvPath, paths[0], className);
        }

        return csvPath;
    }

    public static String getGenericCsvFileName(Class<?> sueperClass, Class<?> subClass,
                                               String csvPath) {

        if (isWrapClass(sueperClass)) {
            log.warn("当前对象时基础类型的对象，建立CSV不支持");
            return null;
        }

        if (null == subClass) {
            return getCsvFileName(sueperClass, csvPath);
        }
        String[] paths = csvPath.split("/");
        ArrayUtils.reverse(paths);

        String className = sueperClass.getSimpleName() + "_" + subClass.getSimpleName() + ".csv";

        if (!StringUtils.equals(className, paths[0])) {
            csvPath = StringUtils.replace(csvPath, paths[0], className);
        }

        return csvPath;
    }

    /**
     * @param specifyClass
     * @param genRootPath
     */
    private static void processInterface(Class<?> specifyClass, String genRootPath,
                                         Set<String> sbfWarnError) {
        ReflectUtils refUtil = ReflectUtils.getInstance();
        ClassFields<?> clsFiled = refUtil.analyzeClass(specifyClass);
        ClassData<?> getPro = clsFiled.getClassData();
        List<Method> listMetchod = getPro.getMethods();
        for (Method method : listMetchod) {
            Invokable invoke = Invokable.from(method);
            ImmutableList<Parameter> mParameters = invoke.getParameters();
            //开始处理入参
            for (Parameter parameter : mParameters) {

                try {
                    Class<?> paraCls = parameter.getType().getRawType();
                    TypeToken<?> preToken = parameter.getType();
                    TypeToken<?> genericTypeToken = preToken.resolveType(preToken.getType());
                    String csvFile = null;
                    if (!(genericTypeToken.getType() instanceof ParameterizedType)) {
                        doProcess(paraCls, null, new HashSet<String>(),
                                mkCsvFolderForCls(paraCls, null, genRootPath), sbfWarnError);
                    } else {
                        if (Map.class.isAssignableFrom(paraCls)) {
                            //MAP类型处理
                            csvFile = mkCsvFolderForCls(paraCls,
                                    HandMutiParameType(genericTypeToken.getType(), 1), genRootPath);
                        } else {
                            csvFile = mkCsvFolderForCls(paraCls,
                                    HandMutiParameType(genericTypeToken.getType(), 0), genRootPath);
                        }
                        ContainerUtils.handContainer(paraCls,
                                HandMutiParameType(genericTypeToken.getType(), 0),
                                HandMutiParameType(genericTypeToken.getType(), 1), csvFile,
                                new HashSet<String>(), sbfWarnError, true);
                    }

                } catch (Throwable e) {
                    //避免一个参数失败，阻碍当前方法或者其他方法参数、结果生成模版
                    String genModelMsg = "类" + parameter.getClass().getName() + "模板生成失败:"
                            + e.getMessage() + "可以使用对象模板生成!" + "\n";
                    sbfWarnError.add(genModelMsg);
                }
            }
            try {
                //开始处理返参
                retClsToMkCsv(genRootPath, method, sbfWarnError);
            } catch (Throwable e) {
                //避免一个参数失败，阻碍其他方法参数、结果生成模版
                String genModelMsg = "类" + method.getGenericReturnType().toString() + "模板生成失败:"
                        + e.getMessage() + "可以使用对象模板生成!" + "\n";
                sbfWarnError.add(genModelMsg);
            }
        }
    }

    /**
     * 生成接口中指定方法的入参和返参
     *
     * @param specifyClass
     * @param genRootPath
     * @param sbfWarnError
     * @param methodName
     * @param isResultOnly
     */
    private static void processInterfaceMethod(Class<?> specifyClass, String genRootPath,
                                               Set<String> sbfWarnError, String methodName,
                                               boolean isResultOnly) {
        ReflectUtils refUtil = ReflectUtils.getInstance();
        ClassFields<?> clsFiled = refUtil.analyzeClass(specifyClass);
        ClassData<?> getPro = clsFiled.getClassData();
        List<Method> listMetchod = getPro.getMethods();
        for (Method method : listMetchod) {
            if (StringUtils.equals(method.getName(), methodName)) {
                Invokable invoke = Invokable.from(method);
                ImmutableList<Parameter> mParameters = invoke.getParameters();
                //开始处理入参
                if (!isResultOnly) {
                    for (Parameter parameter : mParameters) {

                        try {
                            Class<?> paraCls = parameter.getType().getRawType();
                            TypeToken<?> preToken = parameter.getType();
                            TypeToken<?> genericTypeToken = preToken
                                    .resolveType(preToken.getType());
                            String csvFile = null;
                            if (!(genericTypeToken.getType() instanceof ParameterizedType)) {
                                doProcess(paraCls, null, new HashSet<String>(),
                                        mkCsvFolderForCls(paraCls, null, genRootPath), sbfWarnError);
                            } else {
                                if (Map.class.isAssignableFrom(paraCls)) {
                                    //MAP类型处理
                                    csvFile = mkCsvFolderForCls(paraCls,
                                            HandMutiParameType(genericTypeToken.getType(), 1),
                                            genRootPath);
                                } else {
                                    csvFile = mkCsvFolderForCls(paraCls,
                                            HandMutiParameType(genericTypeToken.getType(), 0),
                                            genRootPath);
                                }
                                ContainerUtils.handContainer(paraCls,
                                        HandMutiParameType(genericTypeToken.getType(), 0),
                                        HandMutiParameType(genericTypeToken.getType(), 1), csvFile,
                                        new HashSet<String>(), sbfWarnError, true);
                            }

                        } catch (Throwable e) {
                            //避免一个参数失败，阻碍当前方法或者其他方法参数、结果生成模版
                            String genModelMsg = "类" + parameter.getClass().getName() + "模板生成失败:"
                                    + e.getMessage() + "可以使用对象模板生成!" + "\n";
                            sbfWarnError.add(genModelMsg);
                        }
                    }
                }
                try {
                    //开始处理返参
                    retClsToMkCsv(genRootPath, method, sbfWarnError);
                } catch (Throwable e) {
                    //避免一个参数失败，阻碍其他方法参数、结果生成模版
                    String genModelMsg = "类" + method.getGenericReturnType().toString() + "模板生成失败:"
                            + e.getMessage() + "可以使用对象模板生成!" + "\n";
                    sbfWarnError.add(genModelMsg);
                }
            }

        }
    }

    /**
     * 返参的CSV解析设置
     *
     * @param genRootPath
     * @param method
     */
    private static void retClsToMkCsv(String genRootPath, Method method, Set<String> sbfWarnError) {
        TypeToken<?> preToken = TypeToken.of(method.getGenericReturnType());
        TypeToken<?> genericTypeToken = preToken.resolveType(method.getGenericReturnType());
        Class<?> rawClss = genericTypeToken.getRawType();

        if (!(method.getGenericReturnType() instanceof ParameterizedType)) {
            doProcess(genericTypeToken.getRawType(), null, new HashSet<String>(),
                    mkCsvFolderForCls(rawClss, null, genRootPath), sbfWarnError);
        } else {
            if (Map.class.isAssignableFrom(rawClss)) {
                //MAP类型处理
                String csvFile = mkCsvFolderForCls(rawClss,
                        HandMutiParameType(genericTypeToken.getType(), 1), genRootPath);
                ContainerUtils.handContainer(rawClss,
                        HandMutiParameType(genericTypeToken.getType(), 0),
                        HandMutiParameType(genericTypeToken.getType(), 1), csvFile,
                        new HashSet<String>(), sbfWarnError, true);
            } else {
                //其他类型
                String csvFile = mkCsvFolderForCls(rawClss,
                        HandMutiParameType(genericTypeToken.getType(), 0), genRootPath);
                ContainerUtils.handContainer(rawClss,
                        HandMutiParameType(genericTypeToken.getType(), 0), null, csvFile,
                        new HashSet<String>(), sbfWarnError, true);
            }
        }
    }

    /**
     * 当前属性为conditionGroups类型为interface java.util.List
     *
     * @param classTopara
     * @param setCalue
     */
    public static void doProcess(Class<?> classTopara, Class<?> subCls, Set<String> setCalue,
                                 String csvRoot, Set<String> sbfWarn) {
        if (StringUtils.isBlank(csvRoot)) {
            TestifyLogUtil.warn(log, "路径为空，无法生成CSV文件");
            return;
        }
        if (null == classTopara) {
            TestifyLogUtil.warn(log, "类名为空，无法生成CSV文件");
            return;
        }

        if (classTopara.isArray()) {
            classTopara = classTopara.getComponentType();
        }

        if (isWrapClass(classTopara) || setCalue.contains(classTopara.getName())) {
            return;
        }

        File file = FileUtil.getTestResourceFileByRootPath(csvRoot);
        if (file.exists()) {
            TestifyLogUtil.warn(log, "文件【" + csvRoot + "】已经存在，直接跳过");
            return;
        }

        if (classTopara.isInterface() || Modifier.isAbstract(classTopara.getModifiers())) {
            //sbfWarn.add(cutCsvName(csvRoot));
        }
        Map<String, Field> getPro = findTargetClsFields(classTopara);

        //防止循环和避免简单类型处理
        avoidDeedLoop(setCalue, classTopara, subCls);

        List<String[]> outputValues = new ArrayList<String[]>();
        //组装CSV文件第一行，标题行
        List<String> header = new ArrayList<String>();
        header.add(CSVColEnum.CLASS.getCode());
        header.add(CSVColEnum.PROPERTY.getCode());
        header.add(CSVColEnum.TYPE.getCode());
        header.add(CSVColEnum.RULE.getCode());
        header.add(CSVColEnum.FLAG.getCode());
        header.add(CSVColEnum.VALUE.getCode());
        outputValues.add(header.toArray(new String[header.size()]));

        int i = 1;

        for (Entry<String, Field> proValue : getPro.entrySet()) {
            if (null == proValue.getValue()) {
                continue;
            }

            List<String> value = new ArrayList<String>();
            if (1 == i) {
                //如果是第一个生成字段，则内容需要包含class名
                value.add(classTopara.getName());
            } else {
                value.add("");
            }
            //属性名
            value.add(proValue.getKey());
            //属性类型
            value.add(proValue.getValue().getType().getName());

            //原子数据规则
            value.add("");

            //这个Filed的Class类型
            Class<?> filedCls = proValue.getValue().getType();
            Boolean isHandle = false;
            if (setCalue.contains(filedCls.getName())) {
                //防止死循环
                value.add("N");
                value.add("null");
                isHandle = true;
            } else if (isWrapClass(filedCls)) {
                //处理简单类型
                addSimpleValue(proValue.getKey(), filedCls, value);
                isHandle = true;
            }

            if (null != proValue.getValue()) {
                if (!isHandle && Map.class.isAssignableFrom(filedCls)) {

                    try {
                        value.add("M");
                        Class<?> rawClass = getParameRawCls(proValue.getValue().getGenericType());

                        Type mapKeycls = HandMutiParameType(proValue.getValue().getGenericType(), 0);
                        Type mapValueCls = HandMutiParameType(proValue.getValue().getGenericType(),
                                1);

                        String mapPath;
                        if (mapValueCls instanceof ParameterizedType) {
                            mapPath = getGenericCsvFileName(rawClass,
                                    (Class) ((ParameterizedType) mapValueCls).getRawType(), csvRoot);
                        } else {
                            mapPath = getGenericCsvFileName(rawClass, getClass(mapValueCls, 0),
                                    csvRoot);
                        }

                        ContainerUtils.handContainer(rawClass, mapKeycls, mapValueCls, mapPath,
                                setCalue, sbfWarn, false);
                        value.add(cutCsvName(mapPath) + "@1");
                    } catch (Throwable e) {
                        //解析失败设置成空，flag为N.
                        value.set(4, "N");
                        value.add("null");
                        e.printStackTrace();
                    }
                } else if (!isHandle) {

                    Class<?> norMalCls = getClass(proValue.getValue().getGenericType(), 0);
                    //Array 数组的情况
                    if (filedCls.isArray()) {
                        norMalCls = filedCls.getComponentType();
                    }

                    value.add("Y");
                    if (objectTypeManager.isCollectionType(filedCls)) {
                        if (objectTypeManager.isSimpleType(norMalCls)) {
                            value.add(LIST_CONTENT_TEMPLATE);
                        } else if (Map.class.isAssignableFrom(norMalCls)) {
                            //List<Map<>>的情况
                            TypeToken<?> preToken = TypeToken.of(proValue.getValue()
                                    .getGenericType());
                            TypeToken<?> genericTypeToken = preToken.resolveType(proValue
                                    .getValue().getGenericType());

                            Type mapType = HandMutiParameType(genericTypeToken.getType(), 0);
                            Class<?> rawClass = CSVApisUtil.getParameRawCls(mapType);

                            Type mapKeycls = CSVApisUtil.HandMutiParameType(mapType, 0);
                            Type mapValueCls = CSVApisUtil.HandMutiParameType(mapType, 1);

                            String mapPath;
                            if (mapValueCls instanceof ParameterizedType) {
                                mapPath = getGenericCsvFileName(rawClass,
                                        (Class) ((ParameterizedType) mapValueCls).getRawType(), csvRoot);
                            } else {
                                mapPath = getGenericCsvFileName(rawClass, getClass(mapValueCls, 0),
                                        csvRoot);
                            }

                            ContainerUtils.handContainer(rawClass, mapKeycls, mapValueCls, mapPath,
                                    setCalue, sbfWarn, false);
                            value.add(cutCsvName(mapPath) + "@1");

                        } else {

                            //暂时不支持Set<Object>,元素为复杂对象的情况
                            if (Set.class.isAssignableFrom(filedCls)) {
                                value.set(4, "N");
                                value.add("null");
                            } else {
                                //List<Object>,元素为复杂对象的情况

                                //预防list元素嵌套
                                if (setCalue.contains(norMalCls.getName())) {
                                    value.set(4, "N");
                                    value.add("null");
                                } else {
                                    value.add(COMPLEX_LIST_CONTENT_TEMPLATE.replace(FILE_WORDS,
                                            norMalCls.getSimpleName() + ".csv"));
                                }

                                try {
                                    doProcess(norMalCls, null, setCalue,
                                            getCsvFileName(norMalCls, csvRoot), sbfWarn);
                                } catch (Throwable e) {
                                    //解析失败设置成空，flag为N.
                                    value.set(4, "N");
                                    value.set(5, "null");
                                    e.printStackTrace();
                                }
                            }
                        }
                    } else if (proValue.getValue().getGenericType() instanceof ParameterizedType) {
                        value.add(COMPLEX_LIST_CONTENT_TEMPLATE.replace(FILE_WORDS,
                                filedCls.getSimpleName() + "_" + norMalCls.getSimpleName() + ".csv"));
                        try {
                            doProcess(filedCls, norMalCls, setCalue,
                                    getGenericCsvFileName(filedCls, norMalCls, csvRoot), sbfWarn);
                        } catch (Throwable e) {
                            //解析失败设置成空，flag为N.
                            value.set(4, "N");
                            value.set(5, "null");
                            e.printStackTrace();
                        }
                    } else if (filedCls.isInterface()
                            || Modifier.isAbstract(filedCls.getModifiers())) {
                        //接口或抽象属性不再继续生成模版
                        value.set(4, "N");
                        value.add("null");
                    } else {
                        value.add(COMPLEX_TYPE_CONTENT_TEMPLATE.replace(FILE_WORDS,
                                filedCls.getSimpleName() + ".csv"));
                    }
                    if (!isWrapClass(norMalCls) && !setCalue.contains(norMalCls.getName())
                            && !Map.class.isAssignableFrom(norMalCls)) {
                        //继续处理内部复杂类
                        try {
                            doProcess(norMalCls, null, setCalue,
                                    getCsvFileName(norMalCls, csvRoot), sbfWarn);
                        } catch (Throwable e) {
                            //解析失败设置成空，flag为N.
                            value.set(4, "N");
                            value.set(5, "null");
                            e.printStackTrace();
                        }
                    }
                }
            }
            outputValues.add(value.toArray(new String[value.size()]));
            i++;
        }

        //复杂对象没有field的情况
        if (getPro.size() == 0) {
            sbfWarn.add(cutCsvName(csvRoot));
            List<String> value = new ArrayList<String>();
            value.add(classTopara.getName());
            value.add("");
            value.add("");
            value.add("");
            value.add("");
            value.add("");
            outputValues.add(value.toArray(new String[value.size()]));
        }
        writeToCsv(file, outputValues);
        setCalue.remove(classTopara.getName());
    }

    public static String cutCsvName(final String csvRoot) {
        return StringUtils.substringAfterLast(csvRoot, "/");
    }

    public static Map<String, Field> findTargetClsFields(Class<?> cls) {
        if (isWrapClass(cls)) {
            return null;
        }
        ReflectUtils refUtil = ReflectUtils.getInstance();
        ClassFields<?> clsFiled = refUtil.analyzeClass(cls);
        List<Field> getPro = clsFiled.getClassData().getFields();
        Map<String, Field> reClsProp = new HashMap<String, Field>();
        for (Field proValue : getPro) {
            if (Modifier.isFinal(proValue.getModifiers())
                    || Modifier.isStatic(proValue.getModifiers())) {
                continue;
            }
            String propName = proValue.getName();
            reClsProp.put(propName, proValue);
        }

        return reClsProp;
    }

    public static void writeToCsv(File file, List<String[]> outputValues) {

        //初始化写入文件
        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file);
        } catch (Exception e) {
            TestifyLogUtil.warn(log, "初始化写入文件【" + file.getName() + "】失败" + e);
            throw new RuntimeException(e);
        }
        //将生成内容写入CSV文件
        try {
            OutputStreamWriter osw = null;
            osw = new OutputStreamWriter(outputStream);
            CSVWriter csvWriter = new CSVWriter(osw);
            csvWriter.writeAll(outputValues);
            csvWriter.close();
            TestifyLogUtil.warn(log, file.getName() + "生成完毕");
        } catch (Exception e) {
            TestifyLogUtil.warn(log, "通过文件流输出数据失败:" + file.getName() + e);
            throw new RuntimeException(e);
        }
    }

    public static void addSimpleValue(String fieldName, Class<?> fieldType, List<String> value) {
        if (StringUtils.equals(Date.class.getName(), fieldType.getName())) {
            value.add("D");
            value.add("today");
        } else if (StringUtils.equals(Currency.class.getName(), fieldType.getName())) {

            value.add("Y");
            value.add("CNY");

        } else if (StringUtils.equals(fieldName, "currencyValue")) {

            //对MultiCurrencyMoney中的currencyValue赋值默认156，否则加载模版和写yaml有问题
            value.add("Y");
            value.add("156");

        } else if (StringUtils.equals(fieldType.getName(), "int")
                || StringUtils.equals(fieldType.getName(), "java.lang.Integer")
                || StringUtils.equals(fieldType.getName(), "long")
                || StringUtils.equals(fieldType.getName(), "java.lang.Long")
                || StringUtils.equals(fieldType.getName(), "short")) {
            value.add("Y");
            value.add("0");
        } else if (StringUtils.equals(fieldType.getName(), "float")
                || StringUtils.equals(fieldType.getName(), "double")) {
            value.add("Y");
            value.add("0.0");
        } else if (StringUtils.equals(fieldType.getName(), "boolean")) {
            value.add("Y");
            value.add("false");
        } else if (StringUtils.equals(fieldType.getName(), "char")) {
            value.add("Y");
            value.add("A");
        } else if (StringUtils.equals(fieldType.getName(), "java.math.BigDecimal")) {
            value.add("Y");
            value.add("0.001");
        } else if (StringUtils.equals(fieldType.getName(), "java.lang.Object")) {
            value.set(2, "java.lang.Object");
            value.add("N");
            value.add("");
        } else if (StringUtils.equals(fieldType.getName(), "java.lang.Void")) {
            value.set(2, "java.lang.Void");
            value.add("N");
            value.add("null");
        } else {
            value.add("Y");
            value.add("null");
        }
    }

    /**
     * 获取范型的类型
     *
     * @param type
     * @param i
     * @return
     */
    public static Class<?> getClass(Type type, int i) {
        if (type instanceof ParameterizedType) { // 处理泛型类型

            return getGenericClass((ParameterizedType) type, i);
        } else if (type instanceof GenericArrayType) { // 处理数组泛型
            return (Class) ((GenericArrayType) type).getGenericComponentType();
        } else if (type instanceof TypeVariable) {
            return getClass(((TypeVariable) type).getBounds()[0], 0); // 处理泛型擦拭对象
        } else if (type instanceof WildcardType) {
            WildcardType wuleType = (WildcardType) type;
            if (null != wuleType.getUpperBounds()[0]) {
                return getClass(wuleType.getUpperBounds()[0], 0);
            } else {
                return getClass(wuleType.getLowerBounds()[0], 0);
            }
        } else if (type instanceof Class) {
            return (Class<?>) type;
        } else {// class本身也是type，强制转型
            return (Class<?>) type;
        }
    }

    /**
     * 是否存在多级Type
     *
     * @param type
     * @param i
     * @return
     */
    public static Type HandMutiParameType(Type type, int i) {
        if (type instanceof ParameterizedType) {
            try {
                ParameterizedType outType = (ParameterizedType) type;
                return outType.getActualTypeArguments()[i];
            } catch (Exception e) {
                return null;
            }
        } else if (type instanceof Class) {
            if (0 == i) {
                return type;
            }
            return null;
        } else {
            if (0 == i) {
                getClass(type, 0);
            } else {
                return null;
            }
        }
        return null;

    }

    /**
     * 是否存在多级Type
     *
     * @param type
     * @return
     */
    public static Class<?> getParameRawCls(Type type) {
        if (type instanceof ParameterizedType) {
            try {
                ParameterizedType outType = (ParameterizedType) type;
                return (Class) outType.getRawType();
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 是否存在多级Type
     *
     * @param type
     * @return
     */
    public static int getTypeCount(Type type) {
        if (type instanceof ParameterizedType) {
            try {
                ParameterizedType outType = (ParameterizedType) type;
                return outType.getActualTypeArguments().length;
            } catch (Exception e) {
                return 0;
            }
        } else if (type instanceof Class) {
            return 0;
        }
        return 1;
    }

    private static Class<?> getGenericClass(ParameterizedType parameterizedType, int i) {
        Type genericClass = null;
        try {
            genericClass = parameterizedType.getActualTypeArguments()[i];
        } catch (Exception e) {
            return null;
        }

        if (genericClass instanceof ParameterizedType) { // 处理多级泛型
            return (Class) ((ParameterizedType) genericClass).getRawType();
        } else if (genericClass instanceof GenericArrayType) { // 处理数组泛型
            return (Class<?>) ((GenericArrayType) genericClass).getGenericComponentType();
        } else if (genericClass instanceof TypeVariable) { // 处理泛型擦拭对象
            return getClass(((TypeVariable) genericClass).getBounds()[0], 0);
        } else if (genericClass instanceof WildcardType) {
            WildcardType wuleType = (WildcardType) genericClass;
            if (null != wuleType.getUpperBounds()[0]) {
                return getClass(wuleType.getUpperBounds()[0], 0);
            } else {
                return getClass(wuleType.getLowerBounds()[0], 0);
            }

        } else {
            return (Class<?>) genericClass;
        }
    }

    private static void avoidDeedLoop(Set<String> mapLoop, Class<?> clsFullName, Class<?> subClass) {

        String clsName = clsFullName.getName();
        //需要加上是否是简单类型的判断
        if (isWrapClass(clsFullName)) {
            return;
        }

        if (null != subClass) {
            mapLoop.remove(clsFullName);
            clsName += "_" + subClass.getSimpleName();

        }
        if (!mapLoop.contains(clsName)) {
            mapLoop.add(clsName);
        }
    }

    /**
     * 是否是简单类型
     *
     * @param clsToJude
     * @return
     */
    public static boolean isWrapClass(Class<?> clsToJude) {

        if (clsToJude.isPrimitive() || clsToJude.isEnum()
                || clsToJude.getName().toLowerCase().contains("enum")
                || simpleClassType.contains(clsToJude.getName())) {
            return true;
        }

        return false;

    }
}
