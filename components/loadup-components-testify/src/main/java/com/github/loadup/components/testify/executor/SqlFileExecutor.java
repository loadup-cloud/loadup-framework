package com.github.loadup.components.testify.executor;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class SqlFileExecutor {

    /**
     * 执行SQL文件（从 classpath 加载）
     *
     * @param filePath     SQL 文件路径（相对于 classpath）
     * @param dataSource   数据源
     */
    public static void execute(String filePath, DataSource dataSource) {
        // 加载资源流
        InputStream inputStream = SqlFileExecutor.class.getClassLoader().getResourceAsStream(filePath);
        if (inputStream == null) {
            throw new IllegalArgumentException("SQL file not found: " + filePath);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            List<String> textLines = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                textLines.add(line);
            }

            List<String> sqls = parseSqlTextToSqls(textLines);
            if (CollectionUtils.isNotEmpty(sqls)) {
                JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

                sqls.forEach(sql -> {
                    if (StringUtils.isNotBlank(sql)) {
                        if (log.isInfoEnabled()) {
                            log.info("execute sql: {}", sql);
                        }
                        jdbcTemplate.execute(sql);
                    }
                });
            }
        } catch (IOException e) {
            log.error("执行 SQL 脚本失败", e);
            throw new RuntimeException("Failed to execute SQL script: " + filePath, e);
        }
    }

    /**
     * 将sql文件中的文本行解析成一条条的sql语句，解析方式：判断当前行的最后一个字符是否为";"，如果是，则将之前的语句结合成一条完成的sql
     * @param textLines
     * @return
     */
    private static List<String> parseSqlTextToSqls(List<String> textLines) {
        List<String> sqls = new ArrayList<>();

        if (CollectionUtils.isEmpty(textLines)) {
            return sqls;
        }

        StringBuilder sb = new StringBuilder();

        for (String line : textLines) {
            // 在sql换行后增加一个空格,避免跑sql出错
            sb.append(line).append(" ");
            if (line.endsWith(";")) {
                sqls.add(sb.toString());
                sb = new StringBuilder();
            }
        }
        return sqls;
    }

    public static String readSqlFromFile(String filePath) {
        try (InputStream is = SqlFileExecutor.class.getClassLoader().getResourceAsStream(filePath)) {
            if (is == null) {
                throw new IllegalArgumentException("SQL file not found: " + filePath);
            }
            return new String(is.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException("Failed to read SQL file: " + filePath, e);
        }
    }
}
