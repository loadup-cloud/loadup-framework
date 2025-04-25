/**
 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.github.loadup.components.testify.object;

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

import com.github.loadup.components.testify.context.TestifyCaseContextHolder;
import com.github.loadup.components.testify.log.TestifyLogUtil;
import com.github.loadup.components.testify.object.processor.ObjectProcessor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 存储旧CTS支持对象相关方法，不再维护，不建议使用
 *
 *
 *
 */
@Deprecated
@Slf4j
public class CtsObjectUtil {


    /**
     * 根据CSV文件生成对象List
     *
     * @param objectClass -目标对象的class
     * @param csvPath     -CSV文件路径
     * @param csvCols     -期望值的列序号，形如1,2..(基数默认1,用逗号分隔)
     * @return 生成对象
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> genObjectListFromCSV(Class<T> objClass, String csvPath, String csvCols) {

        if (StringUtils.isBlank(csvPath) || StringUtils.isBlank(csvCols)) {
            TestifyLogUtil.info(log, "csvPath或列号为空，构建" + objClass.getSimpleName() + "为空");
            return null;
        }
        List<T> objs = new ArrayList<T>();

        String[] descArray = csvCols.split(";");
        for (String desc : descArray) {
            ObjectProcessor processor = new ObjectProcessor(csvPath, desc);
            try {
                objs.add((T) processor.genObject());
            } catch (Exception e) {
                log.error("添加当前的T对象失败", e);
            }

        }
        TestifyLogUtil.info(log,
                "从" + csvPath + "第" + csvCols + "列准备List对象" + objClass.getSimpleName());
        TestifyLogUtil.addProcessLog(objs);
        check();
        return objs;
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
    @SuppressWarnings("unchecked")
    public static <T> T genObjectFromCSV(Class<T> objClass, String csvPath, String index)
            throws NumberFormatException,
            Exception {
        if (StringUtils.isBlank(csvPath) || StringUtils.isBlank(index)) {
            TestifyLogUtil.info(log, "csvPath或列号为空，构建" + objClass.getSimpleName() + "为空");
            return null;
        }
        ObjectProcessor processor = new ObjectProcessor(csvPath, index);
        T obj = (T) processor.genObject();
        TestifyLogUtil.info(log, "从" + csvPath + "第" + index + "列准备对象" + objClass.getSimpleName());
        TestifyLogUtil.addProcessLog(obj);
        check();
        return obj;
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
        if (StringUtils.isBlank(csvPath) || StringUtils.isBlank(indexs)) {
            return;
        }
        TestifyLogUtil.addProcessLog(actuals);
        String[] cols = indexs.split(",");
        for (int i = 0; i < cols.length; i++) {
            ObjectProcessor processor = new ObjectProcessor(csvPath, cols[i]);
            processor.checkObject(actuals.get(i));
        }
        check();
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
        if (StringUtils.isBlank(csvPath) || StringUtils.isBlank(index)) {
            return;
        }
        TestifyLogUtil.addProcessLog(actual);
        ObjectProcessor processor = new ObjectProcessor(csvPath, index);
        processor.checkObject(actual);
        check();
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
        Map<String, Object> uniqueMap = TestifyCaseContextHolder.get().getUniqueMap();
        for (Entry<String, String> entry : map.entrySet()) {
            uniqueMap.put(entry.getKey(), entry.getValue());
        }
        TestifyLogUtil.addProcessLog(actual);
        ObjectProcessor processor = new ObjectProcessor(csvPath, index);
        processor.checkObject(actual);
        check();
        uniqueMap.clear();
    }

    private static void check() {
        if (TestifyCaseContextHolder.get().getProcessErrorLog().size() > 0) {
            Assert.fail("数据准备失败:" + TestifyCaseContextHolder.get().getProcessErrorLog());
        }
    }
}
