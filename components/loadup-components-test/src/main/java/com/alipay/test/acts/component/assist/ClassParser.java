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
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.Opcode;

/**
 * 类解析工具
 * 
 * @author shuhe.th
 * @version $Id: ClassParser.java, v 0.1 2015-6-17 下午3:12:59 shuhe.th Exp $
 */
public class ClassParser {

    /**
     * 获取具有特定注解的类。
     * 
     * @param ClassPool
     * @param classPath
     * @param packageName
     * @param annotationType
     * @return
     * @throws Throwable 
     */
    public static CtClass[] getClassesWithAnnotation(ClassPool pool, String classPath,
                                                     String packageName, Class annotationType) {

        List<CtClass> classes = getClassesWithAnnotationHelper(pool, classPath, packageName,
            annotationType);
        return classes.toArray(new CtClass[classes.size()]);
    }

    private static List<CtClass> getClassesWithAnnotationHelper(ClassPool pool, String classPath,
                                                                String packageName,
                                                                Class annotationType) {
        List<CtClass> classes = new ArrayList<CtClass>();
        String path = classPath + packageName.replace(".", File.separator);
        File dir = new File(path);
        if (!dir.exists())
            return classes;
        for (File file : dir.listFiles()) {
            if (file.isFile()) {
                CtClass cls;
                try {
                    cls = pool.get(packageName + "." + file.getName().split("\\.")[0]);
                } catch (NotFoundException e) {
                    continue;
                }
                if (cls.hasAnnotation(annotationType)) {
                    classes.add(cls);
                }
            } else if (file.isDirectory()) {
                classes.addAll(getClassesWithAnnotationHelper(pool, classPath, packageName + "."
                                                                               + file.getName(),
                    annotationType));

            }
        }
        return classes;
    }

    /**
     * 获取继承特定父类的子类。
     * 
     * @param annotationType 注解类型
     * @return
     * @throws Throwable 
     */
    public static CtClass[] getClassesExtendCCBase(ClassPool pool, String classPath,
                                                   String packageName) {
        List<CtClass> classes = getClassesExtendCCBaseHelper(pool, classPath, packageName);
        return classes.toArray(new CtClass[classes.size()]);

    }

    private static List<CtClass> getClassesExtendCCBaseHelper(ClassPool pool, String classPath,
                                                              String packageName) {

        List<CtClass> classes = new ArrayList<CtClass>();
        String path = classPath + packageName.replace(".", File.separator);
        File dir = new File(path);
        if (!dir.exists())
            return classes;
        for (File file : dir.listFiles()) {
            if (file.isFile()) {
                CtClass cls = null;
                try {
                    cls = pool.get(packageName + "." + file.getName().split("\\.")[0]);
                } catch (NotFoundException e1) {
                    continue;
                }
                CtClass temp = cls;
                boolean find = false;
                while (temp != null) {
                    CtClass[] interfaces;
                    try {
                        interfaces = temp.getInterfaces();
                    } catch (NotFoundException e1) {
                        break;
                    }
                    for (CtClass intf : interfaces) {
                        if (intf.getSimpleName().equals("CCBase")) {
                            find = true;
                            break;
                        }
                    }
                    if (find)
                        break;
                    try {
                        temp = temp.getSuperclass();
                    } catch (NotFoundException e) {
                        break;
                    }
                }
                if (find) {
                    classes.add(cls);
                }
            } else if (file.isDirectory()) {
                classes.addAll(getClassesExtendCCBaseHelper(pool, classPath, packageName + "."
                                                                             + file.getName()));
            }
        }
        return classes;
    }

    /**
     * 获取类中拥有特定注解的方法。
     * 
     * @param obj 操作的类
     * @param annotationType 注解类型
     * @return
     * @throws Throwable 
     */
    public static Map<CtMethod, Object> getMethodWithAnnotation(CtClass cls, Class annotationType) {

        CtMethod[] methods = cls.getDeclaredMethods();

        Map<CtMethod, Object> result = new HashMap<CtMethod, Object>();
        for (CtMethod method : methods) {
            if (method.hasAnnotation(annotationType)) {
                try {
                    result.put(method, method.getAnnotation(annotationType));
                } catch (ClassNotFoundException e) {
                    continue;
                }
            }
        }
        return result;
    }

    /**
     * 获取注解的属性值。
     * 
     * @param ann 操作的注解
     * @param field 属性
     * @return
     * @throws Throwable
     */
    public static Object getAnnotationFieldValue(Object ann, String field) {
        try {
            Method method = ann.getClass().getMethod(field);
            return method.invoke(ann);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取方法返回值(只限return "XX" 的形式)
     * 
     * @param cls 操作的类
     * @param methodName 方法名
     * @return 
     * @throws Throwable
     */
    public static String getMethodReturnString(CtClass cls, String methodName) {
        MethodInfo info;
        try {
            info = cls.getDeclaredMethod(methodName).getMethodInfo();
        } catch (NotFoundException e) {
            return null;
        }
        CodeAttribute ca = info.getCodeAttribute();
        CodeIterator iter = ca.iterator();
        ConstPool constPool = cls.getClassFile().getConstPool();
        int lastIndex = 0;
        while (iter.hasNext()) {
            int index;
            try {
                index = iter.next();
            } catch (BadBytecode e) {
                break;
            }
            int op = iter.byteAt(index);
            if (op == Opcode.ARETURN) {
                if (iter.byteAt(lastIndex) == Opcode.LDC) {
                    int next = iter.byteAt(lastIndex + 1);
                    int tag = constPool.getTag(next);
                    switch (tag) {
                        case ConstPool.CONST_String:
                            return constPool.getStringInfo(next);
                        default:
                            return null;
                    }
                }
            }
            lastIndex = index;
        }
        return null;
    }

}
