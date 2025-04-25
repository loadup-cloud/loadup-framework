/**

 * Copyright (c) 2004-2015 All Rights Reserved.
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

import com.github.loadup.components.testify.constant.TestifyPathConstants;
import com.github.loadup.components.testify.helper.CSVHelper;
import com.github.loadup.components.testify.util.CSVApisUtil;
import com.github.loadup.components.testify.util.FileUtil;

import java.io.File;
import java.util.Set;

/**
 * CSV文件操作入口
 *
 * 
 * 
 */
public class CSVApis {

    /**
     * 生成类中指定方法的入参、返回对象模版
     *
     * @param genRootPath
     * @param clsLoader
     * @param clsName
     * @param methodName
     * @param isResultOnly
     * @return
     * @throws ClassNotFoundException
     */
    public static Set<String> genCsvFromSpeciMethodByRootPath(String genRootPath,
                                                              ClassLoader clsLoader,
                                                              String clsName, String methodName,
                                                              boolean isResultOnly)
            throws ClassNotFoundException {

        return CSVApisUtil.paraClassSpeciMethodToCscFile(clsName, clsLoader, genRootPath,
                methodName, isResultOnly);
    }

    /**
     * 根据对象class和绝对路径生成CSV文件框架，内容需要自行填充
     * 不需要启动框架，可以本地单测脚本直接执行来生成csv文件
     *
     * @param objectClass -目标对象的class
     * @param csvPath     -CSV文件路径
     * @throws ClassNotFoundException
     */
    public static Set<String> genCsvFromObjClassByRootPath(String genRootPath,
                                                           ClassLoader clsLoader, String clsName)
            throws ClassNotFoundException {

        //暂存当前对象及嵌套对象
        return CSVApisUtil.paraClassToCscFile(clsName, clsLoader, genRootPath);
    }

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
            String csvFolder = TestifyPathConstants.OBJECT_DATA_PATH
                    + actual.getClass().getSimpleName();
            File file = FileUtil.getTestResourceFile(csvFolder);
            if (!file.exists())
                file.mkdir();
            csvPath = csvFolder + "/" + actual.getClass().getSimpleName() + ".csv";
        }
        CSVHelper.genObjCSVFileWithData(actual, csvPath);
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
     * @param tableName   -目标数据库表名
     * @param csvPath     -CSV文件路径
     * @param dbConfigKey -可选参数，默认OB时输入null即可
     */
    public static void genDBCsvFile(String tableFullName, String dbConfigKey) {
        CSVHelper.genDBCSVFile(tableFullName, dbConfigKey);
    }

    /**
     * 根据数据库表名生成数据校验对应格式CSV文件，
     * 第一列为对应表名，
     * 第二列为对应列名，
     * 第三列为注释
     * 第四列为校验所需标签类型
     * <p>
     * 不需要启动框架，可以本地单测脚本直接执行来生成csv文件，默认使用OceanBase来生成
     * (可以通过调整ObjectController.obFlag=false来设定为旧版oracle)
     *
     * @param tableName   -目标数据库表名
     * @param csvPath     -CSV文件路径
     * @param dbConfigKey -可选参数，默认OB时输入null即可
     */
    public static void genDBCsvFile(String tableFullName, String csvPath, String dbUrl,
                                    String userName, String password, String schema) {
        CSVHelper.genDBCSVFile(tableFullName, csvPath, dbUrl, userName, password, schema);
    }
}
