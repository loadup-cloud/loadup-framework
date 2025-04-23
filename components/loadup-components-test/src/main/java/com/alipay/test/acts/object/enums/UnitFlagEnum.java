/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.alipay.test.acts.object.enums;

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

import org.apache.commons.lang3.StringUtils;

/**
 * 
 * @author baishuo.lp
 * @version $Id: UnitComparerFlagEnum.java, v 0.1 2015年8月12日 下午2:28:41 baishuo.lp Exp $
 */
public enum UnitFlagEnum {

    Y("Y", "常规比较器"),

    N("N", "不校验"),

    D("D", "日期比较器"),

    C("C", "条件校验器"),

    R("R", "正则比较器"),

    A("A", "非空比较器"),

    M("M", "Map比较器"),

    F("F", "文件比较器"),

    CUSTOM("CUSTOM", "自定义比较器");

    private String code;

    private String description;

    private UnitFlagEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 基于code获取UnitComparerFlagEnum
     * 
     * @param code
     * @return UnitComparerFlagEnum
     */
    public static UnitFlagEnum getByCode(String code) {
        for (UnitFlagEnum unitComparerFlagEnum : UnitFlagEnum.values()) {
            if (StringUtils.equals(unitComparerFlagEnum.getCode(), code)) {
                return unitComparerFlagEnum;
            }
        }
        return UnitFlagEnum.CUSTOM;
    }

    /**
     * Getter method for property <tt>code</tt>.
     * 
     * @return property value of code
     */
    public String getCode() {
        return code;
    }

    /**
     * Getter method for property <tt>description</tt>.
     * 
     * @return property value of description
     */
    public String getDescription() {
        return description;
    }

}
