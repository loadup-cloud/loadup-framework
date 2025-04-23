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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.alipay.acts.utils.file.CsvUtils;
import com.alipay.acts.utils.file.JsonUtils;
import com.alipay.acts.utils.file.YamlUtils;
import com.alipay.test.acts.component.db.DBDatasProcessor;
import com.alipay.test.acts.constants.DBFlagConstant;
import com.alipay.test.acts.model.VirtualTable;
import com.alipay.test.acts.template.ActsTestBase;
import com.alipay.test.acts.util.FileUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 预跑反填工具包括结果、db等，可以直接通过工具类进行预跑反填
 *
 * @author jinsai.chen
 * @version : DbBackFillUtil.java, v 0.1 2021年03月04日 上午10:54 jinsai.chen Exp $
 */
@Component
public class BackFillUtil {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(BackFillUtil.class);

    /**
     * 数据处理
     */
    public static DBDatasProcessor dbDatasProcessor = ActsTestBase.dbDatasProcessor;

    /**
     * 对象的预跑反填工具
     *
     * @param object   对象
     * @param filePath 生成文件的路径地址
     * @param fileType json、yaml
     */
    public static void doObjectBackFill(Object object, String filePath, String fileType) {
        String data = "";
        if (StringUtils.equals(fileType, "json")) {
            data = JsonUtils.toJson(object);
        }

        if (StringUtils.equals(fileType, "yaml")) {
            data = YamlUtils.dump(object);
        }
        try {
            FileUtils.writeStringToFile(new File(filePath), data);
        } catch (Exception e) {
//            LOGGER.error(e);
        }
    }

    /**
     * db的预跑反填工具
     *
     * @param sql      sql
     * @param filePath 生成文件的路径地址,只支持csv文件
     */
    public static void doSingleDbBackFill(String sql, String filePath) {
        String tableName = StringUtils.substringBetween(
                StringUtils.toRootLowerCase(sql),
                "from", "where").trim();
        LOGGER.info(String.format("ACTS:查询sql= %s", sql));
        List<Map<String, Object>> tableList = dbDatasProcessor.queryForList(tableName, sql);
        if (CollectionUtils.isEmpty(tableList)) {
            LOGGER.info(String.format("ACTS:该表%s没有查询到结果", tableName));
        }
        VirtualTable virtualTable = new VirtualTable();
        virtualTable.setTableName(tableName);
        virtualTable.setTableData(tableList);
        Map<String, String> flags = new HashMap<String, String>();
        for (Map.Entry<String, Object> map : tableList.get(0).entrySet()) {
            flags.put(map.getKey(), DBFlagConstant.Y);
        }
        virtualTable.setFlags(flags);
        CsvUtils.genCsvFromObjTable(virtualTable, filePath);
    }

    /**
     * 多条dbsql的预跑反填工具
     *
     * @param sqlList    sql
     * @param folderPath 生成文件的路径地址,只支持csv文件
     */
    public static void doDbBackFill(List<String> sqlList, String folderPath) {
        if (CollectionUtils.isEmpty(sqlList)) {
            return;
        }

        for (String sql : sqlList) {
            String tableName = StringUtils.substringBetween(
                    StringUtils.toRootLowerCase(sql),
                    "from", "where").trim();
            doSingleDbBackFill(sql, folderPath + "/" + tableName + ".csv");
        }
    }

}
