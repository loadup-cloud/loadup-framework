/**
 * Alipy.com Inc.
 * Copyright (c) 2004-2013 All Rights Reserved.
 */
package com.github.loadup.components.gateway.core.common.enums;

/*-
 * #%L
 * loadup-components-gateway-core
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

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <P>文件加工处理单元</P>
 */
public enum ProcessUnit {

    /**
     * COMPRESS
     */
    COMPRESS("COMPRESS", "压缩处理"),

    /**
     * DECOMPRESS
     */
    DECOMPRESS("DECOMPRESS", "解压处理"),

    /**
     * ENCRYPT
     */
    ENCRYPT("ENCRYPT", "加密处理"),

    /**
     * DECRYPT
     */
    DECRYPT("DECRYPT", "解密处理"),

    /**
     * SIGN
     */
    SIGN("SIGN", "加签处理"),

    /**
     * VERIFY
     */
    VERIFY("VERIFY", "验签处理"),

    /**
     * DIGEST
     */
    DIGEST("DIGEST", "摘要处理");

    /**
     * 配置分隔符
     */
    private static final String SEPARATOR = ",";
    /**
     * code
     */
    private String code;
    /**
     * desc
     */
    private String desc;

    /**
     * 构造器
     */
    private ProcessUnit(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 通过code获取枚举类
     */
    public static ProcessUnit getProcessUnitByCode(String code) {
        for (ProcessUnit unit : ProcessUnit.values()) {
            if (StringUtils.equalsIgnoreCase(unit.getCode(), code)) {
                return unit;
            }
        }
        return null;
    }

    /**
     * 通过codes获取枚举列表
     */
    public static List<ProcessUnit> getProcessUnitsByCodes(List<String> codes) {

        if (CollectionUtils.isEmpty(codes)) {
            return null;
        }

        List<ProcessUnit> result = new ArrayList<ProcessUnit>();

        for (String code : codes) {
            result.add(getProcessUnitByCode(code));
        }

        return result;
    }

    /**
     * 通过codes 字符串获取枚举列表
     */
    public static List<ProcessUnit> getProcessUnitsByCodesString(String codes) {

        if (StringUtils.isBlank(codes)) {
            return null;
        }

        return getProcessUnitsByCodes(Arrays.asList(codes.split(SEPARATOR)));
    }

    /**
     * getter
     */
    public String getCode() {
        return code;
    }

    /**
     * getter
     */
    public String getDesc() {
        return desc;
    }
}
