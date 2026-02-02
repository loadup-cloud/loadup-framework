package io.github.loadup.testify.data.engine.db;

/*-
 * #%L
 * Testify Data Engine
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.fasterxml.jackson.databind.JsonNode;
import io.github.loadup.testify.data.engine.variable.VariableEngine;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;

@RequiredArgsConstructor
@Slf4j
public class SqlExecutionEngine {

  private final JdbcTemplate jdbcTemplate;
  private final VariableEngine variableEngine;

  public void execute(JsonNode setupNode, Map<String, Object> context, String yamlPath) {
    if (setupNode == null || setupNode.isNull()) return;

    // 1. 处理原始 SQL
    if (setupNode.has("clean_sql")) {
      String cleanSql = setupNode.get("clean_sql").asText();
      log.info("executing clean_sql: {}", cleanSql);
      executeRawSql(cleanSql, context);
    }

    // 2. 处理数据填充 (db_setup 列表)
    JsonNode dbSetupNode = setupNode.get("db_setup");
    if (dbSetupNode != null && dbSetupNode.isArray()) {
      for (JsonNode item : dbSetupNode) {
        String tableName = item.get("table").asText();
        if (item.has("file")) {
          executeCsvSetup(tableName, item.get("file").asText(), context, yamlPath);
        } else if (item.has("data")) {
          executeDataSetup(tableName, item.get("data"), context);
        }
      }
    }
  }

  /** 模式 A: 从 YAML 直接读取数据列表并插入 */
  private void executeDataSetup(String tableName, JsonNode dataNode, Map<String, Object> context) {
    if (!dataNode.isArray() || dataNode.isEmpty()) return;

    // 1. 递归解析整个 Data 节点的变量
    JsonNode resolvedData = variableEngine.resolveJsonNode(dataNode, context);

    // 2. 提取列名
    List<String> columns = new ArrayList<>();
    resolvedData.get(0).fieldNames().forEachRemaining(columns::add);

    // 3. 构造批量参数
    String sql = buildInsertSql(tableName, columns);
    List<Object[]> batchArgs = new ArrayList<>();
    for (JsonNode row : resolvedData) {
      Object[] params = new Object[columns.size()];
      for (int i = 0; i < columns.size(); i++) {
        params[i] = extractValue(row.get(columns.get(i)));
      }
      batchArgs.add(params);
    }

    jdbcTemplate.batchUpdate(sql, batchArgs);
    log.info("Loaded {} rows into [{}] from YAML", batchArgs.size(), tableName);
  }

  /** 模式 B: 从 CSV 文件加载数据 */
  private void executeCsvSetup(
      String tableName, String fileName, Map<String, Object> context, String yamlPath) {
    // 计算相对于 YAML 的路径
    String csvPath = yamlPath.substring(0, yamlPath.lastIndexOf("/") + 1) + fileName;

    try (Reader reader =
        new InputStreamReader(
            new ClassPathResource(csvPath).getInputStream(), StandardCharsets.UTF_8)) {

      CSVParser csvParser =
          CSVFormat.DEFAULT
              .builder()
              .setHeader()
              .setSkipHeaderRecord(true)
              .setIgnoreSurroundingSpaces(true)
              .build()
              .parse(reader);

      List<String> headers = csvParser.getHeaderNames();
      String sql = buildInsertSql(tableName, headers);
      List<Object[]> batchArgs = new ArrayList<>();

      for (CSVRecord record : csvParser) {
        Object[] rowValues = new Object[headers.size()];
        for (int i = 0; i < headers.size(); i++) {
          // CSV 文本统一走 VariableEngine.evaluate
          Object resolved = variableEngine.evaluate(record.get(i), context);
          rowValues[i] = "NULL".equalsIgnoreCase(String.valueOf(resolved)) ? null : resolved;
        }
        batchArgs.add(rowValues);
      }

      jdbcTemplate.batchUpdate(sql, batchArgs);
      log.info("Loaded {} rows into [{}] from CSV: {}", batchArgs.size(), tableName, fileName);
    } catch (Exception e) {
      throw new RuntimeException("Failed to load CSV: " + csvPath, e);
    }
  }

  /**
   * 执行原始 SQL 脚本，支持多行和变量替换
   *
   * @param rawSql 包含占位符的原始 SQL 字符串
   * @param context 变量上下文
   */
  private void executeRawSql(String rawSql, Map<String, Object> context) {
    if (rawSql == null || rawSql.isBlank()) return;

    // 解析 SQL 中的变量占位符
    String processedSql = String.valueOf(variableEngine.evaluate(rawSql, context));

    jdbcTemplate.execute(
        (java.sql.Connection conn) -> {
          ScriptUtils.executeSqlScript(
              conn, new ByteArrayResource(processedSql.getBytes(StandardCharsets.UTF_8)));
          return null;
        });
    log.info("Executed clean_sql for setup");
  }

  private String buildInsertSql(String tableName, List<String> columns) {
    String cols = String.join(",", columns);
    String placeholders = String.join(",", Collections.nCopies(columns.size(), "?"));
    return "INSERT INTO " + tableName + " (" + cols + ") VALUES (" + placeholders + ")";
  }

  private Object extractValue(JsonNode node) {
    if (node == null || node.isNull()) return null;
    if (node.isNumber()) return node.numberValue();
    if (node.isBoolean()) return node.booleanValue();
    return node.asText();
  }
}
