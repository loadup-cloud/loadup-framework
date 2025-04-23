/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.alipay.test.acts.component.assist;

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

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import com.alibaba.fastjson2.JSON;
import com.alipay.test.acts.component.enums.MockPhase;

/**
 * 生成用例组件索引。
 * 
 * @author dasong.jds
 * @version $Id: CCIndexBuilder.java, v 0.1 2015年5月17日 上午2:01:42 dasong.jds Exp $
 */
public class CCIndexBuilder {

    private final String                           classPath;
    private final String[]                         packageNames;
    private final String                           outPath;
    private final String                           jsonPath;
    private final Map<String, List<ComponentDesc>> components;

    public CCIndexBuilder(String classPath, String[] packageNames, String outPath, String jsonPath) {
        this.classPath = classPath;
        this.packageNames = packageNames;
        this.outPath = outPath;
        this.jsonPath = jsonPath;
        this.components = new HashMap<String, List<ComponentDesc>>();
    }

    /**
     * 入口方法。
     * @throws Throwable 
     * 
     */
    public void run() throws Throwable {
        ClassPool pool = ClassPool.getDefault();
        pool.insertClassPath(classPath);
        Class cchost = pool.get("com.ipay.itest.common.annotation.CCHost").toClass();
        for (String packg : packageNames) {
            createFile(pool, cchost, packg);
        }
        //生成json格式文件
        File jsonfile = new File(jsonPath);
        if (!jsonfile.getParentFile().exists()) {
            jsonfile.getParentFile().mkdirs();
        }
        if (!jsonfile.exists()) {
            jsonfile.createNewFile();
        }
        FileOutputStream jsonfos = new FileOutputStream(jsonfile);
        PrintStream jsonp = new PrintStream(jsonfos, true, "UTF8");
        String componentInfo = JSON.toJSONString(components);
        jsonp.print(componentInfo);
        jsonp.close();
    }

    /**
     * 自动生成index文件。
     * @throws Throwable 
     * 
     */
    private void createFile(ClassPool pool, Class cchost, String packg) throws Throwable {

        List<ComponentDesc> compList = new ArrayList<ComponentDesc>();
        String[] dirs = packg.split("\\.");
        String path = outPath + dirs[dirs.length - 1] + File.separator;
        File parent = new File(path);
        if (!parent.exists()) {
            parent.mkdirs();
        }
        FileOutputStream fos = new FileOutputStream(path + getIndexName(packg));
        PrintStream p = new PrintStream(fos, true, "UTF8");
        p.println(getClassHeader(packg));
        String annName = getAnnotationName(packg);
        Class anno = pool.get("com.ipay.itest.common.annotation." + annName).toClass();
        //处理通过注解方法申明的组件
        CtClass[] classes = ClassParser.getClassesWithAnnotation(pool, classPath, packg, cchost);
        for (CtClass cls : classes) {
            Map<String, Map<CtMethod, Object>> idmap = new HashMap<String, Map<CtMethod, Object>>();
            Map<CtMethod, Object> map = ClassParser.getMethodWithAnnotation(cls, anno);

            for (Map.Entry<CtMethod, Object> entry : map.entrySet()) {
                String id = (String) ClassParser.getAnnotationFieldValue(entry.getValue(), "id");
                if (id == null || id.isEmpty())
                    continue;
                if (!idmap.containsKey(id)) {
                    idmap.put(id, new HashMap<CtMethod, Object>());
                }
                idmap.get(id).put(entry.getKey(), entry.getValue());
            }

            for (Map.Entry<String, Map<CtMethod, Object>> entry : idmap.entrySet()) {
                String id = entry.getKey();
                String[] dscp = new String[entry.getValue().size()];
                String[] ref = new String[entry.getValue().size()];

                List<Map<String, String>> detail = new ArrayList<Map<String, String>>();

                int index = 0;
                for (Map.Entry<CtMethod, Object> inner : entry.getValue().entrySet()) {
                    String tempdscp = (String) ClassParser.getAnnotationFieldValue(
                        inner.getValue(), "description");
                    String tempref = getref(cls.getSimpleName(), inner.getKey());
                    dscp[index] = tempdscp;
                    ref[index] = tempref;
                    index++;
                    Map<String, String> item = new LinkedHashMap<String, String>();
                    if (annName.equals("CCMock")) {
                        item.put("phase", ((MockPhase) ClassParser.getAnnotationFieldValue(
                            inner.getValue(), "phase")).name());
                    }
                    item.put("reference", tempref);
                    item.put("description", tempdscp);
                    detail.add(item);
                }
                p.print(getFieldDescription(dscp, ref));
                p.println(getFieldDef(id, packg));
                compList.add(new ComponentDesc(annName, id, detail));
            }
        }
        //处理通过继承CCBase方法申明的组件
        classes = ClassParser.getClassesExtendCCBase(pool, classPath, packg);
        for (CtClass cls : classes) {
            String id = ClassParser.getMethodReturnString(cls, "getId");
            if (id == null || id.isEmpty())
                continue;
            String dscp = ClassParser.getMethodReturnString(cls, "getDescription");
            if (dscp == null)
                dscp = "";
            String ref = "@see " + cls.getSimpleName();
            p.print(getFieldDescription(dscp, ref));
            p.println(getFieldDef(id, packg));
            List<Map<String, String>> detail = new ArrayList<Map<String, String>>();
            Map<String, String> map = new LinkedHashMap<String, String>();
            map.put("reference", ref);
            map.put("description", dscp);
            detail.add(map);
            compList.add(new ComponentDesc(annName, id, detail));
        }
        p.println(getClassTail());
        p.close();
        components.put(annName, compList);
    }

