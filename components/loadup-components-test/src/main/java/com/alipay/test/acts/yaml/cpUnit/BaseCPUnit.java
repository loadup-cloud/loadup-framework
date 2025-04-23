/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.alipay.test.acts.yaml.cpUnit;

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

import com.alipay.test.acts.yaml.enums.CPUnitTypeEnum;

/**
 * CP基准单位
 * 
 * @author baishuo.lp
 * @version $Id: BaseCPUnit.java, v 0.1 2015年8月12日 上午11:15:53 baishuo.lp Exp $
 */
public abstract class BaseCPUnit {

    /** 单位名称*/
    protected String                    unitName;

    /** 单位描述，同时用作序列号*/
    protected String                    description;

    /** 目标csv路径*/
    protected String                    targetCSVPath;

    /** 自定义规则Map*/
    protected final Map<String, Object> specialMap = new LinkedHashMap<String, Object>();

    /** CP单元类型*/
    protected CPUnitTypeEnum            unitType;

    /** 
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return "BaseCPUnit [unitName=" + unitName + ", description=" + description
               + ", targetCSVPath=" + targetCSVPath + ", specialMap=" + specialMap + ", unitType="
               + unitType + "]";
    }

    /**
     * 生成Yaml文件对应区块Map
     * 
     * @return
     */
    public abstract Object dump();

    /**
     * Getter method for property <tt>unitName</tt>.
     * 
     * @return property value of unitName
     */
    public String getUnitName() {
        return unitName;
    }

    /**
     * Setter method for property <tt>unitName</tt>.
     * 
     * @param unitName value to be assigned to property unitName
     */
    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    /**
     * Getter method for property <tt>specialMap</tt>.
     * 
     * @return property value of specialMap
     */
    public Map<String, Object> getSpecialMap() {
        return specialMap;
    }

    /**
     * Getter method for property <tt>targetCSVPath</tt>.
     * 
     * @return property value of targetCSVPath
     */
    public String getTargetCSVPath() {
        return targetCSVPath;
    }

    /**
     * Setter method for property <tt>targetCSVPath</tt>.
     * 
     * @param targetCSVPath value to be assigned to property targetCSVPath
     */
    public void setTargetCSVPath(String targetCSVPath) {
        this.targetCSVPath = targetCSVPath;
    }

    /**
     * Getter method for property <tt>description</tt>.
     * 
     * @return property value of description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Setter method for property <tt>description</tt>.
     * 
     * @param description value to be assigned to property description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Getter method for property <tt>unitType</tt>.
     * 
     * @return property value of unitType
     */
    public CPUnitTypeEnum getUnitType() {
        return unitType;
    }
}
