package com.github.loadup.components.testify.db.convertor;

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

import com.github.loadup.components.testify.cache.TestifyCacheData;
import com.github.loadup.components.testify.constant.TestifySpecialMapConstants;
import com.github.loadup.components.testify.db.enums.ColumnTypeEnum;
import com.github.loadup.components.testify.db.enums.DataBaseTypeEnum;
import com.github.loadup.components.testify.object.enums.UnitFlagEnum;
import com.github.loadup.components.testify.yaml.cpUnit.DataBaseCPUnit;
import com.github.loadup.components.testify.yaml.cpUnit.property.BaseUnitProperty;
import com.github.loadup.components.testify.yaml.enums.CheckPointActionEnum;
import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

/**
 * 数据转换sql接口
 *
 * 
 *
 */
public class DataRowConvertor {

    /**
     * 转换为sql
     *
     * @param dataRow
     * @return
     */
    public static String rowToSqL(DataBaseCPUnit unit, CheckPointActionEnum action,
                                  DataBaseTypeEnum dbType) {
//        Assert.assertNotNull("action不能为空", action);
//        Assert.assertNotNull("dbType不能为空", dbType);
        StringBuffer sb = new StringBuffer();
        switch (action) {
            case CHECK:
                sb.append("SELECT * FROM " + unit.getUnitName() + " WHERE "
                        + convertToWhere(unit, dbType));
                if (unit.getSpecialMap().containsKey(TestifySpecialMapConstants.ORDERBY)) {
                    sb.append(" " + unit.getSpecialMap().get(TestifySpecialMapConstants.ORDERBY));
                }
                break;
            case CLEAN:
                sb.append("DELETE FROM " + unit.getUnitName() + " WHERE "
                        + convertToWhere(unit, dbType));
                break;
            case PREPARE:
                String tableName = unit.getUnitName();
                List<String> Keys = new ArrayList<String>();
                List<String> Values = new ArrayList<String>();
                for (Entry<String, BaseUnitProperty> entry : unit.getModifyMap().entrySet()) {
                    String key = entry.getKey();
                    Keys.add(key);
                    BaseUnitProperty cell = entry.getValue();
                    Values.add(getDBValue(cell.getExpectValue(), tableName + "." + key,
                            cell.getDbColumnType(), cell.getFlagCode(), dbType));
                }

                String key = convertToString(Keys);
                String value = convertToString(Values);
                sb.append("INSERT INTO " + tableName + " (" + key + ") VALUES(" + value + ")");
                break;
            default:
                Assert.fail("不支持的action类型" + action);
        }

        return sb.toString();
    }

    /**
     * 生成where条件
     *
     * @param dataRow
     * @param dbType
     * @return
     */
    @SuppressWarnings({"rawtypes"})
    private static String convertToWhere(DataBaseCPUnit unit, DataBaseTypeEnum dbType) {
        StringBuffer sb = new StringBuffer();
        for (Iterator iterator = unit.getConditionKeys().iterator(); iterator.hasNext(); ) {
            String key = (String) iterator.next();
            BaseUnitProperty cell = unit.getModifyMap().get(key);
            String value = getDBValue(cell.getExpectValue(), unit.getUnitName() + "." + key,
                    cell.getDbColumnType(), cell.getFlagCode(), dbType);
            if (StringUtils.isNotBlank(value)) {
                if (sb.length() != 0) {
                    sb.append(" and ");
                }
                sb.append(key).append("=").append(value);
            }
        }
        return sb.toString();
    }

    /**
     * 生成数据库实际值
     *
     * @param value
     * @param keyPath
     * @param type
     * @param flagCode
     * @param dbType
     * @return
     */
    private static String getDBValue(Object value, String keyPath, String type, String flagCode,
                                     DataBaseTypeEnum dbType) {
        String realValue = "null";
        if (value != null) {
            switch (ColumnTypeEnum.getByPrefix(type)) {
                case INT:
                case NUMERIC:
                    realValue = "" + value;
                    break;
                case NULL:
                    if (UnitFlagEnum.getByCode(flagCode) == null
                            && TestifyCacheData.getCustomGenerator(flagCode) != null) {
                        //生成自定义flagCode数值
                        realValue = (String) TestifyCacheData.getCustomGenerator(flagCode).generater(
                                keyPath, value, type);
                    } else {
                        Assert.fail("找不到合适的生成回调函数");
                    }
                    break;
                case TIMESTAMP:
                    if (UnitFlagEnum.getByCode(flagCode) != UnitFlagEnum.D) {
                        if (StringUtils.isNotBlank("" + value)) {
                            realValue = "'" + value + "'";
                        } else {
                            realValue = null;
                        }
                    } else if (value instanceof String) {
                        String dateValue = (String) value;
                        if (dateValue.startsWith("today")) {
                            switch (dbType) {
                                case MYSQL:
                                    realValue = "now()";
                                    break;
                                default:
                                    Assert.fail("不支持的DB类型" + dbType);
                            }
                        }
                    }
                    break;
                case VARCHAR:
                    if (StringUtils.isNotBlank("" + value)) {
                        realValue = "'" + value + "'";
                    } else {
                        realValue = null;
                    }
                    break;
                default:
                    Assert.fail("这里不能进入");
            }
        }
        return realValue;
    }

    //列表转换成字符串
    private static String convertToString(List<String> list) {
        String str = "";
        if (list != null && list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                str += list.get(i) + ",";
            }
        }
        str = str.substring(0, str.length() - 1);
        return str;
    }
}
