/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.testify.db.enums;

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

/**
 *
 *
 */
public enum DBFlagEnum {
    /**
     * DB准备时：插入字段
     * DB校验时：校验字段
     */
    Y,
    /**
     * DB准备时：插入字段数据为null
     * DB校验时：不校验该字段
     */
    N,
    /**
     * DB准备时：以此为where条件对数据进行清理
     * DB校验时：以此为where条件对数据进行捞取比对/清理
     */
    C,
    /**
     * DB准备时：不支持
     * DB校验时：当前这张表中以C和CN的字段作为组合条件查询出的结果为空（确保没有这条数据）
     */
    CN,
    /**
     * 同一张表多行数据的场景下，对于同个字段需要配置不同的flag，该字段配置M flag，之后在value里面通过以下格式拼接：
     * {flag}|{value}，如F|now()
     */
    M,
    /**
     * DB准备时：不支持
     * DB校验时：按时间Date类型进行比对
     */
    D,
    /**
     * DB准备时：不支持
     * DB校验时：JSONObject比对
     */
    J,
    /**
     * DB准备时：不支持
     * DB校验时：JSONArray比对
     */
    JA,
    /**
     * DB准备时：不支持
     * DB校验时：Json with BlackList，可在Json最后增加一个####键值对，值为不校验的key，用#分割。
     * 如下例子为不校验内部的budget_type和budgetId字段：
     * {"splitResult":"10,100,297;29700,1000|39700","budgetId":"20190228101223800002","budget_type":"0000000", "####":"budget_type#budgetId"}
     */
    JB,
    /**
     * DB准备时：数据库大字段准备，方式为A=B;C=D
     * DB校验时：数据库大字段换行数据校验
     */
    L,
    /**
     * DB准备时：不支持
     * DB校验时：db大字段校验，以期望结果的kv为基准，对db大字段里的kv进行校验，要求db里的kv之间是换行分隔
     */
    P,
    /**
     * DB准备时：不支持
     * DB校验时：正则匹配该字段数据
     */
    R,
    /**
     * DB准备时：数据库函数
     * DB校验时：不支持
     */
    F,
    ;
}
