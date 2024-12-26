package com.github.loadup.components.retrytask.util;

/*-
 * #%L
 * loadup-components-retrytask
 * %%
 * Copyright (C) 2022 - 2023 loadup_cloud
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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import lombok.extern.slf4j.Slf4j;

/**
 * DB查询结果ResultSet工具类
 */
@Slf4j
public class ResultSetUtil {

    /**
     * 获取对应日期列的日期
     */
    public static Date obtainDateValue(ResultSet resultSet, String columnName) {

        try {
            return new Date(resultSet.getTimestamp(columnName).getTime());
        } catch (SQLException sqlException) {
            //            LogUtils.warn(log, sqlException, "convert to dataValue,sql failed，columnName=",
            //                    columnName);
            return null;
        }
    }
}