    //~~private 方法

    /**
     * 获取注解名。
     * 
     */
    private String getAnnotationName(String packg) {

        String[] dirs = packg.split("\\.");
        String prefix = dirs[dirs.length - 1];
        StringBuilder name = new StringBuilder();
        name.append(prefix.substring(0, 3).toUpperCase());
        name.append(prefix.substring(3));
        return name.toString();
    }

    /**
     * 获取Index文件名。
     * 
     */
    private String getIndexName(String packg) {

        return getAnnotationName(packg) + "Index.java";
    }

    /**
     * 获取index文件头部内容。
     * 
     * @param packg package name
     * @return index文件头部内容
     */
    private String getClassHeader(String packg) {

        String head = "/**\n * Alipay.com Inc.\n * Copyright (c) 2004-2015 All Rights Reserved.\n */";
        String packgdscp = "package " + packg + ";";
        String tmp;
        String classdscp = "/**\n * 用例" + getAnnotationName(packg).substring(2)
                           + "组件索引.\n * <p>\n * 此文件自动根据注解生成,请勿人工修改.\n *\n */";
        String classdef = "public class " + getAnnotationName(packg) + "Index {";

        String patten = "%s\n%s\n\n%s\n%s\n";
        return String.format(patten, head, packgdscp, classdscp, classdef);
    }

    /**
     * 根据类名和方法生成引用描述。
     * 
     * @param dlassName 类名
     * @param method 方法
     * @return 引用描述
     */
    private String getref(String className, CtMethod method) {
        String longname = method.getLongName();
        String tmp = longname.split("\\(")[1];
        tmp = tmp.substring(0, tmp.length() - 1);
        String[] paras = tmp.split(",");
        StringBuilder param = new StringBuilder();
        param.append(method.getName());
        param.append("(");
        for (int i = 0; i < paras.length; i++) {
            if (i != 0)
                param.append(", ");
            param.append(paras[i]);
        }
        param.append(")");
        return String.format("@see %s#%s", className, param);
    }

    /**
     * 根据注解描述、类名和方法生成变量注释。
     * 
     * @param description 注解描述
     * @param ref 引用描述
     * @return 变量注释
     * @throws Throwable
     */
    private String getFieldDescription(String description, String ref) {
        StringBuilder builder = new StringBuilder();
        builder.append("/**\n");
        builder.append(" * " + description + "\n");
        builder.append(" *\n");
        builder.append(" * " + ref + "\n");
        builder.append(" */\n");
        return builder.toString();
    }

    /**
     * 根据注解描述、类名和方法生成变量注释。
     * 
     * @param description 注解描述
     * @param ref 引用描述
     * @return 变量注释
     * @throws Throwable
     */
    private String getFieldDescription(String[] description, String[] ref) {
        StringBuilder builder = new StringBuilder();
        builder.append("/**\n");
        for (int i = 0; i < ref.length; i++) {
            builder.append(" * " + description[i] + "\n");
            builder.append(" *\n");
            builder.append(" * " + ref[i] + "\n");
            if (i < ref.length - 1) {
                builder.append(" *\n");
            }
        }
        builder.append(" */\n");
        return builder.toString();
    }

    /**
     * 根据注解Id生成变量申明语句。
     * 
     * @param id 注解Id
     * @param packg package name
     * @return 变量申明语句
     */
    private String getFieldDef(String id, String packg) {

        String patten = "public static String %s = \"%s:%s\";\n";
        String[] dirs = packg.split("\\.");
        String prefix = dirs[dirs.length - 1];
        return String.format(patten, id, prefix, id);
    }

    /**
     * 获取CCPrepareIndex.java文件尾部内容。
     * 
     */
    private String getClassTail() {

        return "}";
    }
}
