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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.util.CollectionUtils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.alipay.test.acts.constant.ActsPathConstants;
import com.alipay.test.acts.data.BbTableModelUtil;
import com.alipay.test.acts.model.VirtualTable;
import com.alipay.test.acts.util.BaseDataUtil;
import com.alipay.test.acts.util.FileUtil;

/**
 * sql日志解析工厂
 * 
 * @author hongling.xiang
 * @version $Id: SqlLogParseFactory.java, v 0.1 2015年10月26日 上午11:14:19
 *          hongling.xiang Exp $
 */
public class SqlLogParseFactory {

	/* logger* */
	private static final Logger logger = LoggerFactory
			.getLogger(SqlLogParseFactory.class);

	/** SQL类型与对应解析器 */
	private static final Map<String, SqlLogParser> sqlParser = new HashMap<String, SqlLogParser>();

	static {
		sqlParser.put(SqlTypeEnum.INSERT_SQL.getCode(),
				new InsertSqlLogParser());
		sqlParser.put(SqlTypeEnum.UPDATE_SQL.getCode(),
				new UpdateSqlLogParser());
	}

	/**
	 * 解析SQL生成VirtualTable对象
	 * 
	 * @param sqlType
	 * @param sqlExecLog
	 *            已经进行了预处理的数据
	 * @return
	 * @throws ClassNotFoundException
	 */
	public static VirtualTable genVirtualTable(String sqlType,
			List<String> sqlExecLog) throws ClassNotFoundException {

		SqlLogParser sqlLogParser = sqlParser.get(sqlType);

		// 解析获取tableName
		String tableName = sqlLogParser.parseTableName(sqlExecLog.get(0));
		if (StringUtils.isBlank(tableName) || !isAppTable(tableName)) {
			return null;
		}

		// 解析获取sql涉及字段
		List<Map<String, Object>> tableDatas = sqlLogParser.parseGenTableDatas(
				sqlExecLog.get(0),
				parseSqlParamValue(sqlExecLog.get(1), sqlExecLog.get(2)),
				Arrays.asList(sqlExecLog.get(2).split(", ")));
		for (Map<String, Object> tableData : tableDatas) {
			for (String key : tableData.keySet()) {
				tableData.put(key, String.valueOf(tableData.get(key)));
			}
		}
		if (CollectionUtils.isEmpty(tableDatas)) {
			return null;
		}

		// 解析获取标记字段
		// 优先从用户配置的数据库模板中获取表字段标记
		Map<String, String> fieldsFlag = fetchFieldFlagsFromDbModel(tableName);
		if (CollectionUtils.isEmpty(fieldsFlag)) {
			// 如果用户未定义此表模板，则解析SQL获取
			fieldsFlag = sqlLogParser.parseTableFlags(sqlExecLog.get(0),
					tableDatas.get(0).keySet());
		}

		// 组装生成VirtualTable
		VirtualTable table = new VirtualTable();
		table.setTableName(tableName);
		table.setFlags(fieldsFlag);
		table.setTableData(tableDatas);
		table.setTableBaseDesc(tableName);

		return table;
	}

	/**
	 * 从DB表模板中获取表字段标记
	 * 
	 * @param tableName
	 * @return
	 */
	private static Map<String, String> fetchFieldFlagsFromDbModel(
			String tableName) {

		File folder = FileUtil
				.getTestResourceFile(ActsPathConstants.DB_DATA_PATH);
		if (!folder.exists()) {
			return null;
		}

		// 兼容处理
		String dbModelFullPath = folder.getAbsolutePath();
		if (StringUtils.contains(dbModelFullPath, "\\")) {
			dbModelFullPath = StringUtils.replace(dbModelFullPath, "\\", "/");
		}
		String dbModelRootPath = dbModelFullPath.substring(0,
				dbModelFullPath.indexOf("model/dbModel"));
		VirtualTable virtualTable = null;
		try {
			virtualTable = BaseDataUtil.getVirtualTableFromBase(tableName,
					tableName, dbModelRootPath,
					System.getProperty("file.encoding"));
		} catch (Throwable t) {
			logger.warn("query DB model exception!");
		}

		if (null == virtualTable) {
			return null;
		}

		return virtualTable.getFlags();
	}

