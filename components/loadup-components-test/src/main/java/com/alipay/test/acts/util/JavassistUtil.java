/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2015 All Rights Reserved.
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

import javassist.CannotCompileException;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;

import com.alipay.test.acts.exception.ActsException;

/**
 * assist 工具类。
 * 
 * @author dasong.jds
 * @author ming.cm
 * @version $Id: JavassistUtil.java, v 0.1 2015-5-21 下午7:08:17 ming.cm Exp $
 */
public class JavassistUtil {
    /**  */
    public static final ClassPool POOL = ClassPool.getDefault();

    static {
        POOL.appendClassPath(new ClassClassPath(JavassistUtil.class));
    }

    /**
     * 获取方法的参数名称。
     * 
     * @param clazz
     * @param methodName
     * @return
     */
    public static String[] getMethodParamNames(Class<?> clazz, String methodName) {
        try {
            CtClass cc = getCtClass(clazz);

            CtMethod cm = cc.getDeclaredMethod(methodName);

            return getMethodParamNames(cm);
        } catch (NotFoundException e) {
            throw new ActsException("can't find method[name=" + methodName + ",class=" + clazz
                                    + "]", e);
        }
    }

    /**
     * 获取类。
     * 
     * @param className
     * @return
     */
    public static CtClass getCtClass(Class<?> clazz) {
        try {
            return POOL.get(clazz.getName());
        } catch (NotFoundException e) {
            // 多尝试一次
            POOL.appendClassPath(new ClassClassPath(clazz));
            try {
                return POOL.get(clazz.getName());
            } catch (NotFoundException e1) {
                throw new ActsException("can't find class[name=" + clazz.getName() + "]", e);
            }
        }
    }

    /**
     * 获取类。
     * 
     * @param className
     * @return
     */
    public static CtClass getCtClass(String className) {
        try {
            return POOL.get(className);
        } catch (NotFoundException e) {
            throw new ActsException("can't find class[name=" + className + "]", e);
        }
    }

    /**
     * 转换为字节码。
     * 
     * @param ctClass
     * @return
     */
    public static byte[] toByteCode(CtClass ctClass) {
        try {
            if (ctClass.isFrozen()) {
                ctClass.defrost();
            }
            return ctClass.toBytecode();
        } catch (Exception e) {
            throw new ActsException("to bytecode error[class=" + ctClass.getName() + "]", e);
        }
    }

    /**
     * 获取方法参数名。
     * 
     * @param ctMethod 
     * @return
     */
    public static String[] getMethodParamNames(CtMethod ctMethod) {
        MethodInfo methodInfo = ctMethod.getMethodInfo();
        CodeAttribute codeAttribute = methodInfo.getCodeAttribute();
        LocalVariableAttribute attr = (LocalVariableAttribute) codeAttribute
            .getAttribute(LocalVariableAttribute.tag);
        String[] paramNames;
        try {
            paramNames = new String[ctMethod.getParameterTypes().length];
        } catch (NotFoundException e) {
            throw new ActsException("can't get method param types[method=" + ctMethod.getName()
                                    + "]", e);
        }
        int pos = Modifier.isStatic(ctMethod.getModifiers()) ? 0 : 1;

        for (int i = 0; i < paramNames.length; i++) {
            for (int j = 0, size = attr.tableLength(); j < size; j++) {
                if (attr.index(j) == i + pos) {
                    paramNames[i] = attr.variableName(j);
                    break;
                }
            }
        }
        return paramNames;
    }

    /**
     * 去除final修饰。
     * 
     * @param clazz
     */
    public static void removeFinalModifierFromClass(CtClass clazz) {
        if (Modifier.isFinal(clazz.getModifiers())) {
            clazz.setModifiers(clazz.getModifiers() ^ Modifier.FINAL);
        }
    }

    /**
     * 构造函数公共化。
     * 
     * @param clazz
     */
    public static void setAllConstructorsToPublic(CtClass clazz) {
        for (CtConstructor c : clazz.getDeclaredConstructors()) {
            int modifiers = c.getModifiers();
            if (!Modifier.isPublic(modifiers)) {
                c.setModifiers(Modifier.setPublic(modifiers));
            }
        }
    }

    /**
     * 添加默认构造函数。
     * 
     * @param clazz
     */
    public static void addDefualtConstructor(CtClass clazz) {
        try {
            CtConstructor classInitializer = clazz.makeClassInitializer();
            classInitializer.setBody("{}");
        } catch (CannotCompileException e) {
            throw new ActsException("add default constructor error[class=" + clazz + "]", e);
        }
    }

    /**
     * 去除静态字段的final修饰。
     * 
     * @param clazz
     */
    public static void removeFinalModifierFromAllStaticFinalFields(final CtClass clazz) {
        for (CtField f : clazz.getDeclaredFields()) {
            final int modifiers = f.getModifiers();
            if (Modifier.isFinal(modifiers) && Modifier.isStatic(modifiers)) {
                f.setModifiers(modifiers ^ Modifier.FINAL);
            }
        }
    }

}
