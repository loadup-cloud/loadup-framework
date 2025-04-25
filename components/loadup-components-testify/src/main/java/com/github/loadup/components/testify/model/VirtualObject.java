package com.github.loadup.components.testify.model;

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

import java.util.LinkedHashMap;
import java.util.Map;

import com.github.loadup.components.testify.setter.FlagSetter;

/**
 * 抽象对象
 *
 * @author tantian.wc
 *
 */
public class VirtualObject extends TestNode {

    /**
     * 描述
     */
    public String description;
    /**
     * 类名
     */
    public String objClass;
    /**
     * 模板csv的名称
     */
    public String objBaseName;
    /**
     * 模板的数据列
     */
    public String objBaseDesc;
    /**
     * 对象实例
     */
    public Object object;
    /**
     * 对象flag,<类名，<fiel名，flag值>>
     */
    public Map<String, Map<String, String>> flags = new LinkedHashMap<String, Map<String, String>>();

    public VirtualObject() {

    }

    public VirtualObject(Object obj) {
        if (obj != null) {
            this.objClass = obj.getClass().getName();
        } else {
            this.objClass = null;
        }
        this.object = obj;

    }

    public VirtualObject(Object obj, String desc) {
        if (obj != null) {
            this.objClass = obj.getClass().getName();
            this.objBaseName = obj.getClass().getSimpleName();
        }
        this.object = obj;
        this.objBaseDesc = desc;
    }

    /**
     * 通过setter可以重复调用set方法  getFlagSetter(A.class).set("a","Y").set("b","N");
     *
     * @param clazz
     * @return
     */
    public FlagSetter getFlagSetter(Class<?> clazz) {
        if (flags == null) {
            flags = new LinkedHashMap<String, Map<String, String>>();
        }
        if (flags.get(clazz.getName()) == null) {
            flags.put(clazz.getName(), new LinkedHashMap<String, String>());
        }

        return FlagSetter.getFlagSetter(clazz.getName(), flags.get(clazz.getName()));
    }

    public String getObjClass() {
        return objClass;
    }

    public void setObjClass(String objClass) {
        this.objClass = objClass;
    }

    public String getObjBaseName() {
        return objBaseName;
    }

    public void setObjBaseName(String objBaseName) {
        this.objBaseName = objBaseName;
    }

    public String getObjBaseDesc() {
        return objBaseDesc;
    }

    public void setObjBaseDesc(String objBaseDesc) {
        this.objBaseDesc = objBaseDesc;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public Map<String, Map<String, String>> getFlags() {
        return flags;
    }

    public void setFlags(Map<String, Map<String, String>> flags) {
        this.flags = flags;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return "VirtualObject [description=" + description + ", objClass=" + objClass
                + ", objBaseName=" + objBaseName + ", objBaseDesc=" + objBaseDesc + ", object="
                + object + ", flags=" + flags + "]";
    }

}
