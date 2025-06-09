/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.testify.utils.file;

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

import com.github.loadup.components.testify.constant.TestifyPathConstants;
import com.github.loadup.components.testify.constants.DBFlagConstant;
import com.github.loadup.components.testify.db.enums.CSVColEnum;
import com.github.loadup.components.testify.exception.TestifyException;
import com.github.loadup.components.testify.helper.CSVHelper;
import com.github.loadup.components.testify.model.VirtualTable;
import com.github.loadup.components.testify.util.FileUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedCaseInsensitiveMap;

/**
 * csv操作工具类
 */
public class CsvUtils {

    /**
     * 根据对象生成CSV文件（包含对象数据），内容需要自行填充
     * <p>
     * 不需要启动框架，可以本地单测脚本直接执行来生成csv文件
     * （也可以启动框架，在过程中生成csv）
     *
     * @param actual  -目标对象
     * @param csvPath -CSV文件路径
     */
    public static void genCsvFromObjData(Object actual, String csvPath) {
        if (csvPath == null) {
            String csvFolder =
                    TestifyPathConstants.OBJECT_DATA_PATH + actual.getClass().getSimpleName();
            File file = FileUtil.getTestResourceFile(csvFolder);
            if (!file.exists()) {
                file.mkdir();
            }
            csvPath = csvFolder + "/" + actual.getClass().getSimpleName() + ".csv";
        }
        CSVHelper.genObjCSVFileWithData(actual, csvPath);
    }

    /**
     * 根据对象生成CSV文件（包含对象数据），内容需要自行填充
     * <p>
     * 不需要启动框架，可以本地单测脚本直接执行来生成csv文件
     * （也可以启动框架，在过程中生成csv）
     *
     * @param virtualTable -表数据
     * @param csvPath      -CSV文件路径
     */
    public static void genCsvFromObjTable(VirtualTable virtualTable, String csvPath) {
        if (csvPath == null) {
            String csvFolder = TestifyPathConstants.DB_DATA_PATH + virtualTable.getTableName();
            File file = FileUtil.getTestResourceFile(csvFolder);
            if (!file.exists()) {
                file.mkdir();
            }
            csvPath = csvFolder + "/" + virtualTable.getTableName() + ".csv";
        }
        if (StringUtils.isBlank(csvPath)) {
            throw new TestifyException("路径为空，无法生成CSV文件" + csvPath);
        }

        File file = new File(csvPath);
        if (file.exists()) {
            throw new TestifyException("文件【" + csvPath + "】已经存在，请尝试输入新文件路径");
        }
        List<String[]> outputValues = new ArrayList<String[]>();
        // 组装CSV文件第一行，标题行
        List<String> header = new ArrayList<String>();
        header.add(CSVColEnum.COLUMN.getCode());
        header.add(CSVColEnum.FLAG.getCode());

        List<Map<String, Object>> mapList = virtualTable.getTableData();
        // 先设置有几列valueheander标题
        Map<String, List<Object>> valueMap = new LinkedCaseInsensitiveMap<>();
        for (int i = 0; i < mapList.size(); i++) {
            header.add(CSVColEnum.VALUE.getCode());
        }
        outputValues.add(header.toArray(new String[header.size()]));
        // 填充数据
        for (int i = 0; i < mapList.size(); i++) {
            for (Map.Entry<String, Object> singleMap : mapList.get(i).entrySet()) {
                List<Object> columValueList = new ArrayList<Object>();
                if (valueMap.containsKey(singleMap.getKey())) {
                    columValueList = valueMap.get(singleMap.getKey());
                }
                columValueList.add(singleMap.getValue());
                valueMap.put(singleMap.getKey(), columValueList);
            }
        }

        for (Map.Entry<String, List<Object>> columMap : valueMap.entrySet()) {
            List<String> list = new ArrayList<String>();
            list.add(columMap.getKey());
            if (!CollectionUtils.isEmpty(columMap.getValue())
                    && columMap.getValue().get(0) instanceof java.sql.Timestamp) {
                list.add(DBFlagConstant.N);
            } else {
                String flag = virtualTable.getFlagByFieldNameIgnoreCase(columMap.getKey());
                if (StringUtils.isNotEmpty(flag)) {
                    list.add(flag);
                } else {
                    list.add(DBFlagConstant.Y);
                }
            }
            for (Object value : columMap.getValue()) {
                list.add(String.valueOf(value));
            }
            outputValues.add(list.toArray(new String[list.size()]));
        }
        CSVHelper.writeToCsv(file, outputValues);
    }

    /**
     * 根据数据库表名生成数据库CSV文件，并生成到默认路径
     * 需要自定制路径或数据库，请调用同名另一方法
     * <p>
     * 第一列为对应列名，
     * 第二列为对应类型，
     * 第三列为注释
     * 第四列为校验所需标签类型
     * <p>
     * 不需要启动框架，会读取本地配置
     *
     * @param tableFullName -目标数据库表名
     * @param csvPath       -生成的csv文件地址
     * @param dbUrl         dburl
     */
    public static void genDBCSVFile(
            String tableFullName, String csvPath, String dbUrl, String userName, String password, String schema) {
        CSVHelper.genDBCSVFile(tableFullName, csvPath, dbUrl, userName, password, schema);
    }
}
