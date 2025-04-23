/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.alipay.test.acts.yaml.cpUnit.property;

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

import org.apache.commons.lang3.StringUtils;
import com.alipay.test.acts.cache.ActsCacheData;
import com.alipay.test.acts.object.ActsObjectUtil;
import com.alipay.test.acts.object.comparer.UnitComparer;
import com.alipay.test.acts.object.enums.UnitFlagEnum;
import com.alipay.test.acts.object.manager.ObjectCompareManager;

/**
 * 数据库字段基础属性
 * 
 * @author baishuo.lp
 * @version $Id: BaseUnitProperty.java, v 0.1 2015年8月12日 下午4:40:26 baishuo.lp Exp $
 */
public class BaseUnitProperty {

    /** 属性字段名称*/
    protected String  keyName;

    /** 属性标识*/
    protected String  flagCode;

    /** 属性期望值*/
    protected Object  expectValue;

    /** 属性基类数值*/
    protected Object  baseValue;

    /** 属性基类Flag*/
    protected String  baseFlagCode;

    /** 数据库列属性，仅数据库使用*/
    protected String  dbColumnType;

    /** 数据库列描述，仅数据库使用*/
    protected String  dbColumnComment;

    /** 属性路径*/
    protected String  keyPath;

    /** 过程中是否被UniqueMap替换*/
    protected boolean isUnique       = true;

    /** 比较结果*/
    protected boolean compareSuccess = true;

    /** 未替换期望值*/
    protected Object  oldExpectValue = null;

    /** 当前比较实际值*/
    protected Object  actualValue    = null;

    public BaseUnitProperty(String keyName, String keyPath, Object expectValue) {
        this.keyName = keyName;
        this.expectValue = expectValue;
        this.keyPath = keyPath;
    }

    /**
     * 校验数据
     * 
     * @param object
     * @return
     */
    public void compare(Object object) {
        UnitFlagEnum flag = UnitFlagEnum.getByCode(this.flagCode);
        UnitComparer comparer;
        if (flag == UnitFlagEnum.CUSTOM) {
            comparer = ActsCacheData.getCustomComparer(this.flagCode);
        } else {
            comparer = ObjectCompareManager.getComparerManager().get(
                UnitFlagEnum.getByCode(this.flagCode));
        }
        if (!comparer.compare(this.expectValue, object, this.flagCode)) {
            this.setCompareSuccess(false);
            this.setActualValue(object);
        }
    }

    /**
     * 生成差异化yaml文件
     * 
     * @return
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Object dump() {
        Object exp = this.expectValue; //未校验则用当前期望值
        if (!this.compareSuccess) {
            if (this.isUnique) {
                //uniqueMap替换，则dump替换前的值
                exp = this.oldExpectValue;
            } else {
                //未用uniqueMap替换，则dump真实值
                exp = this.actualValue;
            }
        }

        Map dumpMap = new LinkedHashMap();
        if (!StringUtils.equals(this.baseFlagCode, this.flagCode)) {
            dumpMap.put(this.keyName + "[" + this.flagCode + "]", exp);
        } else if (!ActsObjectUtil.easyCompare(this.baseValue, exp)) {
            dumpMap.put(this.keyName, exp);
        }
        return dumpMap;
    }

    /** 
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return "BaseUnitProperty [keyName=" + keyName + ", flagCode=" + flagCode + ", expectValue="
               + expectValue + ", baseValue=" + baseValue + ", baseFlagCode=" + baseFlagCode
               + ", dbColumnType=" + dbColumnType + ", dbColumnComment=" + dbColumnComment
               + ", keyPath=" + keyPath + "]";
    }

    /**
     * Getter method for property <tt>keyName</tt>.
     * 
     * @return property value of keyName
     */
    public String getKeyName() {
        return keyName;
    }

