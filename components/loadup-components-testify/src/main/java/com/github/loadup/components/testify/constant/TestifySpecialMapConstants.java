/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.testify.constant;

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

import java.util.HashSet;
import java.util.Set;

/**
 * 一些自定义字段常量，用于数据库数据准备和校验
 *
 *
 *
 */
public class TestifySpecialMapConstants {

    // 数据校验时只校验不存在，只在使用check方法时生效
    public static final String NOTEXIST = "$NotExist";

    // 数据准备时只预删除数据，只在使用insert方法时生效
    public static final String ONLYDELETE = "$OnlyDelete";

    // 数据源信息
    public static final String DBCONFIGKEY = "$DBConfigKey";

    // 虚拟分库分表键名
    public static final String SPLITKEY = "$SplitKey";

    // 虚拟分库分表键值
    public static final String SPLITVALUE = "$SplitValue";

    // 组数据校验，内部用来对sql进行补充排序，不供外部使用
    public static final String ORDERBY = "$OrderBy";

    // 特殊字段集合，添加新字段时，需要将字段填入此集合
    public static final Set<String> specialConstantSet = new HashSet<String>();

    static {
        specialConstantSet.add(NOTEXIST);
        specialConstantSet.add(ONLYDELETE);
        specialConstantSet.add(DBCONFIGKEY);
        specialConstantSet.add(SPLITKEY);
        specialConstantSet.add(SPLITVALUE);
    }
}
