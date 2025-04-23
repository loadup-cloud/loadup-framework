/**
 * Alipay.com Inc. Copyright (c) 2004-2021 All Rights Reserved.
 */
package com.alipay.test.acts.model;

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

import com.alipay.test.acts.enums.YamlFieldEnum;

import java.util.List;
import java.util.Map;

/**
 * @author pumo
 * @version $Id: CaseObjectSet.java, v 0.1 2021年03月10日 11:01 AM pumo Exp $
 */
public class CaseObjectSet {

    // 用例入参对象列表
    private List<Object> args;

    // 对象比对标识
    private Map<String, Map<String, String>> flags;

    // 用例返回对象（出参/异常）
    private Object retObj;

    // 用例事件/消息列表
    private List<Map<String, Object>> events;

    // 自定义变量
    private Map<String, Object> params;


    // 组件化列表
    private List<Map<String, Object>> components;

    /**
     * 从yaml对象列表中读取相关对象集合，默认顺序为[入参列表，flags列表，返回对象，事件/消息列表，自定义变量，VirtualMock]
     *
     * @param yamlObjs
     */
    public CaseObjectSet(List<Object> yamlObjs) {
        args = yamlObjs.size() > YamlFieldEnum.ARGS.getPos() ?
                (List<Object>) yamlObjs.get(YamlFieldEnum.ARGS.getPos()) : null;
        flags = yamlObjs.size() > YamlFieldEnum.FLAGS.getPos() ?
                (Map<String, Map<String, String>>)yamlObjs.get(YamlFieldEnum.FLAGS.getPos()) : null;
        retObj = yamlObjs.size() > YamlFieldEnum.RESULT.getPos() ?
                yamlObjs.get(YamlFieldEnum.RESULT.getPos()) : null;

        if (yamlObjs.size() > YamlFieldEnum.EVENTS.getPos()
                && yamlObjs.get(YamlFieldEnum.EVENTS.getPos()) != null
                && ((List)yamlObjs.get(YamlFieldEnum.EVENTS.getPos())).size() > 0) {
            events = (List<Map<String, Object>>) yamlObjs.get(YamlFieldEnum.EVENTS.getPos());
        } else {
            events = null;
        }

        params = yamlObjs.size() > YamlFieldEnum.PARAMS.getPos() ?
                (Map<String, Object>)yamlObjs.get(YamlFieldEnum.PARAMS.getPos()) : null;

        if (yamlObjs.size() > YamlFieldEnum.COMPOS.getPos()
                && yamlObjs.get(YamlFieldEnum.COMPOS.getPos()) != null
                && ((List)yamlObjs.get(YamlFieldEnum.COMPOS.getPos())).size() > 0) {
            components = (List<Map<String, Object>>) yamlObjs.get(YamlFieldEnum.COMPOS.getPos());
        } else {
            components = null;
        }

    }

    public boolean isException() {
        if (retObj == null) {
            return false;
        }
        return retObj.getClass().getName().toLowerCase().endsWith("exception");
    }

    public boolean isEmpty() {
        return null == args && null == retObj && null == events;
    }

    public List<Object> getArgs() {
        return args;
    }

    public void setArgs(List<Object> args) {
        this.args = args;
    }

    public Map<String, Map<String, String>> getFlags() {
        return flags;
    }

    public void setFlags(Map<String, Map<String, String>> flags) {
        this.flags = flags;
    }

    public Object getRetObj() {
        return retObj;
    }

    public void setRetObj(Object retObj) {
        this.retObj = retObj;
    }

    public List<Map<String, Object>> getEvents() {
        return events;
    }

    public void setEvents(List<Map<String, Object>> events) {
        this.events = events;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }


    public List<Map<String, Object>> getComponents() {
        return components;
    }

    public void setComponents(List<Map<String, Object>> components) {
        this.components = components;
    }
}
