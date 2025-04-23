/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.alipay.test.acts.collector.sqlLog;

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

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * sql日志解析器
 * 
 * @author hongling.xiang
 * @version $Id: SqlLogParser.java, v 0.1 2015年10月26日 上午11:04:25 hongling.xiang Exp $
 */
public interface SqlLogParser {

    /**
     * 解析sql获取表名
     * 
     * @param sql
     * @return
     */
    public String parseTableName(String sql);

    /**
     * 解析sql执行日志生成表数据
     * 
     * @param sql
     * @param paramValue
     * @param paramType
     * @return
     * @throws ClassNotFoundException 
     */
    public List<Map<String, Object>> parseGenTableDatas(String sql, List<String> paramValue,
                                                        List<String> paramType);

    /**
     * 解析sql字段标记
     * 
     * @param sql
     * @param TableFields
     * @return
     */
    public Map<String, String> parseTableFlags(String sql, Set<String> tableFields);
}
