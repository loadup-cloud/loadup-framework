/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2021 All Rights Reserved.
 */
package com.alipay.acts.utils.check;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.alipay.test.acts.component.db.DBDatasProcessor;
import com.alipay.test.acts.model.VirtualTable;
import com.alipay.test.acts.template.ActsTestBase;

import java.util.List;

/**
 * db比对工具
 *
 * @author jinsai.chen
 * @version : DbCompareUtil.java, v 0.1 2021年03月04日 上午10:08 jinsai.chen Exp $
 */
public class DbCompareUtil {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(DbCompareUtil.class);

    /**
     * 数据处理
     */
    public static DBDatasProcessor dbDatasProcessor = ActsTestBase.dbDatasProcessor;

    /**
     * db比对工具
     *
     * @param tables 期望校验的db数据，索引字段需要打C标签，查询字段需要通过变量替换，变量设置成$userId
     * @return
     */
    public static void compare2DBDatas(List<VirtualTable> tables) {
        dbDatasProcessor.compare2DBDatas(tables);
    }


}