	/**
	 * 解析
	 * 
	 * @param sqlParamValueStr
	 * @param sqlParamTypeStr
	 * @return
	 */
	public static List<String> parseSqlParamValue(String sqlParamValueStr,
			String sqlParamTypeStr) {

		// sql参数类型字符串中肯定不包含分割敏感字符串“， ”
		String[] paramTypes = sqlParamTypeStr.split(", ");

		// 1.尝试使用“， ”sql参数值字符串
		String[] paramValues = sqlParamValueStr.split(", ");
		// 如果分割出的长度与参数类型个数相等，说明参数值中不包含分割字符串“， ”，直接返回
		if (paramValues.length == paramTypes.length) {
			return Arrays.asList(paramValues);
		}
		// 解决字符串最后是" "的情况
		if ((paramValues.length == (paramTypes.length - 1))
				&& sqlParamValueStr.endsWith(" ")) {
			sqlParamValueStr = sqlParamValueStr + " ";
			paramValues = sqlParamValueStr.split(", ");
		}

		// 2.如果参数值包含特殊字符“, ”,按场景分析解析
		// 2.1其中参数值为一个json字符串，复杂对象属性使用“\, ”换行表示
		/*
		 * String tempParamValues = StringUtils.replace(sqlParamValueStr, "\n, ",
		 * "_placeholder_"); String[] newParamValues =
		 * tempParamValues.split(", "); if (newParamValues.length ==
		 * paramTypes.length) { List<String> values = new ArrayList<String>();
		 * for (String tempValue : newParamValues) {
		 * values.add(StringUtils.replace(tempValue, "_placeholder_", "\n, ")); }
		 * 
		 * //返回之前对于map还要做一次处理 return values; }
		 */

		// FIXME 尝试解决map的问题.
		String[] newParamValues = sqlParamValueStr.split(", ");
		List<String> newArray = new ArrayList<String>();

		for (int i = 0; i < newParamValues.length; i++) {
			String currentStr = newParamValues[i];
			boolean needConnect = false;
			if (StringUtils.contains(newParamValues[i], "{")) {
				needConnect = true;
				i++;
			}
			while (!StringUtils.contains(newParamValues[i], "}") && needConnect) {
				currentStr += (newParamValues[i] + ", ");
				i++;
			}
			if (StringUtils.contains(newParamValues[i], "}")) {
				currentStr += (", " + newParamValues[i]);
			}
			newArray.add(currentStr);
		}
		logger.debug("解析结果:" + newArray);
		return newArray;
	}

	/**
	 * 指定表名是否为应用数据库表
	 * 
	 * @param tableName
	 * @return
	 */
	private static boolean isAppTable(String tableName) {

		// 降级处理
		boolean isApptable = !StringUtils.contains(tableName.toLowerCase(),
				"seq_")
				&& !StringUtils.contains(tableName.toLowerCase(), "sequence_")
				&& !StringUtils.contains(tableName.toLowerCase(), "_sequence")
				&& !StringUtils.contains(tableName.toLowerCase(), "_seq");

		if (isApptable) {
			return true;
		} else {
			logger.warn(tableName + "is not Apptable !!!");
			return false;
		}

	}

	/**
	 * 判断依据是dalgen文件和table名称 指定表名是否为应用数据库表 该方法由于某些系统可能没有dalgen文件
	 * 
	 * @param tableName
	 * @return
	 */
	private static boolean isAppTableFromDalgen(String tableName) {

		String userDirPath = System.getProperty("user.dir");
		if (StringUtils.contains(userDirPath, "\\")) {
			userDirPath = StringUtils.replace(userDirPath, "\\", "/");
		}

		if (!StringUtils.contains(userDirPath, "app/test")) {
			// 尝试根据当前类路径查询
			userDirPath = SqlLogParseFactory.class.getResource("/").getPath();
			if (StringUtils.contains(userDirPath, "\\")) {
				userDirPath = StringUtils.replace(userDirPath, "\\", "/");
			}
		}

		if (StringUtils.contains(userDirPath, "app/test")) {
			userDirPath = userDirPath.substring(0,
					userDirPath.indexOf("app/test"));
		} else {
			// 降级处理
			logger.warn("Collecting case result-unknown exception while parsing SQL,tableName="
					+ tableName + "未进行是否属于业务表校验！");
			return !StringUtils.contains(tableName.toLowerCase(), "seq_")
					&& !StringUtils.contains(tableName.toLowerCase(),
							"sequence_")
					&& !StringUtils.contains(tableName.toLowerCase(),
							"_sequence")
					&& !StringUtils.contains(tableName.toLowerCase(), "_seq");
		}

		Set<String> tableNameSet = BbTableModelUtil
				.generateAppTableModel(userDirPath);
		for (String table : tableNameSet) {
			if (table.equalsIgnoreCase(tableName)) {
				return true;
			}
		}

		logger.warn("Collecting case result-unknown exception while parsing SQL, tableName="
				+ tableName + "not belong to App Tables！");

		return false;
	}

}