    /**
     * Setter method for property <tt>keyName</tt>.
     * 
     * @param keyName value to be assigned to property keyName
     */
    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }

    /**
     * Getter method for property <tt>flagCode</tt>.
     * 
     * @return property value of flagCode
     */
    public String getFlagCode() {
        return flagCode;
    }

    /**
     * Setter method for property <tt>flagCode</tt>.
     * 
     * @param flagCode value to be assigned to property flagCode
     */
    public void setFlagCode(String flagCode) {
        this.flagCode = flagCode;
    }

    /**
     * Getter method for property <tt>expectValue</tt>.
     * 
     * @return property value of expectValue
     */
    public Object getExpectValue() {
        return expectValue;
    }

    /**
     * Setter method for property <tt>expectValue</tt>.
     * 
     * @param expectValue value to be assigned to property expectValue
     */
    public void setExpectValue(Object expectValue) {
        this.expectValue = expectValue;
    }

    /**
     * Getter method for property <tt>keyPath</tt>.
     * 
     * @return property value of keyPath
     */
    public String getKeyPath() {
        return keyPath;
    }

    /**
     * Setter method for property <tt>keyPath</tt>.
     * 
     * @param keyPath value to be assigned to property keyPath
     */
    public void setKeyPath(String keyPath) {
        this.keyPath = keyPath;
    }

    /**
     * Getter method for property <tt>baseValue</tt>.
     * 
     * @return property value of baseValue
     */
    public Object getBaseValue() {
        return baseValue;
    }

    /**
     * Setter method for property <tt>baseValue</tt>.
     * 
     * @param baseValue value to be assigned to property baseValue
     */
    public void setBaseValue(Object baseValue) {
        this.baseValue = baseValue;
    }

    /**
     * Getter method for property <tt>dbColumnType</tt>.
     * 
     * @return property value of dbColumnType
     */
    public String getDbColumnType() {
        return dbColumnType;
    }

    /**
     * Setter method for property <tt>dbColumnType</tt>.
     * 
     * @param dbColumnType value to be assigned to property dbColumnType
     */
    public void setDbColumnType(String dbColumnType) {
        this.dbColumnType = dbColumnType;
    }

    /**
     * Getter method for property <tt>dbColumnComment</tt>.
     * 
     * @return property value of dbColumnComment
     */
    public String getDbColumnComment() {
        return dbColumnComment;
    }

    /**
     * Setter method for property <tt>dbColumnComment</tt>.
     * 
     * @param dbColumnComment value to be assigned to property dbColumnComment
     */
    public void setDbColumnComment(String dbColumnComment) {
        this.dbColumnComment = dbColumnComment;
    }

    /**
     * Getter method for property <tt>baseFlagCode</tt>.
     * 
     * @return property value of baseFlagCode
     */
    public String getBaseFlagCode() {
        return baseFlagCode;
    }

    /**
     * Setter method for property <tt>baseFlagCode</tt>.
     * 
     * @param baseFlagCode value to be assigned to property baseFlagCode
     */
    public void setBaseFlagCode(String baseFlagCode) {
        this.baseFlagCode = baseFlagCode;
    }

    /**
     * Getter method for property <tt>isUnique</tt>.
     * 
     * @return property value of isUnique
     */
    public boolean isUnique() {
        return isUnique;
    }

    /**
     * Setter method for property <tt>isUnique</tt>.
     * 
     * @param isUnique value to be assigned to property isUnique
     */
    public void setUnique(boolean isUnique) {
        this.isUnique = isUnique;
    }

    /**
     * Getter method for property <tt>oldExpectValue</tt>.
     * 
     * @return property value of oldExpectValue
     */
    public Object getOldExpectValue() {
        return oldExpectValue;
    }

    /**
     * Setter method for property <tt>oldExpectValue</tt>.
     * 
     * @param oldExpectValue value to be assigned to property oldExpectValue
     */
    public void setOldExpectValue(Object oldExpectValue) {
        this.oldExpectValue = oldExpectValue;
    }

    /**
     * Getter method for property <tt>actualValue</tt>.
     * 
     * @return property value of actualValue
     */
    public Object getActualValue() {
        return actualValue;
    }

    /**
     * Setter method for property <tt>actualValue</tt>.
     * 
     * @param actualValue value to be assigned to property actualValue
     */
    public void setActualValue(Object actualValue) {
        this.actualValue = actualValue;
    }

    /**
     * Getter method for property <tt>compareSuccess</tt>.
     * 
     * @return property value of compareSuccess
     */
    public boolean isCompareSuccess() {
        return compareSuccess;
    }

    /**
     * Setter method for property <tt>compareSuccess</tt>.
     * 
     * @param compareSuccess value to be assigned to property compareSuccess
     */
    public void setCompareSuccess(boolean compareSuccess) {
        this.compareSuccess = compareSuccess;
    }

}
