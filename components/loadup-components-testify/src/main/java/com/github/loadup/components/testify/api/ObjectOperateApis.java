/**

 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.github.loadup.components.testify.api;

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

import com.github.loadup.components.testify.object.CtsObjectUtil;

import java.util.List;
import java.util.Map;

/**
 * cts操作工具类常规入口，已弃用，这里仅保留用于过渡
 *
 * 
 *
 */
@SuppressWarnings("deprecation")
public class ObjectOperateApis {

    /**
     * 根据CSV文件生成对象List
     *
     * @param objectClass -目标对象的class
     * @param csvPath     -CSV文件路径
     * @param csvCols     -期望值的列序号，形如1,2..(基数默认1,用逗号分隔)
     * @return 生成对象
     */
    @Deprecated
    public static <T> List<T> genObjectListFromCSV(Class<T> objClass, String csvPath, String csvCols) {

        return CtsObjectUtil.genObjectListFromCSV(objClass, csvPath, csvCols);
    }

    /**
     * 根据CSV文件生成对象
     *
     * @param objectClass -目标对象的class
     * @param csvPath     -CSV文件路径
     * @param index       -期望值的列序号，基数默认1
     * @return 生成对象
     * @throws Exception
     * @throws NumberFormatException
     */
    public static <T> T genObjectFromCSV(Class<T> objClass, String csvPath, String index)
            throws NumberFormatException,
            Exception {
        return CtsObjectUtil.genObjectFromCSV(objClass, csvPath, index);
    }

    /**
     * 按指定顺序校验多列，用逗号分隔,例如：1,2,5
     *
     * @param actuals
     * @param csvPath,可以为驱动文件夹下相对路径
     * @param indexs
     * @return 校验结果：true-校验成功     false-校验失败
     */
    public static void checkObjectListByOrder(List<?> actuals, String csvPath, String indexs) {
        CtsObjectUtil.checkObjectListByOrder(actuals, csvPath, indexs);
    }

    /**
     * 校验单个普通对象
     *
     * @param actual
     * @param csvPath,可以为驱动文件夹下相对路径
     * @param index
     * @return 校验结果：true-校验成功     false-校验失败
     */

    public static void checkObject(Object actual, String csvPath, String index) {
        CtsObjectUtil.checkObject(actual, csvPath, index);
    }

    /**
     * 校验单个普通对象，对csv文件中对应字段用map中的键值进行替换
     *
     * @param actual
     * @param csvPath,可以为驱动文件夹下相对路径
     * @param index
     * @param map
     * @return 校验结果：true-校验成功     false-校验失败
     */
    public static void checkObject(Object actual, String csvPath, String index,
                                   Map<String, String> map) {
        CtsObjectUtil.checkObject(actual, csvPath, index, map);
    }

}
