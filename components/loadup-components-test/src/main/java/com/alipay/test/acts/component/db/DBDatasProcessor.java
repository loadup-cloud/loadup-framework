package com.alipay.test.acts.component.db;

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

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alipay.acts.utils.check.ObjectCompareUtil;
import com.alipay.test.acts.annotation.IVTableGroupCmdMethod;
import com.alipay.test.acts.config.DataAccessConfig;
import com.alipay.test.acts.config.DataAccessConfigManager;
import com.alipay.test.acts.constant.ActsConstants;
import com.alipay.test.acts.db.enums.DBFlagEnum;
import com.alipay.test.acts.driver.ActsConfiguration;
import com.alipay.test.acts.exception.ActsTestException;
import com.alipay.test.acts.model.VirtualTable;
import com.alipay.test.acts.runtime.ActsRuntimeContextThreadHold;
import com.alipay.test.acts.runtime.ComponentsActsRuntimeContextThreadHold;
import com.alipay.test.acts.util.LogUtil;
import com.alipay.test.acts.util.VelocityUtil;
import com.alipay.test.acts.utils.DetailCollectUtils;
import com.alipay.test.acts.utils.config.ConfigrationFactory;
import com.alipay.test.acts.utils.config.ConfigurationKey;
import com.github.loadup.commons.util.context.ApplicationContextUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DBDatasProcessor {

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(DBDatasProcessor.class);

    protected Map<String, JdbcTemplate> jdbcTemplateMap;

    protected DataAccessConfigManager dataAccessConfigManager;

    /**
     * before table do something
     */
    private final List<IVTableGroupCmdMethod> beforeVTableExecuteMethodList = new LinkedList<IVTableGroupCmdMethod>();

    /**
     * after table do something
     */
    private final List<IVTableGroupCmdMethod> afterVTableExecuteMethodList = new LinkedList<IVTableGroupCmdMethod>();

    /**
     * before exec sql do something
     */
    private final List<IVTableGroupCmdMethod> beforeExecSqlMethodList = new LinkedList<IVTableGroupCmdMethod>();

    /**
     * special groupid
     */
    private final List<String> specialGroupIds = new ArrayList<String>();

    public void initDataSource() {
//        dataAccessConfigManager =null;// (DataAccessConfigManager) BundleBeansManager.getBean(
////            "dataAccessConfigManager", DataAccessConfigManager.class,
////            null);
//
//        String[] bundleNames = ConfigrationFactory.getConfigration()
//            .getPropertyValue("datasource_bundle_name").split(";");
//
//        ConfigrationFactory.loadFromConfig("config/dbConf/"
//                                           + ConfigrationFactory.getSofaConfig("dbmode")
//                                           + "db.conf");
//        Map<String, String> configs = ConfigrationFactory.getConfigration().getConfig();
//        if (configs == null) {
//            Assert
//                .assertTrue(
//                    false,
//                    "datasource config not exist, add config in [acts-config.properties] ,modify datasource config starting with [ds_]");
//
//        }
//        for (String key : configs.keySet()) {
//            if (key.startsWith("ds_")) {
//                String dsName = key.replace("ds_", "");
//                List<String> tables = new ArrayList<String>();
//                for (String tableName : configs.get(key).split(",")) {
//                    tables.add(tableName);
//                }
//
//                DataSource ds = null;
//                //先尝试从db.conf里读取
//                if (configs.containsKey(dsName + "_url")) {
//                    String db_url = configs.get(dsName + "_url");
//                    String db_username = configs.get(dsName + "_username");
//                    String db_password = configs.get(dsName + "_password");
//                    BasicDataSource datasource = new BasicDataSource();
//                    String type = getDBType(db_url);
//
//                    try {
//                        if (type.equalsIgnoreCase("oracle")) {
//                            Class.forName("oracle.jdbc.OracleDriver");
//                            datasource.setDriverClassName("oracle.jdbc.OracleDriver");
//                        } else {
//                            Class.forName("com.mysql.jdbc.Driver");
//                            datasource.setDriverClassName("com.mysql.jdbc.Driver");
//                        }
//                        datasource.setUrl(db_url);
//                        datasource.setUsername(db_username);
//                        datasource.setPassword(db_password);
//                        ds = datasource;
//                    } catch (Exception e) {
//                        throw new ActsTestException("Failed obtaining datasource:" + dsName, e);
//                    }
//
//                } else {
//                    // 尝试从多个bundle中获取数据源bean
//                    for (String bundleName : bundleNames) {
//                        ds = null;//((DataSource) BundleBeansManager.getBean(dsName, bundleName));
//                        if (ds != null) {
//                            break;
//                        }
//                    }
//                    if (ds == null) {
//                        //默认on-the-fly-bundle
//                        ds = null;//((DataSource) BundleBeansManager.getBean(dsName, null));
//                    }
//                }
//                if (ds == null) {
//                    Assert.assertTrue(false, "Failed obtaining datasource bean:[" + dsName
//                                             + "] from the specified bundleName: " + Arrays.toString(bundleNames) + "，"
//                            + "未能从对应bundle中获取数据源bean，请确认acts-config.properties文件中的bundleName/beanName是否配置正确");
//                }
//
//                JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);
//                if (jdbcTemplateMap == null) {
//                    jdbcTemplateMap = new HashMap<String, JdbcTemplate>();
//                }
//                jdbcTemplateMap.put(dsName, jdbcTemplate);
//                DataAccessConfigManager.dataSourceMap.put(dsName, tables);
//            }
//
//        }
        if (jdbcTemplateMap == null) {
            jdbcTemplateMap = new HashMap<String, JdbcTemplate>();
        }
        JdbcTemplate jdbcTemplate = ApplicationContextUtils.getBean("jdbcTemplate", JdbcTemplate.class);
        jdbcTemplateMap.put("dataSource", jdbcTemplate);

    }

    /**
     * User-defined table-datasource Map
     * extMapInfo:Map<tableName,dataSourceName>
     * Only extra zdal datasource suppported for the moment
     * direct connection config not supported
     */
    public void updateDataSource(Map<String, String> extMapInfo) {
        if (extMapInfo == null) {
            return;
        }
        DataAccessConfigManager.clearExtDataSourceMap();
        for (Entry<String, String> entry : extMapInfo.entrySet()) {
            String dataSourceName = "datasource_bean_name_" + entry.getValue();
            JdbcTemplate jdbcTemplate = ActsDBUtils.getJdbcTemplate(entry.getKey(),
                    entry.getValue());
            jdbcTemplateMap.put(dataSourceName, jdbcTemplate);
            DataAccessConfigManager.updateExtDataSourceMap(dataSourceName, entry.getKey());
        }
    }

    @SuppressWarnings("rawtypes")
    public boolean importDepDBDatas(List<VirtualTable> depTables, String... groupIds) {
        if (depTables == null || depTables.isEmpty()) {
            return true;
        }

        List<VirtualTable> tables = reorderTables(depTables);

        //根据DO类属性前缀动态拼装SQL 并执行
        for (int i = 0; i < tables.size(); i++) {

            VirtualTable table = tables.get(i);
            if (table == null) {
                continue;
            }
            if ((ArrayUtils.isEmpty(groupIds) && StringUtils.isEmpty(table.getNodeGroup()))
                    || ArrayUtils.contains(groupIds, table.getNodeGroup())) {
                List<String> fields = new ArrayList(table.getTableData().get(0).keySet());

                String sql = genInsertSql(table, table.getTableName(), fields, null);
                DetailCollectUtils.appendDetail(sql);

                //表执行前执行
                Execute(beforeVTableExecuteMethodList, table.getTableName(), table.getNodeGroup());

                // 替换相互引用的变量值
                ActsRuntimeContextThreadHold.getContext().refreshSingleTableParam(table);

                //组装参数执行SQL
                doImport(table, sql, fields);

                //更新变量map
                doSelectAndUpdateParams(table);

                //表执行后执行
                Execute(afterVTableExecuteMethodList, table.getTableName(), table.getNodeGroup());
            }
        }
        return true;
    }

    /**
     * 捞取准备数据并保存需要赋值的变量
     *
     * @param table
     */
    protected void doSelectAndUpdateParams(VirtualTable table) {
        if (null == table) {
            return;
        }

        // 逐行捞取赋值
        for (Map<String, Object> rowData : table.getTableData()) {
            // <fieldName, paramName>
            Map<String, String> fieldParams = new HashMap<>();

            for (String key : rowData.keySet()) {
                String val = String.valueOf(rowData.get(key));
                if (val.startsWith("=")
                        && table.getFlagByFieldNameIgnoreCase(key).equalsIgnoreCase("N")) {
                    fieldParams.put(key, val.replace("=", ""));
                }
            }

            if (fieldParams.size() <= 0) {
                // 该条数据无待赋值的变量
                continue;
            }

            Map<String, Object> dbRowResult = doSelect(table, rowData).get(0);

            for (String field : fieldParams.keySet()) {
                ActsRuntimeContextThreadHold.getContext().addOneParam(fieldParams.get(field), dbRowResult.get(field));
            }
        }
    }

    /**
     * 根据C/CN标签字段捞取对应数据
     *
     * @param table
     * @param rowData
     * @return
     */
    protected List<Map<String, Object>> doSelect(VirtualTable table, Map<String, Object> rowData) {
        List<String> fields = new ArrayList(table.getTableData().get(0).keySet());
        String[] selectKeys = getSelectKeys(table, fields);
        String sql = genSelectSql(table, fields);
        Object[] valsOfArgs = getValsForSelectKeys(selectKeys, rowData, table.getTableName());

        List<Map<String, Object>> dbRowsResult = getJdbcTemplate(table.getTableName()).queryForList(sql, valsOfArgs);
        return dbRowsResult;
    }

    public boolean importDepDBDatasWithDao(List<VirtualTable> depTables) {
        //TODO: DAO的方式
        //        if (CollectionUtils.isEmpty(depTables)) {
        //            return true;
        //        }
        //
        //        //根据DO类属性前缀动态拼装SQL 并执行
        //        for (int i = 0; i < depTables.size(); i++) {
        //
        //            //获取数据表
        //            VirtualTable table = depTables.get(i);
        //            //获取数据表对应的DAO类
        //            String dataObjClassName = table.getDataObjClazz();
        //            Object obj = dataAccessConfigManager.getDAO(dataObjClassName);
        //            Method method = ObjectUtil.findMethod("insert", obj);
        //
        //            //获取数据表记录
        //            rows = table.getTableData();
        //            for (int rowNum = 0; rowNum < rows.size(); rowNum++) {
        //                try {
        //                    method.invoke(obj, table.getRow(rowNum));
        //                } catch (Exception e) {
        //                    LOGGER.error(e.getMessage(), e);
        //                }
        //            }
        //        }
        return true;
    }

    @SuppressWarnings("rawtypes")
    public void deleteDBDatasWithDao(List<VirtualTable> depTables) {
        //TODO  DAO的方式
        //        if (CollectionUtils.isEmpty(depTables)) {
        //            return;
        //        }
        //
        //        //根据DO类属性前缀动态拼装SQL 并执行
        //        for (int i = 0; i < depTables.size(); i++) {
        //
        //            //获取数据表
        //            VirtualTable table = depTables.get(i);
        //            //获取数据表对应的DAO类
        //            String dataAccessObjClassName = table.getDataObjClazz();
        //            Object obj = dataAccessConfigManager.getDAO(dataAccessObjClassName);
        //            Method method = ObjectUtil.findMethod("delete", obj);
        //
        //            //获取数据表记录
        //            JSONArray rows = table.getAllRows();
        //            for (int rowNum = 0; rowNum < rows.size(); rowNum++) {
        //                try {
        //                    method.invoke(obj, table.getRow(rowNum));
        //                } catch (Exception e) {
        //                    LOGGER.error(e.getMessage(), e);
        //                }
        //            }
        //        }
    }

    /**
     * 生成SQL模板
     *
     * @param table
     * @param tableName
     * @param fields
     * @param row
     * @return
     */
    protected String genInsertSql(VirtualTable table, String tableName, List<String> fields,
                                  Map<String, Object> row) {
        StringBuffer fieldBuffer = new StringBuffer();
        StringBuffer fieldPlaceholderBuffer = new StringBuffer();

        for (String field : fields) {
            Object val = (null != row) ? row.get(field) : null;

            //获取flag兼容大小写不一致问题
            String currentFlag = table.getFlags().get(field);
            if (StringUtils.isBlank(currentFlag)) {
                currentFlag = table.getFlags().get(field.toLowerCase());
                if (StringUtils.isBlank(currentFlag)) {
                    currentFlag = table.getFlags().get(field.toUpperCase());
                }
            }

            if (currentFlag != null) {

                // 多行数据不同flag的场景
                if (currentFlag.equalsIgnoreCase(DBFlagEnum.M.name())) {
                    // 需要有指定行数据再进行组装insert语句（因为每行的flag有差异）
                    if (row == null || val == null) {
                        return null;
                    }

                    String[] sl = ((String) val).split("\\|", 2);
                    if (sl.length == 2) {
                        // 重组实际flag和值
                        currentFlag = sl[0];
                        val = sl[1];
                    } else if (sl.length == 1) {
                        // 仅存在flag, 一般是N flag的场景
                        currentFlag = sl[0];
                        val = null;
                    } else {
                        throw new ActsTestException(
                                "[M_Flag]字段对应值解析失败，请保证M flag的字段内容格式为 {flag}|{value}，如Y|actual_cotent");
                    }

                }

                if (currentFlag.equalsIgnoreCase(DBFlagEnum.F.name())) {
                    if (row == null || val == null) {
                        return null;
                    }
                    //系统函数原样写入sql
                    fieldBuffer.append(field).append(",");
                    fieldPlaceholderBuffer.append(val).append(",");
                } else if (!currentFlag.equalsIgnoreCase(DBFlagEnum.N.name())) {
                    fieldBuffer.append(field).append(",");
                    fieldPlaceholderBuffer.append("?").append(",");
                }

            } else {
                fieldBuffer.append(field).append(",");
                fieldPlaceholderBuffer.append("?").append(",");
            }

        }

        String fieldPart = "insert into " + tableName + " ("
                + fieldBuffer.substring(0, fieldBuffer.length() - 1) + ") ";

        String placeholderPart = " ("
                + fieldPlaceholderBuffer.substring(0,
                fieldPlaceholderBuffer.length() - 1) + ") ";
        return fieldPart + "values" + placeholderPart;
    }

    /**
     * 执行SQL导入数据
     *
     * @param table
     * @param fields
     * @param sql
     * @param fields
     */
    protected void doImport(VirtualTable table, String sql, List<String> fields) {
        List<Map<String, Object>> rows = table.getTableData();
        if (rows == null || rows.isEmpty()) {
            return;
        }

        for (int rowNum = 0; rowNum < rows.size(); rowNum++) {
            String insertSql = sql;
            Map<String, Object> rowData = rows.get(rowNum);
            List<Object> args = new ArrayList<Object>();
            if (insertSql == null) {
                //SQL为空，代表每行都需要拼接一次
                insertSql = genInsertSql(table, table.getTableName(), fields, rows.get(rowNum));
                if (insertSql == null) {
                    throw new ActsTestException(
                            "tagged with F，but no [function name] in [value]但是值里面没有对应的函数名");
                }
            }

            // 组装args
            for (String field : fields) {
                Object arg = rowData.get(field);
                if (table.getFlags() != null && table.getFlags().get(field) != null) {
                    String flag = table.getFlags().get(field);

                    // 多行数据不同flag的场景
                    if (flag.equalsIgnoreCase(DBFlagEnum.M.name())) {
                        String data = (String) arg;
                        String[] sl = data.split("\\|", 2);
                        // 直接赋值，如果分割不了前面genInsertSql就跪了
                        if (sl.length == 1) {
                            // 仅存在flag, 一般是N flag的场景
                            flag = sl[0];
                            arg = null;
                        } else if (sl.length == 2) {
                            flag = sl[0];
                            arg = sl[1];
                        }
                    }

                    // 这一坨前人留下的 !条件组合 以后有机会再改吧。。。
                    if (!flag.equalsIgnoreCase(DBFlagEnum.N.name()) && !flag.equalsIgnoreCase(DBFlagEnum.F.name())
                            && !flag.equalsIgnoreCase(DBFlagEnum.L.name())) {
                        //F是系统函数，拼接的时候已经放入sql,L☞换行大字段，单独处理
                        if (StringUtils.isBlank(arg.toString()) || StringUtils.equals(arg.toString(), "null")) {
                            args.add(null);
                        } else {
                            args.add(arg);
                        }
                    } else if (flag.equalsIgnoreCase(DBFlagEnum.L.name())) {
                        String fieldValue = "";// com.alipay.common.lang.PropertiesUtil.convert2String(
//                            transStringToMap((String) arg), false);
                        args.add(fieldValue);
                    }
                } else {
                    args.add(arg);
                }

            }
            //执行SQL
            getJdbcTemplate(table.getTableName()).update(insertSql, args.toArray());
            String message = "";
            for (Object arg : args) {
                message += arg + ",";
            }
            DetailCollectUtils.appendAndLog(
                    "Executing sql:" + insertSql + ",parameters:" + message, LOGGER);
        }
    }

    public void cleanDBDatas(List<VirtualTable> depTables, String... groupIds) {

        if (depTables == null || depTables.isEmpty()) {
            return;
        }

        Map<String, Integer> tableRows = new HashMap<>();
        for (VirtualTable virtualTable : depTables) {
            String tableName = virtualTable.getTableName();

            if (tableRows.containsKey(tableName)) {
                tableRows.put(tableName, tableRows.get(tableName) + virtualTable.getRecordNum());
            } else {
                tableRows.put(tableName, virtualTable.getRecordNum());
            }
        }

        for (int i = 0; i < depTables.size(); i++) {

            //生成SQL
            //根据DO类属性前缀动态拼装SQL 并执行
            VirtualTable table = depTables.get(i);
            if (table == null) {
                continue;
            }
            if ((ArrayUtils.isEmpty(groupIds) && StringUtils.isEmpty(table.getNodeGroup()))
                    || ArrayUtils.contains(groupIds, table.getNodeGroup())) {
                List<String> fields = new ArrayList(table.getTableData().get(0).keySet());

                //拼装SQL
                String sql = genDeleteSql(table, fields);

                //表执行前执行
                Execute(beforeVTableExecuteMethodList, table.getTableName(), table.getNodeGroup());
                //组装参数执行SQL
                doDelete(table, sql, fields, tableRows.get(table.getTableName()));

                //表执行之后执行
                Execute(afterVTableExecuteMethodList, table.getTableName(), table.getNodeGroup());
            }
        }

    }

    /**
     * 查询批量结果
     *
     * @param tableName
     * @param sql
     * @return
     */
    public List<Map<String, Object>> queryForList(String tableName, String sql) {
        return getJdbcTemplate(tableName).queryForList(sql);
    }

    /**
     * 生成SQL模板
     *
     * @param table
     * @param fields
     * @return
     */
    protected String genDeleteSql(VirtualTable table, List<String> fields) {

        String tableName = table.getTableName();

        String prefixPart = "delete from " + tableName + " where ";

        String wherePart = "";

        String[] selectKeys = getSelectKeys(table, fields);
        for (String key : selectKeys) {
            wherePart = wherePart + " and " + key + " = ?";
        }

        return prefixPart + StringUtils.substringAfter(wherePart, "and ");
    }

    protected void doDelete(VirtualTable table, String sql, List<String> fields, Integer limits) {
        List<Map<String, Object>> rows = table.getTableData();
        String[] keys = getSelectKeys(table, fields);
        Map<String, Object> paramBuffer = ActsRuntimeContextThreadHold.getContext().paramMap;

        for (int rowNum = 0; rowNum < rows.size(); rowNum++) {
            Map<String, Object> rowData = rows.get(rowNum);
            Object[] args = new Object[keys.length];
            int i = 0;
            for (; i < args.length; ++i) {
                if (String.valueOf(rowData.get(keys[i])).startsWith("=")) {
                    throw new ActsTestException("tableName " + table.getTableName()
                            + " condition: " + keys[i]
                            + "cannot be assignment statement for deleting");
                }
                if (String.valueOf(rowData.get(keys[i])).startsWith("$")) {
                    String key = String.valueOf(rowData.get(keys[i])).replace("$", "");
                    if (paramBuffer.containsKey(key)) {
                        args[i] = paramBuffer.get(key);
                    } else {
                        //没有找到变量就跳出
                        break;
                    }
                } else {
                    args[i] = rowData.get(keys[i]);
                }
            }
            if (i < args.length) {
                //这里如果删除的时候参数不足，则不进行删除，执行下一条语句
                continue;
            }

            String argVals = "";
            for (Object arg : args) {
                argVals += arg + ",";
            }

            // 删除熔断机制：先捞一把确保不会有额外的其他数据被意外删除
            String checkBeforeDel = ConfigrationFactory.getConfigration().
                    getPropertyValue(ConfigurationKey.DB_DEL_LIMITATION.getCode(), "true");
            if (StringUtils.equalsIgnoreCase(checkBeforeDel.trim(), Boolean.TRUE.toString())) {
                List dataList = getJdbcTemplate(table.getTableName()).queryForList(sql.replaceFirst("delete ", "select * "), args);
                if (dataList.size() > limits) {
                    String errMsg = "Executing sql:" + sql + ",parameters:" + argVals;
                    errMsg += "\n" + "Actual deleted rows: " + dataList.size() + "; Expected deleted rows: " + table.getTableData().size();
                    errMsg += "\nPlease check fields with C/CN flag, make sure the scope of selected data is not out of your controll!";
                    DetailCollectUtils.appendAndLog(errMsg, LOGGER);
                    LOGGER.error(errMsg);
                    return;
                }
            }

            //执行删除SQL
            getJdbcTemplate(table.getTableName()).update(sql, args);
            DetailCollectUtils.appendAndLog("Executing sql:" + sql + ",parameters:" + argVals,
                    LOGGER);
            LOGGER.info("Executing sql:" + sql + ",parameters:" + argVals);

        }
    }

    public void compare2DBDatas(List<VirtualTable> virtualTables, String... groupIds) {

        boolean isAllTableSame = true;
        StringBuilder allErr = new StringBuilder();

        List<VirtualTable> tables = reorderTables(virtualTables);

        for (int i = 0; i < tables.size(); i++) {
            // 每张待校验表做数据提取和校验
            VirtualTable table = tables.get(i);
            if ((ArrayUtils.isEmpty(groupIds) && StringUtils.isEmpty(table.getNodeGroup()))
                    || ArrayUtils.contains(groupIds, table.getNodeGroup())) {
                // 提取数据表的字段名
                List<String> fields = new ArrayList(table.getTableData().get(0).keySet());

                //拼装SQL
                String sql = genSelectSql(table, fields);
                DetailCollectUtils.appendDetail(sql);

                //表执行前执行
                Execute(beforeVTableExecuteMethodList, table.getTableName(), table.getNodeGroup());
                //组装参数执行SQL
                boolean isTableSame = doSelectAndCompare(table, fields, sql, allErr);

                //表执行后执行
                Execute(afterVTableExecuteMethodList, table.getTableName(), table.getNodeGroup());

                if (!isTableSame) {
                    isAllTableSame = false;
                }
            }
        }

        if (!isAllTableSame) {
            throw new AssertionError("[CheckFailed]Failed checking DB!\n" + allErr);
        }

    }

    /**
     * 考虑到表数据内的自定义变量的取值(=val)和赋值($val)，
     * 对表数据处理顺序进行排序，确保对应变量取值先于赋值，
     * 否则赋值时还未取值会导致变量数据缺失
     *
     * @param virtualTables
     * @return
     */
    protected List<VirtualTable> reorderTables(List<VirtualTable> virtualTables) {
        List<VirtualTable> unsortedTables = new ArrayList<VirtualTable>();
        unsortedTables.addAll(virtualTables);

        List<VirtualTable> sortedTables = new ArrayList<VirtualTable>();
        // 记录有效变量值
        List<String> availableVals = new ArrayList<String>();

        // 防止死循环
        int deadLoopControll = virtualTables.size() * 3;
        while (unsortedTables.size() != 0 && sortedTables.size() != virtualTables.size()) {
            VirtualTable table = unsortedTables.remove(0);

            // 该表数据可生产的变量，可从真实数据中提取
            List<String> produceVals = new ArrayList<String>();
            // 该表数据需消费的变量，必须提前存在
            List<String> consumeVals = new ArrayList<String>();

            // 尝试提取内部变量，用作稍后排序
            for (Map<String, Object> rowData : table.getTableData()) {
                for (Object fieldData : rowData.values()) {

                    if (String.valueOf(fieldData).startsWith("$")) {
                        // 消费变量（引用先前读取/生产出来的变量）
                        consumeVals.add(String.valueOf(fieldData).substring(1));
                    } else if (String.valueOf(fieldData).startsWith("=")) {
                        // 生产变量（从真实数据中读取出来作为变量供后续使用）
                        produceVals.add(String.valueOf(fieldData).substring(1));
                    }
                }
            }

            // 确保需依赖的消费变量已存在
            boolean allAvailable = true;
            for (String val : consumeVals) {
                if (!availableVals.contains(val)) {

                    // 熔断检测，如遇无法解析的变量原封返回，待后续处理抛出异常
                    deadLoopControll -= 1;
                    if (deadLoopControll < 1) {
                        // 触发熔断
                        LOGGER.error("[UnsolvableError]校验表" + table + "中包含无法解析的变量值：" + consumeVals.toString());
                        return virtualTables;
                    }

                    // 如果当前可用变量列表内未存在本次需消费的变量，则放置队尾
                    unsortedTables.add(table);
                    allAvailable = false;
                    break;
                }
            }

            if (allAvailable) {
                sortedTables.add(table);
                // 增加本次生产的变量，供后续参考消费
                availableVals.addAll(produceVals);
            }
        }


        return sortedTables;
    }

    protected String genSelectSql(VirtualTable table, List<String> fields) {

        String tableName = table.getTableName();

        //生成查询列
        StringBuffer queryCoulumsBuffer = new StringBuffer();

        for (String field : fields) {
            //拼装查询列部分
            queryCoulumsBuffer.append(field + " as " + field).append(",");
        }

        String queryCoulums = queryCoulumsBuffer.substring(0, queryCoulumsBuffer.length() - 1);

        //生成查询条件 （先只支持默认查询条件）
        StringBuffer wherePartBuffer = new StringBuffer();
        String[] selectKeys = getSelectKeys(table, fields);

        for (String selectKey : selectKeys) {
            //                wherePartBuffer.append(convertFieldName2ColumnName(selectKey) + " = ? and ");
            wherePartBuffer.append("(" + selectKey + " = ? " + " ) and ");
        }

        String wherePart = wherePartBuffer.substring(0, wherePartBuffer.length() - 4);

        return "select " + queryCoulums + " from " + tableName + " where " + wherePart;
    }

    protected boolean doSelectAndCompare(VirtualTable table, List<String> fields, String sql,
                                         StringBuilder allErr) {
        // 提取待校验的多条表数据
        List<Map<String, Object>> rows = table.getTableData();
        String[] selectKeys = getSelectKeys(table, fields);

        boolean isSame = true;
        StringBuilder err = new StringBuilder();
        Map<String, Object> paramBuffer = ComponentsActsRuntimeContextThreadHold.getContext().paramMap;

        // 是否期望 对应表数据不存在
        boolean assertEmpty = willAssertEmpty(table, fields);

        // 针对每行预期表数据，进行捞取比对
        for (int rowNum = 0; rowNum < rows.size(); rowNum++) {
            Map<String, Object> rowData = rows.get(rowNum);
            if (rowData == null) {
                continue;
            }

            // select 语句中占位符对应的真实值
            Object[] valsOfArgs = getValsForSelectKeys(selectKeys, rowData, table.getTableName());

            // 执行sql后保存的多行结果
            List<Map<String, Object>> dbRowsResult = null;
            try {
                // 执行select获取真实db数据
                dbRowsResult = getJdbcTemplate(table.getTableName()).queryForList(sql, valsOfArgs);
                String message = "";
                for (Object arg : valsOfArgs) {
                    message += arg + ",";
                }
                LOGGER.info("Executing sql:" + sql + ",parameters:" + message);
                if (assertEmpty && dbRowsResult.size() > 0) {
                    isSame = false;
                    err.append("Failed checking db, tableName" + table.getTableName()
                            + "has unexpected data " + dbRowsResult + "\n");
                } else if (!assertEmpty && dbRowsResult.size() <= 0) {
                    isSame = false;
                    err.append("Failed checking DB, tableName: " + table.getTableName()
                            + " does not contains data: " + rowData + "\n");
                }

            } catch (Exception e) {
                isSame = false;
                throw new ActsTestException("sql="
                        + sql
                        + ",args="
                        + ToStringBuilder.reflectionToString(valsOfArgs,
                        ToStringStyle.SHORT_PREFIX_STYLE), e);
            }

            // 开启表数据比对流程
            for (Map<String, Object> dbOneRowResult : dbRowsResult) {
                //多个结果要求所有结果都满足期望
                Iterator<String> iterator = rowData.keySet().iterator();
                // 逐个字段比对，前辈们在这个while里精心写了200多行if-else，走读代码请做好心理准备
                while (iterator.hasNext()) {
                    String fieldName = iterator.next();
                    Object expectedFieldValue = rowData.get(fieldName);
                    Object actualFieldValue = dbOneRowResult.get(fieldName);

                    //【提取变量值】先处理变量赋值，把原先数据表期望中定义的变量，加入到上下文参数列表中
                    if (String.valueOf(expectedFieldValue).startsWith("=")) {
                        paramBuffer.put(String.valueOf(expectedFieldValue).replace("=", ""),
                                actualFieldValue);
                        continue;
                    }

                    // 统一使用velocity做变量替换
                    String expectedFieldValueStr = String.valueOf(expectedFieldValue);
                    if (expectedFieldValueStr.indexOf("$") != -1) {
                        String parsedValue = VelocityUtil.evaluateString(paramBuffer,
                                expectedFieldValueStr);
                        if (parsedValue.indexOf("$") != -1) { // 说明还有无法解析的变量，也可能是真实值含$
                            LOGGER.error("[PARAM_WARNING]unable to resolve param in value [" + parsedValue
                                    + "] from " + table.getTableName() + "." + fieldName
                                    + ", will process the value as a plain string.");
                        }
                        expectedFieldValue = parsedValue;
                    }

                    //获取当前flag 兼容大小写问题
                    String currentFlag = table.getFlagByFieldNameIgnoreCase(fieldName);

                    if (currentFlag != null) {

                        // 多行数据不同flag的场景
                        if (currentFlag.equalsIgnoreCase(DBFlagEnum.M.name())) {

                            String[] sl = ((String) expectedFieldValue).split("\\|", 2);
                            if (sl.length == 2) {
                                // 重组实际flag和值
                                currentFlag = sl[0];
                                expectedFieldValue = sl[1];
                            } else if (sl.length == 1) {
                                // 仅存在flag, 一般是N flag的场景
                                currentFlag = sl[0];
                                expectedFieldValue = null;
                            } else {
                                throw new ActsTestException(
                                        "[M_Flag]字段对应值解析失败，请保证M flag的字段内容格式为 {flag}|{value}，如Y|actual_cotent");
                            }

                        }


                        if (currentFlag.equalsIgnoreCase(DBFlagEnum.N.name())) {
                            continue;
                        } else if (currentFlag.startsWith(DBFlagEnum.D.name())) {
                            //日期的逻辑
                            long timeFlow = Long.valueOf(currentFlag.replace(DBFlagEnum.D.name(), ""));
                            Date real = null;
                            Date expect = null;
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            try {
                                if (actualFieldValue instanceof Date) {
                                    real = (Date) actualFieldValue;
                                } else {
                                    real = sdf.parse((String) actualFieldValue);
                                }
                                if (expectedFieldValue instanceof Date) {
                                    expect = (Date) expectedFieldValue;
                                } else if (((String) expectedFieldValue).equalsIgnoreCase("now()")) {
                                    expect = new Date();
                                } else if (((String) expectedFieldValue).equalsIgnoreCase("today")) {
                                    expect = new Date();
                                } else {
                                    expect = sdf.parse((String) expectedFieldValue);
                                }
                            } catch (ParseException e) {
                                throw new ActsTestException(table.getTableName() + " key:  "
                                        + fieldName
                                        + " is not Date or valid date format"
                                        + err);
                            }
                            Long realTime = real.getTime();
                            Long expectTime = expect.getTime();
                            //相差多少秒与给定值比较
                            if (Math.abs((realTime - expectTime) / 1000) > timeFlow) {
                                isSame = false;
                                err.append("Failed checking db param, tableName: "
                                        + table.getTableName() + " key:  " + fieldName
                                        + " ,value is " + real + " expect value is " + expect
                                        + "time shift: " + real.compareTo(expect) + " is over "
                                        + timeFlow + "\n");

                            }
                            continue;
                        } else if (currentFlag.startsWith(DBFlagEnum.R.name())) {
                            Pattern pattern = Pattern.compile(expectedFieldValue.toString());
                            Matcher matcher = pattern.matcher(actualFieldValue.toString());
                            boolean matchRes = matcher.matches();
                            if (!matchRes) {
                                isSame = false;
                                err.append("db字段比对失败，表为：" + table.getTableName() + " key为 "
                                        + fieldName + " ,value is "
                                        + actualFieldValue + " expect value is "
                                        + expectedFieldValue + "\n");
                            }
                            continue;
                        } else if (currentFlag.equalsIgnoreCase(DBFlagEnum.J.name())) {
                            //JSON字符串的对比
                            String valueExp = String.valueOf(expectedFieldValue);
                            String valueDb = String.valueOf(actualFieldValue);
                            if (!JSONObject.parseObject(valueExp).equals(
                                    JSONObject.parseObject(valueDb))) {
                                isSame = false;
                                err.append("db字段(JSON字符串)比对失败，表为：" + table.getTableName()
                                        + " key为 " + fieldName + " ,value is "
                                        + actualFieldValue + " expect value is "
                                        + expectedFieldValue + "\n");
                            }
                            continue;
                        } else if (currentFlag.equalsIgnoreCase(DBFlagEnum.JB.name())) {
                            //JSON字符串的对比 新增key黑名单
                            String valueExp = String.valueOf(expectedFieldValue);
                            String valueDb = String.valueOf(actualFieldValue);

                            JSONObject jsonExp = JSONObject.parseObject(valueExp);
                            JSONObject jsonDb = JSONObject.parseObject(valueDb);

                            //读取黑名单key列表
                            String blackTag = "####";
                            String blackEle = (String) jsonExp.remove(blackTag);
                            String[] blackList = StringUtils.split(blackEle, "#");

                            //剔除黑名单key
                            for (String blackKey : blackList) {
                                jsonDb.remove(blackKey);
                                jsonExp.remove(blackKey);
                            }

                            if (!jsonExp.equals(jsonDb)) {
                                isSame = false;
                                err.append("db字段(JSON字符串)比对失败，表为：" + table.getTableName()
                                        + " key为 " + fieldName + " ,value is "
                                        + actualFieldValue + " expect value is "
                                        + expectedFieldValue + "\n");
                            }
                            continue;
                        } else if (currentFlag.equalsIgnoreCase(DBFlagEnum.JA.name())) {
                            //JSONArray的对比
                            String valueExp = String.valueOf(expectedFieldValue);
                            String valueDb = String.valueOf(actualFieldValue);

                            JSONArray jsonArrayExp = JSONArray.parseArray(valueExp);
                            JSONArray jsonArrayDB = JSONArray.parseArray(valueDb);

                            if (!jsonArrayExp.equals(jsonArrayDB)) {
                                isSame = false;
                                err.append("db字段(JSONArray字符串)比对失败，表为：" + table.getTableName()
                                        + " key为 " + fieldName + " ,value is "
                                        + actualFieldValue + " expect value is "
                                        + expectedFieldValue + "\n");
                            }
                            continue;
                        } else if (currentFlag.equalsIgnoreCase(DBFlagEnum.L.name())) {
                            //大字段换行问题
                            String valueExp = "";//com.alipay.common.lang.PropertiesUtil.convert2String(
//                                transStringToMap(String.valueOf(expectedFieldValue)), false);
                            String valueDb = String.valueOf(actualFieldValue);
                            if (!StringUtils.equals(valueExp, valueDb)) {
                                isSame = false;
                                err.append("db字段比对失败，表为：" + table.getTableName() + " key为 "
                                        + fieldName + " ,value is "
                                        + actualFieldValue + " expect value is "
                                        + expectedFieldValue + "\n");
                            }
                            continue;
                        } else if (currentFlag.startsWith(DBFlagEnum.P.name())) {
                            currentFlag = StringUtils.trim(currentFlag);

                            // 自定义分隔符
                            String customSeparator = ";"; // 默认使用分号
                            if (currentFlag.length() > 1) {
                                customSeparator = currentFlag.substring(1);
                            }

                            //根据exp的扩展字段去匹配db里的字段
                            Map<String, String> valueExpMap = convertToMap(
                                    String.valueOf(expectedFieldValue), customSeparator);

                            String valueDbStr = String.valueOf(actualFieldValue);
                            Map<String, String> valueDbMap = new HashMap<>();//com.alipay.common.lang.PropertiesUtil
//                                .toMap(com.alipay.common.lang.PropertiesUtil
//                                    .restoreFromString(valueDbStr));
                            for (Entry<String, String> entry : valueExpMap.entrySet()) {
                                String extKey = entry.getKey();
                                String valueExp = entry.getValue();
                                String valueDb = valueDbMap.get(extKey);
                                if (!StringUtils.equals(valueExp, valueDb)) {
                                    //判断是否unicode
                                    if (valueExp != null && valueExp.startsWith("\\u")) {
                                        String decodeValueExp = decodeUnicodeStr(valueExp);
                                        if (StringUtils.equals(decodeValueExp, valueDb))
                                            continue;
                                    }
                                    isSame = false;
                                    err.append("db字段比对失败，表为：" + table.getTableName() + " key为 "
                                            + fieldName + "." + extKey + " ,value is " + valueDb
                                            + " expect value is " + valueExp + "\n");
                                }
                            }

                            continue;
                        } else if (ObjectCompareUtil.flagMethodsHolder.get() != null && ObjectCompareUtil.flagMethodObjHolder.get() != null
                                && ObjectCompareUtil.flagMethodsHolder.get().containsKey(currentFlag)) {
                            // 搜索并尝试自定义flag方法，此处复用对象比对的自定义flag
                            Method flagMethod = ObjectCompareUtil.flagMethodsHolder.get().get(currentFlag);
                            String cmpMsg = null;
                            try {
                                cmpMsg = (String) flagMethod.invoke(ObjectCompareUtil.flagMethodObjHolder.get(), expectedFieldValue, actualFieldValue);
                            } catch (IllegalAccessException e) {
                                throw new RuntimeException(e);
                            } catch (InvocationTargetException e) {
                                throw new RuntimeException(e);
                            }
                            if (StringUtils.isNotEmpty(cmpMsg)) {
                                isSame = false;
                                err.append("db字段比对失败，表为：" + table.getTableName() + " key为 "
                                        + fieldName + "." + fieldName + " ,value is " + actualFieldValue
                                        + " expect value is " + expectedFieldValue + "\n" + cmpMsg + "\n");
                            }
                            continue;
                        }
                    }

                    // 抵达这里就是无flag或是Y flag或其他未匹配到的flag
                    // 逻辑很混乱，但不了解历史背景不敢乱动，怕影响历史用例被锤
                    if (expectedFieldValue instanceof Date) {
                        Date real = (Date) actualFieldValue;
                        Date expect = (Date) expectedFieldValue;

                        if (real.compareTo(expect) != 0) {
                            isSame = false;
                            err.append("db字段比对失败，表为：" + table.getTableName() + " key为 " + fieldName
                                    + " ,value is " + real + " expect value is " + expect + "\n");
                            continue;
                        }
                    } else {
                        if (String.valueOf(expectedFieldValue).equalsIgnoreCase("now()")) {
                            expectedFieldValue = new Date();
                        }

                        //数据库字符串字段“”与null不做区分
                        if (!isNullBlankDiff()
                                && (expectedFieldValue == null || StringUtils.equals(
                                String.valueOf(expectedFieldValue), ""))) {

                            if (actualFieldValue != null) {

                                if (!StringUtils.equals(String.valueOf(actualFieldValue), "null")
                                        && !StringUtils.equals(String.valueOf(actualFieldValue), "")) {
                                    isSame = false;
                                    err.append("db字段比对失败，表为：" + table.getTableName() + " key为 "
                                            + fieldName + " ,value is " + actualFieldValue
                                            + " expect value is " + expectedFieldValue + "\n");

                                }

                            }

                            continue;

                        }

                        if (!String.valueOf(expectedFieldValue).equals(String.valueOf(actualFieldValue))) {
                            isSame = false;
                            err.append("db字段比对失败，表为：" + table.getTableName() + " key为 " + fieldName
                                    + " ,value is " + actualFieldValue
                                    + " expect value is " + expectedFieldValue + "\n");
                            continue;
                        }

                    }
                    LOGGER.info("db字段比成功，表为：" + table.getTableName() + " ,key为 " + fieldName
                            + " ,value is " + actualFieldValue
                            + " expect value is " + expectedFieldValue);

                }
            }
        }

        if (!isSame) {
            //throw new ActsTestException("Failed checking DB" + err);
            allErr.append(err);
            LogUtil.printColoredError(LOGGER, "Failed checking DB:\n" + err);
        }

        return isSame;

    }

    /**
     * 转换Unicode编码为字符串
     */
    private String decodeUnicodeStr(String unicodeStr) {
        StringBuilder string = new StringBuilder();
        String[] hex = unicodeStr.split("\\\\u");
        for (int t = 1; t < hex.length; t++) {
            int data = Integer.parseInt(hex[t], 16);
            string.append((char) data);
        }
        return string.toString();
    }

    /**
     * 是否期望数据库行不生成
     *
     * @param table
     * @param fields
     * @return true 数据行不应该生成；false 数据行应该生成
     */
    private boolean willAssertEmpty(VirtualTable table, List<String> fields) {
        if (table.getFlags() != null) {
            for (String field : fields) {
                //兼容flag大小写问题
                String currentFlag = table.getFlagByFieldNameIgnoreCase(field);

                if (currentFlag != null) {
                    if (currentFlag.equalsIgnoreCase(DBFlagEnum.CN.name())) {
                        return true;
                    }
                }
            }

        }

        return false;
    }

    /**
     * 找到select 字段对应的真实值
     *
     * @param selectKeys 用来select的表字段
     * @param rowData    数据库中表的一行数据
     * @param tableName  数据库表名
     */
    private Object[] getValsForSelectKeys(String[] selectKeys, Map<String, Object> rowData,
                                          String tableName) {
        Object[] vals = new Object[selectKeys.length];
        Map<String, Object> paramBuffer = ActsRuntimeContextThreadHold.getContext().paramMap;
        for (int i = 0; i < selectKeys.length; i++) {
            String val = String.valueOf(rowData.get(selectKeys[i]));
            // actualVal is assigned to the original val by default
            Object actualVal = rowData.get(selectKeys[i]);

            if (val.startsWith("=")) {
                throw new ActsTestException("tableName: " + tableName + " condition: "
                        + selectKeys[i] + "cannot be assignment statement!");
            }

            if (val.startsWith("$")) {
                String key = val.replace("$", "");
                if (!paramBuffer.containsKey(key)) {
                    LOGGER.error("[PARAM_WARNING]unable to resolve param in value [" + val
                            + "] from " + tableName + "." + selectKeys[i]
                            + ", will process the value as a plain string.");
                }

                actualVal = VelocityUtil.evaluateString(
                        paramBuffer, String.valueOf(actualVal));
            }

            vals[i] = actualVal;
        }

        return vals;
    }

    /**
     * 获取查询条件，其实就是标签为C/CN的字段
     *
     * @param table
     * @param fields
     * @return
     */
    public String[] getSelectKeys(VirtualTable table, List<String> fields) {

        List<String> tmpSelectKeys = new ArrayList<String>();
        if (table.getFlags() != null) {
            for (String field : fields) {
                //获取当前field的flag，看是否能获取到，兼容Flag，Value大小写
                String currentFlag = table.getFlagByFieldNameIgnoreCase(field);
                if (currentFlag != null &&
                        (currentFlag.equalsIgnoreCase("c") || currentFlag.equalsIgnoreCase("cn"))) {
                    tmpSelectKeys.add(field);
                }
            }

        }
        if (dataAccessConfigManager != null) {
            DataAccessConfig dataAccessConfig = dataAccessConfigManager.findDataAccessConfig(table
                    .getDataObjClazz());

            if (dataAccessConfig != null) {
                for (String configKey : dataAccessConfig.getSelectKeys()) {
                    if (!tmpSelectKeys.contains(configKey)) {
                        tmpSelectKeys.add(configKey);
                    }
                }
            }
        }
        return tmpSelectKeys.toArray(new String[0]);
    }

    /**
     * 获取DB type
     *
     * @param url
     * @return
     */
    public String getDBType(String url) {

        if (StringUtils.isBlank(url)) {
            return "mysql";
        }
        if (url.indexOf("mysql") != -1) {
            return "mysql";
        } else if (url.indexOf("oracle") != -1) {
            return "oracle";
        } else {
            return "mysql";
        }

    }

    /**
     * 获取数据源模板
     *
     * @param tableName
     * @return
     */
    public JdbcTemplate getJdbcTemplate(String tableName) {
        String dsName = "dataSource";

        if (tableName.contains("#")) {
            if (tableName.split("#").length != 2) {
                throw new ActsTestException("[Invalid TableName]请按如下格式指定表对应的数据源：YourDataSource#table_name");
            }
            dsName = tableName.split("#")[0];
            tableName = tableName.split("#")[1];
        } else {
//            dsName = DataAccessConfigManager.findDataSourceName(tableName);
        }

        if (StringUtils.isBlank(dsName)) {
            LOGGER
                    .error("Table not Found. tableName="
                            + tableName
                            + "add datasource config in [acts-config.properties], modify variable [ds_datasource]");
        }

        try {
            // 在获取数据模板前，执行一遍自定义逻辑，如设置zdal路由值
            Execute(beforeExecSqlMethodList, tableName, null);
            return jdbcTemplateMap.get(dsName);
        } catch (NullPointerException ne) {
            throw new ActsTestException("[datasource缺失]请提前在acts-config.properties中配置数据源！", ne);
        }

    }

    /**
     * get datasource
     *
     * @param dsName
     * @param bundleName
     * @return
     */
    public DataSource getDataSource(String dsName, String bundleName) {
        DataSource ds = null;// ((DataSource) BundleBeansManager.getBean(dsName, bundleName));
//        if (ds == null) {
//            默认on-the-fly-bundle
//            ds = ((DataSource) BundleBeansManager.getBean(dsName, null));
//        }

        return ds;
    }

    /**
     * String to map for large
     *
     * @param mapString
     * @return
     */
    public static Map<String, String> transStringToMap(String mapString) {
        Map<String, String> map = new HashMap<String, String>();
        StringTokenizer items;
        for (StringTokenizer entrys = new StringTokenizer(mapString, ";"); entrys.hasMoreTokens(); map
                .put(items.nextToken(), items.hasMoreTokens() ? ((String) (items.nextToken())) : null))
            items = new StringTokenizer(entrys.nextToken(), "=");
        return map;
    }

    /**
     * 将数据表中的大字段转换为map结构
     *
     * @param expectedDbStr 数据表中填写的期望值
     * @param separator     自定义分隔符，不指定默认使用分号
     * @return
     */
    private Map<String, String> convertToMap(String expectedDbStr, String separator) {
        Map<String, String> map = new HashMap<String, String>();
        if (StringUtils.isNotBlank(expectedDbStr)) {
            if (separator == null) {
                separator = ";"; // 默认使用英文分号
            }

            String[] pairs = StringUtils.split(expectedDbStr, separator);
            if (pairs != null && pairs.length > 0) {
                for (String pair : pairs) {
                    String[] keyAndValue = pair.split("=");
                    if (keyAndValue != null && keyAndValue.length == 2) {
                        map.put(keyAndValue[0], keyAndValue[1]);
                    }
                }
            }
        }

        return map;
    }

    private boolean isNullBlankDiff() {
        String isdiff = ActsConfiguration.getInstance().getActsConfigMap()
                .get(ActsConstants.IS_DB_NULL_BLANK_DIFF);

        if (StringUtils.isNotBlank(isdiff)) {
            return Boolean.valueOf(isdiff).booleanValue();
        } else {
            return false;
        }
    }

    /**
     * 注解执行
     *
     * @param methods
     * @param tableName
     * @param groupId
     */
    private void Execute(List<IVTableGroupCmdMethod> methods, String tableName, String groupId) {

        for (IVTableGroupCmdMethod m : methods) {
            m.invoke(tableName, groupId);
        }
    }

    public Map<String, JdbcTemplate> getJdbcTemplateMap() {
        return jdbcTemplateMap;
    }

    public void setJdbcTemplateMap(Map<String, JdbcTemplate> jdbcTemplateMap) {
        this.jdbcTemplateMap = jdbcTemplateMap;
    }

    public DataAccessConfigManager getDataAccessConfigManager() {
        return dataAccessConfigManager;
    }

    public void setDataAccessConfigManager(DataAccessConfigManager dataAccessConfigManager) {
        this.dataAccessConfigManager = dataAccessConfigManager;
    }

    /**
     * Getter method for property <tt>beforeVTableExecuteMethodList</tt>.
     *
     * @return property value of beforeVTableExecuteMethodList
     */
    public List<IVTableGroupCmdMethod> getBeforeVTableExecuteMethodList() {
        return beforeVTableExecuteMethodList;
    }

    /**
     * Getter method for property <tt>afterVTableExecuteMethodList</tt>.
     *
     * @return property value of afterVTableExecuteMethodList
     */
    public List<IVTableGroupCmdMethod> getAfterVTableExecuteMethodList() {
        return afterVTableExecuteMethodList;
    }

    /**
     * Getter method for property <tt>beforeExecSqlMethodList</tt>.
     *
     * @return property value of beforeExecSqlMethodList
     */
    public List<IVTableGroupCmdMethod> getBeforeExecSqlMethodList() {
        return beforeExecSqlMethodList;
    }

    /**
     * Getter method for property <tt>specialGroupIds</tt>.
     *
     * @return property value of specialGroupIds
     */
    public List<String> getSpecialGroupIds() {
        return specialGroupIds;
    }

    public void addSpecialGroupIds(String[] groupIds) {
        this.specialGroupIds.addAll(Arrays.asList(groupIds));
    }

}
